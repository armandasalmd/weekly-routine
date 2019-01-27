package com.armandasalmd.weeklyroutine.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.ItemListener;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.List;

public class SmartestAdapter extends RecyclerView.Adapter {
    private static final int TYPE_FOOTER = 0;
    private static final int TYPE_ITEM = 1;

    private Context mContext;
    private int dayId;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    private ItemListener itemListener;

    public SmartestAdapter(Context mContext, int dayId, ItemListener listener) {
        this.mContext = mContext;
        this.dayId = dayId;
        binderHelper.setOpenOnlyOne(true);
        itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.my_smart_row, parent, false);
            return new ViewHolder(view);
        } else {
            View footer = new View(mContext);
            final int footerHeight = 300;
            footer.setLayoutParams( new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, footerHeight));
            return new FooterHolder(footer);
        }
    }

    public void setLockSwipe(boolean lock) {
        if (lock)
            for (Event event:getEvents())
                binderHelper.lockSwipe(Integer.toString(event.getNotificationId()));
        else
            for (Event event:getEvents())
                binderHelper.unlockSwipe(Integer.toString(event.getNotificationId()));
    }

    public void closeMenus() {
        for (Event event:getEvents())
            binderHelper.closeLayout(Integer.toString(event.getNotificationId()));
    }

    public void openMenu(int notifId) {
        binderHelper.openLayout(Integer.toString(notifId));
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
            ViewHolder mHolder = (ViewHolder) holder;
            final Event event = getEvents().get(position);

            binderHelper.bind(mHolder.swipeLayout, Integer.toString(event.getNotificationId()));
            if (!OpenData.lockMode) // atrakinta
                binderHelper.lockSwipe(Integer.toString(event.getNotificationId()));


            if (event.isSpecial) { // TODO: styliaus nustatymas saraso eilutei
                mHolder.setCardBackground(R.color.row_second_back);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mHolder.tv_name.setTextAppearance(R.style.rowTextSpecial);
                } else {
                    mHolder.tv_name.setTextAppearance(mContext, R.style.rowTextSpecial);
                }
            } else {
                mHolder.setCardBackground(R.color.background_card);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mHolder.tv_name.setTextAppearance(R.style.rowTextNormal);
                } else {
                    mHolder.tv_name.setTextAppearance(mContext, R.style.rowTextNormal);
                }
            }

            mHolder.tv_name.setText(event.getTitle());
            String dec = event.getDescription();
            if (dec.isEmpty())
                mHolder.tv_dec.setVisibility(View.GONE);
            else {
                mHolder.tv_dec.setVisibility(View.VISIBLE);
                mHolder.tv_dec.setText(dec);
            }

            if (event.useTimeTo) {
                mHolder.tv_time_from.setText(String.format("%s %s", mContext.getString(R.string.from), event.getTimeFrom()));
                mHolder.tv_time_from.setTextSize(TypedValue.COMPLEX_UNIT_SP, mContext.getResources().getInteger(R.integer.time_from_size_int));
                mHolder.tv_time_to.setText(String.format("%s %s", mContext.getString(R.string.to), event.getTimeTo()));
                mHolder.tv_time_to.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams params = mHolder.tv_time_from.getLayoutParams();
                params.height = mContext.getResources().getDimensionPixelSize(R.dimen.time_from_height);
                mHolder.tv_time_from.setLayoutParams(params);
            }
            else {
                mHolder.tv_time_from.setText(event.getTimeFrom());
                mHolder.tv_time_from.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                mHolder.tv_time_to.setVisibility(View.GONE);
                // match_parent
                ViewGroup.LayoutParams params = mHolder.tv_time_from.getLayoutParams();
                params.height = mContext.getResources().getDimensionPixelSize(R.dimen.row_height);
                mHolder.tv_time_from.setLayoutParams(params);
            }
            mHolder.done.setChecked(OpenData.mDays.get(dayId).getEvents().get(position).isDone());
            mHolder.bindClick();
            mHolder.rippleLayout.forceLayout();
            //mHolder.setIsRecyclable(false);
        }
    }

    @Override
    public int getItemCount() {
        return getEvents().size() + 1;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_dec, tv_time_from, tv_time_to;
        RelativeLayout container;
        private SwipeRevealLayout swipeLayout;
        RelativeLayout delete, edit;
        LinearLayout rippleLayout;
        CheckBox done;

        ViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.mano_text);
            tv_dec = view.findViewById(R.id.mano_dec);
            tv_time_from = view.findViewById(R.id.time_from);
            tv_time_to = view.findViewById(R.id.time_to);
            container = view.findViewById(R.id.row_container);
            swipeLayout = view.findViewById(R.id.swipe_layout);
            delete = view.findViewById(R.id.delete_button);
            edit = view.findViewById(R.id.edit_button);
            rippleLayout = view.findViewById(R.id.rippleRow);
            done = view.findViewById(R.id.check_done);
            done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    OpenData.mDays.get(dayId).getEvents().get(getAdapterPosition()).setDone(isChecked);
                }
            });

            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemListener.onItemLongClick(getAdapterPosition());
                    return false;
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
                    container.cancelLongPress();
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

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setOnClickListener(null);
                    swipeLayout.close(true);
                    itemListener.onEdit(getAdapterPosition());
                }
            });
        }

        void setCardBackground(int resourceColor) {
            container.setBackgroundColor(ContextCompat.getColor(mContext, resourceColor));
        }
    }

    private class FooterHolder extends RecyclerView.ViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    private List<Event> getEvents() {
        return OpenData.mDays.get(dayId).getEvents();
    }

    public void deleteItem(int position) {
        notifyItemRemoved(position);
    }
}
