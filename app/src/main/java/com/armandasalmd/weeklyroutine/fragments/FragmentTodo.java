package com.armandasalmd.weeklyroutine.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.armandasalmd.weeklyroutine.ExtraInfoFragment;
import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.adapters.TodoAdapter;
import com.armandasalmd.weeklyroutine.classes.BusStation;
import com.armandasalmd.weeklyroutine.classes.ExtraDataHolder;
import com.armandasalmd.weeklyroutine.classes.ItemListener;
import com.armandasalmd.weeklyroutine.classes.SortHelper;
import com.armandasalmd.weeklyroutine.helper.OnStartDragListener;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.TodoEvent;
import com.armandasalmd.weeklyroutine.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class FragmentTodo extends Fragment implements ItemListener, OnStartDragListener {

    @BindView(R.id.todo_title) EditText title;
    @BindView(R.id.todo_description) EditText description;
    @BindView(R.id.todo_appbar) AppBarLayout appbar;
    @BindView(R.id.free_todo_day_layout) LinearLayout freeDayLayout;
    @BindView(R.id.todo_list) RecyclerView listView;

    private View mView;
    public TodoAdapter mAdapter;
    //private int editId = -1; // -1 edit mode disabled
    private ItemTouchHelper mItemTouchHelper;
    private boolean done;

    public FragmentTodo() {    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_fragment_todo, container, false);
        ButterKnife.bind(this, mView);

        if (OpenData.mTodo != null)
            chooseFreeDay(OpenData.mTodo.size() == 0);
        else
            OpenData.mTodo = new ArrayList<>();

        enableKeyboardAdding();
        loadList();

        return mView;
    }

    private void chooseFreeDay(boolean freeDay) {
        if (freeDay) {
            listView.setVisibility(View.INVISIBLE);
            freeDayLayout.setVisibility(View.VISIBLE);
        }
        else {
            freeDayLayout.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void enableKeyboardAdding() {
        description.setImeActionLabel(getString(R.string.imeLabel), KeyEvent.KEYCODE_ENTER);
        description.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != 0 || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    addEvent();
                    return true;
                } else
                    return false;
            }
        });
    }

    private void loadList() {
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new TodoAdapter(getContext(), this, this);
        listView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(listView);
    }

    public void addEvent() {
        if (!title.getText().toString().isEmpty()) {
            removeETFocus();
            TodoEvent new_event = new TodoEvent(title.getText().toString(),
                    description.getText().toString(), OpenData.getTodayDate());
            new_event.setDone(done);

            if (mAdapter.editMode == -1) {
                OpenData.mTodo.add(new_event);
                mAdapter.notifyItemInserted(OpenData.mTodo.size() - 1);
            }
            else {
                OpenData.mTodo.set(mAdapter.editMode, new_event);
                mAdapter.notifyItemChanged(mAdapter.editMode);
                cancelEditing();
            }

            if (listView.getVisibility() == View.INVISIBLE)
                chooseFreeDay(false);


            title.setText(null);
            description.setText(null);
        } else {
            title.requestFocus();
            Toasty.warning(getContext(), getString(R.string.blank_title)).show();
        }
    }

    private void removeETFocus() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        appbar.requestFocus();
    }

    public void drawerOpened() {
        removeETFocus();
        mAdapter.closeMenus();
    }

    @OnClick(R.id.free_todo_day_layout)
    public void freeLayoutClick(View v) {
        removeETFocus();
    }

    public void cancelEditing() {
        BusStation.getBus(0).post(false); // pakeicia toolbar icons
        title.setText(null);
        description.setText(null);
        removeETFocus();
        setEditListenerForEditPos();
        mAdapter.editMode = -1;
    }

    private void startEditing(int position) {
        TodoEvent event = OpenData.mTodo.get(position);
        title.setText(event.getTitle());
        description.setText(event.getDescription());
        done = event.isDone();
        mAdapter.editMode = position;
        mAdapter.closeMenus();
    }

    public void requestToSort(int sortMode) {
        OpenData.mTodo = SortHelper.sortTodo(OpenData.mTodo, sortMode);
        mAdapter.notifyDataSetChanged();
    }

    private void showExtendedInfo(int position) {
        if (!OpenData.infoShown) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            TodoEvent event = OpenData.mTodo.get(position);
            ExtraDataHolder holder = new ExtraDataHolder(event.getTitle(),
                    event.getDescription(), R.string.todo, false);
            holder.setDate(event.getDate());

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
        if (mAdapter.editMode == -1) {
            mAdapter.deleteItem(position);
            if (OpenData.mTodo.size() == 0)
                chooseFreeDay(true);
        } else {
            Toasty.error(getContext(), getString(R.string.edit_in_progress)).show();
            mAdapter.closeMenus();
            mAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onEdit(int position) {
        if (mAdapter.editMode != -1)
            setEditListenerForEditPos();
        startEditing(position);
        BusStation.getBus(0).post(true); // pakeicia toolbar icons
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void afterWipe() {
        mAdapter.notifyDataSetChanged();
        chooseFreeDay(true);
    }

    private void setEditListenerForEditPos() {
        try {
            ((TodoAdapter.ViewHolder)listView.findViewHolderForAdapterPosition(mAdapter.editMode)).bindEdit();
        } catch (Exception e) {
            Log.i("Armandas", "onEdit: viewholder null");
        }
    }
}
