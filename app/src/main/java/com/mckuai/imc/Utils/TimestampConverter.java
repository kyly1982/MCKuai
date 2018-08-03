package com.mckuai.imc.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kyly on 2016/1/22.
 */
public class TimestampConverter {
    public static String toString(long timestamp) {
        int time = (int) (System.currentTimeMillis() - timestamp) / 60000;
        return getTime(time);
    }

    public static String getTime(Long time) {
        Date date = new Date(time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return timeFormat.format(date);
    }

    public static String toString(String timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date insertTime = null;
        try {
            insertTime = timeFormat.parse(timestamp);
        } catch (Exception e) {
            return "未知";
        }

        int time = (int) ((System.currentTimeMillis() - insertTime.getTime()) / 60000);//分钟
        return getTime(time);
    }

    private static String getTime(int time) {
        if (time < 5) {
            return "刚刚";
        } else if (time < 60) {
            return time + "分钟前";
        } else {
            time = time / 60;//小时
            if (time < 24) {
                return time + "小时前";
            } else {
                time = time / 24;//天
                if (time < 30) {
                    if (time == 1) {
                        return "昨天";
                    } else {
                        return time + "天前";
                    }
                } else {
                    time = time / 30;
                    if (time < 12) {
                        if (1 == time) {
                            return "上个月";
                        } else {
                            return time + "个月前";
                        }
                    } else {
                        time = time / 12;
                        switch (time) {
                            case 1:
                                return "去年";
                            default:
                                return time + "年前";
                        }
                    }
                }
            }
        }
    }
}
