package com.armandasalmd.weeklyroutine.classes;

import com.armandasalmd.weeklyroutine.notification.NotifCenter;

import java.util.ArrayList;
import java.util.List;

public class EventControl {

    public static class Plans {

        static void sortEvents(int day) {
            List<Event> eventList = getEvents(day);
            for (int i = (eventList.size() - 1); i >= 0; i--)
                for (int j = 1; j <= i; j++)
                    if (firstTimeIsGreater(eventList.get(j - 1), eventList.get(j))) {
                        Event temp = eventList.get(j - 1);
                        eventList.set(j - 1, eventList.get(j));
                        eventList.set(j, temp);
                    }
            OpenData.mDays.get(day).setEvents(eventList);
        }

        private static int findPosition(int day, Event event) {
            int add_pos = getEvents(day).size();
            for (int i = 0; i < getEvents(day).size(); i++)
                if (firstTimeIsGreater(getEvents(day).get(i), event)) {
                    add_pos = i;
                    break;
                }
            return add_pos;
        }

        public static int addEvent(int day, Event event) {
            int currentPos = findPosition(day, event); // galutine ivykio vieta sarase
            getEvents(day).add(currentPos, event);
            if (event.isUseNotif())
                NotifCenter.appendPlan(OpenData.mDays.get(day).getDate(), event); // TODO: add notif
            return currentPos;
        }

        public static int editEvent(int day, int pos, Event event) {
            getEvents(day).remove(pos);
            int currentPos = findPosition(day, event); // galutine ivykio vieta sarase
            getEvents(day).add(currentPos, event);
            if (event.isUseNotif())
                NotifCenter.editPlan(OpenData.mDays.get(day).getDate(), event); // TODO: edit notif
            else
                NotifCenter.removePlan(event);

            return currentPos;
        }

        public static void deleteEvent(int day, int pos) { // special - true
            Event event = getEvents(day).get(pos);
            if (event.isSpecial) {
                SpecialEvent specialEvent = event.specialEventCopy;
                SpecialEvent.removeDateFromList(specialEvent, OpenData.mDays.get(day).getDate());
                OpenData.mDays.get(day).minusBadge();
                BusStation.getBus(1).post('u'); // update badge
                NotifCenter.removeSpecial(specialEvent);
            } else
                NotifCenter.removePlan(event);
            OpenData.mDays.get(day).getEvents().remove(event);
        }

        public static void deleteNormal(int day, int pos) {
            Event event = getEvents(day).get(pos);
            NotifCenter.removePlan(event);
            OpenData.mDays.get(day).getEvents().remove(event);
        }

        public static void addSpecialEvents(SpecialEvent event) {
            event.setDates(Special.sortDates(event.getDates())); // rusiuoja datas
            OpenData.currentMaxNotifId += 10;
            OpenData.mSpecials.add(event);
            NotifCenter.appendSpecial(event);

            for (int i = 0; i < event.getDates().size(); i++) {
                for (int j = 0; j < OpenData.mDays.size(); j++) {
                    String s1 = OpenData.mDays.get(j).getDate(),
                           s2 = event.getDates().get(i).toString();
                    if (s1.equals(s2)) {
                        //OpenData.mDays.get(j).getEvents().add(event.getEvent());
                        getEvents(j).add(findPosition(j, event.getEvent()), event.getEvent()); // galutine ivykio vieta sarase
                        OpenData.mDays.get(j).plusBadge();
                        //sortEvents(j);
                        break;
                    }
                }
            }
            BusStation.getBus(1).post('u'); // update badge
        }

        public static int addSpecialEvent(int day, SpecialEvent event) {
            OpenData.mSpecials.add(event);
            event = OpenData.mSpecials.get(OpenData.mSpecials.size() - 1);
            NotifCenter.appendSpecial(event);
            OpenData.mDays.get(day).plusBadge();
            int pos = findPosition(day, event.getEvent()); // galutine ivykio vieta sarase
            getEvents(day).add(pos, event.getEvent());

            BusStation.getBus(1).post('u'); // update badge
            return pos;
        }

        private static List<Event> getEvents(int day) {
            return OpenData.mDays.get(day).getEvents();
        }

        private static boolean firstTimeIsGreater(Event event1, Event event2) {
            if (event1.getFromHour() == event2.getFromHour())
                return event1.getFromMinutes() > event2.getFromMinutes();
            else
                return event1.getFromHour() > event2.getFromHour();
        }
    }

    public static class Special {

        private static List<DateHolder> sortDates(List<DateHolder> dates) {
            if (dates.size() == 1)
                return dates;
            for (int i = 0; i < dates.size(); i++)
                for (int j = i; j > 0; j--)
                    if (DateHolder.firstBigger(dates.get(j - 1), dates.get(j)) ) {
                        DateHolder temp = dates.get(j);
                        dates.set(j, dates.get(j - 1));
                        dates.set(j - 1, temp);
                    }
            return dates;
        }

        public static void addEvent(List<DateHolder> dates, Event event) {
            OpenData.currentMaxNotifId += 9;
            dates = sortDates(dates);
            SpecialEvent specialEvent = new SpecialEvent(dates, event);
            OpenData.mSpecials.add(specialEvent);
            NotifCenter.appendSpecial(specialEvent); // TODO: special add notif
        }

        public static void editEvent(SpecialEvent event, int pos) {
            OpenData.currentMaxNotifId += 9;
            event.setDates(sortDates(event.getDates()));
            OpenData.mSpecials.set(pos, event);
            if (event.getEvent().useNotif)
                NotifCenter.editSpecial(event); // TODO: special edit notif
            else
                NotifCenter.removeSpecial(event);
        }

        public static void deleteEvent(int pos) {
            NotifCenter.removeSpecial(OpenData.mSpecials.get(pos));
            OpenData.mSpecials.remove(pos);
        }

        public static List<Integer> deleteWithoutDate() {
            List<Integer> list = new ArrayList<>();
            for(int i = OpenData.mSpecials.size() - 1; i >= 0; i--)
                if (OpenData.mSpecials.get(i).getDates().size() == 0) {
                    OpenData.mSpecials.remove(i);
                    list.add(i);
                }
            return list;
        }
    }

}
