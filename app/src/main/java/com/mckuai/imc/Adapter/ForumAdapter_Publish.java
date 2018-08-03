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

import com.mckuai.imc.Bean.ForumInfo;
import com.mckuai.imc.R;

import java.util.ArrayList;

/**
 * @author kyly
 */
public class ForumAdapter_Publish extends RecyclerView.Adapter<ForumAdapter_Publish.ViewHolder> {

    private Context context;
    private ArrayList<ForumInfo> forumList;
    private OnItemClickListener listener;
    private AppCompatTextView selectedView = null;

    public interface OnItemClickListener {
        void onItemClicked(ForumInfo forumInfo);
    }

    public ForumAdapter_Publish(Context context, ArrayList<ForumInfo> forumList, OnItemClickListener listener) {
        this.context = context;
        this.forumList = forumList;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return null == forumList ? 0 : forumList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publish_froum, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ForumInfo forum = forumList.get(position);
        if (null != forum) {
            holder.name.setText(forum.getName());
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
                        listener.onItemClicked(forum);
                    }
                });
            }
            //第一次显示时，设置选中第一个
            if (null == selectedView && 0 == position && 0 != getItemCount()) {
                selectedView = holder.name;
                setSelectedColor(selectedView);
                listener.onItemClicked(forum);
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
