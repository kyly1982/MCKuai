package com.mckuai.imc.Adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mckuai.imc.Bean.CommunityDynamic;
import com.mckuai.imc.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/2/1.
 */
public class CommunityDynamicAdapter extends RecyclerView.Adapter<CommunityDynamicAdapter.ViewHolder> {
    private ArrayList<CommunityDynamic> dynamics;
    private Context context;
    private ImageLoader loader;
    private OnItemClickListener listener;

    public CommunityDynamicAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.loader = ImageLoader.getInstance();
    }

    public interface OnItemClickListener {
        void onItemClicked(CommunityDynamic dynamic);
    }

    public void setData(ArrayList<CommunityDynamic> dynamics, boolean isRefresh) {
        if (null == this.dynamics || isRefresh) {
            this.dynamics = dynamics;
            notifyDataSetChanged();
        } else if (null != dynamics) {
            int start = this.dynamics.size();
            this.dynamics.addAll(dynamics);
            notifyItemRangeInserted(start, dynamics.size());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_dynamic, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (null != dynamics && -1 < position && position < dynamics.size()) {
            final CommunityDynamic dynamic = dynamics.get(position);
            if (null != dynamic) {
                switch (dynamic.getTypeEx()) {
                    case CommunityDynamic.TYPE_CREATE:
                        showCreate(holder, dynamic);
                        break;
                    case CommunityDynamic.TYPE_REPLY:
                        showReply(holder, dynamic);
                        break;
                }
                if (null != listener) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClicked(dynamic);
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return null == dynamics ? 0 : dynamics.size();
    }

    private void showReply(ViewHolder holder, CommunityDynamic dynamic) {
        holder.icon.setBackgroundResource(R.mipmap.ic_usercenter_community_reply);
        holder.type.setText(R.string.replyPost);
        holder.time.setText(dynamic.getInsertTime());
        holder.title.setText(dynamic.getTalkTitle());
        holder.content.setLines(1);
        holder.content.setText(dynamic.getCont());
        holder.title.setVisibility(View.VISIBLE);
    }

    private void showCreate(ViewHolder holder, CommunityDynamic dynamic) {
        holder.icon.setBackgroundResource(R.mipmap.ic_usercenter_community_creat);
        holder.type.setText(R.string.createPost);
        holder.time.setText(dynamic.getInsertTime());
        holder.title.setVisibility(View.GONE);
        holder.content.setLines(2);
        holder.content.setText(dynamic.getCont());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView icon;
        private AppCompatTextView type;
        private AppCompatTextView time;
        private AppCompatTextView title;
        private AppCompatTextView content;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (AppCompatImageView) itemView.findViewById(R.id.typeicon);
            type = (AppCompatTextView) itemView.findViewById(R.id.dynamictype);
            time = (AppCompatTextView) itemView.findViewById(R.id.dynamictime);
            title = (AppCompatTextView) itemView.findViewById(R.id.posttitle);
            content = (AppCompatTextView) itemView.findViewById(R.id.content);
        }
    }
}
