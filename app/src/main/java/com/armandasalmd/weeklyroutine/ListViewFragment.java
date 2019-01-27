package com.armandasalmd.weeklyroutine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.armandasalmd.weeklyroutine.adapters.SmartestAdapter;
import com.armandasalmd.weeklyroutine.classes.BusStation;
import com.armandasalmd.weeklyroutine.classes.DateHolder;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.EventControl;
import com.armandasalmd.weeklyroutine.classes.ExtraDataHolder;
import com.armandasalmd.weeklyroutine.classes.ItemListener;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class ListViewFragment extends Fragment implements ItemListener {

    @BindView(R.id.free_day_layout) LinearLayout freeDayLayout;
    @BindView(R.id.recView) RecyclerView recyclerView;

    public int fragId = 0;
    private SmartestAdapter mAdapter;

    private List<Event> getEvents() { return OpenData.mDays.get(fragId).getEvents(); }

    public static ListViewFragment newInstance(int position) {
        ListViewFragment f = new ListViewFragment();
        f.fragId = position;
        return f;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (OpenData.tutoMode == fragId)
            ((MainActivity)getActivity()).sayReady(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.tab_fragment_layout, container, false);
        ButterKnife.bind(this, mView);

        chooseFreeDay(OpenData.mDays.get(fragId).getEvents().size() == 0);
        //EventControl.Plans.sortEvents(fragId);
        setUpRecycler();
        lockMenus(false);

        return mView;
    }

    private void chooseFreeDay(boolean freeDay) {
        if (freeDay) {
            recyclerView.setVisibility(View.GONE);
            freeDayLayout.setVisibility(View.VISIBLE);
        }
        else {
            freeDayLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void closeMenu() {
        mAdapter.closeMenus();
    }

    public void lockMenus(boolean lock) {
        mAdapter.setLockSwipe(lock);
    }

    public void openMenu(int notifId) {
        mAdapter.openMenu(notifId);
    }

    public void notifyAdapter(int item_position) {
        mAdapter.notifyItemChanged(item_position);
    }

    private void setUpRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SmartestAdapter(getContext(), fragId, this);
        recyclerView.setAdapter(mAdapter);
    }

    public void showExtendedInfo(int position) {
        if (!OpenData.infoShown) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            String date = OpenData.mDays.get(fragId).getDate();

            Event event = OpenData.mDays.get(fragId).getEvents().get(position);
            ExtraDataHolder holder = new ExtraDataHolder(event.getTitle(), event.getDescription(), R.string.plans, event.useTimeTo);
            holder.setDate(date);
            holder.setTime(new int[] {event.getFromHour(), event.getFromMinutes(),
                    event.getToHour(), event.getToMinutes()});
            holder.setDuration(event.getDuration());

            ExtraInfoFragment mFragAbout = ExtraInfoFragment.getInstance(holder);
            mFragAbout.show(fragmentManager, "info");
        }
    }

    public void addAndSortEvent (Event event) {
        int insertPos = EventControl.Plans.addEvent(fragId, event);
        if (this.isResumed()) {
            mAdapter.notifyItemInserted(insertPos);
            if (getEvents().size() == 1)
                chooseFreeDay(false);
            else
                recyclerView.scrollToPosition(insertPos);
        }
    }

    public void editEvent(int event_id, Event event) {
        int destination = EventControl.Plans.editEvent(fragId, event_id, event);
        if (this.isResumed()) {
            //mAdapter.notifyItemChanged(event_id); //jis ir perkeliamas keicias + subugina (GONE,VISIBLE)
            if (event_id != destination) {
                mAdapter.notifyItemMoved(event_id, destination);
                mAdapter.notifyItemChanged(destination);
            }
            recyclerView.scrollToPosition(destination);
        }
    }

    public void dataSetChanged() {
        if (isResumed()) {
            if (getEvents().size() == 1)
                chooseFreeDay(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void afterCleanUp() {
        // poto kai buna isjungta programa ir iterpia specials i sarasa, reikia paupdatint adapterius
        if (getEvents().size() == 0)
            chooseFreeDay(true);
        else if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemLongClick(int position) {
        showExtendedInfo(position);
    }

    @Override
    public void onDelete(int position) {
        EventControl.Plans.deleteEvent(fragId, position);

        mAdapter.deleteItem(position);
        if (mAdapter.getItemCount() == 1) // footer
            chooseFreeDay(true);
    }

    @Override
    public void onEdit(int position) {
        if (getEvents().get(position).isSpecial) {
            Toasty.info(getContext(), "Redaguoti galima „" + getString(R.string.specialEvents) + "“ skiltyje", Toast.LENGTH_LONG).show();
        } else // TODO: pranesti apie edit pasirinkima
            BusStation.getBus(1).post(new OpenData.BusEventHolder(fragId, position));
    }

    public void startSample() {
        OpenData.tutoSave = OpenData.mDays.get(fragId).getEvents();
        if (OpenData.tutoSave.size() == 0)
            chooseFreeDay(false);

        Event sample1 = new Event(12, 0, 14, 45, getString(R.string.edit_text_title), getString(R.string.edit_text_description));
        sample1.setUseNotif(false);
        sample1.useTimeTo = true;

        List<Event> events = new ArrayList<>();
        events.add(sample1);

        List<DateHolder> dates = new ArrayList<>();
        dates.add(new DateHolder(2017, 1, 1));

        Event sample2e = new Event(14, 15, 17, 10, getString(R.string.edit_text_title), getString(R.string.edit_text_description));
        sample1.setUseNotif(false);
        sample1.useTimeTo = true;

        SpecialEvent sample2 = new SpecialEvent(dates, sample2e);
        events.add(sample2.getEvent());

        OpenData.mDays.get(fragId).setEvents(events);
        mAdapter.notifyDataSetChanged();

        BusStation.getBus(1).post('a');
    }

    public void get2Rows() {
        final View[] views = new View[2];
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recyclerView.findViewHolderForAdapterPosition(0) != null) {
                    views[0] = recyclerView.findViewHolderForAdapterPosition(0).itemView;
                    views[1] = recyclerView.findViewHolderForAdapterPosition(1).itemView;
                    ((MainActivity)getActivity()).rowsReady(views);
                }
            }
        }, 150);
    }

    public void endSample() {
        if (OpenData.tutoSave.size() == 0)
            chooseFreeDay(true);
        OpenData.mDays.get(fragId).setEvents(OpenData.tutoSave);
        mAdapter.notifyDataSetChanged();
        OpenData.tutoViews.add(recyclerView.getChildAt(0));
        OpenData.tutoViews.add(recyclerView.getChildAt(1));
    }

    public void deleteEvent(int position) {
        EventControl.Plans.deleteEvent(fragId, position);
        mAdapter.notifyItemRemoved(position);
        if (getEvents().size() == 0)
            chooseFreeDay(true);
    }

    public void deleteNormal(int position) {
        EventControl.Plans.deleteNormal(fragId, position);
        mAdapter.notifyItemRemoved(position);
        if (getEvents().size() == 0)
            chooseFreeDay(true);
    }

    public void addSpecial(SpecialEvent specialEvent) {
        int pos = EventControl.Plans.addSpecialEvent(fragId, specialEvent);
        if (isResumed() && mAdapter != null) {
            if (OpenData.mDays.get(fragId).getEvents().size() == 1)
                chooseFreeDay(false);
            mAdapter.notifyItemInserted(pos);
        }
    }

    public void afterWipe() {
        mAdapter.notifyDataSetChanged();
        if (OpenData.mDays.get(fragId).getEvents().size() == 0)
            chooseFreeDay(true);
    }
}
