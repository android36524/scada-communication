package com.ht.scada.communication.util;

public class DataValueUtil {

	public static boolean parseBoolValue(byte[] data, int byteOffset, int bitOffset) {
		return (data[byteOffset] & (1 << bitOffset)) > 0;
	}

	public static int parseInt16(byte[] data, int byteOffset) {
		int i = (data[byteOffset] << 8) & 0xFFFF;
		i |= data[byteOffset + 1] & 0xFF;
		return i;
	}

	public static int parseInt32(final byte[] data, final int byteOffset) {
		int i = (data[byteOffset] << 24) & 0xFFFFFFFF;
		i |= (data[byteOffset + 1] << 16) & 0xFFFFFF;
		i |= (data[byteOffset + 2] << 8) & 0xFFFF;
		i |= data[byteOffset + 3] & 0xFF;
		return i;
	}
	
	/**
	 * 
	 * @param data 高字节在前，低字节在后
	 * @param byteOffset
	 * @return
	 */
	public static int parseMod10000(final byte[] data, final int byteOffset) {
		int h = (data[byteOffset] << 8) & 0xFFFF;
		h |= data[byteOffset + 1] & 0xFF;
		
		int l = (data[byteOffset + 2] << 8) & 0xFFFF;
		l |= data[byteOffset + 3] & 0xFF;
		
		return h * 10000 + l;
	}

	public static float parseFloatValue(final byte[] data, final int byteOffset) {
		return Float.intBitsToFloat(parseInt32(data, byteOffset));
	}

	public static long toLong(byte[] data, final int byteOffset) {
		long l = data[byteOffset + 7] & 0xFF;
		l |= ((long) data[byteOffset + 6] << 8);
		l |= ((long) data[byteOffset + 5] << 16);
		l |= ((long) data[byteOffset + 4] << 24);
		l |= ((long) data[byteOffset + 3] << 32);
		l |= ((long) data[byteOffset + 2] << 40);
		l |= ((long) data[byteOffset + 1] << 48);
		l |= ((long) data[byteOffset] << 56);
		return l;
	}

	public static double parseDoubleValue(final byte[] data, final int byteOffset) {
		return Double.longBitsToDouble(toLong(data, byteOffset));
	}
}
