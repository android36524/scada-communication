/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package com.ht.scada.communication.kv;

import com.ht.scada.data.kv.FaultRecord;
import com.ht.scada.data.kv.OffLimitsRecord;
import com.ht.scada.data.kv.YXData;
import oracle.kv.Key;
import org.joda.time.LocalDateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * KV数据库Key定义
 * @author 薄成文
 *
 */
public class KeyDefinition {
	public static final String DB_NAME = "db";
	
	/* 实时数据类型  */
	public static final String REAL_BOOL = "real_bool";
	public static final String REAL_NUM = "real_num";
	
	/* 记录类型  */
	public static final String VAR_GROUP_RECORD = "VAR_GROUP";
	public static final String YX_RECORD = "YX";
	public static final String OFF_LIMITS_RECORD = "OFF_LIMITS";
	public static final String FAULT_RECORD = "FAULT";

    /**
     * 创建遥信数据Key,格式为
     * /YX/code/name/-/datetime
     * @param data
     * @return
     */
    public static Key makeYXDataKey(YXData data) {
    	return createKey(YX_RECORD, data.getCode(), data.getName(), data.getDatetime());
    }
    
    /**
     * 创建遥测越限记录Key,格式为
     * /OFF_LIMITS/code/name/-/datetime
     * @param data
     * @return
     */
    public static Key makeOffLimitsRecordKey(OffLimitsRecord data) {
    	return createKey(OFF_LIMITS_RECORD, data.getCode(), data.getName(), data.getActionTime());
    }
    
    /**
     * 创建故障记录Key,格式为
     * /FAULT/code/name/-/datetime
     * @param data
     * @return
     */
    public static Key makeFaultRecordKey(FaultRecord data) {
    	return createKey(FAULT_RECORD, data.getCode(), data.getName(), data.getActionTime());
    }
    
    public static Key makeVarGroupKey(String code, String varGroup, Date datetime) {
        final String timestamp = LocalDateTime.fromDateFields(datetime).toString();
    	return Key.createKey(Arrays.asList(DB_NAME, VAR_GROUP_RECORD, code), Arrays.asList(varGroup, timestamp));
    }
    
    private static Key createKey(String recordType, String code, String name, Date datetime) {
        final String timestamp = LocalDateTime.fromDateFields(datetime).toString();
    	return Key.createKey(Arrays.asList(DB_NAME, recordType, code, name), timestamp);
    }
    
    public static Key getKey(String recordType, String code) {
    	return Key.createKey(Arrays.asList(DB_NAME, recordType, code));
    }
    
    public static Key getKey(String recordType, String code, String name) {
    	return Key.createKey(Arrays.asList(DB_NAME, recordType, code, name));
    }
    
    public static Key getKey(String recordType) {
    	return Key.createKey(Arrays.asList(DB_NAME, recordType));
    }
    
    public static Key getVarGroupKey(String code, String varGroup) {
    	return Key.createKey(Arrays.asList(DB_NAME, VAR_GROUP_RECORD, code), Arrays.asList(varGroup));
    }
    
    //private static VarGroup
    
    /**
     * 解析遥信数据Key
     * @param key
     * @param data
     */
    private static YXData parseYXDataKey(Key key, YXData data) {
        final List<String> majorPath = key.getMajorPath();

        if (!YX_RECORD.equals(majorPath.get(1))) {
            throw new IllegalArgumentException("Not a yx kv: " + key);
        }

        data.setCode(majorPath.get(2));
        data.setName(majorPath.get(3));
        
        final List<String> minorPath = key.getMinorPath();
        data.setDatetime(LocalDateTime.parse(minorPath.get(0)).toDate());
        return data;
    }
    
    /**
     * 解析遥测越限记录Key
     * @param key
     * @param data
     */
    private static OffLimitsRecord parseOffLimitsRecordKey(Key key, OffLimitsRecord data) {
        final List<String> majorPath = key.getMajorPath();

        if (!OFF_LIMITS_RECORD.equals(majorPath.get(1))) {
            throw new IllegalArgumentException("Not a off limits record: " + key);
        }

        data.setCode(majorPath.get(2));
        data.setName(majorPath.get(3));
        
        final List<String> minorPath = key.getMinorPath();
        data.setActionTime(LocalDateTime.parse(minorPath.get(0)).toDate());
        return data;
    }
    
    /**
     * 解析故障记录Key
     * @param key
     * @param data
     */
    private static FaultRecord parseFaultRecordKey(Key key, FaultRecord data) {
        final List<String> majorPath = key.getMajorPath();

        if (!FAULT_RECORD.equals(majorPath.get(1))) {
            throw new IllegalArgumentException("Not a fault record: " + key);
        }

        data.setCode(majorPath.get(2));
        data.setName(majorPath.get(3));
        
        final List<String> minorPath = key.getMinorPath();
        data.setActionTime(LocalDateTime.parse(minorPath.get(0)).toDate());
        
        return data;
    }
    
    /**
     * 解析故障记录Key
     * @param key
     * @param data
     */
    public static <T> T parseKey(Key key, T data) {
        
        if (data instanceof YXData) {
        	parseYXDataKey(key, (YXData) data);
		} else if (data instanceof FaultRecord) {
        	parseFaultRecordKey(key, (FaultRecord) data);
		} else if (data instanceof OffLimitsRecord) {
        	parseOffLimitsRecordKey(key, (OffLimitsRecord) data);
		}
		return data;
    }



    /**
     * Translates the given Key/Value to its corresponding Java object.  This
     * is useful when an arbitrary user Key/Value pair is obtained, for example
     * by iterating over all Key/Value pairs in the store or all Key/Value
     * pairs for a particular user.
     */
/*    static Object deserializeAny(Bindings bindings, Key key, Value value) {

        final List<String> majorPath = key.getMajorPath();
        final List<String> minorPath = key.getMinorPath();
        final String objectType = majorPath.get(0);

        if (!USER_OBJECT_TYPE.equals(objectType)) {
            throw new IllegalArgumentException("Unknown object type: " + key);
        }

        final String email = majorPath.get(1);
        final String propertyName =
            (minorPath.size() > 0) ? minorPath.get(0) : null;

        if (INFO_PROPERTY_NAME.equals(propertyName) &&
            minorPath.size() == 1) {
            final UserInfo userInfo = new UserInfo(email);
            userInfo.setStoreValue(bindings, value);
            return userInfo;
        }

        if (IMAGE_PROPERTY_NAME.equals(propertyName) &&
            minorPath.size() == 1) {
            final UserImage userImage = new UserImage(email);
            userImage.setStoreValue(bindings, value);
            return userImage;
        }

        if (LOGIN_PROPERTY_NAME.equals(propertyName)) {
            if (minorPath.size() == 1) {
                final LoginSummary loginSummary = new LoginSummary(email);
                loginSummary.setStoreValue(bindings, value);
                return loginSummary;
            }
            if (minorPath.size() == 2) {
                final String timestamp = minorPath.get(1);
                final long loginMs = parseTimestamp(timestamp);
                final LoginSession loginSession =
                    new LoginSession(email, loginMs);
                loginSession.setStoreValue(bindings, value);
                return loginSession;
            }
        }

        throw new IllegalArgumentException("Unknown key property: " + key);
    }*/
}
