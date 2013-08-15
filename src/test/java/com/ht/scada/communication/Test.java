package com.ht.scada.communication;

import co.mewf.sqlwriter.Queries;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.common.tag.util.VarSubTypeEnum;
import com.ht.scada.common.tag.util.VarTypeEnum;
import com.ht.scada.communication.dao.ChannelInfoDao;
import com.ht.scada.communication.entity.ChannelInfo;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.TagVarTpl;
import com.ht.scada.communication.web.MyGuiceApplicationListener;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.text.DecimalFormat;
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
        String s = LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        System.out.println(s);
        System.out.println(new DecimalFormat("#").format(Float.MAX_VALUE));
        System.out.println(LocalDate.parse("1922-6-12").getMonthOfYear());
        System.out.println(LocalDate.parse("1922-06-12").getMonthOfYear());
        int i = -1;
        System.out.println(0x8FFF0001);
        System.out.println(0x8FFF0000 | 0x0001);
        System.out.println((i & 0xFF));// 255
        System.out.println((byte)(i & 0xFF));// -1
        System.out.println((int)(byte)(i & 0xFF));// -1

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE `T_GroupTest2` (");
        sqlBuilder.append("`id` INT(10) NOT NULL, \n");
        sqlBuilder.append("`ia` FLOAT NULL,\n");
        sqlBuilder.append("`ib` FLOAT NULL,\n");
        sqlBuilder.append("`ic` FLOAT NULL,\n");
        sqlBuilder.append("`ua` FLOAT NULL,\n");
        sqlBuilder.append("`ub` FLOAT NULL,\n");
        sqlBuilder.append("`yx` TINYINT NULL,\n");
        sqlBuilder.append("`code` VARCHAR(50) NULL,\n");
        sqlBuilder.append("`datetime` DATETIME NULL,\n");
        sqlBuilder.append("PRIMARY KEY (`id`)\n");
        sqlBuilder.append(")");
        DataBaseManager.getInstance().init();
        //System.out.println(sqlBuilder.toString());
        //DataBaseManager.getInstance().getDbTemplate().update(sqlBuilder.toString());
        List<Map<String, Object>> dataList = DataBaseManager.getInstance().getDbTemplate().find("select * from t_dian_yc");

        for (Map<String, Object> map : dataList) {
            System.out.println(map);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                System.out.println(entry.getKey());
                if (entry.getValue() != null)
                System.out.println(entry.getValue().getClass());
            }
            System.out.println();
        }

/*        DataBaseManager.getInstance().init();
        List<VarGroupData> list =  DataBaseManager.getInstance().getHistoryDataService()
                .getVarGroupData("y93p5", VarGroupEnum.DIAN_YM,
                        LocalDateTime.now().minusHours(1).toDate(), LocalDateTime.now().toDate(), 0, 10);
        System.out.println(list.size());
        for (VarGroupData varGroupData : list) {
            System.out.println(varGroupData.getYmValueMap().size());
        }*/

        //Config.INSTANCE.init("src/main/webapp/WEB-INF/config.properties");
/*
        DataBaseManager.getInstance().init();

        VarGroupDataDao varGroupDataDao = DataBaseManager.getInstance().getVarGroupDataDao();
        VarGroupData varGroupData = new VarGroupData();
        varGroupData.setGroup(VarGroupEnum.DIAN_YC);
        varGroupData.setDatetime(new Date());
        varGroupData.setCode("code_001");
        varGroupData.getYcValueMap().put("i_a", 100F);
        varGroupData.getYcValueMap().put("i_b", 101F);
        varGroupData.getYxValueMap().put("yx_1", true);
        //varGroupData.getArrayValueMap().put("array_1", new float[]{0.022F,0.000F,0.000F,0.006F,0.035F,0.077F,0.131F,0.208F,0.292F,0.372F,0.468F,0.551F,0.638F,0.731F,0.830F,0.920F,1.004F,1.093F,1.170F,1.263F,1.356F,1.449F,1.536F,1.632F,1.712F,1.802F,1.895F,1.994F,2.087F,2.177F,2.260F,2.350F,2.440F,2.533F,2.632F,2.712F,2.793F,2.879F,2.975F,3.072F,3.164F,3.254F,3.344F,3.434F,3.520F,3.613F,3.710F,3.796F,3.879F,3.972F,4.065F,4.158F,4.251F,4.341F,4.428F,4.521F,4.604F,4.700F,4.796F,4.893F,4.986F,5.075F,5.152F,5.245F,5.338F,5.438F,5.531F,5.611F,5.704F,5.790F,5.883F,5.970F,6.072F,6.162F,6.249F,6.332F,6.428F,6.518F,6.621F,6.710F,6.791F,6.884F,6.977F,7.066F,7.159F,7.252F,7.339F,7.425F,7.515F,7.615F,7.711F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.753F,7.692F,7.615F,7.531F,7.451F,7.365F,7.284F,7.191F,7.102F,7.025F,6.941F,6.861F,6.768F,6.685F,6.592F,6.509F,6.435F,6.355F,6.271F,6.178F,6.085F,5.992F,5.922F,5.842F,5.755F,5.662F,5.579F,5.486F,5.406F,5.325F,5.245F,5.159F,5.063F,4.976F,4.886F,4.803F,4.719F,4.643F,4.550F,4.453F,4.367F,4.280F,4.210F,4.123F,4.033F,3.947F,3.863F,3.770F,3.690F,3.610F,3.520F,3.431F,3.334F,3.248F,3.158F,3.081F,3.001F,2.918F,2.825F,2.735F,2.652F,2.568F,2.491F,2.401F,2.305F,2.212F,2.122F,2.042F,1.962F,1.872F,1.783F,1.693F,1.606F,1.520F,1.440F,1.356F,1.270F,1.183F,1.100F,1.010F,0.923F,0.843F});
        varGroupDataDao.insert(varGroupData);

        long l = varGroupDataDao.getCount("code_001", VarGroupEnum.DIAN_YC, LocalDate.now().toDate(), LocalDate.now().plusDays(1).toDate());
        System.out.println(l);
        varGroupDataDao.findByCodeAndDatetime("code_001", VarGroupEnum.DIAN_YC, LocalDate.now().toDate(), LocalDate.now().plusDays(1).toDate(), 0, 10);
*/

//        Config.INSTANCE.init("src/main/webapp/WEB-INF/config.properties");
//        DataBaseManager.getInstance().init();
//        List<VarGroupInfo> list = DataBaseManager.getInstance().getVarGroupInfoDao().getAll();
//        System.out.println(list);
//        for (VarGroupInfo varGroupInfo : list) {
//            System.out.println(varGroupInfo.getName());
//            System.out.println(varGroupInfo.getIntvl());
//        }

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

    }

    private static void ttt(int i) {
        i++;
        System.out.println(i);
    }

    private static void dbTest2() {
        ChannelInfoDao channelInfoDao = MyGuiceApplicationListener.injector.getInstance(ChannelInfoDao.class);

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
        int dianYcStart = 0x401E;
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
                    dataId = dianYcStart;
                    dianYcStart++;
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
                    byteLen=62;
                    break;
                case JLC_QL_SH:
                case JLC_YL_SH:
                case JLC_SL_SH:
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
                case JLC_QL_LJ:
                case JLC_YL_LJ:
                case JLC_SL_LJ:
                    varGroupEnum = VarGroupEnum.JI_LIANG;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT32;
                    dataId = jlcStart;
                    jlcStart += 2;
                    byteLen = 4;
                    break;
                case ZC_ZQLL_LJ:
                    varGroupEnum = VarGroupEnum.ZHU_CAI;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT32;
                    dataId = zcStart;
                    zcStart+=2;
                    byteLen = 4;
                    break;
                case ZC_ZQLL_SH:
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
                    varGroupEnum = VarGroupEnum.SENSOR_RUN;
                    varTypeEnum = VarTypeEnum.YX;
                    dataType = DataType.BOOL;
                    dataId = yxStart;
                    yxStart++;
                    break;
                case CGQ_RTU_TIME:
                case CGQ_REMAINED_TIME:
                case CGQ_REMAINED_DIANLIANG:
                    varGroupEnum = VarGroupEnum.SENSOR_RUN;
                    varTypeEnum = VarTypeEnum.YC;
                    dataType = DataType.INT16;
                    dataId = ycStart;
                    ycStart++;
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
