package com.mckuai.imc.Adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.RecommendItem;
import com.mckuai.imc.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by kyly on 2017/6/21.
 */

public class RecommandPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private Context context;
    private OnItemClickListener listener;
    private ArrayList<RecommendItem> items;
    private ImageLoader imageLoader;
    private DisplayImageOptions circle, normal;

    public interface OnItemClickListener {
        void onPostClicked(Post item);

        void onUserClicked(int userId);

        void onRaidersClicked(int id);

        void onAdClicked(String downloadUrl);
    }

    public RecommandPostAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        imageLoader = ImageLoader.getInstance();
        circle = MCKuai.instence.getCircleOptions();
        normal = MCKuai.instence.getNormalOptions();
    }

    public void setData(ArrayList<RecommendItem> recommendItems, boolean isRefresh) {
        if (null != recommendItems) {
            if (null == this.items) {
                this.items = (ArrayList<RecommendItem>) recommendItems.clone();
                notifyDataSetChanged();
            } else {
                if (isRefresh) {
                    if (null == items) {
                        items = (ArrayList<RecommendItem>) recommendItems.clone();
                    } else {
                        items.clear();
                        items.addAll(recommendItems);
                    }
                    notifyDataSetChanged();
                } else {
                    int start = items.size();
                    int count = recommendItems.size();
                    items.addAll(recommendItems);
                    notifyItemRangeInserted(start, count);
                }
            }
        }
    }

    public void setAd(RecommendItem adItem) {
        if (null == items) {
            items = new ArrayList<>(21);
            items.add(adItem);
        } else {
            items.add(0, adItem);
        }
        notifyItemInserted(0);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }

    @Override

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case RecommendItem.TYPE_POST:
                view = LayoutInflater.from(context).inflate(R.layout.item_post_recommend_new, parent, false);
                return new PostViewHolder(view);
            case RecommendItem.TYPE_RAIDERS:
                view = LayoutInflater.from(context).inflate(R.layout.item_raiders_recommend, parent, false);
                return new RaidersViewHolder(view);
            default:
                view = LayoutInflater.from(context).inflate(R.layout.item_ad, parent, false);
                return new ADViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (0 < getItemCount()) {
            RecommendItem item = items.get(position);
            if (holder instanceof PostViewHolder) {
                showPost((PostViewHolder) holder, item);
            } else if (holder instanceof RaidersViewHolder) {
                showRaides((RaidersViewHolder) holder, item);
            } else if (holder instanceof ADViewHolder) {
                showAd((ADViewHolder) holder, item);
            }

        }
    }

    @Override
    public int getItemCount() {
        return null == items ? 0 : items.size();
    }

    @Override
    public void onClick(View v) {
        int id = (int) v.getTag();
        if (null != listener) {
            if (0 < id) {
                switch (v.getId()) {
                    case R.id.ownerCover:
                    case R.id.ownerName:
                        listener.onUserClicked(id);
                        break;
                    case R.id.postItem_New:
                        Post post = new Post(id);
                        listener.onPostClicked(post);
                        break;
                    case R.id.raidersItem:
                        listener.onRaidersClicked(id);
                        break;
                }
            } else if (v.getId() == R.id.Aditem) {
                listener.onAdClicked(items.get(0).getUrl());
            }
        }


    }

    private void showPost(PostViewHolder holder, RecommendItem item) {
        if (null == holder.postCover.getTag() || !((String) holder.postCover.getTag()).equalsIgnoreCase(item.getIcon())) {
            imageLoader.displayImage(item.getIcon(), holder.postCover, normal);
            holder.postCover.setTag(item.getIcon());
        }
        if (null == holder.ownerCover.getTag() || ((int) holder.ownerCover.getTag()) != item.getUserId()) {
            imageLoader.displayImage(item.getHeadImg(), holder.ownerCover, circle);
        }
        holder.ownerName.setText(item.getUserName());
        holder.ownerCover.setTag(item.getUserId());
        holder.ownerName.setTag(item.getUserId());
        holder.ownerCover.setOnClickListener(this);
        holder.ownerName.setOnClickListener(this);
        holder.postTitle.setText(item.getName());
        holder.postType.setText(item.getKinds());
        holder.lastTime.setText(item.insertTime);
        holder.itemView.setTag(item.getId());
        holder.itemView.setOnClickListener(this);
    }

    private void showRaides(RaidersViewHolder holder, RecommendItem item) {
        holder.postTitle.setText(item.getName());
        holder.postType.setText(item.getKinds());
        holder.lastTime.setText(item.insertTime);
        holder.itemView.setTag(item.getId());
        holder.itemView.setOnClickListener(this);
    }

    private void showAd(ADViewHolder holder, RecommendItem item) {
        if (null == holder.cover.getTag()) {
            imageLoader.displayImage(item.getHeadImg(), holder.cover, normal);
        }
        holder.cover.setTag(item.url);
        holder.title.setText(item.getName() + "");
        holder.itemView.setTag(0);
        holder.itemView.setOnClickListener(this);
    }


    class PostViewHolder extends RecyclerView.ViewHolder {
        public ImageView postCover, ownerCover;
        public TextView postTitle, postType, lastTime, ownerName;

        public PostViewHolder(View itemView) {
            super(itemView);
            postCover = (ImageView) itemView.findViewById(R.id.postCover);
            ownerCover = (ImageView) itemView.findViewById(R.id.ownerCover);
            postTitle = (TextView) itemView.findViewById(R.id.postTitle);
            postType = (TextView) itemView.findViewById(R.id.postType);
            lastTime = (TextView) itemView.findViewById(R.id.lastTime);
            ownerName = (TextView) itemView.findViewById(R.id.ownerName);
        }
    }

    class RaidersViewHolder extends RecyclerView.ViewHolder {
        public TextView postTitle, postType, lastTime;

        public RaidersViewHolder(View itemView) {
            super(itemView);
            postTitle = (TextView) itemView.findViewById(R.id.postTitle);
            postType = (TextView) itemView.findViewById(R.id.postType);
            lastTime = (TextView) itemView.findViewById(R.id.lastTime);
        }
    }

    class ADViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView cover;
        public AppCompatTextView title;

        public ADViewHolder(View itemView) {
            super(itemView);

            cover = itemView.findViewById(R.id.AD_Cover);
            title = itemView.findViewById(R.id.AD_Title);
        }
    }
}
