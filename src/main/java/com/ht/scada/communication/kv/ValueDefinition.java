package com.ht.scada.communication.kv;

import com.ht.scada.data.kv.*;
import oracle.kv.Value;

import java.io.*;
import java.util.Date;

/**
 * KV数据库值定义
 * @author 薄成文
 *
 */
public class ValueDefinition {
	
	public static Value makeYXDataStoreValue(YXData data) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        try {
        	dos.writeUTF(data.getInfo());
        	dos.writeBoolean(data.getValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Value.createValue(baos.toByteArray());
    }
	
	private static void parseYXDataStoreValue(Value value, YXData data) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(value.getValue());
        final DataInputStream dis = new DataInputStream(bais);
        try {
        	data.setInfo(dis.readUTF());
			data.setValue(dis.readBoolean());
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
	}
	
	public static Value makeYCDataStoreValue(YCData data) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        try {
        	dos.writeDouble(data.getValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Value.createValue(baos.toByteArray());
    }
	
	private static void parseYCDataStoreValue(Value value, YCData data) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(value.getValue());
        final DataInputStream dis = new DataInputStream(bais);
        try {
			data.setValue(dis.readDouble());
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
	}
	
	public static Value makeYMDataStoreValue(YMData data) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        try {
        	dos.writeDouble(data.getValue());
        	dos.writeDouble(data.getChange());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Value.createValue(baos.toByteArray());
    }
	
	private static void parseYMDataStoreValue(Value value, YMData data) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(value.getValue());
        final DataInputStream dis = new DataInputStream(bais);
        try {
			data.setValue(dis.readDouble());
			data.setChange(dis.readDouble());
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
	}
	
	public static Value makeFaultRecordStoreValue(FaultRecord data) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        try {
        	dos.writeUTF(data.getInfo());
        	dos.writeBoolean(data.getValue());
        	if (data.getResumeTime() == null) {
        		dos.writeBoolean(false);
        	} else {
        		dos.writeBoolean(true);
        		dos.writeLong(data.getResumeTime().getTime());
        	}
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Value.createValue(baos.toByteArray());
    }
	
	private static void parseFaultRecordStoreValue(Value value, FaultRecord data) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(value.getValue());
        final DataInputStream dis = new DataInputStream(bais);
        try {
			data.setInfo(dis.readUTF());
			data.setValue(dis.readBoolean());
			if (dis.readBoolean()) {
				data.setResumeTime(new Date(dis.readLong()));
			}
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
	}
	
	public static Value makeOffLimitsRecordStoreValue(OffLimitsRecord data) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        try {
        	dos.writeUTF(data.getInfo());
        	dos.writeDouble(data.getValue());
        	dos.writeDouble(data.getThreshold());
        	dos.writeBoolean(data.getType());
        	if (data.getResumeTime() == null) {
        		dos.writeBoolean(false);
        	} else {
        		dos.writeBoolean(true);
        		dos.writeLong(data.getResumeTime().getTime());
        	}
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Value.createValue(baos.toByteArray());
    }
	
	private static void parseOffLimitsRecordStoreValue(Value value, OffLimitsRecord data) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(value.getValue());
        final DataInputStream dis = new DataInputStream(bais);
        try {
			data.setInfo(dis.readUTF());
			data.setValue(dis.readDouble());
			data.setThreshold(dis.readDouble());
			data.setType(dis.readBoolean());
			if (dis.readBoolean()) {
				data.setResumeTime(new Date(dis.readLong()));
			}
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
	}
	
	public static <T> Value makeValue(T data) {
		if (data instanceof YMData) {
			return makeYMDataStoreValue((YMData) data);
		} else if (data instanceof YCData) {
			return makeYCDataStoreValue((YCData) data);
		} else if (data instanceof YXData) {
			return makeYXDataStoreValue((YXData) data);
		} else if (data instanceof FaultRecord) {
			return makeFaultRecordStoreValue((FaultRecord) data);
		} else if (data instanceof OffLimitsRecord) {
			return makeOffLimitsRecordStoreValue((OffLimitsRecord) data);
		}
        return null;
    }
	
	public static <T> T parseValue(Value value, T data) {
		if (data instanceof YMData) {
			parseYMDataStoreValue(value, (YMData) data);
		} else if (data instanceof YCData) {
			parseYCDataStoreValue(value, (YCData) data);
		} else if (data instanceof YXData) {
			parseYXDataStoreValue(value, (YXData) data);
		} else if (data instanceof FaultRecord) {
			parseFaultRecordStoreValue(value, (FaultRecord) data);
		} else if (data instanceof OffLimitsRecord) {
			parseOffLimitsRecordStoreValue(value, (OffLimitsRecord) data);
		}
		return data;
//		final ByteArrayInputStream bais = new ByteArrayInputStream(value.getValue());
//        final DataInputStream dis = new DataInputStream(bais);
//        try {
//			kv.setValue(dis.readDouble());
//			kv.setChange(dis.readDouble());
//		} catch (IOException e) {
//            throw new RuntimeException(e);
//		}
//        return kv;
	}
}
