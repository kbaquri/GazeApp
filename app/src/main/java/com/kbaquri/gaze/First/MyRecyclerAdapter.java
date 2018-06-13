package com.kbaquri.gaze.First;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kbaquri.gaze.R;

/**
 * Created by kbaqu on 11-Feb-18.
 */

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private Category[] mCategories;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView backgroundIV;
        public TextView titleTV;

        public ViewHolder(View itemView) {
            super(itemView);

            backgroundIV = itemView.findViewById(R.id.item_background);
            titleTV = itemView.findViewById(R.id.item_title);
        }
    }

    public MyRecyclerAdapter(Category[] categories) {
        this.mCategories = categories;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView =LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.backgroundIV.setImageResource(mCategories[position].getBackground());
        holder.titleTV.setText(mCategories[position].getTitle());
    }

    @Override
    public int getItemCount() {
        return mCategories.length;
    }

}
