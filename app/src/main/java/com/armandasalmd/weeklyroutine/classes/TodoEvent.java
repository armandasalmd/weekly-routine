package com.armandasalmd.weeklyroutine.classes;

public class TodoEvent {

    private String title, description, date;

    private boolean done;

    public int getId() {
        return id;
    }
    private int id;

    public TodoEvent(String title, String dec, String date) {
        this.title = title;
        description = dec;
        this.date = date;
        done = false;
        id = ++OpenData.currentMaxNotifId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }
}
