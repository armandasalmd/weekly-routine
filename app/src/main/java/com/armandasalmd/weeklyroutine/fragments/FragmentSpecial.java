package com.armandasalmd.weeklyroutine.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.armandasalmd.weeklyroutine.MainActivity;
import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.adapters.SpecialPagerAdapter;
import com.armandasalmd.weeklyroutine.classes.BusStation;
import com.armandasalmd.weeklyroutine.classes.CustomViewPager;
import com.armandasalmd.weeklyroutine.classes.EventControl;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SortHelper;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentSpecial extends Fragment {

    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.viewpager) CustomViewPager viewPager;
    public FragmentSpecialList firstTab;
    private FragmentCreateSpecial secondTab;
    private SpecialPagerAdapter adapter;
    private int editId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_fragment_special, container, false);
        ButterKnife.bind(this, mView);

        firstTab = new FragmentSpecialList();
        secondTab = new FragmentCreateSpecial();
        initViewPager();

        return mView;
    }

    private void initViewPager() {
        adapter = new SpecialPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(firstTab, getResources().getString(R.string.special_list));
        adapter.addFragment(secondTab, getResources().getString(R.string.special_create));
        viewPager.setAdapter(adapter);
        viewPager.pageSwipeEnabled(false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override public void onPageScrollStateChanged(int state) { }
            @Override public void onPageSelected(int position) {
                if (position == 0) {
                    secondTab.mMenu.close(false);
                    secondTab.makeLikeNew();
                    cancel(secondTab.edit_mode);
                    ((MainActivity)getActivity()).muteOptions(false);
                } else {
                    secondTab.thisTabClicked();
                    ((MainActivity)getActivity()).muteOptions(true);
                }
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    private void cancel(boolean editMode) {
        if (editMode) {
            secondTab.editStop();
            firstTab.setEditListener(editId);
        }
        adapter.changeSecondTitle(getString(R.string.special_create));
    }

    public void removeSecondTabFocus() {
        secondTab.removeETFocus();
        secondTab.mMenu.close(false);
    }

    public void closeMenus() {
        firstTab.mAdapter.closeMenus();
    }

    public void removeEventsWithoutDate() {
        List<Integer> deletedIds = EventControl.Special.deleteWithoutDate(); // trina tik opendata info
        // buvo bug kai nuo kito galo pradedavau. pvz [0]:3id, [1]:1id, [2]:0id
        for (int i = 0; i < deletedIds.size(); i++)
            firstTab.notidyRemoved(deletedIds.get(i));
    }

    public void requestToSort(int sortMode) {
        OpenData.mSpecials = SortHelper.sortSpecial(OpenData.mSpecials, sortMode);
        firstTab.notifyDataChanged();
    }

    @Subscribe
    public void tabsListener(OpenData.BusSpecialHolder info) {
        switch (info.getAction()) {
            case OpenData.FIRST_ITEM:
                firstTab.showListHideFreeDay();
            case OpenData.ADD_NEW:
                firstTab.mAdapter.notifyDataSetChanged();
                break;
            case OpenData.CANCEL_EDIT:
                Log.i("Armandas", "tabsListener: ");
                viewPager.setCurrentItem(0);
                cancel(info.edit);
                break;
            case OpenData.START_EDIT:
                adapter.changeSecondTitle(getString(R.string.edit));
                editId = info.getPosition(); // tam kad veliau galeciau edit listener uzdeti jam
                viewPager.setCurrentItem(1);
                secondTab.editStart(info.getPosition());
                break;
            case OpenData.STOP_EDIT:
                viewPager.setCurrentItem(0);
                secondTab.editStop();
                firstTab.mAdapter.notifyItemChanged(info.getPosition());
                firstTab.itemChanged(info.getPosition());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusStation.getBus(2).register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusStation.getBus(2).unregister(this);
    }
}
