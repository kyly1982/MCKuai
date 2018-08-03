package com.mckuai.imc.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/7/20.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> implements View.OnClickListener {
    private Context context;
    private ArrayList<Post> videoList;
    private OnItemClickListener listener;

    private ImageLoader loader;
    private DisplayImageOptions circleOperation;
    private DisplayImageOptions normalOperation;

    public interface OnItemClickListener {
        void onItemClicked(int position, Post item);

        void onItemUserClicked(int userId);
    }

    public VideoAdapter(Context context, OnItemClickListener itemClickListener) {
        this.context = context;
        this.listener = itemClickListener;
    }

    public void setDisplayOperations(DisplayImageOptions circle, DisplayImageOptions normal) {
        this.circleOperation = circle;
        this.normalOperation = normal;
    }

    public void setData(ArrayList<Post> posts, boolean isRefresh) {
        if (null == videoList || videoList.isEmpty() || isRefresh) {
            //第一次设置值或者刷新数据
            videoList = posts;
            notifyDataSetChanged();
        } else {
            //添加更多的数据
            int start = videoList.size();
            videoList.addAll(posts);
            notifyItemRangeInserted(start, posts.size());
        }
    }


    @Override
    public int getItemCount() {
        return null == videoList ? 0 : videoList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post post = videoList.get(position);
        if (null != post) {
            if (null == loader) {
                loader = ImageLoader.getInstance();
            }
            //预览图
            if (null == holder.video_cover.getTag() || !holder.video_cover.getTag().equals(post.getMobilePic())) {
                loader.displayImage(post.getMobilePic(), holder.video_cover, normalOperation, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.video_cover.setImageResource(R.mipmap.ic_empty);
                        holder.video_cover.setBackgroundResource(R.drawable.mask);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                        if (null != loadedImage) {
                            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                            float scal = screenWidth / loadedImage.getWidth();
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, (int) (loadedImage.getHeight() * scal));
                            holder.video_cover.setLayoutParams(params);
                            holder.video_cover.setImageResource(R.mipmap.videocover_mask);
                            holder.video_cover.setBackgroundDrawable(new BitmapDrawable(loadedImage));
                            holder.video_cover.setTag(post.getMobilePic());
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        holder.video_cover.setImageResource(R.mipmap.ic_empty);
                        holder.video_cover.setBackgroundResource(R.drawable.mask);
                    }
                });
            }
            //头像
            if (null == holder.video_ownercover.getTag() || (int) holder.video_ownercover.getTag() != post.getUserId()) {
                loader.displayImage(post.getHeadImg(), holder.video_ownercover, circleOperation);
                holder.video_ownercover.setTag(post.getUserId());
                holder.video_ownercover.setOnClickListener(this);
            }
            //昵称
            if (null == holder.video_ownername.getTag() || (int) holder.video_ownername.getTag() != post.getUserId()) {
                holder.video_ownername.setText(post.getUserName());
                holder.video_ownername.setTag(post.getUserId());
                holder.video_ownername.setOnClickListener(this);
            }
            //回复数
            holder.video_replycount.setText(post.getReplyNumEx());
            holder.video_replytime.setText(post.getLastReplyTime());
            holder.video_title.setText(post.getTalkTitle());
            Post tag = (Post) holder.itemView.getTag();
            if (null != listener && (null == tag || tag.getId() != post.getId())) {
                holder.itemView.setTag(post);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClicked(position, post);
                    }
                });
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView video_cover;
        public AppCompatImageView video_ownercover;
        public AppCompatTextView video_ownername;
        public AppCompatTextView video_replycount;
        public AppCompatTextView video_replytime;
        public AppCompatTextView video_title;

        public ViewHolder(View itemView) {
            super(itemView);
            video_cover = (AppCompatImageView) itemView.findViewById(R.id.video_cover);
            video_ownercover = (AppCompatImageView) itemView.findViewById(R.id.video_ownercover);
            video_ownername = (AppCompatTextView) itemView.findViewById(R.id.video_ownername);
            video_replycount = (AppCompatTextView) itemView.findViewById(R.id.video_replycount);
            video_replytime = (AppCompatTextView) itemView.findViewById(R.id.video_replytime);
            video_title = (AppCompatTextView) itemView.findViewById(R.id.vidio_title);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_ownercover:
            case R.id.video_ownername:
                if (null != listener) {
                    listener.onItemUserClicked((int) v.getTag());
                }
                break;
        }
    }
}
