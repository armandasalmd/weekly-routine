package com.armandasalmd.weeklyroutine.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.ToggleButtonsManager;
import com.armandasalmd.weeklyroutine.classes.AnimationUtils;
import com.armandasalmd.weeklyroutine.classes.DateHolder;
import com.armandasalmd.weeklyroutine.classes.Dismissible;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.RevealAnimationSetting;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;
import com.rengwuxian.materialedittext.MaterialEditText;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class FragmentCreatePlan extends Fragment implements Dismissible, View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    interface WhenSave {
        void create(List<Integer> integers, Event event);
        void override(int originDay, int itemId, int selDayId, Event new_event);
        void transformToSpecial(SpecialEvent event, int originDayId, int selDayId, int eventId);
        void createNewSpecials(SpecialEvent event, List<Integer> weekDays);
    }
    private WhenSave listener;
    public void setListener(WhenSave listener) {
        this.listener = listener;
    }

    private static final int TIMEFROM = 0, TIMETO = 1;
    private int timeDialogId = -1;

    private int[] times = new int[] {-1, -1, -1, -1};
    public boolean edit_mode = false, anim = true;
    public static final String SETTINGS = "parseleble";
    private int dayId, itemId, pickerDay = -1; // if itemId -1 -> edit = false
    private Unregistrar unreg;
    private View mView;
    private Event infoEvent;

    @BindView(R.id.dummy_layout) LinearLayout dummy;
    @BindView(R.id.day_select_layout) LinearLayout daySelectLayout;
    @BindView(R.id.toogle_layout) ToggleButtonsManager mToogleButtons;
    @BindView(R.id.event_title) MaterialEditText editTitle;
    @BindView(R.id.event_description) MaterialEditText editDec;
    @BindView(R.id.text_day_name) TextView tv;
    @BindView(R.id.time_to_check) CheckBox timeTo;
    @BindView(R.id.use_notification) CheckBox useNotif;
    @BindView(R.id.like_special) CheckBox useAsSpecial;
    @BindView(R.id.button_time_from) Button bFrom;
    @BindView(R.id.button_time_to) Button bTo;

    public static FragmentCreatePlan getInstance(int dayId, int itemId) {
        FragmentCreatePlan f = new FragmentCreatePlan();
        f.dayId = dayId;
        f.itemId = itemId;
        return f;
    }

    public static FragmentCreatePlan getInstanceEditMode(int dayId, int itemId, Event event) {
        FragmentCreatePlan f = getInstance(dayId, itemId);
        f.edit_mode = true;
        f.infoEvent = event;
        f.pickerDay = dayId;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_fragment_create_plan, container, false);
            ButterKnife.bind(this, mView);

            anim = getPref(R.string.key_anim, true);
            AnimationUtils.registerCreateShareLinkCircularRevealAnimation(getContext(), mView,
                    (RevealAnimationSetting) getArguments().getParcelable(SETTINGS), anim);

            mToogleButtons.setUp(OpenData.get7DayNames(getContext()));
            mToogleButtons.checkButton(dayId, true);
            bFrom.setOnClickListener(this);
            bTo.setOnClickListener(this);

            if (edit_mode) {
                startEditing();
                setSheetInfo(infoEvent);
            } else {
                setTimeFrom();
                if (getPref(R.string.key_time_to, false))
                    timeTo.setChecked(true);
            }
        }

        return mView;
    }

    private boolean getPref(int id, boolean def) {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(id), def);
    }

    @OnCheckedChanged(R.id.checkBox_this_day)
    public void thisDayCheck(boolean isChecked) {
        mToogleButtons.checkButton(dayId, isChecked);
    }

    @OnCheckedChanged(R.id.time_to_check)
    public void timeToCheck(boolean isChecked) {
        bTo.setEnabled(isChecked);
        if (isChecked)
            if (times[2] == -1 && times[3] == -1)
                setTimeToAsFrom();
            else
                bTo.setText(String.format("%s (%s)", getString(R.string.to), Event.formatTime(times[2], times[3])));
        else {
            bTo.setText(getString(R.string.to));
            times[2] = -1;
            times[3] = -1;
        }
    }

    private void setDaySelectSingle() {
        mView.findViewById(R.id.const_day).setVisibility(View.VISIBLE);
        daySelectLayout.setVisibility(View.GONE);
        tv.setText(OpenData.get7DayNames(getContext())[dayId]);
    }

    private int dialog_sel;
    @OnClick(R.id.dialog_for_day)
    public void pickOneDay(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.choose_day));

        dialog_sel = pickerDay;
        builder.setSingleChoiceItems(OpenData.get7DayNames(getContext()), pickerDay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_sel = which;
            }
        });
        builder.setPositiveButton(getString(R.string.choose), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pickerDay = dialog_sel;
                tv.setText(OpenData.get7DayNames(getContext())[pickerDay]);
            }
        });
        builder.setNegativeButton(getString(R.string.action_cancel), null);
        builder.create().show();
    }

    public boolean saveClicked() throws ParseException {
        if (editTitle.getText().toString().trim().equalsIgnoreCase("")) {
            editTitle.setError(getString(R.string.blank_title));
            return false;
        } else {
            if (itemId == -1 && mToogleButtons.getSelectedIds().size() == 0) { // not edit mode(create mode)
                Toasty.warning(getContext(), getString(R.string.no_days_selected)).show();
                return false;
            }
            if (infoEvent == null)
                infoEvent = new Event();
            editTitle.setError(null);
            infoEvent.setTitle(editTitle.getText().toString());
            infoEvent.setDescription(editDec.getText().toString());
            infoEvent.setTimes(times[0], times[1], times[2], times[3]);
            infoEvent.useTimeTo = timeTo.isChecked();
            infoEvent.setUseNotif(useNotif.isChecked());
            infoEvent.isSpecial = useAsSpecial.isChecked();

            if (!edit_mode) { // naujas
                if (!useAsSpecial.isChecked()) { // naujas paprastas
                    listener.create(mToogleButtons.getSelectedIds(), infoEvent);
                } else  { // naujas specialus
                    List<DateHolder> list = new ArrayList<>(); // gali buti 1-7 datu
                    List<Integer> selDays = mToogleButtons.getSelectedIds();
                    try {
                        for (int day : selDays)
                            list.add(OpenData.dateStringToHolder(OpenData.mDays.get(day).getDate()));
                    } catch (Exception e) {
                        Log.i("Armandas", "saveClicked: date parse error");
                    }
                    listener.createNewSpecials(new SpecialEvent(list, infoEvent), selDays);
                }
            } else { // paredaguotas
                if (!useAsSpecial.isChecked()) { // redaguojamas paprastas
                    listener.override(dayId, itemId, pickerDay, infoEvent);
                } else  { // redaguojamas specialus
                    List<DateHolder> list = new ArrayList<>(); // edit mode galima tik 1 data
                    try {
                        list.add(OpenData.dateStringToHolder(OpenData.mDays.get(pickerDay).getDate()));
                    } catch (Exception e) {
                        Log.i("Armandas", "saveClicked: date parse error");
                    }
                    listener.transformToSpecial(new SpecialEvent(list, infoEvent), dayId, pickerDay, itemId);
                }
            }
        }

        return true;
    }

    private void clearETFocus() {
        dummy.requestFocus();
    }

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }

    @Override
    public void dismiss(final OnDismissedListener listener) {
        AnimationUtils.startCreateShareLinkCircularRevealExitAnimation(getContext(), getView(),
                (RevealAnimationSetting) getArguments().getParcelable(SETTINGS), new AnimationUtils.AnimationFinishedListener() {
                    @Override
                    public void onAnimationFinished() {
                        listener.onDismissed();
                    }
                }, anim);
        hideKeyboard();
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
                if (OpenData.createPlansIsShown && !isOpen)
                    clearETFocus();
            }
        });
    }

    @Override
    public void onClick(View v) {
        TimePickerDialog timeDialog;
        if (v.getId() == R.id.button_time_from) {
            timeDialog = new TimePickerDialog(
                    getActivity(), this, times[0], times[1], true);
            timeDialogId = TIMEFROM;
        } else {
            timeDialog = new TimePickerDialog(
                    getActivity(), this, times[2], times[3], true);
            timeDialogId = TIMETO;
        }
        timeDialog.show();
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

    private void setTimeFrom() {
        if(OpenData.mDays.get(dayId).getEvents().size() == 0) {
            Calendar cal = Calendar.getInstance();
            times[0] = cal.get(Calendar.HOUR_OF_DAY);
            times[1] = cal.get(Calendar.MINUTE);
        } else {
            Event e = OpenData.mDays.get(dayId).getEvents().get(OpenData.mDays.get(dayId).getEvents().size() - 1);
            if (e.useTimeTo) {
                times[0] = e.getToHour();
                times[1] = e.getToMinutes();
            } else {
                times[0] = e.getFromHour();
                times[1] = e.getFromMinutes();
            }
        }
        bFrom.setText(String.format("%s (%s)", getString(R.string.from), Event.formatTime(times[0], times[1])));
    }

    private void setTimeLabels() {
        bFrom.setText(String.format("%s (%s)", getString(R.string.from), Event.formatTime(times[0], times[1])));
        if (bTo.isEnabled())
            bTo.setText(String.format("%s (%s)", getString(R.string.to), Event.formatTime(times[2], times[3])));
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

    public void setSheetInfo(Event event) {
        editTitle.setText(event.getTitle());
        editDec.setText(event.getDescription());
        times[0] = event.getFromHour();
        times[1] = event.getFromMinutes();
        if (event.useTimeTo) {
            times[2] = event.getToHour();
            times[3] = event.getToMinutes();
            timeTo.setChecked(true);
        }
        useNotif.setChecked(event.isUseNotif());
        setTimeLabels();
    }

    public void startEditing() {
        edit_mode = true;
        setDaySelectSingle();
    }
}
