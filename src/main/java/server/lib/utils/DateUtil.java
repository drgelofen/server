package server.lib.utils;

import java.util.Calendar;

public class DateUtil {

    public static Long toMillis(String date) {
        try {
            date = StringUtil.format(date).trim();
            int year, month, day, hour = 0, minute = 0;
            String time = null, params = null;
            if (date.contains(" ")) {
                String[] split = date.split(" ");
                params = split[0];
                time = split[1];
            } else if (date.contains("T")) {
                String[] split = date.split("T");
                params = split[0];
                time = split[1];
            } else {
                params = date;
            }
            if (params != null) {
                if (time != null) {
                    String[] split = time.split(":");
                    hour = convert(split[0]);
                    minute = convert(split[1]);
                }
                String[] split = params.split(getSplitter(params));
                year = convert(split[0]);
                Calendar calendar;
                month = convert(split[1]) - 1;
                day = convert(split[2]);
                calendar = Calendar.getInstance();
                if (year >= 1800) {
                    calendar.set(year, month, day, hour, minute);
                } else {
                    CalendarUtil util = new CalendarUtil(year, month + 1, day);
                    calendar = util.toGregorian();
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)
                            , calendar.get(Calendar.DAY_OF_MONTH), hour, minute, 0);
                }
                return calendar.getTimeInMillis();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static String toJalali(long millis) {
        CalendarUtil util = new CalendarUtil(millis);
        return util.toString();
    }

    private static String getSplitter(String params) {
        if (params.contains("/")) {
            return "/";
        } else if (params.contains("\"")) {
            return "\"";
        } else if (params.contains("-")) {
            return "-";
        } else if (params.contains("_")) {
            return "_";
        } else if (params.contains("|")) {
            return "|";
        } else if (params.contains("+")) {
            return "+";
        }
        return null;
    }

    private static int convert(String input) {
        if (input.length() > 1 && input.startsWith("0")) {
            return Integer.parseInt(input.substring(1));
        }
        return Integer.parseInt(input);
    }

    public static CalendarUtil fromJalali(String startDate) {
        String[] split = startDate.split(getSplitter(startDate));
        int year, month, day;
        year = convert(split[0]);
        month = convert(split[1]);
        day = convert(split[2]);
        CalendarUtil calendar = new CalendarUtil();
        calendar.set(year, month, day);
        return calendar;
    }

    public static void printNow(String tag) {
        Calendar instance = Calendar.getInstance();
        System.out.println("Tag:" + tag + "   stamp:" + instance.get(Calendar.HOUR_OF_DAY)
                + ':' + instance.get(Calendar.MINUTE) + ':' + instance.get(Calendar.SECOND));
    }
}
