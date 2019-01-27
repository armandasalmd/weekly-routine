package com.armandasalmd.weeklyroutine.notification;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.armandasalmd.weeklyroutine.MainActivity;
import com.armandasalmd.weeklyroutine.classes.Day;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;
import com.armandasalmd.weeklyroutine.classes.TodoEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static com.armandasalmd.weeklyroutine.classes.OpenData.mDays;

/**
 * Created by Armandas on 2017-10-12.
 */

public class RebootService extends IntentService {

    public RebootService() {
        super("RebootService");
    }

    public RebootService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mDays = new ArrayList<>();
        OpenData.mSpecials = new ArrayList<>();
        load();
        resetNotifications();
    }

    private void load() {

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(OpenData.SHARED_PREF, Context.MODE_PRIVATE);
        String savedData = sharedPref.getString(OpenData.SAVE_PLANS, ""),
                savedSpecial = sharedPref.getString(OpenData.SAVE_SPECIAL, "");

        if (!savedData.isEmpty()) {
            Gson gson = new Gson();

            mDays = gson.fromJson(savedData, new TypeToken<ArrayList<Day>>(){}.getType());
            OpenData.mSpecials = gson.fromJson(savedSpecial, new TypeToken<ArrayList<SpecialEvent>>(){}.getType());
            OpenData.setDatesForWeek();
        }
    }

    private void resetNotifications() {
        NotifCenter.context = getApplicationContext();
        String date;
        for (int i = 0; i < OpenData.WEEK_DAY_COUNT; i++) {
            date = mDays.get(i).getDate();
            for (int j = 0; j < mDays.get(i).getEvents().size(); j++) {
                Event event = mDays.get(i).getEvents().get(j);
                if (event.isUseNotif())
                    NotifCenter.appendPlan(date, event);
            }
        }

        for (SpecialEvent event : OpenData.mSpecials)
            if (event.getEvent().isUseNotif())
                NotifCenter.appendSpecial(event);
    }

}
