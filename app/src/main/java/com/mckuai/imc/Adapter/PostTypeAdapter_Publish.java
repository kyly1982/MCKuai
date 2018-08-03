/**
 *
 */
package com.mckuai.imc.Adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mckuai.imc.Bean.PostType;
import com.mckuai.imc.R;

import java.util.ArrayList;

/**
 * @author kyly
 */
public class PostTypeAdapter_Publish extends RecyclerView.Adapter<PostTypeAdapter_Publish.ViewHolder> {
    private Context context;
    private ArrayList<PostType> typeList;
    private OnItemClickListener listener;
    private AppCompatTextView selectedView = null;

    public interface OnItemClickListener {
        void onItemClicked(PostType postType, boolean isManual);
    }

    public PostTypeAdapter_Publish(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(ArrayList<PostType> postTypes) {
        this.typeList = postTypes;
        if (null != selectedView) {
            setUnSelectedColor(selectedView);
        }
        selectedView = null;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return null == typeList ? 0 : typeList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publish_froum, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PostType type = typeList.get(position);
        if (null != type) {
            holder.name.setText(type.getSmallName());
            if (null != listener) {
                holder.name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //移除旧选择项的色彩
                        if (null != selectedView) {
                            setUnSelectedColor(selectedView);
                        }
                        //设置新选择项的色彩
                        selectedView = (AppCompatTextView) v;
                        setSelectedColor(selectedView);
                        listener.onItemClicked(type, false);
                    }
                });
            }
            //第一次显示时，设置选中第一个
            if (null == selectedView && 0 == position && 0 != getItemCount()) {
                selectedView = holder.name;
                setSelectedColor(selectedView);
                listener.onItemClicked(type, true);
            }
        }
    }

    private void setSelectedColor(AppCompatTextView view) {
        view.setBackgroundResource(R.color.colorPrimary);
        view.setTextColor(context.getResources().getColor(R.color.color_white));
    }

    private void setUnSelectedColor(AppCompatTextView view) {
        view.setBackgroundResource(R.color.color_white);
        view.setTextColor(context.getResources().getColor(R.color.textColorSecondary));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (AppCompatTextView) itemView;
        }
    }
}
