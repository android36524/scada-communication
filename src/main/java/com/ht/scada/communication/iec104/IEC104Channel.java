package com.ht.scada.communication.iec104;

import com.ht.iec104.frame.IEC104IFrame;
import com.ht.iec104.master.IEC104Master;
import com.ht.iec104.master.MasterHandler;
import com.ht.iec104.master.YKHandler;
import com.ht.iec104.master.YTHandler;
import com.ht.iec104.util.TiConst;
import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.util.ChannelFrameFactory;
import com.ht.scada.common.tag.util.ChannelFrameFactory.IEC104Frame;
import com.ht.scada.common.tag.util.PortInfoFactory;
import com.ht.scada.common.tag.util.PortInfoFactory.TcpIpInfo;
import com.ht.scada.common.tag.util.VarGroup;
import com.ht.scada.common.tag.util.VarType;
import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.model.*;
import com.ht.scada.communication.util.CommUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IEC104规约采集通道<br>
 * <pre>
 *     启动采集后本程序将自动创建一个线程并在线程是循环执行采集。
 * </pre>
 * <pre>
 *     通讯帧配置时总召唤的帧间隔请设置为0，历史数据召唤的帧间隔请结合RTU的历史数据存储周期进行设置。
 * </pre>
 *
 * @author 薄成文
 */
public class IEC104Channel extends CommunicationChannel {
    private static final Logger log = LoggerFactory.getLogger(IEC104Channel.class);
    private final int realtimeDataInterval = 1;
    private final int historyDataInterval = 60;

    private volatile boolean running = false;
    private List<IEC104Frame> frameList;
    private IEC104Master master;

    private IEC104Channel.MyMasterHandler masterHandler;
    private ScheduledExecutorService executorService;

    public IEC104Channel(AcquisitionChannel channel, List<EndTagWrapper> endTagList) throws Exception {
        super(channel, endTagList);
    }

    @Override
    public void init() throws Exception {
        log.info("{}:初始化采集通道", channel.getName());

        String portInfo = channel.getPortInfo();
        if (portInfo.startsWith("tcp/ip")) {

            frameList = ChannelFrameFactory.parseIEC104Frames(channel.getFrames());
            log.info("{} - 召唤帧共：{}", channel.getName(), frameList.size());

            TcpIpInfo tcpIpInfo = PortInfoFactory.parseTcpIpInfo(portInfo);
            master = new IEC104Master(tcpIpInfo.ip, tcpIpInfo.port, 1);

            masterHandler = new MyMasterHandler();
            master.setMasterHandler(masterHandler);

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
            master.open();

            executorService = Executors.newSingleThreadScheduledExecutor();

            /** 定时执行数据召唤 **/
            for (final IEC104Frame frame: frameList) { // 历史数据召唤不在此处执行
                if (frame.interval > 0 && frame.ti != TiConst.CALL_HIS_DAT) {
                    // 按召唤间隔执行数据召唤
                    executorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            master.call(frame.ti);
                        }
                    }, 0, frame.interval, TimeUnit.SECONDS);
                }
            }

            // 定时更新实时数据库
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    updateRealtimeData();
                }
            }, realtimeDataInterval, realtimeDataInterval, TimeUnit.SECONDS);

            // 定时更新历史数据库
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    persistHistoryData();
                }
            }, historyDataInterval, historyDataInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stop() {
        log.info("{}:停止采集", channel.getName());

        running = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
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

            forEachYcTagVar(master.getSlaveID(), new DataHandler<YcTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YcTagVar var) {

                    VarGroup varGroup = var.tpl.getVarGroup();
                    VarGroupWrapper varGroupWrapper = model.varGroupWrapperMap.get(varGroup);
                    if (varGroupWrapper == null) {// 未配置分组
                        return true;
                    }

                    int interval = varGroupWrapper.cfg.getInterval();
                    if (interval <= 0) {
                        interval = defaultInterval;
                    }

                    if (var.lastArrayValue != null) {// 功图、谐波历史数据

                        int startID = var.tpl.getDataID();
                        int len = var.lastArrayValue.length;
                        if (startID > infoID[infoID.length - 1] || startID + len <= infoID[0]) {
                            return true;
                        }

                        Calendar cal = datetime[0];
                        String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                        float[] v = model.addRTUYcArrayHisData(var, key, cal.getTime());
                        for (int i = 0; i < value.length; i++) {
                            int offset = infoID[i] - startID;
                            if (offset >= 0 && offset < len) {
                                v[offset] = (var.coefValue) * value[i] + var.baseValue;
                            }
                        }
                    } else {// 单个的遥测变量
                        int index = -1;
                        if (sq) {// 连续的地址
                            int i = var.tpl.getDataID() - infoID[0];
                            if (i >= 0 && i < value.length) {
                                index = i;
                            }
                        } else {// 非连续的地址
                            for (int i = 0; i < infoID.length; i++) {
                                if (infoID[i] == var.tpl.getDataID()) {
                                    index = i;
                                    break;
                                }
                            }
                        }

                        if (index >= 0) {
                            float v = (var.coefValue) * value[index] + var.baseValue;
                            Calendar cal = datetime[index];
                            String key = CommUtil.createRTUHisDataKey(varGroup, cal, interval);
                            model.addRTUYcHisData(var, key, cal.getTime(), v);
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

                    VarGroup varGroup = var.tpl.getVarGroup();
                    VarGroupWrapper varGroupWrapper = model.varGroupWrapperMap.get(varGroup);
                    if (varGroupWrapper == null) {
                        return true;
                    }

                    int index = -1;
                    if (sq) {// 连续的地址
                        int i = var.tpl.getDataID() - infoID[0];
                        if (i >= 0 && i < value.length) {
                            index = i;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataID()) {
                                index = i;
                                break;
                            }
                        }
                    }

                    int interval = varGroupWrapper.cfg.getInterval();
                    if (interval <= 0) {
                        interval = defaultInterval;
                    }

                    if (index >= 0) {
                        double v = (var.coefValue) * value[index] + var.baseValue;
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

                    VarGroup varGroup = var.tpl.getVarGroup();
                    VarGroupWrapper varGroupWrapper = model.varGroupWrapperMap.get(varGroup);
                    if (varGroupWrapper == null) {
                        return true;
                    }

                    int index = -1;
                    if (sq) {// 连续的地址
                        int i = var.tpl.getDataID() - infoID[0];
                        if (i >= 0 && i < value.length) {
                            index = i;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataID()) {
                                index = i;
                                break;
                            }
                        }
                    }

                    int interval = varGroupWrapper.cfg.getInterval();
                    if (interval <= 0) {
                        interval = defaultInterval;
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
        IEC104IFrame frame;
        while ((frame = master.getYmFrameQueue().poll()) != null) {

            final int[] infoID = frame.infoID;
            final long[] value = frame.getYmValue();
            final boolean sq = frame.sq;

            forEachYmTagVar(master.getSlaveID(), new DataHandler<YmTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YmTagVar var) {
                    if (sq) {
                        int i = var.tpl.getDataID() - infoID[0];
                        if (i >= 0 && i < value.length) {
                            var.update((double) (var.coefValue) * value[i] + var.baseValue, date, realtimeDataMap);
                            return true;
                        }
                    } else {
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataID()) {
                                var.update((double) (var.coefValue) * value[i] + var.baseValue, date, realtimeDataMap);
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
        IEC104IFrame frame;
        while ((frame = master.getYcFrameQueue().poll()) != null) {

            final int[] infoID = frame.infoID;
            final int[] value = frame.getYcValue();
            final boolean sq = frame.sq;

            forEachYcTagVar(master.getSlaveID(), new DataHandler<YcTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YcTagVar var) {
                    if (sq) {
                        if (var.lastArrayValue != null) {// 遥测数组
                            // 处理示功图、谐波等数组数据
                            int startID = var.tpl.getDataID();
                            int len = var.lastArrayValue.length;
                            if (startID > infoID[infoID.length - 1]
                                    || startID + len <= infoID[0]) {
                                return true;
                            }
                            for (int i = 0; i < value.length; i++) {
                                int offset = infoID[i] - startID;
                                if (offset >= 0 && offset < len) {
                                    var.lastArrayValue[offset] = (float) (var.coefValue) * value[i] + var.baseValue;
                                }
                            }
                        } else {// 单个遥测
                            int i = var.tpl.getDataID() - infoID[0];
                            if (i >= 0 && i < value.length) {
                                var.update((var.coefValue) * value[i] + var.baseValue, date, realtimeDataMap);
                                return true;
                            }
                        }
                    } else {
                        if (var.tpl.getVarType() == VarType.YC) {
                            for (int i = 0; i < infoID.length; i++) {
                                if (infoID[i] == var.tpl.getDataID()) {
                                    var.update((var.coefValue) * value[i] + var.baseValue, date, realtimeDataMap);
                                    return true;
                                }
                            }
                        }
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
        IEC104IFrame frame = null;
        while ((frame = master.getYxFrameQueue().poll()) != null) {

            final int[] infoID = frame.infoID;
            final boolean[] value = frame.getYxValue();
            final boolean sq = frame.sq;

            forEachYxTagVar(master.getSlaveID(), new DataHandler<YxTagVar>() {

                @Override
                public boolean each(EndTagWrapper model, YxTagVar var) {
                    if (sq) {// 连续的地址
                        int index = var.tpl.getDataID() - infoID[0];
                        if (index >= 0 && index < value.length) {
                            var.update(value[index], date, realtimeDataMap);
                            return true;
                        }
                    } else {// 非连续的地址
                        for (int i = 0; i < infoID.length; i++) {
                            if (infoID[i] == var.tpl.getDataID()) {
                                var.update(value[i], date, realtimeDataMap);
                                return true;
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }


    @Override
    public boolean exeYK(int deviceAddr, final int dataID, final boolean status) {
        MyYKHandler ykHandler = new MyYKHandler(dataID, status);
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
    }

    @Override
    public boolean exeYT(int deviceAddr, int dataID, int value) {
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
    }

    private List<VarGroup> getVarGroupByTi(int ti) {
        switch (ti) {
            case TiConst.CALL_ALL:// 总召唤结束
                return Arrays.asList(VarGroup.DIAN_YM, VarGroup.DIAN_YC, VarGroup.YOU_JING, VarGroup.SHUI_JING);
            case TiConst.CALL_YC_YX:
                return Arrays.asList(VarGroup.DIAN_YM, VarGroup.DIAN_YC, VarGroup.YOU_JING, VarGroup.SHUI_JING);
            case TiConst.CALL_HIS_DAT:// RTU历史数据忽略
                break;
            case TiConst.CALL_YM:// 电度
                return Arrays.asList(VarGroup.DIAN_YM);
            case TiConst.CALL_XB:// 谐波
                return Arrays.asList(VarGroup.DIAN_XB);
            case TiConst.CALL_DGT:// 电功图
                return Arrays.asList(VarGroup.YOU_JING_DGT);
            case TiConst.CALL_SGT:// 示功图
                return Arrays.asList(VarGroup.YOU_JING_SGT);
            case TiConst.CALL_JLC:// 计量间
                return Arrays.asList(VarGroup.JI_LIANG);
            case TiConst.CALL_ZC:// 注采
                return Arrays.asList(VarGroup.ZHU_CAI);
            case TiConst.CALL_RTU_CFG:// RTU参数
            case TiConst.CALL_SENSOR_CFG:// 传感器参数
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

        private MyYTHandler(int infoID, int value) {
            this.infoID = infoID;
            this.value = value;
            endTime = System.currentTimeMillis() + 3000;
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
            ret = 0;
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

    private static class MyYKHandler implements YKHandler {
        private int ret = -1;
        private int infoID;
        private boolean value;
        private long endTime;

        private MyYKHandler(int infoID, boolean value) {
            this.infoID = infoID;
            this.value = value;
            endTime = System.currentTimeMillis() + 3000;
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
            ret = 0;
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
                    if (frame.ti == TiConst.CALL_HIS_DAT) {// RTU历史数据召唤结束
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
                    } else { // 处理历史数据
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                Date date = new Date();
                                handleYxFrame(date);
                                handleYcFrame(date);
                                handleYmFrame(date);
                                for (VarGroup group : getVarGroupByTi(frame.ti)) {
                                    generateHistoryData(group, date);
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
                // TODO:未解除的越限变量更新
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
            for (final IEC104Frame frame: frameList) {
                if(frame.ti == TiConst.CALL_HIS_DAT) {// 历史数据召唤
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            callHistoryData(frame.interval / 60);
                        }
                    });
                } else if (frame.interval <= 0) {// 只执行1次的数据召唤
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            master.call(frame.ti);
                        }
                    });
                }
            }
        }

        @Override
        public void onDisconnect() {
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
