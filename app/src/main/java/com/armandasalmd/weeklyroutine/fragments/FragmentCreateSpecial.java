package com.armandasalmd.weeklyroutine.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.adapters.HoriAdapter;
import com.armandasalmd.weeklyroutine.classes.BusStation;
import com.armandasalmd.weeklyroutine.classes.DateHolder;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.EventControl;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class FragmentCreateSpecial extends Fragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    public boolean edit_mode = false;
    public SpecialEvent thisEvent;
    public int edit_id = -1;
    private HoriAdapter mAdapter;
    private View mView;
    private int[] selDate = new int[3];
    private Unregistrar unreg;
    private int[] times = new int[] {-1, -1, -1, -1};
    private static final int TIMEFROM = 0, TIMETO = 1;
    private int timeDialogId = -1;

    @BindView(R.id.recyclerHorizontalView) RecyclerView mRecycler;
    @BindView(R.id.event_title) TextView mTit;
    @BindView(R.id.event_description) TextView mDec;
    @BindView(R.id.calendar) MaterialCalendarView mCalendar;
    @BindView(R.id.dummy_layout) LinearLayout dummy;
    @BindView(R.id.fab_add) FloatingActionButton fAdd;
    @BindView(R.id.fab_cancel) FloatingActionButton fCancel;
    @BindView(R.id.floatingMenu) FloatingActionMenu mMenu;

    @BindView(R.id.button_time_from) Button bFrom;
    @BindView(R.id.button_time_to) Button bTo;
    @BindView(R.id.time_to_check) CheckBox timeTo;
    @BindView(R.id.use_notification) CheckBox useNotif;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_fragment_create_special, container, false);
            ButterKnife.bind(this, mView);

            initRecycler();
            initFab();

            mCalendar.state().edit().setFirstDayOfWeek(Calendar.MONDAY)
                    .setMinimumDate(new Date(System.currentTimeMillis() - 1000))
                    .commit();
            mCalendar.setWeekDayLabels(R.array.short_week_names);
            mCalendar.setTitleMonths(R.array.month_names);
            mCalendar.setSelectedDate(new Date(System.currentTimeMillis()));

            bFrom.setOnClickListener(this);
            bTo.setOnClickListener(this);
            mMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
                @Override
                public void onMenuToggle(boolean opened) {
                    mMenu.setClickable(opened);
                }
            });
        }
        return mView;
    }

    @OnClick(R.id.addButton)
    public void buttonAddClick(View v) {
        CalendarDay day = mCalendar.getSelectedDate();
        if (day != null) {
            changeDate(day.getYear(), day.getMonth() + 1, day.getDay());
            addNewDate();
        } else
            Log.i("Armandas", "buttonAddClick: no date selected");
    }

    @Override
    public void onClick(View v) {
        TimePickerDialog timeDialog;
        switch (v.getId()) {
            case R.id.button_time_from:
                if (timeDialogId == -1) { // save for double click
                    timeDialog = new TimePickerDialog(
                            getActivity(), this, times[0], times[1], true);
                    timeDialogId = TIMEFROM;
                    timeDialog.show();
                }
                break;
            case R.id.button_time_to:
                if (timeDialogId == -1) {
                    timeDialog = new TimePickerDialog(
                            getActivity(), this, times[2], times[3], true);
                    timeDialogId = TIMETO;
                    timeDialog.show();
                }
                break;
            case R.id.fab_add:
                addClicked();
                break;
            case R.id.fab_erase:
                makeLikeNew();
                setTimeFrom();
                break;
            case R.id.fab_cancel:
                BusStation.getBus(2).post(new OpenData.BusSpecialHolder(OpenData.CANCEL_EDIT, edit_mode));
                break;
        }
    }

    private void initRecycler() {
        LinearLayoutManager layoutMan = new LinearLayoutManager(getContext());
        layoutMan.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecycler.setLayoutManager(layoutMan);
        mAdapter = new HoriAdapter();
        mRecycler.setAdapter(mAdapter);
    }

    private void initFab() {
        fAdd.setOnClickListener(this);
        fCancel.setOnClickListener(this);
        mView.findViewById(R.id.fab_erase).setOnClickListener(this);
    }

    private void changeDate(int y, int m, int d) {
        selDate[0] = y;
        selDate[1] = m;
        selDate[2] = d;
    }

    private void addNewDate() {
        DateHolder date = new DateHolder(selDate[0], selDate[1], selDate[2]);
        if (SpecialEvent.currentlyExist(mAdapter.getDates(), date))
            Toasty.error(getContext(), getString(R.string.date_exits), Toast.LENGTH_SHORT).show();
        else if (mAdapter.getItemCount() >= 10)
            Toasty.error(getContext(), getString(R.string.dates_limit), Toast.LENGTH_SHORT).show();
        else
            mAdapter.addItem(date);
        mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private boolean addNew() {
        boolean statusOK = fieldsMeetsRules();
        if (statusOK)
            EventControl.Special.addEvent(mAdapter.getDates(), collectInfoEvent());
        return statusOK;
    }

    private boolean overrideOld(int position) {
        boolean statusOK = fieldsMeetsRules();
        if (statusOK)
            EventControl.Special.editEvent(collectInfo(), position);
        return statusOK;
    }

    private SpecialEvent collectInfo() {
        if (edit_id != -1) {
            SpecialEvent event = OpenData.mSpecials.get(edit_id);
            event.getEvent().setTimes(times[0], times[1], times[2], times[3]);
            event.setDates(mAdapter.getDates());
            event.getEvent().setTitle(mTit.getText().toString());
            event.getEvent().setDescription(mDec.getText().toString());
            event.getEvent().useTimeTo = timeTo.isChecked();
            event.getEvent().setUseNotif(useNotif.isChecked());
            return event;
        } else return new SpecialEvent(mAdapter.getDates(), new Event());
    }

    private Event collectInfoEvent() {
        Event event = new Event();
        event.setTimes(times[0], times[1], times[2], times[3]);
        event.setTitle(mTit.getText().toString());
        event.setDescription(mDec.getText().toString());
        event.useTimeTo = timeTo.isChecked();
        event.setUseNotif(useNotif.isChecked());
        return event;
    }

    private boolean fieldsMeetsRules() {
        if (mTit.getText().toString().isEmpty()) {
            Toasty.error(getContext(), getString(R.string.blank_title), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mAdapter.getDates().size() == 0) {
            Toasty.error(getContext(), getString(R.string.horizontal_list_error), Toast.LENGTH_SHORT).show();
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                day = calendar.get(Calendar.DAY_OF_MONTH);
        if (selDate[2] == day && selDate[1] == month && selDate[0] == year)
            return checkTimePast();
        return true;
    }

    private boolean checkTimePast() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if ((times[0] == hour && times[1] < minute) || times[0] < hour) {
            Toasty.error(getContext(), getString(R.string.time_in_past), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void addClicked() {
        if (edit_mode) {
            if (overrideOld(edit_id)) {
                Toasty.success(getContext(), getString(R.string.save_success)).show();
                BusStation.getBus(2).post(new OpenData.BusSpecialHolder(OpenData.STOP_EDIT));
                makeLikeNew();
            }
        } else {
            if (addNew()) {
                Toasty.success(getContext(), getString(R.string.add_success)).show();
                if (OpenData.mSpecials.size() == 1) // footer
                    BusStation.getBus(2).post(new OpenData.BusSpecialHolder(OpenData.FIRST_ITEM));
                else
                    BusStation.getBus(2).post(new OpenData.BusSpecialHolder(OpenData.ADD_NEW));
                makeLikeNew();
            }
        }
    }

    public void makeLikeNew() {
        ScrollView sv = mView.findViewById(R.id.create_scroll_view);
        sv.scrollTo(0, 0);
        clearInfo();
        removeETFocus();
    }

    public void clearInfo() {
        mTit.setText(null);
        mTit.setError(null);
        mDec.setText(null);
        mAdapter.clearList();

        mCalendar.setSelectedDate(new Date(System.currentTimeMillis()));
        //mCalendar.setDate(System.currentTimeMillis(), false, true);

        times[2] = -1;
        times[3] = -1;
        useNotif.setChecked(true);
        timeTo.setChecked(false);
        bTo.setText(getString(R.string.to));
        bTo.setEnabled(false);
        setTimeFrom();
        setTimeLabels();
    }

    public void thisTabClicked() {
        setTimeFrom();
        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.key_time_to), false))
            timeTo.setChecked(true);
    }

    public void removeETFocus() {
        dummy.requestFocus();
        mMenu.close(true);
        closeKeyboard();
    }

    private void closeKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        assert getView() != null;
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void editStart(int position) {
        edit_mode = true;
        changeFabButtons(true);
        loadInfo(OpenData.mSpecials.get(position));
        edit_id = position;
    }

    public void editStop() {
        edit_mode = false;
        changeFabButtons(false);
        clearInfo();
        removeETFocus();
        edit_id = -1;
        mAdapter.clearList();
    }

    private void loadInfo(SpecialEvent specialEvent) {
        thisEvent = specialEvent;
        mTit.setText(thisEvent.getEvent().getTitle());
        mDec.setText(thisEvent.getEvent().getDescription());

        Event event = thisEvent.getEvent();
        times[0] = event.getFromHour();
        times[1] = event.getFromMinutes();
        if (thisEvent.getEvent().useTimeTo) {
            times[2] = event.getToHour();
            times[3] = event.getToMinutes();
            timeTo.setChecked(true);
        } else
            timeTo.setChecked(false);
        setTimeLabels();
        useNotif.setChecked(event.isUseNotif());
        mAdapter.setDateList(cloneDates(thisEvent.getDates()));
        mCalendar.setSelectedDate(new Date(System.currentTimeMillis()));
    }

    private List<DateHolder> cloneDates(List<DateHolder> list) {
        List<DateHolder> copied = new ArrayList<>();
        copied.addAll(list);
        return copied;
    }

    private void changeFabButtons(boolean inEdit) {
        if (inEdit) {
            fAdd.setImageResource(R.drawable.ic_save);
            fAdd.setLabelText(getString(R.string.edit));
        } else {
            fAdd.setImageResource(R.drawable.ic_plus);
            fAdd.setLabelText(getString(R.string.add));
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (timeDialogId == TIMEFROM) {
            times[0] = hourOfDay;
            times[1] = minute;
            if (times[2] != -1 && !timeFromIsGreater(hourOfDay, minute, times[2], times[3]))
                setTimeToAsFrom();
        } else if (timeDialogId == TIMETO) {
            times[2] = hourOfDay;
            times[3] = minute;
            if (!timeFromIsGreater(times[0], times[1], hourOfDay, minute)) {
                times[0] = times[2];
                times[1] = times[3] - 1;
                if (minute == -1) {
                    times[0]--;
                    times[1] = 59;
                }
            }
        }
        setTimeLabels();
        timeDialogId = -1;
    }

    private void setTimeLabels() {
        bFrom.setText(String.format("%s (%s)", getString(R.string.from), Event.formatTime(times[0], times[1])));
        if (bTo.isEnabled())
            bTo.setText(String.format("%s (%s)", getString(R.string.to), Event.formatTime(times[2], times[3])));
    }

    private void setTimeFrom() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        String hour = sdf.format(new Date());
        times[0] = Integer.parseInt(hour);
        times[1] = Calendar.getInstance().get(Calendar.MINUTE);
        if (getResources() != null)
            bFrom.setText(String.format("%s (%s)", getResources().getString(R.string.from), Event.formatTime(times[0], times[1])));
    }

    private boolean timeFromIsGreater(int fHour, int fMinute, int tHour, int tMinute) {
        if (fHour == tHour)
            return fMinute < tMinute;
        else
            return fHour < tHour;
    }

    private void setTimeToAsFrom() {
        if (timeTo.isChecked()) {
            times[2] = times[0];
            times[3] = times[1] + 1;
            if (times[3] == 60) {
                times[2]++;
                times[3] = 0;
            }
            bTo.setText(String.format("%s (%s)", getString(R.string.to), Event.formatTime(times[2], times[3])));
        }
    }

    @OnCheckedChanged(R.id.time_to_check)
    public void timeToCheck(boolean isChecked) {
        bTo.setEnabled(isChecked);
        if (isChecked) {
            //if (times[2] == -1 && times[3] == -1)
            setTimeToAsFrom();
            //else
            //bTo.setText(String.format("%s (%s)", getString(R.string.to), Event.formatTime(times[2], times[3])));
        }
        else {
            bTo.setText(getString(R.string.to));
            times[2] = -1;
            times[3] = -1;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unreg.unregister();
        unreg = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        unreg = KeyboardVisibilityEvent.registerEventListener(getActivity(), new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (!isOpen)
                    clearETFocus();
            }
        });
    }

    private void clearETFocus() {
        dummy.requestFocus();
    }
}