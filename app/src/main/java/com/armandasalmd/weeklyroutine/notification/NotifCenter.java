package com.armandasalmd.weeklyroutine.notification;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.EventControl;
import com.armandasalmd.weeklyroutine.classes.EventProp;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;
import com.google.gson.Gson;

public class NotifCenter {

    public static Context context;
    static String TAG_EVENT = "event";

    public static void appendPlan(String date, Event event) {
        append(date, event, true);
    }

    public static void appendSpecial(SpecialEvent event) {
        Event event1 = event.getEvent();
        event1.isSpecial = true;
        for (int i = 0; i < event.getDates().size(); i++) /* eina per pasirinktas datas */ {
            event1.setNotificationId(event.getIdList().get(i));
            append(event.getDates().get(i).toString(), event1, false);
        }
    }

    public static void removePlan(Event event) {
        removeById(event.getNotificationId());
    }

    public static void removeSpecial(SpecialEvent event) {
        for (int i = 0; i < event.getIdList().size(); i++) {
            removeById(event.getIdList().get(i));
        }
    }

    public static void editPlan(String date, Event event) {
        removePlan(event);
        appendPlan(date, event);
    }

    public static void editSpecial(SpecialEvent event) {
        removeSpecial(event);
        appendSpecial(event);
    }

    private static void append(String date, Event event, boolean repeat) {
        long launchTime = OpenData.getMiliForDate(date, event.getFromHour(), event.getFromMinutes());
        Intent notifyIntent = new Intent(context, NotifReceiver.class);

        Gson gson = new Gson();
        String info = gson.toJson(event);
        notifyIntent.putExtra(TAG_EVENT, info);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, event.getNotificationId(),
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (repeat) {
            if (launchTime < System.currentTimeMillis())
                launchTime += AlarmManager.INTERVAL_DAY * 7;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, launchTime,
                    AlarmManager.INTERVAL_DAY * 7, pendingIntent);
        }
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, launchTime, pendingIntent);
    }

    private static void removeById(int notificationId) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationId != 0) {
            notifManager.cancel(notificationId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId,
                    new Intent(context, NotifReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            } else {
                Log.i("Armandas", "removePlan: pendingIntent null");
            }
        } else {
            Log.i("Armandas", "removePlan: notificationId 0");
        }
    }
}
