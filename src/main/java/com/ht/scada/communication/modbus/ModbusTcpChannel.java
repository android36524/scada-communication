package com.ht.scada.communication.modbus;

import com.ht.modbus.ModbusMaster;
import com.ht.modbus.dtu.DTUMaster;
import com.ht.modbus.msg.response.IModbusReadResponse;
import com.ht.modbus.tcp.TCPMaster;
import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.entity.TagCfgTpl;
import com.ht.scada.common.tag.exception.PortInfoErrorException;
import com.ht.scada.common.tag.util.ChannelFrameFactory;
import com.ht.scada.common.tag.util.ChannelFrameFactory.ModbusFrame;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.common.tag.util.PortInfoFactory;
import com.ht.scada.common.tag.util.PortInfoFactory.DtuInfo;
import com.ht.scada.common.tag.util.PortInfoFactory.TcpIpInfo;
import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.model.*;
import com.ht.scada.communication.util.DataValueUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ModbusTcpChannel extends CommunicationChannel {
	private boolean running = false;
    private ScheduledExecutorService executorService;

	private ModbusMaster master;
	private List<ModbusFrame> frameList;
	
	public ModbusTcpChannel(AcquisitionChannel channel, List<EndTagWrapper> endTagList) throws Exception {
		super(channel, endTagList);
	}

	@Override
	public void init() throws Exception {
		String portInfo = channel.getPortInfo();
		if (portInfo.startsWith("tcp/ip")) {
			TcpIpInfo tcpIpInfo = PortInfoFactory.parseTcpIpInfo(portInfo);
			master = new TCPMaster(tcpIpInfo.ip, tcpIpInfo.port);
		} else if (portInfo.startsWith("dtu")) {
			DtuInfo dtuInfo = PortInfoFactory.parseDtuInfo(portInfo);
			master = new DTUMaster(dtuInfo.dtuId, dtuInfo.heartBeat, 5000, 1000);
		} else {
			throw new PortInfoErrorException("ModbusTcp通讯规约不支持该通讯模式:" + portInfo);
		}
		
		frameList = ChannelFrameFactory.parseModbusFrames(channel.getFrames());
	}

	@Override
	public void start() {
		if(master != null) {
			running = true;
            executorService = Executors.newSingleThreadScheduledExecutor();

            // 解析通讯帧并生成数据存储区域
            for (final ModbusFrame frame : frameList) {
                if (frame.interval > 0) {
                    executorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            executeFrame(frame);
                        }
                    }, 0, frame.interval, TimeUnit.MILLISECONDS);
                } else {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            executeFrame(frame);
                        }
                    });
                }
            }
		}
		
	}

    private void executeFrame(ModbusFrame frame) {
        int[][] slaveArray = frame.slave;
        for (int[] slaveRange : slaveArray) {
            if (!running) {
                break;
            }

            int slaveStart = slaveRange[0];
            int slaveEnd = slaveRange[1];
            if (slaveEnd > 0) {// 地址范围
                for (int i = slaveStart; i <= slaveEnd; i++) {
                    if (!running) {
                        break;
                    }
                    try {
                        final IModbusReadResponse resp = master.sendReadRequest(i, frame.funCode, frame.start, frame.len);
                        handleResponse(i, frame.funCode, frame.start, frame.len, resp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {// 只启用起始地址
                try {
                    IModbusReadResponse resp = master.sendReadRequest(slaveStart, frame.funCode, frame.start, frame.len);
                    handleResponse(slaveStart, frame.funCode, frame.start, frame.len, resp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
	 * @param slaveID
	 * @param resp
	 */
	private void handleResponse(int slaveID, final int funCode, final int start, final int len,
			final IModbusReadResponse resp) {
		final Date datetime = new Date();
		if (funCode == 3 || funCode == 4) {// 读二进制数据
			final byte[] respData = resp.getByteArray();
			
			forEachYxTagVar(slaveID, new DataHandler<YxTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YxTagVar var) {
                    TagCfgTpl tpl = var.tpl;
                    if (tpl.getDataType() == DataType.BOOL && tpl.getBitOffset() >= 0) {
                        int index = tpl.getDataID() - start;
                        if (tpl.getFunCode() != funCode || index < 0 || index + tpl.getByteLen() / 2 >= len) {
                            return true;
                        }
                        var.update(DataValueUtil.parseBoolValue(respData, index * 2 + tpl.getByteOffset(), tpl.getBitOffset()), datetime, realtimeDataMap);
                    }
                    return true;
                }
            });
			
			forEachYcTagVar(slaveID, new DataHandler<YcTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YcTagVar var) {

                    TagCfgTpl tpl = var.tpl;

                    float value = Float.NaN;
                    int index = tpl.getDataID() - start;
                    if (tpl.getFunCode() != funCode || index < 0 || index + tpl.getByteLen() / 2 >= len) {
                        return true;
                    }

                    switch (tpl.getDataType()) {
                        case INT16:
                            int int16 = DataValueUtil.parseInt16(respData, index * 2 + tpl.getByteOffset());
                            var.update(int16 * tpl.getCoefValue() + tpl.getBaseValue(), datetime, realtimeDataMap);
                            break;
                        case FLOAT:
                            float floatValue = DataValueUtil.parseFloatValue(respData, index * 2 + tpl.getByteOffset());
                            var.update(floatValue * tpl.getCoefValue() + tpl.getBaseValue(), datetime, realtimeDataMap);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
			forEachYmTagVar(slaveID, new DataHandler<YmTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YmTagVar var) {

                    TagCfgTpl tpl = var.tpl;

                    double value = Double.NaN;
                    int index = tpl.getDataID() - start;
                    if (tpl.getFunCode() != funCode || index < 0 || index + tpl.getByteLen() / 2 >= len) {
                        return true;
                    }

                    switch (tpl.getDataType()) {
                        case INT32:
                            int int32 = DataValueUtil.parseInt32(respData, index * 2 + tpl.getByteOffset());
                            var.update(int32 * tpl.getCoefValue() + tpl.getBaseValue(), datetime, realtimeDataMap);
                            break;
                        case DOUBLE:
                            double doubleValue = DataValueUtil.parseDoubleValue(respData, index * 2 + tpl.getByteOffset());
                            var.update(doubleValue * tpl.getCoefValue() + tpl.getBaseValue(), datetime, realtimeDataMap);
                        case MOD10000:
                            int mod10000 = DataValueUtil.parseMod10000(respData, index * 2 + tpl.getByteOffset());
                            var.update(mod10000 * tpl.getCoefValue() + tpl.getBaseValue(), datetime, realtimeDataMap);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
		} else if (funCode == 1 || funCode == 2) {// 读遥信数据
			final boolean[] respData = resp.getBooleanResultArray();
			if (respData == null) {
				return;
			}
			
			forEachYxTagVar(slaveID, new DataHandler<YxTagVar>() {
                @Override
                public boolean each(EndTagWrapper model, YxTagVar var) {
                    TagCfgTpl tpl = var.tpl;
                    if (tpl.getDataType() == DataType.BOOL) {

                        if (tpl.getFunCode() != funCode) {
                            return true;
                        }
                        int index = tpl.getDataID() - start;
                        if (index >= 0 && index < respData.length) {
                            var.update(respData[index], datetime, realtimeDataMap);
                        }
                    }
                    return true;
                }
            });
		}
	}

	@Override
	public void stop() {
		running = false;
		if (executorService != null) {
            executorService.shutdownNow();
		}
		if (master != null) {
			master.close();
		}
	}

	@Override
	public boolean exeYK(int deviceAddr, int dataID, boolean value) {
        // TODO: Modbus遥控未实现
		return false;
	}

	@Override
	public boolean exeYT(int deviceAddr, int dataID, int value) {
        // TODO: Modbus遥调未实现
		return false;
	}

}
