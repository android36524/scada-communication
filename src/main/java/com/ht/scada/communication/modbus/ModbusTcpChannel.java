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
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.TagVar;
import com.ht.scada.communication.util.DataValueUtil;

import java.util.Date;
import java.util.List;

public class ModbusTcpChannel extends CommunicationChannel {
	private boolean running = false;
	private Thread thread;
	
	private ModbusMaster master;
	private List<ModbusFrame> frameList;
	
	public ModbusTcpChannel(AcquisitionChannel channel) throws Exception {
		super(channel);
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
			thread = new Thread(this);
			thread.start();
		}
		
	}
	
	@Override
	public void execute() {
		// 解析通讯帧并生成数据存储区域
		for (final ModbusFrame frame : frameList) {
			
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
	}
	
	@Override
	public void run() {

		while(running) {
			execute();
		}
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
			
			forEachTagVar(slaveID, funCode, datetime, Boolean.class, new DataHandler<Boolean>() {
				@Override
				public Boolean each(EndTagWrapper model, TagVar var) {
					TagCfgTpl tpl = var.tpl;
					if (tpl.getDataType() == DataType.BOOL && tpl.getBitOffset() >= 0) {
						int index = tpl.getDataID() - start;
						if (tpl.getFunCode() != funCode || index < 0 || index + tpl.getByteLen() / 2 >= len) {
							return null;
						}
					
						return DataValueUtil.parseBoolValue(respData, index * 2 + tpl.getByteOffset(), tpl.getBitOffset());
					} else {
						return null;
					}
				}
			});
			
			forEachTagVar(slaveID, funCode, datetime, Float.class, new DataHandler<Float>() {
				@Override
				public Float each(EndTagWrapper model, TagVar var) {
					
					TagCfgTpl tpl = var.tpl;
					
					float value = Float.NaN;
					int index = tpl.getDataID() - start;
					if (tpl.getFunCode() != funCode || index < 0 || index + tpl.getByteLen() / 2 >= len) {
						return value;
					}
					
					switch (tpl.getDataType()) {
					case INT16:
						int int16 = DataValueUtil.parseInt16(respData, index * 2 + tpl.getByteOffset());
						value = int16 * tpl.getCoefValue() + tpl.getBaseValue();
						break;
					case FLOAT:
						float floatValue = DataValueUtil.parseFloatValue(respData, index * 2 + tpl.getByteOffset());
						value = floatValue * tpl.getCoefValue() + tpl.getBaseValue();
						break;
					default:
						break;
					}
					return value;
				}
			});
			forEachTagVar(slaveID, funCode, datetime, Double.class, new DataHandler<Double>() {
				@Override
				public Double each(EndTagWrapper model, TagVar var) {
					
					TagCfgTpl tpl = var.tpl;
					
					double value = Double.NaN;
					int index = tpl.getDataID() - start;
					if (tpl.getFunCode() != funCode || index < 0 || index + tpl.getByteLen() / 2 >= len) {
						return value;
					}
					
					switch (tpl.getDataType()) {
					case INT32:
						int int32 = DataValueUtil.parseInt32(respData, index * 2 + tpl.getByteOffset());
						value = int32 * tpl.getCoefValue() + tpl.getBaseValue();
						break;
					case DOUBLE:
						double doubleValue = DataValueUtil.parseDoubleValue(respData, index * 2 + tpl.getByteOffset());
						value = doubleValue * tpl.getCoefValue() + tpl.getBaseValue();
					case MOD10000:
						int mod10000 = DataValueUtil.parseMod10000(respData, index * 2 + tpl.getByteOffset());
						value = mod10000 * tpl.getCoefValue() + tpl.getBaseValue();
						break;
					default:
						break;
					}
					return value;
				}
			});
		} else if (funCode == 1 || funCode == 2) {// 读遥信数据
			final boolean[] respData = resp.getBooleanResultArray();
			if (respData == null) {
				return;
			}
			
			forEachTagVar(slaveID, funCode, datetime, Boolean.class, new DataHandler<Boolean>() {
				@Override
				public Boolean each(EndTagWrapper model, TagVar var) {
					TagCfgTpl tpl = var.tpl; 
					if (tpl.getDataType() == DataType.BOOL) {
						
						if (tpl.getFunCode() != funCode) {
							return null;
						}
						int index = tpl.getDataID() - start;
						if (index >= 0 && index < respData.length) {
							return respData[index];
						} else {
							return null;
						}
					} else {
						return null;
					}
				}
			});
		}
	}

	@Override
	public void stop() {
		running = false;
		if (thread != null) {
			thread.interrupt();
		}
		if (master != null) {
			master.close();
		}
	}

	@Override
	public boolean exeYK(int dataID, boolean value) {
		return false;
	}

	@Override
	public boolean exeYT(int dataID, int value) {
		return false;
	}

}
