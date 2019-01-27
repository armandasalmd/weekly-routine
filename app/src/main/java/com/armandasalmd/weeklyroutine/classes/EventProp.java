package com.armandasalmd.weeklyroutine.classes;

public class EventProp {

    public boolean useTimeTo;
    public boolean isSpecial;
    transient SpecialEvent specialEventCopy;

    boolean done;
    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }

    boolean useNotif;
    public boolean isUseNotif() {
        return useNotif;
    }
    public void setUseNotif(boolean useNotif) {
        this.useNotif = useNotif;
    }

    int fromHour;
    public int getFromHour() {
        return fromHour;
    }

    int fromMinutes;
    public int getFromMinutes() {
        return fromMinutes;
    }

    int toHour;
    public int getToHour() {
        return toHour;
    }

    int toMinutes;
    public int getToMinutes() {
        return toMinutes;
    }

    String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    int notificationId;
    public int getNotificationId() {
        return notificationId;
    }
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

}
