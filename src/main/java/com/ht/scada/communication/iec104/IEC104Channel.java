package com.ht.scada.communication.iec104;

import com.ht.iec104.frame.IEC104IFrame;
import com.ht.iec104.master.IEC104Master;
import com.ht.iec104.master.MasterHandler;
import com.ht.iec104.master.YKHandler;
import com.ht.iec104.master.YTHandler;
import com.ht.iec104.util.TiConst;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.common.tag.util.VarTypeEnum;
import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.entity.ChannelInfo;
import com.ht.scada.communication.model.*;
import com.ht.scada.communication.util.ChannelFrameFactory;
import com.ht.scada.communication.util.ChannelFrameFactory.IEC104Frame;
import com.ht.scada.communication.util.CommUtil;
import com.ht.scada.communication.util.DataValueUtil;
import com.ht.scada.communication.util.PortInfoFactory;
import com.ht.scada.communication.util.PortInfoFactory.TcpIpInfo;
import io.netty.channel.EventLoop;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * IEC104规约采集通道<br>
 * <pre>
 *     启动采集后本程序将自动创建一个线程并在线程里循环执行采集。
 * </pre>
 * <pre>
 *     通讯帧配置时总召唤的帧间隔请设置为0，历史数据召唤的帧间隔请结合RTU的历史数据存储周期进行设置。
 * </pre>
 *
 * @author 薄成文
 */
public class IEC104Channel extends CommunicationChannel {
    private static final Logger log = LoggerFactory.getLogger(IEC104Channel.class);

    private int realtimeDataInterval = 5;// 实时数据更新间隔
    private final int historyDataInterval = 60;

    private volatile boolean running = false;
    private List<IEC104Frame> frameList;
    private IEC104Master master;

    private IEC104Channel.MyMasterHandler masterHandler;
    //private ScheduledExecutorService executorService;
    private EventLoop executorService;

    public IEC104Channel(ChannelInfo channel, List<EndTagWrapper> endTagList) throws Exception {
        super(channel, endTagList);
    }

    @Override
    public void init() throws Exception {
        log.info("{}:初始化采集通道", channel.getName());

        String portInfo = channel.getPortInfo();
        if (portInfo.startsWith("tcp/ip")) {

            frameList = ChannelFrameFactory.parseIEC104Frames(channel.getFrames());
            log.info("{} - 召唤帧共：{}", channel.getName(), frameList.size());
            // 以召唤帧最小的间隔为实时数据更新间隔
            for (IEC104Frame frame : frameList) {
                if (frame.interval > 0 && frame.interval < realtimeDataInterval) {
                    realtimeDataInterval = frame.interval;
                }
            }

            TcpIpInfo tcpIpInfo = PortInfoFactory.parseTcpIpInfo(portInfo);

            //group.next().execute(null);
            masterHandler = new MyMasterHandler();
            master = new IEC104Master(CommunicationManager.getInstance().getNioEventLoopGroup(), tcpIpInfo.ip, tcpIpInfo.port, 1, masterHandler);

        } else {
            log.error("采集通道{}的物理端口信息配置错误：{}", portInfo);
            throw new RuntimeException("IEC104通讯规约只支持TCP/IP通讯方式。");
        }

    }

    @Override
    public void start() {
        log.info("{}:启动采集", channel.getName());

        if (master != null) {
            running = true;

            //executorService = Executors.newSingleThreadScheduledExecutor();
            executorService = CommunicationManager.getInstance().getNioEventLoopGroup().next();
            master.open();

            /** 定时执行数据召唤 **/
            for (final IEC104Frame frame: frameList) { // 只需要执行一次的召唤和历史数据召唤不在此处执行
                if (frame.interval > 0 && frame.ti != TiConst.CALL_HIS_DAT) {
                    // 按召唤间隔执行数据召唤
                    executorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            master.call(frame.ti);
                        }
                    }, frame.interval, frame.interval, TimeUnit.SECONDS);
                }
            }

            // 定时更新实时数据库
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (master.isConnected()) {
                        updateRealtimeData();
                    }
                }
            }, realtimeDataInterval, realtimeDataInterval, TimeUnit.SECONDS);

            // 定时更新历史数据库
            // TODO 暂时不写历史数据
//            executorService.scheduleAtFixedRate(new Runnable() {
//                @Override
//                public void run() {
//                    persistHistoryData();
//                }
//            }, historyDataInterval, historyDataInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stop() {
        log.info("{}:停止采集", channel.getName());

        running = false;
        executorService.shutdownNow();
        if (master != null) {
            master.close();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 处理RTU上送的遥测历史数据帧
     * @param defaultInterval
     */
    private void handleHisYcFrame(final int defaultInterval) {
        IEC104IFrame frame;
        while ((frame = master.getHisYcFrameQueue().poll()) != null) {
            final int[] infoID = frame.infoID;
            final int[] value = frame.getYcValue();
            final Calendar[] datetime = frame.getTimes();
            final boolean sq = frame.sq;

            forEachYc2YxTagVar(master.getSlaveID(), new DataHandler<YxTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YxTagVar var) {
                    VarGroupEnum varGroup = var.tpl.getVarGroup();
                    int interval = model.getSaveIntvl4VarGroup(varGroup);
                    if (interval <= 0) {
                        interval = defaultInterval;
                    }
                    if (sq) {// 连续的地址
                        int index = var.tpl.getDataId() - infoID[0];
                        if (index >= 0 && index < value.length) {
                            boolean v = DataValueUtil.parseBoolValue(value[index], var.getTpl().getBitOffset());
                            Calendar cal = datetime[index];
                            String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                            model.addRTUYxHisData(var, key, cal.getTime(), v);
                            return true;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataId()) {
                                boolean v = DataValueUtil.parseBoolValue(value[i], var.getTpl().getBitOffset());
                                Calendar cal = datetime[i];
                                String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                                model.addRTUYxHisData(var, key, cal.getTime(), v);
                                return true;
                            }
                        }
                    }
                    return true;
                }

            });
            forEachYcTagVar(master.getSlaveID(), new DataHandler<YcTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YcTagVar var) {

                    VarGroupEnum varGroup = var.tpl.getVarGroup();
                    int interval = model.getSaveIntvl4VarGroup(varGroup);
                    if (interval <= 0) {
                        interval = defaultInterval;
                    }

                    if (var.getLastArrayValue() != null) {// 功图、谐波历史数据

                        int startID = var.tpl.getDataId();
                        int len = var.getLastArrayValue().length;
                        if (startID > infoID[infoID.length - 1] || startID + len <= infoID[0]) {
                            return true;
                        }

                        Calendar cal = datetime[0];
                        String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                        float[] v = model.addRTUYcArrayHisData(var, key, cal.getTime());
                        for (int i = 0; i < value.length; i++) {
                            int offset = infoID[i] - startID;
                            if (offset >= 0 && offset < len) {
                                v[offset] = var.calcValue(value[i]);
                            }
                        }
                    } else {// 单个的遥测变量
                        if (sq) {// 连续的地址
                            int i = var.tpl.getDataId() - infoID[0];
                            DataType dataType = var.getTpl().getDataType();
                            float v = Float.NaN;//.calcValue(value[i]);

                            switch (dataType) {
                                case INT16:
                                    if (i >= 0 && i < value.length) {
                                        v = var.calcValue((short)value[i]);
                                    }
                                    break;
                                case UINT16:
                                    if (i >= 0 && i < value.length) {
                                        v = var.calcValue(value[i] & 0xFFFF);
                                    }
                                    break;
                                case INT32:
                                    if (i >= 0 && i < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF) | (value[i] << 16);
                                        var.setRawValue(rawValue);
                                        v = var.calcValue(rawValue);
                                    }
                                    if (i + 1 >= 0 && i + 1 < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF0000) | value[i + 1];
                                        var.setRawValue(rawValue);
                                        v = var.calcValue(rawValue);
                                    }
                                    break;
                                case UINT32:
                                    if (i >= 0 && i < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF) | (value[i] << 16);
                                        var.setRawValue(rawValue);
                                        v = var.calcValue(rawValue);
                                    }
                                    if (i + 1 >= 0 && i + 1 < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF0000) | value[i + 1];
                                        var.setRawValue(rawValue);
                                        v = var.calcValue(rawValue);
                                    }
                                    break;
                                case MOD1000:// 高字节*1000+低字节/10
                                    if (i >= 0 && i < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF) + (value[i] << 16);
                                        var.setRawValue(rawValue);
                                        float f = (var.getRawValue() & 0xFFFF) / 10F + (var.getRawValue() >> 16) * 1000;
                                        v = var.calcValue(f);
                                    }
                                    if (i + 1 >= 0 && i + 1 < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF0000) + value[i + 1];
                                        var.setRawValue(rawValue);
                                        float f = (var.getRawValue() & 0xFFFF) / 10F + (var.getRawValue() >> 16) * 1000;
                                        v = var.calcValue(f);
                                    }
                                    break;
                                case BYTE_H:
                                    if (i >= 0 && i < value.length) {
                                        v = var.calcValue(value[i] >> 8);
                                    }
                                    break;
                                case BYTE_L:
                                    if (i >= 0 && i < value.length) {
                                        v = var.calcValue(value[i] & 0xFF);
                                    }
                                    break;
                            }

                            if (!Float.isNaN(v)) {
                                Calendar cal = datetime[i];
                                String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                                model.addRTUYcHisData(var, key, cal.getTime(), v);
                            }
                        } else {// 非连续的地址
                            DataType dataType = var.getTpl().getDataType();
                            Calendar cal = null;//datetime[i];
                            float v = Float.NaN;//.calcValue(value[i]);
                            if (dataType == DataType.INT16) {
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.tpl.getDataId()) {
                                        v = var.calcValue(value[i]);
                                        cal = datetime[i];
                                        break;
                                    }
                                }
                            } else if (dataType == DataType.INT32) {
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.getTpl().getDataId()) {// 高Word
                                        int rawValue = (var.getRawValue() & 0xFFFF) | (value[i] << 16);
                                        var.setRawValue(rawValue);
                                        v = var.calcValue(rawValue);
                                    } else if (infoID[i] == var.getTpl().getDataId() + 1) {// 低Word
                                        int rawValue = (var.getRawValue() & 0xFFFF0000) + value[i];
                                        var.setRawValue(rawValue);
                                        v = var.calcValue(rawValue);
                                        return true;
                                    }
                                }
                            } else if (dataType == DataType.BYTE_H) {
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.tpl.getDataId()) {
                                        cal = datetime[i];
                                        v = var.calcValue(value[i] >> 8);
                                        break;
                                    }
                                }
                            } else if (dataType == DataType.BYTE_L) {
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.tpl.getDataId()) {
                                        cal = datetime[i];
                                        v = var.calcValue(value[i] & 0xFF);
                                        break;
                                    }
                                }
                            }
                            if (!Float.isNaN(v) && cal != null) {
                                String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                                model.addRTUYcHisData(var, key, cal.getTime(), v);
                            }
                        }

                    }

                    return true;
                }
            });
        }
    }

    /**
     * 处理RTU上送的遥脉历史数据帧
     *
     * @param defaultInterval
     */
    private void handleHisYmFrame(final int defaultInterval) {
        IEC104IFrame frame;
        while ((frame = master.getHisYmFrameQueue().poll()) != null) {
            final int[] infoID = frame.infoID;
            final long[] value = frame.getYmValue();
            final Calendar[] datetime = frame.getTimes();
            final boolean sq = frame.sq;

            forEachYmTagVar(master.getSlaveID(), new DataHandler<YmTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YmTagVar var) {

                    VarGroupEnum varGroup = var.tpl.getVarGroup();
                    int interval = model.getSaveIntvl4VarGroup(varGroup);
                    if (interval <= 0) {
                        interval = defaultInterval;
                    }

                    int index = -1;
                    if (sq) {// 连续的地址
                        int i = var.tpl.getDataId() - infoID[0];
                        if (i >= 0 && i < value.length) {
                            index = i;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataId()) {
                                index = i;
                                break;
                            }
                        }
                    }

                    if (index >= 0) {
                        double v = var.calcValue(value[index]);
                        Calendar cal = datetime[index];
                        String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                        model.addRTUYmHisData(var, key, cal.getTime(), v);
                    }

                    return true;
                }
            });
        }
    }

    /**
     * 处理RTU上送的遥信历史数据帧
     *
     * @param defaultInterval
     */
    private void handleHisYxFrame(final int defaultInterval) {
        IEC104IFrame frame;
        while ((frame = master.getHisYxFrameQueue().poll()) != null) {
            final int[] infoID = frame.infoID;
            final boolean[] value = frame.getYxValue();
            final Calendar[] datetime = frame.getTimes();
            final boolean sq = frame.sq;

            forEachYxTagVar(master.getSlaveID(), new DataHandler<YxTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YxTagVar var) {

                    // 遥测转遥信变量不在此进行处理
                    if (var.getTpl().getBitOffset() >= 0) {
                        return true;
                    }
                    VarGroupEnum varGroup = var.tpl.getVarGroup();
                    int interval = model.getSaveIntvl4VarGroup(varGroup);
                    if (interval <= 0) {
                        interval = defaultInterval;
                    }

                    int index = -1;
                    if (sq) {// 连续的地址
                        int i = var.tpl.getDataId() - infoID[0];
                        if (i >= 0 && i < value.length) {
                            index = i;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataId()) {
                                index = i;
                                break;
                            }
                        }
                    }

                    if (index >= 0) {
                        Calendar cal = datetime[index];
                        String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                        model.addRTUYxHisData(var, key, cal.getTime(), value[index]);
                    }

                    return true;
                }
            });
        }
    }

    /**
     * 处理遥脉数据帧
     */
    private void handleYmFrame(final Date date) {

        //log.debug("{}:本次召唤累计返回{}个遥脉帧", channel.getName(), master.getYmFrameQueue().size());
        IEC104IFrame frame;
        while ((frame = master.getYmFrameQueue().poll()) != null) {

            final int[] infoID = frame.infoID;
            final long[] value = frame.getYmValue();
            final boolean sq = frame.sq;

            forEachYmTagVar(master.getSlaveID(), new DataHandler<YmTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YmTagVar var) {
                    if (sq) {
                        int i = var.tpl.getDataId() - infoID[0];
                        if (i >= 0 && i < value.length) {
                            var.update(value[i], date);
                            return true;
                        }
                    } else {
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataId()) {
                                var.update(value[i], date);
                                return true;
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 处理遥测数据帧
     * @param date
     */
    private void handleYcFrame(final Date date) {

        log.debug("{}:本次召唤累计返回{}个遥测帧", channel.getName(), master.getYcFrameQueue().size());

        IEC104IFrame frame;
        while ((frame = master.getYcFrameQueue().poll()) != null) {

            final int[] infoID = frame.infoID;
            final int[] value = frame.getYcValue();
            final boolean sq = frame.sq;
            log.debug("infoID:{}", infoID);

            forEachYc2YxTagVar(master.getSlaveID(), new DataHandler<YxTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YxTagVar var) {
                    if (sq) {// 连续的地址
                        int index = var.tpl.getDataId() - infoID[0];
                        if (index >= 0 && index < value.length) {
                            boolean v = DataValueUtil.parseBoolValue(value[index], var.getTpl().getBitOffset());
                            var.update(v, date);
                            return true;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataId()) {
                                boolean v = DataValueUtil.parseBoolValue(value[i], var.getTpl().getBitOffset());
                                var.update(v, date);
                                return true;
                            }
                        }
                    }
                    return true;
                }
            });

            forEachAsciiTagVar(master.getSlaveID(), new DataHandler<AsciiTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, AsciiTagVar var) {
                    if (sq) {// 连续的地址
                        int index = var.getTpl().getDataId() - infoID[0];
                        for (int i = 0; i < var.getTpl().getByteLen(); i++) {
                            int j = index + (i / 2);
                            if (j >= 0 && j < value.length) {
                                if (i % 2 == 0) {// 低字节
                                    byte b = (byte) (value[index] & 0xFF);
                                    var.getRawValue()[i] = b;
                                } else {// 高字节
                                    byte b = (byte) (value[index] >> 8);
                                    var.getRawValue()[i] = b;
                                }
                            }
                            if ( j >= value.length) {
                                return true;
                            }
                        }
                        var.update(date);
                    } else {// 非连续的地址
                        // TODO: 暂不支持
                    }
                    return true;
                }
            });

            //log.debug("   ID:{}", Arrays.toString(infoID));
            //log.debug("Value:{}", Arrays.toString(value));
            forEachYcTagVar(master.getSlaveID(), new DataHandler<YcTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YcTagVar var) {
                    if (sq) {
                        if (var.getLastArrayValue() != null) {// 遥测数组
                            // 处理示功图、谐波等数组数据
                            int startID = var.tpl.getDataId();
                            int len = var.getLastArrayValue().length;
                            if (startID > infoID[infoID.length - 1]
                                    || startID + len <= infoID[0]) {
                                return true;
                            }
                            for (int i = 0; i < value.length; i++) {
                                int offset = infoID[i] - startID;
                                if (offset >= 0 && offset < len) {
                                    var.updateArrayValue(value[i], offset);
                                }
                            }
                        } else {// 单个遥测
                            int i = var.getTpl().getDataId() - infoID[0];
                            DataType dataType = var.getTpl().getDataType();
                            switch (dataType) {
                                case INT16:
                                    if (i >= 0 && i < value.length) {
                                        var.update(value[i], date);
                                        return true;
                                    }
                                    break;
                                case INT32:// 高字在前，低字在后
                                    if (i >= 0 && i < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF) | (value[i] << 16);
                                        var.setRawValue(rawValue);
                                        var.update(rawValue, date);
                                    }
                                    if (i + 1 >= 0 && i + 1 < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF0000) | value[i + 1];
                                        var.setRawValue(rawValue);
                                        var.update(rawValue, date);
                                    }
                                    return true;
                                case BYTE_H:
                                    if (i >= 0 && i < value.length) {
                                        var.update(value[i] >> 8, date);
                                        return true;
                                    }
                                    break;
                                case BYTE_L:
                                    if (i >= 0 && i < value.length) {
                                        var.update(value[i] & 0xFF, date);
                                        return true;
                                    }
                                    break;
                                case MOD1000:// 高字节*1000+低字节/10
                                    if (i >= 0 && i < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF) + (value[i] << 16);
                                        var.setRawValue(rawValue);
                                        float f = (var.getRawValue() & 0xFFFF) / 10F + (var.getRawValue() >> 16) * 1000;
                                        var.update(f, date);
                                    }
                                    if (i + 1 >= 0 && i + 1 < value.length) {
                                        int rawValue = (var.getRawValue() & 0xFFFF0000) + value[i + 1];
                                        var.setRawValue(rawValue);
                                        float f = (var.getRawValue() & 0xFFFF) / 10F + (var.getRawValue() >> 16) * 1000;
                                        var.update(f, date);
                                    }
                                    return true;

                            }
                        }
                    } else {// 非连续
                        if (var.tpl.getVarType() == VarTypeEnum.YC) {
                            DataType dataType = var.getTpl().getDataType();

                            if (dataType == DataType.INT16) {
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.tpl.getDataId()) {
                                        var.update(value[i], date);
                                        return true;
                                    }
                                }
                            } else if (dataType == DataType.INT32) {// 高字在前，低字在后
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.getTpl().getDataId()) {// 高Word

                                        int rawValue = (var.getRawValue() & 0xFF) + (value[i] << 16);
                                        var.setRawValue(rawValue);
                                        var.update(rawValue, date);
                                    } else if (infoID[i] == var.getTpl().getDataId() + 1) {// 低Word
                                        int rawValue = (var.getRawValue() & 0xFF00) + value[i];
                                        var.setRawValue(rawValue);
                                        var.update(rawValue, date);
                                        return true;
                                    }
                                }
                            } else if (dataType == DataType.BYTE_H) {// 高字节
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.tpl.getDataId()) {
                                        var.update(value[i] >> 8, date);
                                        return true;
                                    }
                                }
                            } else if (dataType == DataType.BYTE_L) {// 低字节
                                for (int i = 0; i < infoID.length; i++) {
                                    if (infoID[i] == var.tpl.getDataId()) {
                                        var.update(value[i] & 0xFF, date);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
            });

            forEachYcArrayTagVar(master.getSlaveID(), new DataHandler<YcTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YcTagVar var) {
                    if (sq) { // 处理示功图、谐波等数组数据
                        int startID = var.tpl.getDataId();
                        int len = var.getLastArrayValue().length;
                        if (startID > infoID[infoID.length - 1]
                                || startID + len <= infoID[0]) {
                            return true;
                        }
                        for (int i = 0; i < value.length; i++) {
                            int offset = infoID[i] - startID;
                            if (offset >= 0 && offset < len) {
                                var.updateArrayValue(value[i], offset);
                            }
                        }
                    } else {// 非连续
                        // TODO 暂不支持遥测数组
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 处理遥信数据帧
     * @param date
     */
    private void handleYxFrame(final Date date) {
        //log.debug("{}:本次召唤累计返回{}个遥信帧", channel.getName(), master.getYxFrameQueue().size());

        IEC104IFrame frame = null;
        while ((frame = master.getYxFrameQueue().poll()) != null) {

            final int[] infoID = frame.infoID;
            final boolean[] value = frame.getYxValue();
            final boolean sq = frame.sq;

            forEachYxTagVar(master.getSlaveID(), new DataHandler<YxTagVar>() {

                @Override
                public boolean each(EndTagWrapper model, YxTagVar var) {
                    // 遥测转遥信变量不在此进行处理
                    if (var.getTpl().getBitOffset() >= 0) {
                        return true;
                    }
                    if (sq) {// 连续的地址
                        int index = var.tpl.getDataId() - infoID[0];
                        if (index >= 0 && index < value.length) {
                            var.update(value[index], date);
                            // 触发采集
                            if (value[index] && var.getTpl().getTriggerName() != null) {
                                exeTrigger(var.getTpl().getTriggerName());
                            }
                            return true;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataId()) {
                                var.update(value[i], date);
                                // 触发采集
                                if (value[i] && var.getTpl().getTriggerName() != null) {
                                    exeTrigger(var.getTpl().getTriggerName());
                                }
                                return true;
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    private int manualCall = -1;
    /**
     * 手动召唤数据. 可用于实时召唤功图数据
     * @param trigger 被触发的帧的名称
     */
    public void exeTrigger(final String trigger) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (IEC104Frame frame : frameList) {
                    if (trigger.equals(frame.name)) {
                        manualCall = frame.ti;
                        master.call(frame.ti);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public boolean exeYK(final String endCode, final String varName, final boolean value) {
    //public boolean exeYK(int deviceAddr, final int dataID, final boolean status) {
        try {
            TagVar var = executorService.schedule(new Callable<TagVar>() {
                @Override
                public TagVar call() throws Exception {
                    return getYkVar(endCode, varName);  //To change body of implemented methods use File | Settings | File Templates.
                }
            },0,TimeUnit.MILLISECONDS).get();

            if (var == null) {
                log.warn("遥控变量未找到, {}:{}", endCode, varName);
                return false;
            }
            int dataID = var.getTpl().getDataId();

            MyYKHandler ykHandler = new MyYKHandler(dataID, value);
            final long endTime = ykHandler.getEndTime();
            master.startYK(ykHandler);

            while (ykHandler.getRet() < 0 && endTime > System.currentTimeMillis()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }

            return ykHandler.getRet() == 1;
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

    @Override
    public boolean exeYT(final String endCode, final String varName, final int value) {
        //public boolean exeYK(int deviceAddr, final int dataID, final boolean status) {
        try {
            TagVar var = executorService.schedule(new Callable<TagVar>() {
                @Override
                public TagVar call() throws Exception {
                    return getYtVar(endCode, varName);  //To change body of implemented methods use File | Settings | File Templates.
                }
            },0,TimeUnit.MILLISECONDS).get();

            int dataID = var.getTpl().getDataId();

            MyYTHandler ytHandler = new MyYTHandler(dataID, value);
            final long endTime = ytHandler.getEndTime();
            master.startYT(ytHandler);

            while (ytHandler.getRet() < 0 && endTime > System.currentTimeMillis()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }

            return ytHandler.getRet() == 1;
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

    private List<VarGroupEnum> getVarGroupByTi(int ti) {
        switch (ti) {
            case TiConst.CALL_ALL:// 总召唤结束
                return Arrays.asList(VarGroupEnum.DIAN_YM, VarGroupEnum.DIAN_YC, VarGroupEnum.YOU_JING,
                        VarGroupEnum.SHUI_JING, VarGroupEnum.RTU_ZHUANG_TAI, VarGroupEnum.SENSOR_RUN,
                        VarGroupEnum.ZYZ_YC, VarGroupEnum.ZSZ_YC);
            case TiConst.CALL_YC_YX:
                return Arrays.asList(VarGroupEnum.DIAN_YC, VarGroupEnum.YOU_JING, VarGroupEnum.SHUI_JING,
                        VarGroupEnum.RTU_ZHUANG_TAI, VarGroupEnum.SENSOR_RUN,
                        VarGroupEnum.ZYZ_YC, VarGroupEnum.ZSZ_YC, VarGroupEnum.LHZ_YC, VarGroupEnum.QI_TA);
            case TiConst.CALL_YM:// 电度
                return Arrays.asList(VarGroupEnum.DIAN_YM);
            case TiConst.CALL_XB:// 谐波
                return Arrays.asList(VarGroupEnum.DIAN_XB);
            case TiConst.CALL_GT:// 功图
                return Arrays.asList(VarGroupEnum.YOU_JING_DGT, VarGroupEnum.YOU_JING_SGT);
            case TiConst.CALL_DGT:// 电功图
                return Arrays.asList(VarGroupEnum.YOU_JING_DGT);
            case TiConst.CALL_SGT:// 示功图
                return Arrays.asList(VarGroupEnum.YOU_JING_SGT);
            case TiConst.CALL_JLC:// 计量间
                return Arrays.asList(VarGroupEnum.JI_LIANG);
            case TiConst.CALL_ZC:// 注采
                return Arrays.asList(VarGroupEnum.ZHU_CAI);
            case TiConst.CALL_RTU_CFG:// RTU参数
            case TiConst.CALL_SENSOR_CFG:// 传感器参数
                return Arrays.asList(VarGroupEnum.CAN_SHU_DEVICE);
            default:
                break;
        }

        return null;
    }

    private static class MyYTHandler implements YTHandler {
        private int ret = -1;
        private int infoID;
        private int value;
        private long endTime;
        private String message;

        private MyYTHandler(int infoID, int value) {
            this.infoID = infoID;
            this.value = value;
            endTime = System.currentTimeMillis() + 5000;
        }

        @Override
        public long getEndTime() {
            return endTime;
        }

        @Override
        public int getInfoID() {
            return infoID;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public void onFailure(String message) {
            this.message = message;
            ret = 0;
        }

        @Override
        public void onSuccess() {
            ret = 1;
        }

        private String getMessage() {
            return message;
        }

        /**
         * @return -1表示超时，1表示成功，0表示失败
         */
        public int getRet() {
            return ret;
        }
    }

    private static class MyYKHandler implements YKHandler {
        private int ret = -1;
        private int infoID;
        private boolean value;
        private long endTime;
        private String message;

        private MyYKHandler(int infoID, boolean value) {
            this.infoID = infoID;
            this.value = value;
            endTime = System.currentTimeMillis() + 5000;
        }

        @Override
        public long getEndTime() {
            return endTime;
        }

        @Override
        public int getInfoID() {
            return infoID;
        }

        @Override
        public boolean getValue() {
            return value;
        }

        @Override
        public void onFailure(String msg) {
            this.message = msg;
            ret = 0;
        }

        private String getMessage() {
            return message;
        }

        @Override
        public void onSuccess() {
            ret = 1;
        }

        /**
         * @return -1表示超时，1表示成功，0表示失败
         */
        public int getRet() {
            return ret;
        }
    }

    private class MyMasterHandler implements MasterHandler {
        private long hisDataStartTime = -1;
        @Override
        public void callEnd(int ti) {

            for (final IEC104Frame frame: frameList) {
                if (frame.ti == ti) {
                    if (frame.ti == TiConst.CALL_HIS_DAT && frame.interval > 60) {// RTU历史数据召唤结束
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                // 处理RTU历史数据
                                handleHisYxFrame(frame.interval / 60);
                                handleHisYcFrame(frame.interval / 60);
                                handleHisYmFrame(frame.interval / 60);
                                // 召唤下一时间段的历史数据
                                callHistoryData(frame.interval / 60);
                            }
                        });
                    } else { // 处理本次召唤收到的数据
                        final Date date = new Date();
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                handleYxFrame(date);
                                handleYcFrame(date);
                                handleYmFrame(date);
                                for (VarGroupEnum group : getVarGroupByTi(frame.ti)) {
                                    generateHistoryData(group, date);
                                }
                                if (frame.ti == TiConst.CALL_GT) {// 功图召唤完成
                                    // todo: 通知前端，功图召唤完成
                                    if (frame.ti == manualCall) {
                                        manualCall = -1;
                                        executorService.schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                forEachEndTag(new EndTagHandler() {
                                                    @Override
                                                    public boolean each(EndTagWrapper model) {
                                                        // 功图召唤完成,更新实时数据库,并通知前端
                                                        DataBaseManager.getInstance().getRealtimeDataService().putValue(model.getEndTag().getCode() + ":MANUAL_GT", LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                                                        return false;  //To change body of implemented methods use File | Settings | File Templates.
                                                    }
                                                });
                                            }
                                        }, realtimeDataInterval, TimeUnit.SECONDS);
                                    }

                                }
                            }
                        });
                    }
                    break;
                }

            }
        }

        /**
         * 召唤历史数据
         * @param interval 历史数据召唤间隔
         */
        private void callHistoryData(int interval) {
            if (hisDataStartTime < 0) {
                return;
            }

            long start = hisDataStartTime;
            long end = hisDataStartTime;
            while(end <= hisDataStartTime) {
                end += interval * 60 * 1000;
            }
            if (end > new Date().getTime()) {
                // TODO:历史数据中未解除的越限变量更新
                hisDataStartTime = -1;
            }

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(new Date(start));
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(new Date(end));
            master.callHisData(startCal, endCal);

        }

        @Override
        public void onConnect() {
            channel.setOnLine(true);

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    forEachEndTag(new EndTagHandler() {
                        @Override
                        public boolean each(EndTagWrapper model) {
                            model.updateRtuStatus(true, new Date());
                            return true;  //To change body of implemented methods use File | Settings | File Templates.
                        }
                    });
                    for (final IEC104Frame frame: frameList) {
                        if(frame.ti == TiConst.CALL_HIS_DAT) {// 历史数据召唤
                            callHistoryData(frame.interval / 60);
                        } else if (frame.interval <= 0) {// 只执行1次的数据召唤
                            master.call(frame.ti);
                        }
                    }
                }
            });
        }

        @Override
        public void onDisconnect() {
            log.info("连接未建立或断开:{}", channel.getName());
            channel.setOnLine(false);
            forEachEndTag(new EndTagHandler() {
                @Override
                public boolean each(EndTagWrapper model) {
                    model.updateRtuStatus(false, new Date());
                    return true;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
            if (hisDataStartTime < 0) {
                hisDataStartTime = new Date().getTime();
            }
        }

        @Override
        public void onSOE(IEC104IFrame frame) {
            final Date date = frame.getTimes()[0].getTime();

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    handleYxFrame(date);
                }
            });
        }

        @Override
        public void onYcOffLimits(IEC104IFrame frame) {
            final Date date = frame.getTimes()[0].getTime();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    handleYcFrame(date);
                }
            });
        }
    }

}
