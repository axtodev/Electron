package lol.vifez.electron.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Electron © Vifez
 * Developed by Vifez
 * Copyright (c) 2025 Vifez. All rights reserved.
 */

public class TimeUtils {

    public static final long MINUTE = TimeUnit.MINUTES.toSeconds(1);
    private static final ThreadLocal<StringBuilder> mmssBuilder = ThreadLocal.withInitial(StringBuilder::new);

    public static long parseTime(String input) {
        if (input == null || input.isEmpty() || input.equals("0") || input.equalsIgnoreCase("0s")) {
            return 0;
        }

        String[] units = {"w", "d", "h", "m", "s"};
        long[] multipliers = {
                TimeUnit.DAYS.toMillis(7),
                TimeUnit.DAYS.toMillis(1),
                TimeUnit.HOURS.toMillis(1),
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.SECONDS.toMillis(1)
        };

        long millis = 0;
        for (int i = 0; i < units.length; i++) {
            Matcher matcher = Pattern.compile("(\\d+)" + units[i]).matcher(input);
            while (matcher.find()) {
                millis += Long.parseLong(matcher.group(1)) * multipliers[i];
            }
        }

        return millis == 0 ? -1 : millis;
    }

    public static long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return 0;

        long multiplier = 1000;
        char unit = duration.charAt(duration.length() - 1);

        switch (unit) {
            case 's':
                multiplier = 1000;
                break;
            case 'm':
                multiplier = 60 * 1000;
                break;
            case 'h':
                multiplier = 60 * 60 * 1000;
                break;
            case 'd':
                multiplier = 24 * 60 * 60 * 1000;
                break;
            default:
                unit = ' ';
                break;
        }

        String number = (unit == 's' || unit == 'm' || unit == 'h' || unit == 'd')
                ? duration.substring(0, duration.length() - 1)
                : duration;

        try {
            return Long.parseLong(number) * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatDetailed(long input) {
        return formatDetailed(input, TimeUnit.MILLISECONDS);
    }

    public static String formatDetailed(long input, TimeUnit unit) {
        if (input == -1) return "Permanent";

        long seconds = unit.toSeconds(input);
        if (seconds == 0) return "0 seconds";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return formatParts(days, "day") + formatParts(hours, "hour") +
                formatParts(minutes, "minute") + formatParts(secs, "second");
    }

    public static String formatTimeShort(long input) {
        return formatTimeShort(input, TimeUnit.MILLISECONDS);
    }

    public static String formatTimeShort(long input, TimeUnit unit) {
        if (input == -1) return "Permanent";

        long seconds = unit.toSeconds(input);
        if (seconds == 0) return "0 seconds";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return formatShortParts(days, "d") + formatShortParts(hours, "h") +
                formatShortParts(minutes, "m") + formatShortParts(secs, "s");
    }

    private static String formatParts(long value, String label) {
        if (value <= 0) return "";
        return " " + value + " " + label + (value > 1 ? "s" : "");
    }

    private static String formatShortParts(long value, String label) {
        return value > 0 ? " " + value + label : "";
    }

    public static String formatHHMMSS(long input) {
        return formatHHMMSS(input, false, TimeUnit.MILLISECONDS);
    }

    public static String formatHHMMSS(long input, TimeUnit unit) {
        return formatHHMMSS(input, false, unit);
    }

    public static String formatHHMMSS(long input, boolean displayMillis) {
        return formatHHMMSS(input, displayMillis, TimeUnit.MILLISECONDS);
    }

    public static String formatHHMMSS(long input, boolean displayMillis, TimeUnit unit) {
        long totalSeconds = unit.toSeconds(input);
        if (displayMillis && totalSeconds < MINUTE) {
            long millis = unit.toMillis(input);
            long ms = millis % 1000;
            return (millis / 1000) + "." + (ms / 100) + "s";
        }

        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        StringBuilder sb = mmssBuilder.get();
        sb.setLength(0);

        if (hours > 0) sb.append(String.format("%02d:", hours));
        sb.append(String.format("%02d:%02d", minutes, seconds));
        return sb.toString();
    }

    public static String formatTimeAgo(long input) {
        return formatTimeAgo(input, TimeUnit.MILLISECONDS);
    }

    public static String formatTimeAgo(long input, TimeUnit unit) {
        long elapsed = System.currentTimeMillis() - unit.toMillis(input);

        if (elapsed < 1000) return "now";

        if (elapsed >= TimeUnit.DAYS.toMillis(365)) return formatElapsed(elapsed, TimeUnit.DAYS.toMillis(365), "year");
        if (elapsed >= TimeUnit.DAYS.toMillis(30)) return formatElapsed(elapsed, TimeUnit.DAYS.toMillis(30), "month");
        if (elapsed >= TimeUnit.DAYS.toMillis(1)) return formatElapsed(elapsed, TimeUnit.DAYS.toMillis(1), "day");
        if (elapsed >= TimeUnit.HOURS.toMillis(1)) return formatElapsed(elapsed, TimeUnit.HOURS.toMillis(1), "hour");
        if (elapsed >= TimeUnit.MINUTES.toMillis(1)) return formatElapsed(elapsed, TimeUnit.MINUTES.toMillis(1), "minute");
        return formatElapsed(elapsed, TimeUnit.SECONDS.toMillis(1), "second");
    }

    private static String formatElapsed(long elapsed, long unitMillis, String label) {
        long value = elapsed / unitMillis;
        return value + " " + label + (value > 1 ? "s" : "") + " ago";
    }

    public static String formatDate(long input) {
        return formatDate(input, true, PracticeConstant.TIME_ZONE);
    }

    public static String formatDate(long input, boolean showTime) {
        return formatDate(input, showTime, PracticeConstant.TIME_ZONE);
    }

    public static String formatDate(long input, TimeZone tz) {
        return formatDate(input, true, tz);
    }

    public static String formatDate(long input, boolean showTime, TimeZone tz) {
        if (input == -1) return "Permanent";
        DateFormat formatter = new SimpleDateFormat("MM/dd/yy" + (showTime ? " hh:mm:ss a" : "") + " z");
        formatter.setTimeZone(tz);
        return formatter.format(input);
    }
}