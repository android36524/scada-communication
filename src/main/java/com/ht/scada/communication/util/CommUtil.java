package com.ht.scada.communication.util;

import com.ht.scada.common.tag.util.VarGroupEnum;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-8 下午3:16
 * To change this template use File | Settings | File Templates.
 */
public class CommUtil {

    /**
     * 同不的变量组在给定时间点上的唯一标识
     * @param varGroup 变量分组
     * @param cal 时间点
     * @param interval 存储间隔
     * @return
     */
    public static String createRTUHisDataKey(VarGroupEnum varGroup, Calendar cal, int interval) {
        int minute = cal.get(Calendar.MINUTE);
        minute = minute / interval * interval;
        cal.set(Calendar.MINUTE, minute);

        StringBuilder sb = new StringBuilder();
        sb.append(varGroup.toString());
        sb.append("-").append(cal.get(Calendar.YEAR));
        sb.append("-").append(cal.get(Calendar.MONTH));
        sb.append("-").append(cal.get(Calendar.DATE));
        sb.append("-").append(cal.get(Calendar.HOUR_OF_DAY));
        sb.append("-").append(minute);
        return sb.toString();
    }
}
