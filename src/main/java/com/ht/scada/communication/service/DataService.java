package com.ht.scada.communication.service;

import com.ht.scada.data.data.FaultRecord;
import com.ht.scada.data.data.OffLimitsRecord;
import com.ht.scada.data.data.VarGroupData;
import com.ht.scada.data.data.YXData;

import java.util.Date;
import java.util.List;

public interface DataService {
	
	public void saveYXData(List<YXData> list);
	public void saveOffLimitsRecord(List<OffLimitsRecord> list);
	public void saveFaultRecord(List<FaultRecord> list);
	
	public void saveVarGroupData(List<VarGroupData> list);
	
	public List<FaultRecord> getFaultRecordByDatetimeRange(String code, Date start, Date end);
	public List<OffLimitsRecord> getOffLimitsRecordByDatetimeRange(String code, Date start, Date end);
	public List<YXData> getYXRecordByDatetimeRange(String code, Date start, Date end);
}
