package com.armandasalmd.weeklyroutine.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.classes.ItemListener;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SpecialEvent;
import com.armandasalmd.weeklyroutine.classes.TodoEvent;
import com.armandasalmd.weeklyroutine.helper.ItemTouchHelperAdapter;
import com.armandasalmd.weeklyroutine.helper.ItemTouchHelperViewHolder;
import com.armandasalmd.weeklyroutine.helper.OnStartDragListener;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.List;

public class SpecialListAdapter extends RecyclerView.Adapter implements ItemTouchHelperAdapter {
    private static final int TYPE_FOOTER = 0;
    private static final int TYPE_ITEM = 1;

    private Context mContext;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private ItemListener itemListener;
    private OnStartDragListener mDragStartListener;

    public SpecialListAdapter(Context mContext, ItemListener itemListener, OnStartDragListener dragListener) {
        this.mContext = mContext;
        this.itemListener = itemListener;
        mDragStartListener = dragListener;
        binderHelper.setOpenOnlyOne(true);
    }

    private List<SpecialEvent> getEvents() {
        return OpenData.mSpecials;
    }

    public void deleteItem(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        SpecialEvent prev = getEvents().remove(fromPosition);
        //int num = toPosition > fromPosition ? toPosition - 1 : toPosition;
        getEvents().add(toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        // helper
    }

    private class FooterHolder extends RecyclerView.ViewHolder {
        FooterHolder(View itemView) {
            super(itemView);
        }
    }

    public class SpecialHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private TextView title, dec, time, dates;
        private SwipeRevealLayout swipeLayout;
        RelativeLayout delete, edit;
        LinearLayout rippleLayout;
        ImageView handle;

        SpecialHolder(View view) {
            super(view);
            title = view.findViewById(R.id.spec_title);
            dec = view.findViewById(R.id.spec_description);
            time = view.findViewById(R.id.spec_time);
            dates = view.findViewById(R.id.spec_date);
            swipeLayout = view.findViewById(R.id.swipe_layout);
            delete = view.findViewById(R.id.delete_button);
            edit = view.findViewById(R.id.edit_button);
            handle = view.findViewById(R.id.handle);
            rippleLayout = view.findViewById(R.id.rippleLayout);
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.special_row, parent, false);
            return new SpecialHolder(view);
        } else {
            View footer = new View(mContext);
            final int footerHeight = 300;
            footer.setLayoutParams( new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, footerHeight));
            return new FooterHolder(footer);
        }
    }

    public void closeMenus() {
        for (SpecialEvent event:getEvents())
            binderHelper.closeLayout(Integer.toString(event.getEvent().getNotificationId()));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // This means that the view that you changed earlier is now being re-used.
        if (holder instanceof SpecialHolder) {
            final SpecialHolder mHolder = (SpecialHolder)holder;
            SpecialEvent specialEvent = getEvents().get(position);

            binderHelper.bind(mHolder.swipeLayout, Integer.toString(specialEvent.getEvent().getNotificationId()));

            mHolder.title.setText(specialEvent.getEvent().getTitle());
            mHolder.dec.setText(specialEvent.getEvent().getDescription());
            mHolder.time.setText(specialEvent.getEvent().getTimeFrom());
            mHolder.dates.setText(String.format("%s ", OpenData.prevDate(SpecialEvent.datesPreviewString(specialEvent.getDates()))));

            mHolder.bindClick();
            mHolder.handle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                        closeMenus();
                        mDragStartListener.onStartDrag(mHolder);
                    }
                    return false;
                }
            });
            mHolder.rippleLayout.forceLayout();
            //mHolder.setIsRecyclable(false); // perpaiso visus texts (perkuria holderi)
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return getEvents().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (getEvents().size() == position)
            return TYPE_FOOTER;
        return TYPE_ITEM;
    }
}
