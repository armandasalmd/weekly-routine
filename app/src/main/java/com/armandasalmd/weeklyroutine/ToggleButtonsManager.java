package com.armandasalmd.weeklyroutine;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class ToggleButtonsManager extends LinearLayout
{
    private List<ToggleButton> toggles;
    //public int lastSelected = 0;

    public ToggleButtonsManager(Context context) {
        super(context);
    }

    public ToggleButtonsManager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleButtonsManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setUp(String[] list) {
        toggles = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            toggles.add((ToggleButton) getChildAt(i));
            String text = list[i].substring(0, 3);
            toggles.get(i).setTextOn(text);
            toggles.get(i).setTextOff(text);
            toggles.get(i).setText(text);
        }
    }

    public void checkButton(int position, boolean check) {
        toggles.get(position).setChecked(check);
    }

    public List<Integer> getSelectedIds() {
        List<Integer> selList = new ArrayList<>();
        for (int i = 0; i < toggles.size(); i++)
            if (toggles.get(i).isChecked())
                selList.add(i);
        return selList;
    }

}
