package com.armandasalmd.weeklyroutine.classes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortHelper {

    public static final int AZ = 0, ZA = 1, TIME = 2;


    public static List<SpecialEvent> sortSpecial(List<SpecialEvent> events, int sortMode) {
        switch (sortMode) {
            case AZ: {
                events = specialAZ(events);
                break; }
            case ZA: {
                events = specialZA(events);
                break; }
            case TIME: {
                events = specialTIME(events);
                break; }
        }
        return events;
    }

    private static List<SpecialEvent> specialAZ(List<SpecialEvent> events) {
        Collections.sort(events, new Comparator<SpecialEvent>() {
            @Override
            public int compare(SpecialEvent o1, SpecialEvent o2) {
                return o1.getEvent().getTitle().compareTo(o2.getEvent().getTitle());
            }
        });
        return events;
    }

    private static List<SpecialEvent> specialZA(List<SpecialEvent> events) {
        Collections.sort(events, new Comparator<SpecialEvent>() {
            @Override
            public int compare(SpecialEvent o1, SpecialEvent o2) {
                return o2.getEvent().getTitle().compareTo(o1.getEvent().getTitle());
            }
        });
        return events;
    }

    private static List<SpecialEvent> specialTIME(List<SpecialEvent> events) {
        Collections.sort(events, new Comparator<SpecialEvent>() {
            @Override
            public int compare(SpecialEvent o1, SpecialEvent o2) {
                if (o1.getDates().size() > 0 && o2.getDates().size() > 0)
                    return getRawDate(o1.getDates().get(0), o1.getEvent().getFromHour(),
                            o1.getEvent().getFromMinutes()).compareTo(getRawDate(o2.getDates().get(0),
                            o2.getEvent().getFromHour(), o2.getEvent().getFromMinutes()));
                else
                    return 1; // -1 : o1 < o2   0 : o1 == o2  +1 : o1 > o2
            }
        });
        return events;
    }

    private static String getRawDate(DateHolder holder, int hour, int minute) {
        StringBuilder ans = new StringBuilder(Integer.toString(holder.getYear()));
        int data[] = new int[] {holder.getMonth(), holder.getDay(), hour, minute};
        for (int aData : data) {
            if (aData < 10)
                ans.append("0");
            ans.append(Integer.toString(aData));

        }
        return ans.toString();
    }

    public static List<TodoEvent> sortTodo(List<TodoEvent> events, int sortMode) {
        switch (sortMode) {
            case AZ: {
                events = todoAZ(events);
                break; }
            case ZA: {
                events = todoZA(events);
                break; }
            case TIME: {
                events = todoTIME(events);
                break; }
        }
        return events;
    }

    private static List<TodoEvent> todoAZ(List<TodoEvent> events) {
        Collections.sort(events, new Comparator<TodoEvent>() {
            @Override
            public int compare(TodoEvent o1, TodoEvent o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        return events;
    }

    private static List<TodoEvent> todoZA(List<TodoEvent> events) {
        Collections.sort(events, new Comparator<TodoEvent>() {
            @Override
            public int compare(TodoEvent o1, TodoEvent o2) {
                return o2.getTitle().compareTo(o1.getTitle());
            }
        });
        return events;
    }

    private static List<TodoEvent> todoTIME(List<TodoEvent> events) {
        Collections.sort(events, new Comparator<TodoEvent>() {
            @Override
            public int compare(TodoEvent o1, TodoEvent o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        return events;
    }

}
