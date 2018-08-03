package com.mckuai.imc.Adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/1/25.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ArrayList<Post> posts;
    private Context context;
    private OnItemClickListener listener;
    private ImageLoader imageLoader;

    public interface OnItemClickListener {
        void onItemClicked(Post post);

        void onUserClicked(User user);
    }

    public PostAdapter(Context context) {
        this.context = context;
        this.imageLoader = ImageLoader.getInstance();
    }

    public PostAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
        this.imageLoader = ImageLoader.getInstance();
    }

    public void setData(ArrayList<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void addData(ArrayList<Post> posts) {
        if (null != this.posts) {
            int position = this.posts.size();
            this.posts.addAll(posts);
            notifyItemRangeInserted(position, posts.size());
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (null != posts && -1 < position && position < posts.size()) {
            final Post post = posts.get(position);
            if (null != post) {
                holder.title.setText(post.getTalkTitle() + "");
                holder.replycount.setText(post.getReplyNumEx());
                holder.updatetime.setText(post.getLastReplyTime());
                //精华
                if (post.isEssence()) {
                    holder.postlable_essence.setVisibility(View.VISIBLE);
                } else {
                    holder.postlable_essence.setVisibility(View.GONE);
                }
                //推荐
                if (post.isTop()) {
                    holder.postlable_top.setVisibility(View.VISIBLE);
                } else {
                    holder.postlable_top.setVisibility(View.GONE);
                }

                if (0 == post.getUserId()) {
                    //个人中心中的作品
                    if (null != post.getIcon() && 10 < post.getIcon().length()) {
                        imageLoader.displayImage(post.getIcon(), holder.usercover, MCKuai.instence.getCircleOptions());
                    }
                    holder.username.setText(post.getForumName());

                    if (null != listener) {


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onItemClicked(post);
                            }
                        });
                    }
                } else {
                    //其它地方展示的帖子
                    if (null != post.getHeadImg() && 10 < post.getHeadImg().length()) {
                        imageLoader.displayImage(post.getHeadImg(), holder.usercover, MCKuai.instence.getCircleOptions());
                    }
                    holder.username.setText(post.getUserName() + "");

                    if (null != listener) {
                        holder.username.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                User user = new User();
                                user.setId((long) post.getUserId());
                                listener.onUserClicked(user);
                            }
                        });
                        holder.usercover.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                User user = new User();
                                user.setId((long) post.getUserId());
                                listener.onUserClicked(user);
                            }
                        });
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onItemClicked(post);
                            }
                        });
                    }

                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return null == posts ? 0 : posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView title;
        public AppCompatTextView username;
        public AppCompatTextView replycount;
        public AppCompatTextView updatetime;
        public AppCompatImageView usercover;
        public AppCompatImageView postlable_top;
        public AppCompatImageView postlable_essence;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (AppCompatTextView) itemView.findViewById(R.id.post_title_normal);
            username = (AppCompatTextView) itemView.findViewById(R.id.postowner_name);
            replycount = (AppCompatTextView) itemView.findViewById(R.id.postreply_count);
            updatetime = (AppCompatTextView) itemView.findViewById(R.id.post_updatetime);
            usercover = (AppCompatImageView) itemView.findViewById(R.id.postowner_cover);
            postlable_top = (AppCompatImageView) itemView.findViewById(R.id.postlable_top);
            postlable_essence = (AppCompatImageView) itemView.findViewById(R.id.postlable_essence);
        }
    }

}
