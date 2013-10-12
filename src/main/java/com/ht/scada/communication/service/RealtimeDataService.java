package com.ht.scada.communication.service;

import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.YxRecord;

import java.util.Map;

/**
 * 实时数据库服务类<br>
 *     key 的格式为[endTagCode]/[varGroup]/[varName] <br/>
 * 存储双浮点型的数据时采用Double.toString(v)的方式<br/>
 * 存储单浮点型的数据时采用Float.toString(v)的方式<br/>
 * 存储浮点型数组的数据时采用","连接成字符串的方式<br/>
 * 存储布尔型的数据时采用Boolean.toString(b)的方式<br/>
 * @author 薄成文
 *
 */
public interface RealtimeDataService {

    void putValus(Map<String, String> kvMap);
    void putValue(String k, String v);

    /**
     * 将末端分组变量写入实时数据库(只包括遥信、遥测、遥控变量,不包括遥测数据)
     * @param code
     * @param groupVarMap
     */
    void setEndModelGroupVar(String code, Map<String, String> groupVarMap);

    /**
     * 批量更新监控对象实时数据(遥测、遥信、遥脉)
     * @param code 监控对象编号
     * @param kvMap
     */
    void updateEndModel(String code, Map<String, String> kvMap);

    /**
     *批量更新监控对象遥测数组实时数据
     * @param code 监控对象编号
     * @param kvMap
     */
    void updateEndModelYcArray(String code, Map<String, String> kvMap);

    void faultOccured(FaultRecord record);
    void faultResumed(FaultRecord record);

    void offLimitsOccured(OffLimitsRecord record);
    void offLimitsResumed(OffLimitsRecord record);

    void yxChanged(YxRecord record);
}
