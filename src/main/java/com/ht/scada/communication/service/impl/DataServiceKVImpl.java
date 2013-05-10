package com.ht.scada.communication.service.impl;

import com.ht.scada.communication.kv.KeyDefinition;
import com.ht.scada.communication.kv.RunOperation;
import com.ht.scada.communication.kv.ValueDefinition;
import com.ht.scada.communication.kv.WriteOperations;
import com.ht.scada.communication.service.DataService;
import com.ht.scada.data.kv.*;
import oracle.kv.*;
import org.joda.time.LocalDateTime;

import java.util.*;

public class DataServiceKVImpl implements DataService {
	
    private KVStore store;
    private WriteOperations writeOps;
    
    public void init() {
        String storeName = "kvstore";
        String hostName = "192.168.1.81";
        String hostPort = "5000";
        
        final KVStoreConfig config = new KVStoreConfig(storeName, hostName + ":" + hostPort);
        config.setRequestLimit(RequestLimitConfig.getDefault());
        store = KVStoreFactory.getStore(config);
        writeOps = new WriteOperations(store, config);
        
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
        
        final List<Operation> ops = new ArrayList<Operation>(list.size());
        for (IKVRecord data : list) {
            ops.add(factory.createPut(data.makeKey(), data.makeValue()));
        }

        new RunOperation() {
            @Override
			public void doOperation() {
                try {
                    writeOps.execute(ops);
                } catch (OperationExecutionException e) {
                    /* One of the insertions failed unexpectedly. */
                    throw new IllegalStateException
                        ("Unexpected failure during initial load", e);
                }
            }
        }.run();
    }

	@Override
	public void saveYXData(List<YXData> list) {
		saveData(list);
	}

	@Override
	public void saveOffLimitsRecord(List<OffLimitsRecord> list) {
		saveData(list);
	}

	@Override
	public void saveFaultRecord(List<FaultRecord> list) {
		saveData(list);
	}

    @Override
    public void saveVarGroupData(List<VarGroupData> list) {
        saveData(list);
    }

	@Override
	public List<YXData> getYXRecordByDatetimeRange(String code, Date start,
			Date end) {
		final List<YXData> list = new ArrayList<>();
		
		final String startTimestamp = LocalDateTime.fromDateFields(start).toString();
		final String endTimestamp = LocalDateTime.fromDateFields(end).toString();
		
		KeyRange keyRange = new KeyRange(startTimestamp /*start*/, true /*startInclusive*/,
                endTimestamp /*end*/, false /*endInclusive*/);
		
		// TODO: 变量名已经转移到minorKey中，查询方式需要修改
		Iterator<Key> iter = store.storeKeysIterator(Direction.UNORDERED, 0 /*batchSize*/, 
				KeyDefinition.getKey(KeyDefinition.YX_RECORD, code),
	            null /*subRange*/, null /*depth*/);
		while(iter.hasNext()) {
			Key parentKey = iter.next();
			final Map<Key, ValueVersion> results = store.multiGet(parentKey, keyRange, null);
			for (Map.Entry<Key, ValueVersion> entry : results.entrySet()) {
				YXData record = KeyDefinition.parseKey(entry.getKey(), new YXData());
				ValueDefinition.parseValue(entry.getValue().getValue(), record);
				list.add(record);
			}
			
		}
		
		return list;
	}
	
	
	@Override
	public List<FaultRecord> getFaultRecordByDatetimeRange(String code, Date start, Date end) {
		
		final List<FaultRecord> list = new ArrayList<>();
		
		final String startTimestamp = LocalDateTime.fromDateFields(start).toString();
		final String endTimestamp = LocalDateTime.fromDateFields(end).toString();
		
		KeyRange keyRange = new KeyRange(startTimestamp /*start*/, true /*startInclusive*/,
                endTimestamp /*end*/, false /*endInclusive*/);
		
		Iterator<Key> iter = store.storeKeysIterator(Direction.UNORDERED, 0 /*batchSize*/, 
				KeyDefinition.getKey(KeyDefinition.FAULT_RECORD, code),
	            null /*subRange*/, null /*depth*/);
		while(iter.hasNext()) {
			Key parentKey = iter.next();
			final Map<Key, ValueVersion> results = store.multiGet(parentKey, keyRange, null);
			for (Map.Entry<Key, ValueVersion> entry : results.entrySet()) {
				FaultRecord record = KeyDefinition.parseKey(entry.getKey(), new FaultRecord());
				ValueDefinition.parseValue(entry.getValue().getValue(), record);
				list.add(record);
			}
			
		}
		
		return list;
	}
	
	@Override
	public List<OffLimitsRecord> getOffLimitsRecordByDatetimeRange(String code,
			Date start, Date end) {
		final List<OffLimitsRecord> list = new ArrayList<>();
		
		final String startTimestamp = LocalDateTime.fromDateFields(start).toString();
		final String endTimestamp = LocalDateTime.fromDateFields(end).toString();
		
		KeyRange keyRange = new KeyRange(startTimestamp /*start*/, true /*startInclusive*/,
                endTimestamp /*end*/, false /*endInclusive*/);
		
		Iterator<Key> iter = store.storeKeysIterator(Direction.UNORDERED, 0 /*batchSize*/, 
				KeyDefinition.getKey(KeyDefinition.OFF_LIMITS_RECORD, code),
	            null /*subRange*/, null /*depth*/);
		while(iter.hasNext()) {
			Key parentKey = iter.next();
			final Map<Key, ValueVersion> results = store.multiGet(parentKey, keyRange, null);
			for (Map.Entry<Key, ValueVersion> entry : results.entrySet()) {
				OffLimitsRecord record = KeyDefinition.parseKey(entry.getKey(), new OffLimitsRecord());
				ValueDefinition.parseValue(entry.getValue().getValue(), record);
				list.add(record);
			}
			
		}
		
		return list;
	}
	
	
}
