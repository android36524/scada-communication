package com.ht.scada.communication.well;

import com.ht.scada.common.middleware.service.WellService;
import com.ht.scada.common.tag.consts.GTSubType;
import com.ht.scada.communication.model.TagVar;

public class WellDataHandler {

	private WellSGTData sgtData;
	private String endCode;
	private String endType;
	private WellService wellService;
	
	
	public WellDataHandler(String code, String endType) {
		this.endCode = code;
		this.endType = endType;
		sgtData =  new WellSGTData(code);
	}
	
	public void handleWellData(TagVar var, float data) {
		if (var.tpl.getSubType().equals(GTSubType.YC_CHONG_CHENG)) {
			sgtData.chongCheng = data;
		} else if (var.tpl.getSubType().equals(GTSubType.YC_CHONG_CI)) {
			sgtData.chongCi = data;
		}
		
		if (sgtData.isReady()) {//油井示功图数据全部接收完成
			// 保存数据
			saveSGTData();
			sgtData.reset();
		}
	}

	/**
	 * @param var
	 * @param data
	 */
	public void handleWellData(TagVar var, float[] data) {
		// 功图位移和载荷
		if (var.tpl.getSubType().equals(GTSubType.YC_WEI_YI)) {
			int idx = var.tpl.getBitOffset();
			switch (idx) {
			case 0:
				sgtData.weiyi0 = data;
				break;
			case 1:
				sgtData.weiyi1 = data;
				break;
			case 2:
				sgtData.weiyi2 = data;
				break;
			case 3:
				sgtData.weiyi3 = data;
				break;

			default:
				break;
			}
			
		} else if (var.tpl.getSubType().equals(GTSubType.YC_ZAI_HE)) {
			int idx = var.tpl.getBitOffset();
			switch (idx) {
			case 0:
				sgtData.zaihe0 = data;
				break;
			case 1:
				sgtData.zaihe1 = data;
				break;
			case 2:
				sgtData.zaihe2 = data;
				break;
			case 3:
				sgtData.zaihe3 = data;
				break;

			default:
				break;
			}
			
		}
		
			if (sgtData.isReady()) {//油井示功图数据全部接收完成
				saveSGTData();
				// 保存数据
				sgtData.reset();
			}
		
	}

	/**
	 * 
	 */
	private void saveSGTData() {
		int len0 = sgtData.weiyi0.length;
		int len1 = sgtData.weiyi1.length;
		int len2 = sgtData.weiyi2.length;
		int len3 = sgtData.weiyi3.length;
		
		float[] weiyi = new float[len0 + len1 + len2 + len3];
		float[] zaihe = new float[len0 + len1 + len2 + len3];
		
		for (int i = 0; i < len0; i++) {
			weiyi[i] = sgtData.weiyi0[i];
		}
		for (int i = 0; i < len1; i++) {
			weiyi[i + len0] = sgtData.weiyi1[i];
		}
		for (int i = 0; i < len2; i++) {
			weiyi[i + len0 + len1] = sgtData.weiyi2[i];
		}
		for (int i = 0; i < len3; i++) {
			weiyi[i + len0 + len1 + len2] = sgtData.weiyi3[i];
		}
		
		for (int i = 0; i < len0; i++) {
			zaihe[i] = sgtData.zaihe0[i];
		}
		for (int i = 0; i < len1; i++) {
			zaihe[i + len0] = sgtData.zaihe1[i];
		}
		for (int i = 0; i < len2; i++) {
			zaihe[i + len0 + len1] = sgtData.zaihe2[i];
		}
		for (int i = 0; i < len3; i++) {
			zaihe[i + len0 + len1 + len2] = sgtData.zaihe3[i];
		}
		
		wellService.saveWellSGTData(endCode, zaihe, weiyi, sgtData.chongCheng, sgtData.chongCi);
	}
}
