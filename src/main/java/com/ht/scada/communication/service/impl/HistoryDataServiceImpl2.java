package com.ht.scada.communication.service.impl;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.dao.FaultRecordDao;
import com.ht.scada.communication.dao.OffLimitsRecordDao;
import com.ht.scada.communication.dao.VarGroupDataDao;
import com.ht.scada.communication.dao.YxRecordDao;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.VarGroupData;
import com.ht.scada.communication.entity.YxRecord;
import com.ht.scada.communication.service.HistoryDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * 历史数据服务MySQL实现
 */
public class HistoryDataServiceImpl2 implements HistoryDataService {
    private static final Logger log = LoggerFactory.getLogger(HistoryDataServiceImpl2.class);
    private static final String VAR_GROUP_TABLE_PREFIX = "T_Group_";

    private OffLimitsRecordDao offLimitsRecordDao;
    private FaultRecordDao faultRecordDao;
    private YxRecordDao yxRecordDao;
    private VarGroupDataDao varGroupDataDao;

    public HistoryDataServiceImpl2() {
        offLimitsRecordDao = DataBaseManager.getInstance().getOffLimitsRecordDao();
        faultRecordDao = DataBaseManager.getInstance().getFaultRecordDao();
        yxRecordDao = DataBaseManager.getInstance().getYxRecordDao();
        varGroupDataDao = DataBaseManager.getInstance().getVarGroupDataDao();
    }
    
	public void destroy() {
	}
    
	@Override
	public void saveYXData(List<YxRecord> list) {
        yxRecordDao.insertAll(list);
	}

	@Override
	public void saveOffLimitsRecord(List<OffLimitsRecord> list) {
        offLimitsRecordDao.insertOrUpdateAll(list);
    }

	@Override
	public void saveFaultRecord(List<FaultRecord> list) {
        faultRecordDao.insertOrUpdateAll(list);
    }

    @Override
    public void saveVarGroupData(List<VarGroupData> list) {
        log.debug("保存变量分组数据");
        for (VarGroupData varGroupData : list) {
            varGroupDataDao.insert(varGroupData);
        }
    }

    @Override
    public long getVarGroupDataCount(String code, VarGroupEnum varGroup, Date start, Date end) {
        return varGroupDataDao.getCount(code, varGroup, start, end);
    }

    @Override
    public List<VarGroupData> getVarGroupData(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit) {
        return varGroupDataDao.getVarGroupData(code, varGroup, start, end, skip, limit);
    }

}
