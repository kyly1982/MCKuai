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

import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/7/21.
 */
public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> implements View.OnClickListener {
    private Context context;
    private ImageLoader loader;
    private DisplayImageOptions circle;
    private DisplayImageOptions normal;
    private OnItemClickListener listener;

    private ArrayList<Post> postList;

    public RecommendAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.normal = MCKuai.instence.getNormalOptions();
        this.circle = MCKuai.instence.getCircleOptions();
        this.listener = listener;
        loader = ImageLoader.getInstance();
    }

    public void setData(ArrayList<Post> posts) {
        postList = posts;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClicked(int position, Post post);

        void onItemUserClicked(int userId);
    }

    @Override
    public int getItemCount() {
        return null == postList ? 0 : postList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_recommend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post post = postList.get(position);
        if (null != post) {
            Post oldPost = (Post) holder.itemView.getTag();
            if (null == oldPost || oldPost.getId() != post.getId()) {
                loader.displayImage(post.getMobilePic(), holder.cover, normal, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
//                        super.onLoadingStarted(imageUri, view);
                        holder.cover.setImageResource(R.mipmap.ic_empty);
                        holder.cover.setBackgroundResource(R.drawable.mask);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (null != loadedImage) {
                            holder.cover.setBackgroundDrawable(new BitmapDrawable(loadedImage));
                        } else {
                            holder.cover.setBackgroundResource(R.mipmap.ic_empty);
                        }
                        holder.cover.setImageResource(R.mipmap.videocover_mask);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        holder.cover.setImageResource(R.mipmap.ic_empty);
                        holder.cover.setBackgroundResource(R.drawable.mask);
                    }
                });
                loader.displayImage(post.getHeadImg(), holder.ownercover, circle);
                holder.title.setText(post.getTalkTitle());
                holder.ownername.setText(post.getUserName());
                holder.reply.setText(post.getReplyNumEx());
                holder.time.setText(post.getLastReplyTime());
                holder.itemView.setTag(post);
                holder.ownercover.setTag(post.getUserId());
                holder.ownername.setTag(post.getUserId());
                if (null != listener) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClicked(position, post);
                        }
                    });
                    holder.ownercover.setOnClickListener(this);
                    holder.ownername.setOnClickListener(this);
                }
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView cover;
        public AppCompatImageView ownercover;
        public AppCompatTextView ownername;
        public AppCompatTextView title;
        public AppCompatTextView reply;
        public AppCompatTextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            cover = (AppCompatImageView) itemView.findViewById(R.id.postcover);
            ownercover = (AppCompatImageView) itemView.findViewById(R.id.ownercover);
            ownername = (AppCompatTextView) itemView.findViewById(R.id.ownername);
            title = (AppCompatTextView) itemView.findViewById(R.id.posttitle);
            reply = (AppCompatTextView) itemView.findViewById(R.id.postreply);
            time = (AppCompatTextView) itemView.findViewById(R.id.posttime);
        }
    }

    @Override
    public void onClick(View v) {
        listener.onItemUserClicked((int) v.getTag());
    }
}
