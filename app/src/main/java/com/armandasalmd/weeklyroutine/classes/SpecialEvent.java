package com.armandasalmd.weeklyroutine.classes;

import java.util.ArrayList;
import java.util.List;

public class SpecialEvent {

    private List<DateHolder> dates;
    public List<DateHolder> getDates() {
        return dates;
    }
    public void setDates(List<DateHolder> dates) {
        this.dates = dates;
    }

    private Event event;
    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Integer> getIdList() {
        return idList;
    }
    private List<Integer> idList;


    public SpecialEvent(List<DateHolder> dates, Event event) {
        this.dates = dates;
        this.event = event;
        idList = new ArrayList<>();
        for (int i = 0; i < OpenData.maxSpecialDates; i++)
            idList.add(event.getNotificationId() + i);

        setSpecial();
    }

    void setSpecial() {
        event.isSpecial = true;
        event.specialEventCopy = this;
    }

    static void removeDateFromList(SpecialEvent specialEvent, String date) {
        for (DateHolder holder : specialEvent.dates)
            if (holder.toString().equals(date)) {
                specialEvent.dates.remove(holder);
                break;
            }
    }

    public static boolean currentlyExist(List<DateHolder> list, DateHolder dateHolder) {
        for (DateHolder listItem : list)
            if (listItem.getDay() == dateHolder.getDay() && listItem.getMonth() == dateHolder.getMonth() && listItem.getYear() == dateHolder.getYear())
                return true;
        return false;
    }

    public static String datesPreviewString(List<DateHolder> mDates) {
        String ans = "";
        if (mDates.size() == 0)
            return ans;

        for (DateHolder dateHolder : mDates)
            ans += dateHolder.toString() + ", ";
        ans = ans.substring(0, ans.length() - 2);
        return ans;
    }


}
