package com.armandasalmd.weeklyroutine.classes;

import java.util.List;

/**
 * Created by Armandas on 2017-10-14.
 */

public class ExtraDataHolder {

    private String title;
    private String dec;
    private List<DateHolder> dates;
    private String date;
    private int[] time = new int[4];

    private boolean useTimeTo;
    private Event.Duration duration;

    public ExtraDataHolder(String title, String dec, int fragId, boolean useTimeTo) {
        this.title = title;
        this.dec = dec;
        fragmentID = fragId;
        this.useTimeTo = useTimeTo;
    }

    public String datesToString() {
        if (dates != null && dates.size() != 0) {
            return OpenData.prevDate(SpecialEvent.datesPreviewString(dates)); // separator
        } else
            return "";
    }

    public String getTimeFrom() {
        return Event.formatTime(time[0], time[1]);
    }
    public String getTimeTo() {
        return Event.formatTime(time[2], time[3]);
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDec() {
        return dec;
    }
    public List<DateHolder> getDates() {
        return dates;
    }
    public void setDates(List<DateHolder> dates) {
        this.dates = dates;
    }
    public String getDate() {
        return OpenData.prevDate(date); // separator
    }
    public void setDate(String date) {
        this.date = date;
    }
    public int[] getTime() {
        return time;
    }
    public void setTime(int[] time) {
        this.time = time;
    }
    public Event.Duration getDuration() {
        return duration;
    }
    public void setDuration(Event.Duration duration) {
        this.duration = duration;
    }
    public boolean isUseTimeTo() {
        return useTimeTo;
    }

    public int getFragmentID() {
        return fragmentID;
    }
    private int fragmentID = -1;
}
