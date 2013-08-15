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

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 历史数据服务MySQL实现
 */
public class HistoryDataServiceImpl2 implements HistoryDataService {
    private static final Logger log = LoggerFactory.getLogger(HistoryDataServiceImpl2.class);
    private static final String VAR_GROUP_TABLE_PREFIX = "T_Group_";

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Map<VarGroupEnum, List<VarGroupData>> varGroupListMap = new HashMap<>();

    private final List<YxRecord> yxRecordList = new ArrayList<>();
    private final List<FaultRecord> faultInsertList = new ArrayList<>();
    private final List<FaultRecord> faultUpdateList = new ArrayList<>();
    private final List<OffLimitsRecord> offLimitsInsertList = new ArrayList<>();
    private final List<OffLimitsRecord> offLimitsUpdateList = new ArrayList<>();

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
    public void saveYXData(final YxRecord record) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                yxRecordList.add(record);
                if (yxRecordList.size() >= 20) {
                    yxRecordDao.insertAll(yxRecordList);
                    yxRecordList.clear();
                }
            }
        });
    }

    @Override
    public void saveOrUpdateOffLimitsRecord(final OffLimitsRecord record) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (record.isPersisted()) {
                    offLimitsUpdateList.add(record);
                    if (offLimitsUpdateList.size() >= 20) {
                        offLimitsRecordDao.updateAll(offLimitsUpdateList);
                        offLimitsUpdateList.clear();
                    }
                } else {
                    offLimitsInsertList.add(record);
                    if (offLimitsInsertList.size() >= 20) {
                        offLimitsRecordDao.insertAll(offLimitsInsertList);
                        offLimitsInsertList.clear();
                    }
                }
            }
        });
    }

    @Override
    public void saveOrUpdateFaultRecord(final FaultRecord record) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (record.isPersisted()) {
                    faultUpdateList.add(record);
                    if (faultUpdateList.size() >= 20) {
                        faultRecordDao.updateAll(faultUpdateList);
                        faultUpdateList.clear();
                    }
                } else {
                    record.setPersisted(true);
                    faultInsertList.add(record);
                    if (faultInsertList.size() >= 20) {
                        faultRecordDao.insertAll(faultInsertList);
                        faultInsertList.clear();
                    }
                }
            }
        });
    }

    @Override
    public void saveVarGroupData(final Collection<VarGroupData> collection) {
        log.debug("保存分组数据");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (VarGroupData data : collection) {
                    List<VarGroupData > list = varGroupListMap.get(data.getGroup());
                    if (list == null) {
                        list = new ArrayList<>();
                        varGroupListMap.put(data.getGroup(), list);
                    }
                    list.add(data);
                    if(list.size() > 10) {
                        varGroupDataDao.insertAll(list);
                        list.clear();
                    }
                }
            }
        });
    }

    @Override
    public void saveVarGroupData(final VarGroupData data) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                List<VarGroupData > list = varGroupListMap.get(data.getGroup());
                if (list == null) {
                    list = new ArrayList<>();
                    varGroupListMap.put(data.getGroup(), list);
                }
                list.add(data);
                if(list.size() > 10) {
                    varGroupDataDao.insertAll(list);
                    list.clear();
                }
            }
        });
        //varGroupDataDao.insert(data);
    }

    @Override
    public long getVarGroupDataCount(String code, VarGroupEnum varGroup, Date start, Date end) {
        return varGroupDataDao.getCount(code, varGroup, start, end);
    }

    @Override
    public long getYxRecordCount(String code, Date start, Date end) {
        return yxRecordDao.getCount(code, start, end);
    }

    @Override
    public long getFaultRecordCount(String code, Date start, Date end) {
        return faultRecordDao.getCount(code, start, end);
    }

    @Override
    public long getOfflimitsRecordCount(String code, Date start, Date end) {
        return offLimitsRecordDao.getCount(code, start, end);
    }

    @Override
    public List<FaultRecord> getFaultRecordByActionTime(String code, Date start, Date end, int skip, int limit) {
        return faultRecordDao.findByActionTime(code, start, end, skip, limit);
    }

    @Override
    public List<YxRecord> getYxRecordByDatetime(String code, Date start, Date end, int skip, int limit) {
        return yxRecordDao.findByDateTime(code, start, end, skip, limit);
    }

    @Override
    public List<OffLimitsRecord> getOffLimitsRecordByActionTime(String code, Date start, Date end, int skip, int limit) {
        return offLimitsRecordDao.findByActionTime(code, start, end, skip, limit);
    }

    @Override
    public List<VarGroupData> getVarGroupData(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit) {
        return varGroupDataDao.getVarGroupData(code, varGroup, start, end, skip, limit);
    }

}
