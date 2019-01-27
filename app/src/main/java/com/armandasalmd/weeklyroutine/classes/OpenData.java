package com.armandasalmd.weeklyroutine.classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.notification.NotifCenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OpenData {
    public static final int WEEK_DAY_COUNT = 7;
    public static final int START_EDIT = 0, FIRST_ITEM = 1,
            CANCEL_EDIT = 2, ADD_NEW = 3, STOP_EDIT = 4;


    public static int maxSpecialDates = 10, currentMaxNotifId;
    public static int askForTuto = 0; // 0 - false, 1 - true, 2 - lock
    public static boolean createPlansIsShown = false,
            lockMode = true, combined = false , infoShown = false;

    public static final String SAVE_PLANS = "savedData1";
    private static final String SAVE_TODO = "savedTodo1";
    public static final String SAVE_SPECIAL = "savedSpecial1";
    private static final String SAVE_NOTIFID = "savedNotifId1";
    public static final String SHARED_PREF = "preference";
    //public static final String AD_ID = "ca-app-pub-1826188022428675/2854509349";
    private static char separator = '.';

    public static List<Day> mDays;
    public static List<SpecialEvent> mSpecials;
    public static List<TodoEvent> mTodo;

    public static int tutoMode = -1;
    public static List<Event> tutoSave;
    public static List<View> tutoViews;

    public static void save(Activity activity) {
        if (tutoMode >= 0)
            mDays.get(tutoMode).setEvents(tutoSave);

        Gson gson = new Gson();
        SharedPreferences sharedPref = activity.getApplicationContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String saveString = gson.toJson(mDays);
        editor.putString(SAVE_PLANS, saveString);

        saveString = gson.toJson(mTodo);
        editor.putString(SAVE_TODO, saveString);

        saveString = gson.toJson(mSpecials);
        editor.putString(SAVE_SPECIAL, saveString);

        saveString = Integer.toString(currentMaxNotifId);
        editor.putString(SAVE_NOTIFID, saveString);

        editor.apply();
    }

    public static void load(Activity activity) {
        //movePreferences(activity);
        NotifCenter.context = activity.getApplicationContext();
        mDays = new ArrayList<>();
        mTodo = new ArrayList<>();
        mSpecials = new ArrayList<>();

        SharedPreferences sharedPref = activity.getApplicationContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);

        changeSep(PreferenceManager.getDefaultSharedPreferences(activity)
                .getString(activity.getString(R.string.key_separator), ".").charAt(0));

        String savedData = sharedPref.getString(SAVE_PLANS, ""),
                savedTodo = sharedPref.getString(SAVE_TODO, ""),
                savedSpecial = sharedPref.getString(SAVE_SPECIAL, ""),
                savedNotifId = sharedPref.getString(SAVE_NOTIFID, "0");
        currentMaxNotifId = Integer.parseInt(savedNotifId);

        if (!savedData.isEmpty()) {
            Gson gson = new Gson();

            mDays = gson.fromJson(savedData, new TypeToken<ArrayList<Day>>(){}.getType());
            mTodo = gson.fromJson(savedTodo, new TypeToken<ArrayList<TodoEvent>>(){}.getType());
            mSpecials = gson.fromJson(savedSpecial, new TypeToken<ArrayList<SpecialEvent>>(){}.getType());

            for (SpecialEvent specialEvent : mSpecials)
                specialEvent.setSpecial();
            setDatesForWeek();
        } else
            firstTimeSetUp();


    }

    public static void changeSep(char num) {
        switch (num) {
            case '1': separator = '.'; break;
            case '2': separator = '-'; break;
            case '3': separator = '/'; break;
            default: separator = '.';
        }
    }

    public static void setDatesForWeek() {
        int day = getWeekDayInt();
        for (int i = day; i < mDays.size(); i++)
            mDays.get(i).setDate(getWeekDayDate(i, false));
        for (int i = 0; i < day; i++)
            mDays.get(i).setDate(getWeekDayDate(i, true));
    }

    public static void deleteSpecialsFromEvents() {
        combined = false;
        for (int i = 0; i < mDays.size(); i++)
            for (int j = mDays.get(i).getEvents().size() - 1; j >= 0 ; j--)
                if (mDays.get(i).getEvents().get(j).isSpecial)
                    mDays.get(i).getEvents().remove(j);
        /*for (int i = 0; i < WEEK_DAY_COUNT; i++)
            mDays.get(i).setBadgeNum(0);*/
    }

    public static void combineSpecialEvents() {
        combined = true;
        List<Event> matchingList;
        for (int i = 0; i < mDays.size(); i++) { // iesko 7 datu
            matchingList = new ArrayList<>();
            for (int j = 0; j < mSpecials.size(); j++)
                for (int k = 0; k < mSpecials.get(j).getDates().size(); k++)
                    if (mSpecials.get(j).getDates().get(k).toString().equals(mDays.get(i).getDate())) {
                        matchingList.add(mSpecials.get(j).getEvent());
                        break;
                    }

            for (Event ev : matchingList)
                mDays.get(i).getEvents().add(ev);
            mDays.get(i).setBadgeNum(matchingList.size());
        }
        for (int i = 0; i < WEEK_DAY_COUNT; i++)
            EventControl.Plans.sortEvents(i);
    }

    public static void removePastDates(boolean completeDelPast) {
        String today = getTodayDate();
        List<Integer> list;
        try {
            for (int j = 0; j < mSpecials.size(); j++) {
                list = new ArrayList<>();
                for (int k = 0; k < mSpecials.get(j).getDates().size(); k++)
                    if (today.compareTo(mSpecials.get(j).getDates().get(k).toString()) > 0) // jei 1 tai data pasenus
                        list.add(k);
                    else
                        break;
                for (int i = list.size() - 1; i >= 0; i--)
                    mSpecials.get(j).getDates().remove(i);
            }
            if (completeDelPast)
                for (int i = mSpecials.size() - 1; i >= 0; i--)
                    if (mSpecials.get(i).getDates().size() == 0)
                        mSpecials.remove(i);
        } catch (Exception e) {
            Log.i("Armandas", "removePastDates: catch");
        }
    }

    private static String getWeekDayDate(int weekDay, boolean plusWeek) {
        int cWeekDay = getWeekDayInt() + 1;
        weekDay += 1;
        long cMili = System.currentTimeMillis();
        DateFormat df1 = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()); //foramt date

        if (weekDay > cWeekDay)
            cMili = cMili + ((weekDay - cWeekDay) * 24 * 60 * 60 * 1000 );
        else
            cMili = cMili - ((cWeekDay - weekDay) * 24 * 60 * 60 * 1000 );

        if (plusWeek)
            return df1.format(cMili + 1000 * 60 * 60 * 24 * 7);
        else
            return df1.format(cMili);
    }

    public static String getCurrentDate(Context context) {
        String[] days = get7DayNames(context);
        return getTodayDate() + " " + days[getWeekDayInt()].substring(0, 3);
    }

    public static String getTodayDate() {
        DateFormat df1 = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());//foramt date
        return df1.format(Calendar.getInstance().getTime());
    }

    public static int getWeekDayInt() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            return 6;
        else
            return calendar.get(Calendar.DAY_OF_WEEK) - 2;
    }

    private static void firstTimeSetUp() {
        int day = getWeekDayInt();
        for (int i = day; i < WEEK_DAY_COUNT; i++)
            mDays.add(new Day(getWeekDayDate(i, false)));
        for (int i = 0; i < day; i++)
            mDays.add(new Day(getWeekDayDate(i, true)));
    }

    public static String[] get7DayNames(Context context) {
        return context.getResources().getStringArray(R.array.week_names);
    }

    public static long getMiliForDate(String date, int hour, int minute) {
        String[] values = date.split("\\.");
        int year = Integer.parseInt(values[0]),
                month = Integer.parseInt(values[1]),
                day = Integer.parseInt(values[2]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, 0);
        return calendar.getTimeInMillis();
    }

    public static class BusEventHolder {
        public int getDayId() {
            return day;
        }
        public int getEventId() {
            return event;
        }

        int day, event;

        public BusEventHolder(int dayId, int eventId) {
            day = dayId;
            event = eventId;
        }
    }

    public static class BusSpecialHolder {
        public int getPosition() {
            return position;
        }
        public int getAction() {return action; }

        int position, action;
        public boolean edit;

        public BusSpecialHolder(int action, int position) {
            edit = false;
            this.position = position;
            this.action = action;
        }

        public BusSpecialHolder(int action, boolean edit) {
            this.edit = edit;
            position = 0;
            this.action = action;
        }

        public BusSpecialHolder(int action) {
            edit = false;
            position = 0;
            this.action = action;
        }

    }

    public static DateHolder dateStringToHolder(String date) {
        int y = Integer.parseInt(date.substring(0, 4));
        int m = Integer.parseInt(date.substring(5, 7));
        int d = Integer.parseInt(date.substring(8, 10));
        return new DateHolder(y, m, d);
    }

    public static String prevDate(String date) {
        return date.replace('.', separator);
    }
}
