package com.armandasalmd.weeklyroutine.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.armandasalmd.weeklyroutine.MainActivity;
import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.adapters.MyPagerAdapter;
import com.armandasalmd.weeklyroutine.classes.BusStation;
import com.armandasalmd.weeklyroutine.classes.CustomViewPager;
import com.armandasalmd.weeklyroutine.classes.Dismissible;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.EventControl;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.RevealAnimationSetting;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;
import com.rahimlis.badgedtablayout.BadgedTabLayout;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentPlans extends Fragment {

    public static int edit_day = -1;
    public static int edit_event = -1;
    public MyPagerAdapter adapter;

    @BindView(R.id.tabs) BadgedTabLayout tabs;
    @BindView(R.id.pager) CustomViewPager pager;
    @BindView(R.id.fabButton) FloatingActionButton mFab;

    FragmentCreatePlan subFrag;
    View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_fragment_plans, container, false);
        ButterKnife.bind(this, mView);
        initTabAndPager();

        return mView;
    }

    private void initTabAndPager() {
        adapter = new MyPagerAdapter(getActivity().getSupportFragmentManager(), OpenData.get7DayNames(getContext()));
        pager.setAdapter(adapter);

        tabs.setupWithViewPager(pager);
        pager.pageSwipeEnabled(false);
        updateTabBadge('u');

        selectTodaysTab();
    }

    public void selectTodaysTab() {
        TabLayout.Tab tab = tabs.getTabAt(OpenData.getWeekDayInt());
        if (tab != null)
            tab.select();
    }

    public FloatingActionButton getFab() {
        return mFab;
    }

    public void closeMenus() {
        adapter.closeCurrentMenu(pager.getCurrentItem());
    }

    @Subscribe
    public void updateTabBadge(Character empty) {
        for (int i = 0; i < OpenData.WEEK_DAY_COUNT; i++)
            if (OpenData.mDays.get(i).getBadgeNum() != 0)
                tabs.setBadgeText(i, Integer.toString(OpenData.mDays.get(i).getBadgeNum()));
            else
                tabs.setBadgeText(i, null);
    }

    @OnClick(R.id.fabButton)
    public void fabClick(View v) {
        closeMenus();
        if (!OpenData.createPlansIsShown) {
            if (edit_event == -1) {
                showDetailFragment();
                BusStation.getBus(0).post(false);
            }
            else {
                showDetailFragment(edit_event);
                BusStation.getBus(0).post(true);
            }
        }
    }

    public void toolbarAddClick() throws ParseException {
        if (subFrag.saveClicked())
            ((MainActivity) getActivity()).createPlanStop();
    }

    @Subscribe
    public void startEditing(OpenData.BusEventHolder values) {
        edit_day = values.getDayId();
        edit_event = values.getEventId();

        mFab.performClick();
    }

    private boolean requestLock = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (requestLock)
            setLocked(true);
        super.onViewCreated(view, savedInstanceState);
    }

    public void setLocked(boolean ans) { // f - atrakint, t - uzrakint
        if (pager == null) {
            requestLock = true;
        } else {
            pager.pageSwipeEnabled(!ans);
            final int currentFrag = pager.getCurrentItem();

            OpenData.lockMode = ans;
            adapter.lockMenus(currentFrag, !ans);
            closeMenus();
            if (currentFrag + 1 != 7) {
                adapter.lockMenus(currentFrag + 1, !ans);
                adapter.closeCurrentMenu(currentFrag + 1);
            }
            if (currentFrag - 1 != -1) {
                adapter.lockMenus(currentFrag - 1, !ans);
                adapter.closeCurrentMenu(currentFrag - 1);
            }
        }

    }

    public void dismissFrag() {
        subFrag.dismiss(new Dismissible.OnDismissedListener() {
            @Override
            public void onDismissed() {
                forseDismiss();
            }
        });
    }

    public void forseDismiss() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(subFrag).commitAllowingStateLoss();
        if ((edit_day & edit_event) != -1) {
            adapter.addEditListener(edit_day, edit_event);
            edit_day = -1;
            edit_event = -1;
        }
    }

    public void updateAfterClean() {
        adapter.updatePagerBefore(pager.getCurrentItem());
        updateTabBadge('n');
        selectTodaysTab();
    }

    private RevealAnimationSetting constructRevealSettings() {
        return RevealAnimationSetting.with(
                (int) (mFab.getX() + mFab.getWidth() / 2),
                (int) (mFab.getY() + mFab.getHeight() / 2),
                mView.getWidth(),
                mView.getHeight());
    }

    public void showDetailFragment() {
        showDetailFragment(-1); // not edit mode (item -1 in edit)
    }

    public void showDetailFragment(int item) {
        if (item == -1)
            subFrag = FragmentCreatePlan.getInstance(pager.getCurrentItem(), item);
        else
            subFrag = FragmentCreatePlan.getInstanceEditMode(pager.getCurrentItem(),
                    item, OpenData.mDays.get(pager.getCurrentItem()).getEvents().get(item));

        subFrag.setListener(new FragmentCreatePlan.WhenSave() {
            @Override
            public void create(List<Integer> integers, Event event) {
                adapter.addNewEvent(integers, event);
            }

            @Override
            public void override(int originDay, int itemId, int selDayId, Event new_event) {
                if (originDay == selDayId)
                    adapter.editEvent(edit_day, edit_event, new_event);
                else
                    adapter.moveEditedEvent(originDay, itemId, selDayId, pager.getCurrentItem(), new_event);
            }

            @Override
            public void transformToSpecial(SpecialEvent event, int originDayId, int selDayId, int eventId) {
                adapter.deleteEvent(originDayId, eventId);
                adapter.addNewSpecial(selDayId, event);
            }

            @Override
            public void createNewSpecials(SpecialEvent event, List<Integer> weekDays) {
                EventControl.Plans.addSpecialEvents(event);

                int current = pager.getCurrentItem();
                if (current != 0 && weekDays.contains(current - 1))
                    adapter.refreshList(current - 1);
                if (weekDays.contains(current))
                    adapter.refreshList(current);
                if (current != 6 && weekDays.contains(current + 1))
                    adapter.refreshList(current + 1);

            }
        });

        Intent intent = new Intent(getContext(), FragmentCreatePlan.class);
        intent.putExtra(FragmentCreatePlan.SETTINGS, constructRevealSettings());

        subFrag.setArguments(intent.getExtras());
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.top_content, subFrag, getString(R.string.createPlan))
                .commit();
    }

    public void showTutoExtraInfoAndOpenMenu() { // guide metu parodomas event more info
        adapter.showExtraInfoAndOpenMenu(pager.getCurrentItem(), 0);
    }

    public void makeAllDone(boolean done) {
        List<Event> list = OpenData.mDays.get(pager.getCurrentItem()).getEvents();
        for (Event ev : list)
            ev.setDone(done);
        adapter.refreshList(pager.getCurrentItem());
    }

    public void afterWipe() {
        int current = pager.getCurrentItem();
        switch (current) {
            case 0: adapter.afterWipe(0);
            adapter.afterWipe(1);
            break;
            case 6: adapter.afterWipe(6);
            adapter.afterWipe(5);
            break;
            default: adapter.afterWipe(current);
            adapter.afterWipe(current - 1);
            adapter.afterWipe(current + 1);
            break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BusStation.getBus(1).unregister(this);
        OpenData.lockMode = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusStation.getBus(1).register(this);
        ((MainActivity)getActivity()).sayReady(0);
    }
}