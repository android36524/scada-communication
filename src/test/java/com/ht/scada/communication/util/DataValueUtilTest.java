package com.ht.scada.communication.util;

import org.testng.annotations.Test;

public class DataValueUtilTest {

  @Test
  public void parseBoolValue() {
	  assert DataValueUtil.parseBoolValue(new byte[]{1}, 0, 0);
	  assert !DataValueUtil.parseBoolValue(new byte[]{0}, 0, 0);
	  assert DataValueUtil.parseBoolValue(new byte[]{0, 1}, 1, 0);
  }

  @Test
  public void parseDoubleValue() {
	  byte b = -23;
	  System.out.println(((b & 0xFF) << 8) | (b&0xff));
	  System.out.println(((((b << 8)) & 0xFFFF) | b ) & 0xFFFF);
	  
	  assert DataValueUtil.parseDoubleValue(new byte[]{0,0,0,0,0,0,0,1}, 0) == Double.longBitsToDouble(1);
  }

  @Test
  public void parseFloatValue() {
	  assert DataValueUtil.parseFloatValue(new byte[]{0,0,0,1}, 0) == Float.intBitsToFloat(1);
  }

  @Test
  public void parseInt16() {
	  assert DataValueUtil.parseInt16(new byte[]{(byte) 0x88,(byte) 0x88}, 0) == 0x8888;
	  assert DataValueUtil.parseInt16(new byte[]{(byte) 0x20,(byte) 0x20}, 0) == 0x2020;
  }

  @Test
  public void parseInt32() {
	  byte b1 = (byte) 0x88;
	  byte b2 = (byte) 0x08;
	  assert DataValueUtil.parseInt32(new byte[]{0, 0, 0,1}, 0) == 1;
	  assert DataValueUtil.parseInt32(new byte[]{b1, b1, b1,b1}, 0) == 0x88888888;
	  assert DataValueUtil.parseInt32(new byte[]{b2, b2, b2,b2}, 0) == 0x08080808;
  }

  @Test
  public void parseMod10000() {
	  byte b1 = (byte) 0x88;
	  byte b2 = (byte) 0x08;
	  assert DataValueUtil.parseMod10000(new byte[]{b1, b1, b1,b1}, 0) == (0x8888 * 10000 + 0x8888);
	  assert DataValueUtil.parseMod10000(new byte[]{b2, b2, b2,b2}, 0) == (0x0808 * 10000 + 0x0808);
  }
}
