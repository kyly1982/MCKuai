package com.mckuai.imc.Adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.CommunityMessage;
import com.mckuai.imc.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/2/1.
 */
public class CommunityMessageAdapter extends RecyclerView.Adapter<CommunityMessageAdapter.ViewHolder> {
    private ArrayList<CommunityMessage> messages;
    private Context context;
    private OnItemClickListener listener;
    private ImageLoader loader;


    public interface OnItemClickListener {
        void onItemClicked(CommunityMessage message);
    }

    public CommunityMessageAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.loader = ImageLoader.getInstance();
    }

    public void setData(ArrayList<CommunityMessage> messages, boolean isRefresh) {
        if (null == this.messages || isRefresh) {
            this.messages = messages;
            notifyDataSetChanged();
        } else if (null != messages) {
            int start = this.messages.size();
            this.messages.addAll(messages);
            notifyItemRangeInserted(start, messages.size());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_message, parent, false);
        ViewHolder holder = new ViewHolder(view);
        if (null != listener) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommunityMessage message = (CommunityMessage) v.getTag();
                    if (null != message) {
                        listener.onItemClicked(message);
                    }
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (null != messages && -1 < position && position < messages.size()) {
            CommunityMessage message = messages.get(position);
            if (null != message) {
                holder.itemView.setTag(message);
                switch (message.getTypeEx()) {
                    case CommunityMessage.TYPE_REPLY:
                        showReplyMessage(holder, message);
                        break;
                    case CommunityMessage.TYPE_AT:
                        showAtMessage(holder, message);
                        break;
                    case CommunityMessage.TYPE_SYSTEM:
                        showSystemMessage(holder, message);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return null == messages ? 0 : messages.size();
    }

    private void showReplyMessage(ViewHolder holder, CommunityMessage message) {
        loader.displayImage(message.getHeadImg(), holder.cover, MCKuai.instence.getCircleOptions());
        holder.username.setText(message.getUserName());
        holder.username.setVisibility(View.VISIBLE);
        holder.time.setText(message.getInsertTimeEx());
        holder.title.setText(message.getTalkTitle());
        holder.content.setText(message.getCont());
        holder.messagetag.setText(context.getString(R.string.replyYou));
    }

    private void showAtMessage(ViewHolder holder, CommunityMessage message) {

        loader.displayImage(message.getHeadImg(), holder.cover, MCKuai.instence.getCircleOptions());
        holder.username.setText(message.getUserName());
        holder.username.setVisibility(View.VISIBLE);
        holder.time.setText(message.getInsertTimeEx());
        holder.messagetag.setText(context.getString(R.string.atYou));
        holder.title.setText(message.getTalkTitle());
        holder.content.setText(message.getCont());
    }

    private void showSystemMessage(ViewHolder holder, CommunityMessage message) {

        holder.cover.setBackgroundResource(R.mipmap.ic_usercenter_community_sysmsg);
        holder.username.setVisibility(View.GONE);
        holder.time.setText(message.getInsertTimeEx());
        holder.messagetag.setText(context.getString(R.string.systemMessage));
        holder.content.setText(message.getShowText());
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView cover;
        public AppCompatTextView username;
        public AppCompatTextView time;
        public AppCompatTextView title;
        public AppCompatTextView content;
        public AppCompatTextView messagetag;

        public ViewHolder(View itemView) {
            super(itemView);
            cover = (AppCompatImageView) itemView.findViewById(R.id.usercover);
            username = (AppCompatTextView) itemView.findViewById(R.id.username);
            time = (AppCompatTextView) itemView.findViewById(R.id.time);
            title = (AppCompatTextView) itemView.findViewById(R.id.posttitle);
            content = (AppCompatTextView) itemView.findViewById(R.id.messagecontent);
            messagetag = (AppCompatTextView) itemView.findViewById(R.id.messagetag);
        }
    }

}
