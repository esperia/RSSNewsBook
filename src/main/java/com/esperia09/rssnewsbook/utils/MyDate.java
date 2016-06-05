package com.esperia09.rssnewsbook.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by esperia on 2016/06/05.
 */
public class MyDate {
    private static final String FMT_YMD = "yyyy/MM/dd";
    //private static final String FMT_YMD = "yyyy/MM/dd HH:mm:ss.SSS";

    private static Date sharedFmtDate = new Date();
    private static SimpleDateFormat fmtYmd = new SimpleDateFormat(FMT_YMD);

    public static String currentYmd() {
        synchronized (MyDate.class) {
            sharedFmtDate.setTime(System.currentTimeMillis());
            return fmtYmd.format(sharedFmtDate);
        }
    }
}
