package com.generated.app;

import java.util.Calendar;

/**
 * Robust Jewish / Hebrew Calendar calculator and formatter.
 * Based on the mathematical astronomical calendar established by Hillel II.
 */
public class HebrewDateHelper {

    private static final String[] HEBREW_MONTHS_NORMAL = {
            "תשרי", "חשוון", "כסלו", "טבת", "שבט", "אדר",
            "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול"
    };

    private static final String[] HEBREW_MONTHS_LEAP = {
            "תשרי", "חשוון", "כסלו", "טבת", "שבט", "אדר א׳", "אדר ב׳",
            "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול"
    };

    public static class HebrewDate {
        public int year;
        public int month; // 1-based (1 = Tishrei)
        public int day;
        public String monthName;

        public String format() {
            String dayStr = formatHebrewDay(day);
            String yearStr = formatHebrewYear(year);
            return dayStr + " ב" + monthName + " " + yearStr;
        }

        public String formatShort() {
            return formatHebrewDay(day) + " ב" + monthName;
        }
    }

    public static HebrewDate getHebrewDate(Calendar calendar) {
        int gYear = calendar.get(Calendar.YEAR);
        int gMonth = calendar.get(Calendar.MONTH) + 1;
        int gDay = calendar.get(Calendar.DAY_OF_MONTH);

        int julianDay = gregorianToJulianDay(gYear, gMonth, gDay);
        return julianDayToHebrew(julianDay);
    }

    public static HebrewDate getHebrewDate(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return getHebrewDate(cal);
    }

    private static int gregorianToJulianDay(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        int a = year / 100;
        int b = 2 - a + (a / 4);
        return (int) (Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524.5);
    }

    private static HebrewDate julianDayToHebrew(int julianDay) {
        // Approximate Hebrew year
        int hYear = (int) Math.floor((julianDay - 347998) / 365.2468) + 1;
        while (hebrewCalendarElapsedDays(hYear) <= julianDay) {
            hYear++;
        }
        hYear--;

        int elapsedDays = hebrewCalendarElapsedDays(hYear);
        int daysSinceRoshHashanah = julianDay - elapsedDays;

        boolean leap = isHebrewLeapYear(hYear);
        int[] monthDays = getMonthLengths(hYear);

        int hMonth = 1;
        int remainingDays = daysSinceRoshHashanah;
        while (hMonth <= monthDays.length && remainingDays >= monthDays[hMonth - 1]) {
            remainingDays -= monthDays[hMonth - 1];
            hMonth++;
        }

        int hDay = remainingDays + 1;

        HebrewDate hd = new HebrewDate();
        hd.year = hYear;
        hd.month = hMonth;
        hd.day = hDay;
        if (leap) {
            hd.monthName = HEBREW_MONTHS_LEAP[Math.min(hMonth - 1, HEBREW_MONTHS_LEAP.length - 1)];
        } else {
            hd.monthName = HEBREW_MONTHS_NORMAL[Math.min(hMonth - 1, HEBREW_MONTHS_NORMAL.length - 1)];
        }
        return hd;
    }

    private static boolean isHebrewLeapYear(int year) {
        return ((7 * year + 1) % 19) < 7;
    }

    private static int hebrewCalendarElapsedDays(int year) {
        int monthsElapsed = (235 * ((year - 1) / 19)) + (12 * ((year - 1) % 19)) + (7 * ((year - 1) % 19) + 1) / 19;
        int partsElapsed = 204 + 793 * (monthsElapsed % 1080);
        int hoursElapsed = 5 + 12 * monthsElapsed + 793 * (monthsElapsed / 1080) + partsElapsed / 1080;
        int day = 1 + 29 * monthsElapsed + hoursElapsed / 24;
        int parts = (hoursElapsed % 24) * 1080 + partsElapsed % 1080;

        int alternativeDay;
        if (parts >= 19440 ||
                (day % 7 == 2 && parts >= 9924 && !isHebrewLeapYear(year)) ||
                (day % 7 == 1 && parts >= 16789 && isHebrewLeapYear(year - 1))) {
            alternativeDay = day + 1;
        } else {
            alternativeDay = day;
        }

        if (alternativeDay % 7 == 0 || alternativeDay % 7 == 3 || alternativeDay % 7 == 5) {
            alternativeDay++;
        }

        // Convert Hebrew day epoch count to Julian day
        return alternativeDay + 347997;
    }

    private static int[] getMonthLengths(int year) {
        boolean leap = isHebrewLeapYear(year);
        int totalDays = hebrewCalendarElapsedDays(year + 1) - hebrewCalendarElapsedDays(year);
        boolean cheshvanLong = (totalDays == 355 || totalDays == 385);
        boolean kislevShort = (totalDays == 353 || totalDays == 383);

        if (!leap) {
            return new int[]{
                    30, // Tishrei
                    cheshvanLong ? 30 : 29, // Cheshvan
                    kislevShort ? 29 : 30, // Kislev
                    29, // Tevet
                    30, // Shevat
                    29, // Adar
                    30, // Nisan
                    29, // Iyar
                    30, // Sivan
                    29, // Tammuz
                    30, // Av
                    29  // Elul
            };
        } else {
            return new int[]{
                    30, // Tishrei
                    cheshvanLong ? 30 : 29, // Cheshvan
                    kislevShort ? 29 : 30, // Kislev
                    29, // Tevet
                    30, // Shevat
                    30, // Adar I
                    29, // Adar II
                    30, // Nisan
                    29, // Iyar
                    30, // Sivan
                    29, // Tammuz
                    30, // Av
                    29  // Elul
            };
        }
    }

    private static String formatHebrewDay(int day) {
        String[] ones = {"", "א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "ז׳", "ח׳", "ט׳"};
        if (day == 15) return "ט״ו";
        if (day == 16) return "ט״ז";
        if (day <= 9) return ones[day];
        if (day == 10) return "י׳";
        if (day < 20) return "י\"" + ones[day - 10].replace("׳", "");
        if (day == 20) return "כ׳";
        if (day < 30) return "כ\"" + ones[day - 20].replace("׳", "");
        if (day == 30) return "ל׳";
        return String.valueOf(day);
    }

    private static String formatHebrewYear(int year) {
        int yearMod = year % 1000;
        int hundreds = yearMod / 100;
        int tens = (yearMod % 100) / 10;
        int ones = yearMod % 10;

        StringBuilder sb = new StringBuilder("תש");
        if (hundreds == 8) sb.append("פ");
        else if (hundreds == 7) sb.append("ע");

        String[] tensLetters = {"", "י", "כ", "ל", "מ", "נ", "ס", "ע", "פ", "צ"};
        String[] onesLetters = {"", "א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט"};

        int small = (tens * 10) + ones;
        if (small == 15) {
            sb.append("טו");
        } else if (small == 16) {
            sb.append("טז");
        } else {
            sb.append(tensLetters[tens]).append(onesLetters[ones]);
        }

        if (sb.length() > 1) {
            sb.insert(sb.length() - 1, "״");
        }
        return sb.toString();
    }
}
