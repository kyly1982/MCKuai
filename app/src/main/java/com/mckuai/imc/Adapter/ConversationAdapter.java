package com.mckuai.imc.Adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.Conversation;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.TimestampConverter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/2/2.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private Context context;
    private ImageLoader loader;
    private ArrayList<Conversation> conversations;

    private OnItemClickListener listener;
    // private ArrayList<String> lastMsg;

    public ConversationAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        loader = ImageLoader.getInstance();
    }

    public void setData(ArrayList<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    public void refreshItem(User user) {
        if (null != conversations && !conversations.isEmpty()) {
            for (int i = 0; i < conversations.size(); i++) {
                if (conversations.get(i).getTarget().getId() == user.getId()) {
                    conversations.get(i).setTarget(user);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }


    public interface OnItemClickListener {
        void onItemClicked(Conversation conversation);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }


    /*NONE(0, "none"),
    PRIVATE(1, "private"),
    DISCUSSION(2, "discussion"),
    GROUP(3, "group"),
    CHATROOM(4, "chatroom"),
    CUSTOMER_SERVICE(5, "customer_service"),
    SYSTEM(6, "system"),
    APP_PUBLIC_SERVICE(7, "app_public_service"),
    PUBLIC_SERVICE(8, "public_service"),
    PUSH_SERVICE(9, "push_service");*/

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (null != conversations && -1 < position && position < conversations.size()) {
            final Conversation conversation = conversations.get(position);

            if (null != conversation && conversation.getConversation().getConversationType() == io.rong.imlib.model.Conversation.ConversationType.PRIVATE) {
                loader.displayImage(conversation.getTarget().getHeadImage(), holder.usercover, MCKuai.instence.getCircleOptions());
                holder.username.setText(conversation.getTarget().getNickEx());
                long time = conversation.getConversation().getSentTime() > conversation.getConversation().getReceivedTime() ? conversation.getConversation().getSentTime() : conversation.getConversation().getReceivedTime();
                holder.time.setText(TimestampConverter.toString(time));
                int count = conversation.getConversation().getUnreadMessageCount();
                if (0 == count) {
                    //holder.lastmessage.setVisibility(View.INVISIBLE);
                    holder.lastmessage.setText("没有新消息");
                } else {
                    holder.lastmessage.setText(count + "条新消息");
                    //holder.lastmessage.setVisibility(View.VISIBLE);
                }

                if (null != listener) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClicked(conversation);
                        }
                    });
                }
            }
        }
    }

/*    private void showLastMessage(RongIMClientWrapper client, int msgId, Conversation conversation) {
        client.getMessage(msgId, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }*/

    @Override
    public int getItemCount() {
        return null == conversations ? 0 : conversations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView usercover;
        public AppCompatTextView username;
        public AppCompatTextView time;
        public AppCompatTextView lastmessage;

        public ViewHolder(View itemView) {
            super(itemView);
            usercover = (AppCompatImageView) itemView.findViewById(R.id.usercover);
            username = (AppCompatTextView) itemView.findViewById(R.id.username);
            time = (AppCompatTextView) itemView.findViewById(R.id.time);
            lastmessage = (AppCompatTextView) itemView.findViewById(R.id.lastmessage);
        }
    }
}
