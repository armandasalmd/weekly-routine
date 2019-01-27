package com.armandasalmd.weeklyroutine.classes;

public class DateHolder {

    private int year, month, day;

    public DateHolder(int y, int m, int d) {
        setDate(y, m, d);
    }

    private void setDate(int y, int m, int d) {
        year = y;
        month = m;
        day = d;
    }

    int getDay() {
        return day;
    }
    int getMonth() {
        return month;
    }
    int getYear() {
        return year;
    }

    public String toString() {
        char separator = '.';
        String ans = Integer.toString(year) + separator;
        if (month < 10)
            ans += "0";
        ans += Integer.toString(month) + separator;
        if (day < 10)
            ans += "0";
        ans += Integer.toString(day);
        return ans;
    }

    static boolean firstBigger(DateHolder holder1, DateHolder holder2) {
        if (holder1.getYear() == holder2.getYear())
            if (holder1.getMonth() == holder2.getMonth())
                return holder1.getDay() != holder2.getDay() && holder1.getDay() > holder2.getDay();
            else
                return holder1.getMonth() > holder2.getMonth();
        else
            return holder1.getYear() > holder2.getYear();
    }
}