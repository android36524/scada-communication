package com.ht.scada.communication;

import co.mewf.sqlwriter.Queries;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.common.tag.util.VarSubTypeEnum;
import com.ht.scada.common.tag.util.VarTypeEnum;
import com.ht.scada.communication.dao.ChannelInfoDao;
import com.ht.scada.communication.data.kv.VarGroupData;
import com.ht.scada.communication.entity.ChannelInfo;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.TagVarTpl;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Test {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
        //dbCreateData();
        //historyServiceTest();
        //realTimeServiceTest();

        System.out.println(LocalDate.parse("1922-6-12").getMonthOfYear());
        System.out.println(LocalDate.parse("1922-06-12").getMonthOfYear());

        //DataBaseManager.getInstance().getRealtimeDataService().

//        TagVarTplDao dao = new TagVarTplDaoImpl();
//        List<TagVarTpl> list = dao.getAll ();
//        System.out.println(list.size());
//        for (TagVarTpl tpl :list) {
//            System.out.println(tpl.getTagName());
//            System.out.println(tpl.getTplName());
//            System.out.println(tpl.getVarStorage());
//            System.out.println(tpl.getMaxValue());
//            System.out.println(tpl.getVarGroup());
//            System.out.println(tpl.getVarType());
//        }

        //redisTest();
    }

    private static void realTimeServiceTest() {
        DataBaseManager.getInstance().init();

        Map<String, String> varGroupMap = Maps.newHashMap(ImmutableMap.<String, String>of(VarGroupEnum.DIAN_YC.toString(), "i_a,i_b,i_c,test", VarGroupEnum.DIAN_YM.toString(), "numberText"));
        DataBaseManager.getInstance().getRealtimeDataService().setEndModelGroupVar("codeTest", varGroupMap);

        Map<String, String> varValueMap = Maps.newHashMap(ImmutableMap.<String, String>of("test", "true", "numberText", "123"));
        System.out.println(varValueMap);
        DataBaseManager.getInstance().getRealtimeDataService().updateEndModel("codeTest", varValueMap);
    }

    private static void historyServiceTest() {
        DataBaseManager.getInstance().init();

//        VarGroupData data = new VarGroupData();
//        data.setCode("001");
//        data.setDatetime(new Date());
//        data.setGroup(VarGroupEnum.DIAN_YM);
//        //Map<String, Double> map = new HashMap<>();
//        data.getYmValueMap().put("i_a", 1.0);
//        //data.setYmValueMap(map);
//        DataBaseManager.getInstance().getHistoryDataService().saveVarGroupData(Arrays.asList(data));

        List<VarGroupData> list =  DataBaseManager.getInstance().getHistoryDataService().getVarGroupDataByDatetimeRange("001", VarGroupEnum.DIAN_YC, LocalDate.now().toDate(), LocalDate.now().plusDays(1).toDate());
        System.out.println(list.size());
        for (VarGroupData data : list) {
            System.out.println(LocalDateTime.fromDateFields(data.getDatetime()).toString());
            System.out.println(data.getYcValueMap());
        }
    }

    private static void ttt(int i) {
        i++;
        System.out.println(i);
    }

    private static void dbTest2() {
        ChannelInfoDao channelInfoDao = DataBaseManager.getInstance().getChannelInfoDao();

        List<ChannelInfo> list = DataBaseManager.getInstance().getDbTemplate().find(ChannelInfo.class, "select * from T_Acquisition_Channel where update_time < ?", new Date());
        //List<ChannelInfo> list = channelInfoDao.getAll();
        for (ChannelInfo channelInfo : list) {
            System.out.println(channelInfo.getUpdateTime());
        }

    }

    private static void dbCreateData() {
        // INSERT INTO T_Tag_Cfg_Tpl(tpl_name, var_name, tag_name, var_group, var_type, sub_type, fun_code, data_id, byte_offset, bit_offset, byte_len, data_type, base_value, coef_value, maxValue, minValue, unitValue, triggerName, varStorage) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        String sql = Queries.insert(TagVarTpl.class).sql();
        System.out.println(sql);

        int yxStart = 1;
        int ycStart = 0x4001;
        int jlcStart = 0x5001;
        int zcStart = 0x5101;
        int xbStart = 0x5201;
        int sgtStart = 0x5401;
        int dgtStart = 0x5801;
        int ymStart = 0x6401;

        int dataId = 0;
        // 遥测
        for(VarSubTypeEnum subType : VarSubTypeEnum.values()) {
            VarGroupEnum varGroupEnum = null;
            VarTypeEnum varTypeEnum = null;
            DataType dataType = null;

            int byteLen = 2;
            switch (subType) {
                case YOU_YA:
                case TAO_YA:
                case HUI_YA:
                case JING_KOU_WEN_DU:
                case HUI_GUAN_WEN_DU:
                    varGroupEnum = VarGroupEnum.YOU_JING;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16;
                    dataId = ycStart;
                    ycStart++;
                    break;
                case QI_TING_ZHUANG_TAI:
                    varGroupEnum = VarGroupEnum.YOU_JING;
                    varTypeEnum = VarTypeEnum.YX;
                    dataType = DataType.BOOL;
                    dataId = yxStart;
                    yxStart++;
                    break;
                case CHONG_CHENG:
                case CHONG_CI:
                case SHANG_XING_CHONG_CI:
                case XIA_XING_CHONG_CI:
                case ZUI_DA_ZAI_HE:
                case ZUI_XIAO_ZAI_HE:
                    varGroupEnum = VarGroupEnum.YOU_JING_SGT;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16;
                    dataId = sgtStart;
                    sgtStart++;
                    break;
                case WEI_YI_ARRAY:
                case ZAI_HE_ARRAY:
                    varGroupEnum = VarGroupEnum.YOU_JING_SGT;
                    varTypeEnum = VarTypeEnum.QT;
                    dataType = DataType.INT16_ARRAY;
                    dataId = sgtStart;
                    sgtStart+=200;
                    byteLen=400;
                    break;
                case DIAN_LIU_ARRAY:
                case GONG_LV_ARRAY:
                case GONG_LV_YIN_SHU_ARRAY:
                case DIAN_GONG_TU_ARRAY:
                    varGroupEnum = VarGroupEnum.YOU_JING_DGT;
                    varTypeEnum = VarTypeEnum.QT;
                    dataType = DataType.INT16_ARRAY;
                    dataId = dgtStart;
                    dgtStart+=200;
                    byteLen=400;
                    break;
                case I_A:
                case I_B:
                case I_C:
                case I_3XBPH:
                case U_A:
                case U_B:
                case U_C:
                case U_AB:
                case U_BC:
                case U_CA:
                case GV_YG:
                case GV_WG:
                case GV_SZ:
                case GV_GLYS:
                case GV_ZB:
                case GV_YG_A:
                case GV_YG_B:
                case GV_YG_C:
                case GV_WG_A:
                case GV_WG_B:
                case GV_WG_C:
                case GV_SZ_A:
                case GV_SZ_B:
                case GV_SZ_C:
                case GV_GVYS_A:
                case GV_GVYS_B:
                case GV_GVYS_C:
                    varGroupEnum = VarGroupEnum.DIAN_YC;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16;
                    dataId = ycStart;
                    ycStart++;
                    break;
                case DL_ZX_Z:
                case DL_ZX_J:
                case DL_ZX_F:
                case DL_ZX_P:
                case DL_ZX_G:
                case DL_FX_Z:
                case DL_FX_J:
                case DL_FX_F:
                case DL_FX_P:
                case DL_FX_G:
                    varGroupEnum = VarGroupEnum.DIAN_YM;
                    varTypeEnum = VarTypeEnum.YM;
                    dataType = DataType.INT32;
                    dataId = ymStart;
                    ymStart++;
                    break;
                case XB_IA:
                case XB_IB:
                case XB_IC:
                case XB_UA:
                case XB_UB:
                case XB_UC:
                    varGroupEnum = VarGroupEnum.DIAN_XB;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16;
                    dataId = xbStart;
                    xbStart++;
                    byteLen=62;
                    break;
                case XB_IA_ARRAY:
                case XB_IB_ARRAY:
                case XB_IC_ARRAY:
                case XB_UA_ARRAY:
                case XB_UB_ARRAY:
                case XB_UC_ARRAY:
                    varGroupEnum = VarGroupEnum.DIAN_XB;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16_ARRAY;
                    dataId = xbStart;
                    xbStart+=31;
                    break;
                case JLC_QL_SH:
                case JLC_QL_LJ:
                case JLC_YL_SH:
                case JLC_YL_LJ:
                case JLC_SL_SH:
                case JLC_SL_LJ:
                case JLC_HSL_SH:
                case JLC_WD_SH:
                case JLC_YALI_SH:
                case JLC_MD_SH:
                    varGroupEnum = VarGroupEnum.JI_LIANG;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16;
                    dataId = jlcStart;
                    jlcStart++;
                    break;
                case ZC_ZQLL_SH:
                case ZC_ZQLL_LJ:
                case ZC_ZQYL:
                case ZC_ZQWD:
                case ZC_ZQGD:
                    varGroupEnum = VarGroupEnum.ZHU_CAI;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16;
                    dataId = zcStart;
                    zcStart++;
                    break;
                case RTU_RJ45_STATUS:
                case RTU_COM1_STATUS:
                case RTU_COM2_STATUS:
                case RTU_COM3_STATUS:
                case RTU_COM4_STATUS:
                case RTU_ZIGBEE_STATUS:
                    varGroupEnum = VarGroupEnum.RTU_ZHUANG_TAI;
                    varTypeEnum = VarTypeEnum.YX;
                    dataType = DataType.BOOL;
                    dataId = yxStart;
                    yxStart++;
                    break;
                case CGQ_RTU_STATUS:
                case CGQ_RTU_TIME:
                case CGQ_REMAINED_TIME:
                case CGQ_REMAINED_DIANLIANG:
                    varGroupEnum = VarGroupEnum.SENSOR_RUN;
                    varTypeEnum = VarTypeEnum.YX;
                    dataType = DataType.BOOL;
                    dataId = yxStart;
                    yxStart++;
                    break;
            }
            DataBaseManager.getInstance().getDbTemplate().update(sql, "变量模板1",
                    subType.toString().toLowerCase(), subType.getValue(), varGroupEnum.toString(), varTypeEnum.toString(), subType.toString(),
                    -1, dataId, 0, -1, byteLen, dataType.toString(), 0, 1, null, null, null, null, null);
        }
    }

    private static void dbTest() {
        //String sql = Queries.insert(OffLimitsRecord.class).columns("id", "code", "name", "info", "value", "threshold", "type", "actionTime").sql();
        String sql = Queries.insert(OffLimitsRecord.class).sql();
        System.out.println(sql);
        sql = Queries.update(OffLimitsRecord.class).set("resumeTime").where().eq("id").sql();
        System.out.println(sql);

        sql = Queries.insert(FaultRecord.class).sql();
        System.out.println(sql);
        sql = Queries.update(FaultRecord.class).set("resumeTime").where().eq("id").sql();
        System.out.println(sql);

        Integer[][] params = new Integer[2][2];
        System.out.println(Arrays.toString(params[0]));
        params[0] = new Integer[] {1, 2};
        System.out.println(Arrays.toString(params[0]));


    }

    private static void redisTest() {
        FaultRecord record = new FaultRecord();
        record.setActionTime(new Date());
        record.setCode("编号");
        record.setInfo("故障报警");
        record.setValue(true);
        DataBaseManager.getInstance().getRealtimeDataService().faultOccured(record);
    }

}
