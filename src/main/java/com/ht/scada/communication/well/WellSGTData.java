package com.ht.scada.communication.well;

import java.util.Date;



/**
 * 油井示功图数据:包括油井编码、载荷数组、位移数组、冲程、冲次
 * 
 * @author 薄成文 
 * 
 */
public class WellSGTData {
	
	/**
	 * 井号，可以是中文或英文，如草13-113、C13-P32
	 */
	public final String code;
	
	/**
	 * 载荷，示功图纵坐标
	 */
	public float[] zaihe0;
	public float[] zaihe1;
	public float[] zaihe2;
	public float[] zaihe3;
	
	/**
	 * 位移，示功图横坐标
	 */
	public float[] weiyi0;
	public float[] weiyi1;
	public float[] weiyi2;
	public float[] weiyi3;
	
	/**
	 * 冲次
	 */
	public float chongCi;
	
	/**
	 * 冲程
	 */
	public float chongCheng;
	
	public Date datetime;
	
	public WellSGTData(String code) {
		this.code = code;
	}

	public void reset() {
		this.weiyi0 = null;
		this.weiyi1 = null;
		this.weiyi2 = null;
		this.weiyi3 = null;
		this.zaihe0 = null;
		this.zaihe1 = null;
		this.zaihe2 = null;
		this.zaihe3 = null;
		this.chongCheng = Float.NaN;
		this.chongCi = Float.NaN;
	}
	
	public boolean isReady() {
		if (this.weiyi0 == null) {
			return false;
		}
		if (this.weiyi1 == null) {
			return false;
		}
		if (this.weiyi2 == null) {
			return false;
		}
		if (this.weiyi3 == null) {
			return false;
		}
		
		if(this.zaihe0 == null) {
			return false;
		}
		if(this.zaihe1 == null) {
			return false;
		}
		if(this.zaihe2 == null) {
			return false;
		}
		if(this.zaihe3 == null) {
			return false;
		}
		
		if (Float.isNaN(this.chongCheng)) {
			return false;
		}
		if (Float.isNaN(this.chongCi)) {
			return false;
		}
		return true;
	}
}