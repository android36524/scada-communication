package com.ht.scada.communication.service.impl;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.dao.FaultRecordDao;
import com.ht.scada.communication.dao.OffLimitsRecordDao;
import com.ht.scada.communication.dao.YxRecordDao;
import com.ht.scada.communication.data.kv.IKVRecord;
import com.ht.scada.communication.data.kv.VarGroupData;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.YxRecord;
import com.ht.scada.communication.service.HistoryDataService;
import com.ht.scada.communication.util.kv.KeyDefinition;
import com.ht.scada.communication.util.kv.RunOperation;
import com.ht.scada.communication.util.kv.WriteOperations;
import oracle.kv.*;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HistoryDataServiceImpl implements HistoryDataService {
    private static final Logger log = LoggerFactory.getLogger(HistoryDataServiceImpl.class);
	
    private KVStore store;
    private WriteOperations writeOps;
    private OffLimitsRecordDao offLimitsRecordDao;
    private FaultRecordDao faultRecordDao;
    private YxRecordDao yxRecordDao;

    public HistoryDataServiceImpl(KVStore store, long requestTimeout) {

        this.store = store;
        writeOps = new WriteOperations(store, requestTimeout);

        offLimitsRecordDao = DataBaseManager.getInstance().getOffLimitsRecordDao();
        faultRecordDao = DataBaseManager.getInstance().getFaultRecordDao();
        yxRecordDao = DataBaseManager.getInstance().getYxRecordDao();
    }
    
	public void destroy() {
        store.close();
	}
    
    public KVStore getStore() {
		return store;
	}
    
    private void saveData(List<? extends IKVRecord> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		
        final OperationFactory factory = store.getOperationFactory();
        
        final List<Operation> ops = new ArrayList<>(list.size());
        for (IKVRecord data : list) {
            try {
                ops.add(factory.createPut(data.makeKey(), data.makeValue()));
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        new RunOperation() {
            @Override
			public void doOperation() {
                try {
                    writeOps.execute(ops);
                } catch (OperationExecutionException e) {
                    e.printStackTrace();
                    /* One of the insertions failed unexpectedly. */
                    throw new IllegalStateException
                        ("Unexpected failure during initial load", e);
                }
            }
        }.run();
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
        log.info("保存变量分组数据");
        saveData(list);
    }

    @Override
    public List<VarGroupData> getVarGroupDataByDatetimeRange(String code, VarGroupEnum varGroup, Date start, Date end) {
        List<VarGroupData> list = new ArrayList<>();

        final String startTimestamp = LocalDateTime.fromDateFields(start).toString();
        final String endTimestamp = LocalDateTime.fromDateFields(end).toString();

        KeyRange keyRange = new KeyRange(startTimestamp /*start*/, true /*startInclusive*/,
                endTimestamp /*end*/, false /*endInclusive*/);

        final Map<Key, ValueVersion> results = store.multiGet(KeyDefinition.getVarGroupKey(code, varGroup.toString()), keyRange, Depth.CHILDREN_ONLY);
        for (Map.Entry<Key, ValueVersion> entry : results.entrySet()) {
            VarGroupData data = new VarGroupData();
            data.parseKey(entry.getKey());
            data.parseValue(entry.getValue().getValue());
            list.add(data);
        }
        return list;
    }

    @Override
    public long getVarGroupDataCount(String code, VarGroupEnum varGroup, Date start, Date end) {
        String startTimestamp = LocalDateTime.fromDateFields(start).toString();
        String endTimestamp = LocalDateTime.fromDateFields(end).toString();

        KeyRange keyRange = new KeyRange(startTimestamp /*start*/, true /*startInclusive*/,
                endTimestamp /*end*/, false /*endInclusive*/);

        Key parentKey = KeyDefinition.getVarGroupKey(code, varGroup.toString());
        Iterator<Key> keyIterator = store.multiGetKeysIterator(Direction.FORWARD, 0, parentKey, keyRange, Depth.CHILDREN_ONLY);
        long count = 0;
        while (keyIterator.hasNext()) {
            keyIterator.next();
            count++;
        }
        return count;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<VarGroupData> getVarGroupData(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit) {
        List<VarGroupData> list = new ArrayList<>();

        String startTimestamp = LocalDateTime.fromDateFields(start).toString();
        String endTimestamp = LocalDateTime.fromDateFields(end).toString();

        KeyRange keyRange = new KeyRange(startTimestamp /*start*/, true /*startInclusive*/,
                endTimestamp /*end*/, false /*endInclusive*/);

        Key parentKey = KeyDefinition.getVarGroupKey(code, varGroup.toString());

        Iterator<KeyValueVersion> keyValueVersionIterator = store.multiGetIterator(Direction.FORWARD, 0, parentKey, keyRange, Depth.CHILDREN_ONLY);
        int i = 0;
        while (keyValueVersionIterator.hasNext()) {
            KeyValueVersion keyValueVersion = keyValueVersionIterator.next();
            if (i >= skip) {
                VarGroupData data = new VarGroupData();
                data.parseKey(keyValueVersion.getKey());
                data.parseValue(keyValueVersion.getValue());
                list.add(data);
                if (list.size() == limit) {
                    break;
                }
            }
            i++;
        }
        return list;
    }

    @Override
    public VarGroupData getVarGroupData(String code, VarGroupEnum varGroup, Date start) {

        String startTimestamp = LocalDateTime.fromDateFields(start).toString();

        KeyRange keyRange = new KeyRange(startTimestamp /*start*/, true /*startInclusive*/,
                null /*end*/, false /*endInclusive*/);

        Key parentKey = KeyDefinition.getVarGroupKey(code, varGroup.toString());
        Iterator<KeyValueVersion> keyValueVersionIterator = store.multiGetIterator(Direction.FORWARD, 1, KeyDefinition.getVarGroupKey(code, varGroup.toString()), keyRange, Depth.CHILDREN_ONLY);

        VarGroupData data = new VarGroupData();
        while (keyValueVersionIterator.hasNext()) {
            KeyValueVersion keyValueVersion = keyValueVersionIterator.next();
            data.parseKey(keyValueVersion.getKey());
            data.parseValue(keyValueVersion.getValue());
            break;
        }
        return data;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
