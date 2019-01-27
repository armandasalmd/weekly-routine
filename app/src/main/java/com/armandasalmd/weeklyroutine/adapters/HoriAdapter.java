package com.armandasalmd.weeklyroutine.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.classes.DateHolder;
import com.armandasalmd.weeklyroutine.classes.OpenData;

import java.util.ArrayList;
import java.util.List;

public class HoriAdapter extends RecyclerView.Adapter<HoriAdapter.ViewHolder> {

    private List<DateHolder> mDateList;

    public HoriAdapter() {
        super();
        mDateList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View view = View.inflate(viewGroup.getContext(), R.layout.vertical_row, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        viewHolder.mText.setText(OpenData.prevDate(mDateList.get(i).toString()));
        viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(viewHolder);
            }
        });
    }

    private void removeItem(ViewHolder holder) {
        mDateList.remove(holder.getAdapterPosition());
        notifyItemRemoved(holder.getAdapterPosition());
    }

    public void addItem(DateHolder dateHolder) {
        mDateList.add(dateHolder);
        notifyItemInserted(mDateList.size() - 1);
    }

    public List<DateHolder> getDates() {
        return mDateList;
    }

    public void setDateList(List<DateHolder> dates) {
        mDateList = dates;
        notifyDataSetChanged();
    }

    public void clearList() {
        mDateList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDateList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;
        ImageView mImage;

        ViewHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.vertical_text);
            mImage = itemView.findViewById(R.id.vertical_image);
        }
    }
}