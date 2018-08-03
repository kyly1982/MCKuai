package com.mckuai.imc.Adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mckuai.imc.Bean.ForumInfo;
import com.mckuai.imc.R;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/1/25.
 */
public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ViewHolder> {
    private ArrayList<ForumInfo> forums;
    private Context context;
    private OnItemClickListener listener;
    private int checkedFroumId = 0;

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public ForumAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(ArrayList<ForumInfo> forums) {
        this.forums = forums;
        checkedFroumId = forums.get(0).getId();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_forum, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (null != forums && -1 < position && position < forums.size()) {
            final ForumInfo forum = forums.get(position);
            if (null != forum) {
                String name = forum.getName();
                int length = name.length();
                StringBuilder builder = new StringBuilder(name);
                builder.insert(length / 2, "\n");
                holder.name.setText(builder.toString());
            }
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkedFroumId = forum.getId();
                    listener.onItemClicked(position);
                }
            });
            if (forum.getId() == checkedFroumId) {
                holder.name.setChecked(true);
            } else {
                holder.name.setChecked(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return null == forums ? 0 : forums.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatRadioButton name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (AppCompatRadioButton) itemView;
        }
    }
}
