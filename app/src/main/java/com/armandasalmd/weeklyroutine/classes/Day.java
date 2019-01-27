package com.armandasalmd.weeklyroutine.classes;

import java.util.ArrayList;
import java.util.List;

public class Day {

    private List<Event> events;
    private String date;

    public int getBadgeNum() {
        return badgeNum;
    }
    void setBadgeNum(int badgeNum) {
        this.badgeNum = badgeNum;
    }
    transient private int badgeNum;

    void minusBadge() {
        badgeNum--;
    }

    void plusBadge() {
        badgeNum++;
    }

    Day(String date) {
        events = new ArrayList<>();
        this.date = date;
        badgeNum = 0;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getDate() {
        return date;
    }

    void setDate(String date) {
        this.date = date;
    }
}
