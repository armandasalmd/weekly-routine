package com.armandasalmd.weeklyroutine.classes;

import android.content.Context;
import com.armandasalmd.weeklyroutine.R;

public class Event extends EventProp {

    public Event() {
        fromHour = 0;
        fromMinutes = 0;
        toHour = 0;
        toMinutes = 0;
        title = "";
        description = "";
        useTimeTo = false;
        useNotif = true;
        done = false;
        notificationId = ++OpenData.currentMaxNotifId;
    }

    public Event(int fHour, int fMin, int tHour, int tMin, String title, String description) {
        this.title = title;
        this.description = description;
        fromHour = fHour;
        fromMinutes = fMin;
        toHour = tHour;
        toMinutes = tMin;
        this.useTimeTo = false;
        useNotif = true;
        done = false;
        notificationId = ++OpenData.currentMaxNotifId;
    }

    public void setTimes(int fHour, int fMinutes, int tHour, int tMinutes) {
        fromHour = fHour;
        fromMinutes = fMinutes;
        toHour = tHour;
        toMinutes = tMinutes;
    }

    public String getTimeFrom() {
        return formatTime(fromHour, fromMinutes);
    }

    public String getTimeTo() {
        return formatTime(toHour, toMinutes);
    }

    public static String formatTime(int hour, int minutes) {
        String time;
        if (hour < 10)
            time = "0" + Integer.toString(hour);
        else
            time = Integer.toString(hour);
        time += ":";
        if (minutes < 10)
            time += ("0" + Integer.toString(minutes));
        else
            time += Integer.toString(minutes);
        return time;
    }

    public Duration getDuration() {
        Duration dur = new Duration();
        dur.setHours(toHour - fromHour);

        if (toMinutes - fromMinutes < 0) {
            dur.setHours(dur.getHours() - 1);
            dur.setMinutes(toMinutes - fromMinutes + 60);
        } else
            dur.setMinutes(toMinutes - fromMinutes);

        return dur;
    }

    public class Duration {
        private int hours;
        int getHours() {
            return hours;
        }
        void setHours(int hours) {
            this.hours = hours;
        }

        private int minutes;
        void setMinutes(int minutes) {
            this.minutes = minutes;
        }

        public String toString(Context context) {
            String ans;
            if (hours == 0)
                return minutes + context.getString(R.string.mins);
            else
                ans = hours + context.getString(R.string.hours);

            if (minutes == 0)
                return ans;
            else
                return ans + " " + minutes + context.getString(R.string.mins);
        }
    }
}
