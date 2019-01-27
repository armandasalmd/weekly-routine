package com.armandasalmd.weeklyroutine.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.ItemListener;
import com.armandasalmd.weeklyroutine.helper.ItemTouchHelperAdapter;
import com.armandasalmd.weeklyroutine.helper.ItemTouchHelperViewHolder;
import com.armandasalmd.weeklyroutine.helper.OnStartDragListener;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.TodoEvent;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class TodoAdapter extends RecyclerView.Adapter implements ItemTouchHelperAdapter {
    private static final int TYPE_FOOTER = 0;
    private static final int TYPE_ITEM = 1;

    private Context mContext;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private ItemListener itemListener;
    private OnStartDragListener mDragStartListener;
    public int editMode = -1; // -1 - false

    private List<TodoEvent> getEvents() {
        return OpenData.mTodo;
    }

    public TodoAdapter(Context mContext, ItemListener itemListener, OnStartDragListener dragListener) {
        this.mContext = mContext;
        this.itemListener = itemListener;
        mDragStartListener = dragListener;
        binderHelper.setOpenOnlyOne(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.todo_row, parent, false);
            return new ViewHolder(view);
        } else {
            View footer = new View(mContext);
            final int footerHeight = 300;
            footer.setLayoutParams( new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, footerHeight));
            return new FooterHolder(footer);
        }
    }

    public void closeMenus() {
        for (TodoEvent event:getEvents())
            binderHelper.closeLayout(Integer.toString(event.getId()));
    }

    @Override
    public int getItemViewType(int position) {
        if (getEvents().size() == position)
            return TYPE_FOOTER;
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            final ViewHolder mHolder = (ViewHolder) holder;
            final TodoEvent event = getEvents().get(position);
            binderHelper.bind(mHolder.swipeLayout, Integer.toString(event.getId()));

            if (event.getDescription().isEmpty()) {
                mHolder.tv_dec.setVisibility(View.GONE);
            }
            else {
                mHolder.tv_dec.setVisibility(View.VISIBLE);
                mHolder.tv_dec.setText(event.getDescription());
            }
            mHolder.checkDone.setChecked(OpenData.mTodo.get(position).isDone());
            mHolder.tv_title.setText(event.getTitle());
            mHolder.bindClick();
            mHolder.handle.setOnTouchListener(new View.OnTouchListener() { // FIXME: transilation
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (editMode != -1)
                        Toasty.warning(mContext, "Edit mode", Toast.LENGTH_SHORT).show();
                    else if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        closeMenus();
                        mDragStartListener.onStartDrag(mHolder);
                    }

                    return false;
                }
            });
            mHolder.height.forceLayout(); // perskaiciuoja visus vaikus is naujo savyje
            //mHolder.setIsRecyclable(false);
        }
    }

    @Override
    public int getItemCount() {
        return getEvents().size() + 1;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        TodoEvent prev = getEvents().remove(fromPosition);
        //int num = toPosition > fromPosition ? toPosition - 1 : toPosition;
        getEvents().add(toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        // helper
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{
        TextView tv_title, tv_dec;
        SwipeRevealLayout swipeLayout;
        RelativeLayout delete, edit, height;
        ImageView handle;
        LinearLayout rippleLayout;
        CheckBox checkDone;

        ViewHolder(View view) {
            super(view);
            tv_title = view.findViewById(R.id.todo_text_title);
            tv_dec = view.findViewById(R.id.todo_text_dec);
            swipeLayout = view.findViewById(R.id.swipe_layout);
            delete = view.findViewById(R.id.delete_button);
            edit = view.findViewById(R.id.edit_button);
            handle = view.findViewById(R.id.handle);
            height = view.findViewById(R.id.todoRelative);
            rippleLayout = view.findViewById(R.id.todoRipple);
            checkDone = view.findViewById(R.id.check_done);

            rippleLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemListener.onItemLongClick(getAdapterPosition());
                    return true;
                }
            });
            swipeLayout.setSwipeListener(new SwipeRevealLayout.SwipeListener() {
                @Override
                public void onClosed(SwipeRevealLayout view) {

                }

                @Override
                public void onOpened(SwipeRevealLayout view) {

                }

                @Override
                public void onSlide(SwipeRevealLayout view, float slideOffset) {
                    rippleLayout.cancelLongPress();
                }
            });
            checkDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    OpenData.mTodo.get(getAdapterPosition()).setDone(isChecked);
                }
            });
        }

        void bindClick() {
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setOnClickListener(null);
                    swipeLayout.close(true);
                    itemListener.onDelete(getAdapterPosition());
                }
            });
            bindEdit();
        }

        public void bindEdit() {
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setOnClickListener(null);
                    swipeLayout.close(true);
                    itemListener.onEdit(getAdapterPosition());
                }
            });
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    private class FooterHolder extends RecyclerView.ViewHolder {
        FooterHolder(View itemView) {
            super(itemView);
        }
    }

    public void deleteItem(int position) {
        getEvents().remove(position);
        notifyItemRemoved(position);
    }

}

// https://bignerdranch.github.io/expandable-recycler-view/