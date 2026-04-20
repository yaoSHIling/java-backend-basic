package com.example.basic.util;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil as HU;
import cn.hutool.core.date.StopWatch;
import java.time.*;
import java.util.Date;

/**
 * 日期时间工具（封装 Hutool DateUtil）。
 *
 * @author hermes-agent
 */
public class DateUtil {

    public static final String FMT_DT   = "yyyy-MM-dd HH:mm:ss";
    public static final String FMT_DATE = "yyyy-MM-dd";
    public static final String FMT_TIME = "HH:mm:ss";
    public static final String FMT_FILE = "yyyyMMddHHmmss";
    public static final String FMT_COMP = "yyyyMMdd";

    public static String format(Date d)        { return format(d, FMT_DT); }
    public static String format(Date d, String p) { return d == null ? "" : HU.format(d, p); }
    public static String format(LocalDateTime l) { return format(l, FMT_DT); }
    public static String format(LocalDateTime l, String p) { return l == null ? "" : l.toString().replace("T", " "); }
    public static String format(LocalDate l)    { return l == null ? "" : l.toString(); }
    public static Date parse(String s)         { return parse(s, FMT_DT); }
    public static Date parse(String s, String p) {
        if (s == null || s.isBlank()) return null;
        try { return HU.parse(s, p); } catch(Exception e) { return null; }
    }
    public static LocalDateTime toLdt(Date d) {
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    public static Date toDate(LocalDateTime l) {
        return l == null ? null : Date.from(l.atZone(ZoneId.systemDefault()).toInstant());
    }
    public static Date addDays(Date d, int n)  { return HU.offsetDay(d, n); }
    public static Date addHours(Date d, int n) { return HU.offsetHour(d, n); }
    public static long diffMs(Date a, Date b)  { return a.getTime() - b.getTime(); }
    public static long diffSec(Date a, Date b)  { return diffMs(a, b) / 1000; }
    public static long diffDays(Date a, Date b){ return Math.abs(diffMs(a, b)) / 86400000; }
    public static Date startOfDay(Date d)      { return HU.beginOfDay(d); }
    public static Date endOfDay(Date d)        { return HU.endOfDay(d); }
    public static StopWatch startWatch()       { StopWatch sw = new StopWatch(); sw.start(); return sw; }
    public static long nowMs()                  { return System.currentTimeMillis(); }
}
