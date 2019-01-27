package com.armandasalmd.weeklyroutine.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.armandasalmd.weeklyroutine.ExtraInfoFragment;
import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.adapters.SpecialListAdapter;
import com.armandasalmd.weeklyroutine.classes.BusStation;
import com.armandasalmd.weeklyroutine.classes.EventControl;
import com.armandasalmd.weeklyroutine.classes.ExtraDataHolder;
import com.armandasalmd.weeklyroutine.classes.ItemListener;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;
import com.armandasalmd.weeklyroutine.helper.OnStartDragListener;
import com.armandasalmd.weeklyroutine.helper.SimpleItemTouchHelperCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentSpecialList extends Fragment implements ItemListener, OnStartDragListener {

    @BindView(R.id.recView) RecyclerView mList;
    @BindView(R.id.free_day_layout) LinearLayout freeDayLayout;
    public SpecialListAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.tab_fragment_layout, container, false);
            ButterKnife.bind(this, mView);
            setFreeDay(OpenData.mSpecials.size() == 0); // footer
            initRecycler();
        }
        return mView;
    }

    private void initRecycler() {
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SpecialListAdapter(getContext(), this, this);
        mList.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mList);
    }

    public void showListHideFreeDay() {
        setFreeDay(false);
    }

    private void setFreeDay(boolean isFreeDay) {
        if (isFreeDay) {
            TextView tv = freeDayLayout.findViewById(R.id.free_day_text);
            tv.setText(getString(R.string.no_special_events));
            mList.setVisibility(View.INVISIBLE);
            freeDayLayout.setVisibility(View.VISIBLE);
        } else {
            freeDayLayout.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        }
    }

    public void itemChanged(int position) {
        //mAdapter.notifyItemChanged(position);
        mAdapter.notifyDataSetChanged();
    }

    public void setEditListener(int pos) {
        try {
            ((SpecialListAdapter.SpecialHolder)mList.findViewHolderForAdapterPosition(pos)).bindEdit();
        } catch (Exception e) {
            Log.i("Armandas", "onEdit: viewholder null");
        }
    }

    public void notidyRemoved(int i) {
        mAdapter.notifyItemRemoved(i);
    }

    public void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public void showExtendedInfo(int position) {
        if (!OpenData.infoShown) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            SpecialEvent event = OpenData.mSpecials.get(position);
            ExtraDataHolder holder = new ExtraDataHolder(event.getEvent().getTitle(),
                    event.getEvent().getDescription(), R.string.specialEvents, event.getEvent().useTimeTo);
            holder.setDates(event.getDates());

            holder.setTime(new int[] {event.getEvent().getFromHour(), event.getEvent().getFromMinutes(),
                    event.getEvent().getToHour(), event.getEvent().getToMinutes()});
            holder.setDuration(event.getEvent().getDuration());

            ExtraInfoFragment mFragAbout = ExtraInfoFragment.getInstance(holder);
            mFragAbout.show(fragmentManager, "info");
        }
    }

    @Override
    public void onItemLongClick(int position) {
        showExtendedInfo(position);
    }

    @Override
    public void onDelete(int position) {
        EventControl.Special.deleteEvent(position);
        mAdapter.deleteItem(position);
        if (mAdapter.getItemCount() == 1) // footer
            setFreeDay(true);
    }

    @Override
    public void onEdit(int position) {
        BusStation.getBus(2).post(new OpenData.BusSpecialHolder(OpenData.START_EDIT, position));
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void afterWipe() {
        mAdapter.notifyDataSetChanged();
        setFreeDay(true);
    }
}
