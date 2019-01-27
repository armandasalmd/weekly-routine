package com.armandasalmd.weeklyroutine.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.armandasalmd.weeklyroutine.ListViewFragment;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends FragmentStatePagerAdapter {

    private String[] mTitles;
    private List<ListViewFragment> mFragments;

    public MyPagerAdapter(FragmentManager manager, String[] dayNames) {
        super(manager);
        mTitles = dayNames;
        mFragments = new ArrayList<>();
        for (int i = 0; i < mTitles.length; i++) {
            ListViewFragment fragment = ListViewFragment.newInstance(i);
            mFragments.add(fragment);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    public void closeCurrentMenu(int position) {
        mFragments.get(position).closeMenu();
    }

    public void lockMenus(int position, boolean lock) {
        mFragments.get(position).lockMenus(lock);
    }

    public void addNewEvent(List<Integer> day_nums, Event event) {
        for (int day_num: day_nums) {
            mFragments.get(day_num).addAndSortEvent(event);
        }
    }

    public void editEvent(int day_id, int event_id, Event event) {
        mFragments.get(day_id).editEvent(event_id, event);
    }

    public void moveEditedEvent(int originDay, int itemId, int selDayId, int pagerPos, Event new_event) {
        mFragments.get(originDay).deleteEvent(itemId);
        mFragments.get(selDayId).addAndSortEvent(new_event);
    }

    public void refreshList(int day) {
        mFragments.get(day).dataSetChanged();
    }

    public void addEditListener(int day, int position) {
        mFragments.get(day).notifyAdapter(position);
    }

    public void updatePagerBefore(int today) {
        if (today > 0)
            mFragments.get(today - 1).afterCleanUp();
    }

    public void tutoEnd(int day) {
        mFragments.get(day).endSample();
    }

    public void addSampleItems(int day) {
        mFragments.get(day).startSample();
    }

    public void get2Views(int day) {
        mFragments.get(day).get2Rows();
    }

    public void showExtraInfoAndOpenMenu(int day, int position) { // kai naudoji guide
        mFragments.get(day).showExtendedInfo(position);
        mFragments.get(day).openMenu(OpenData.mDays.get(day).getEvents().get(position).getNotificationId());
    }

    public void deleteEvent(int day, int position) {
        mFragments.get(day).deleteNormal(position);
    }

    public void addNewSpecial(int day, SpecialEvent event) {
        mFragments.get(day).addSpecial(event);
    }

    public void afterWipe(int day) {
        mFragments.get(day).afterWipe();
    }

}
