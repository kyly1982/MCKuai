package com.mckuai.imc.Fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mckuai.imc.Activity.PostActivity;
import com.mckuai.imc.Activity.UserCenterActivity2;
import com.mckuai.imc.Adapter.RecommendAdapter;
import com.mckuai.imc.Base.BaseFragment;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/7/20.
 */
public class MainFragment_Recommend extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, MCNetEngine.OnLoadRecommendListener, RecommendAdapter.OnItemClickListener {
    private SuperRecyclerView listView;


    private RecyclerView.LayoutManager layoutManager;
    private RecommendAdapter adapter;
    private ArrayList<Post> posts;

    public MainFragment_Recommend() {
        mTitle = "推荐";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == listView) {
            view = inflater.inflate(R.layout.fragment_list, container, false);

        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != view && null == layoutManager) {
            initView();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showData();
        }
    }

    private void initView() {
        listView = (SuperRecyclerView) view.findViewById(R.id.list_content);


        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int height = getResources().getDimensionPixelOffset(R.dimen.dividerPrimary);
                outRect.set(0, 0, 0, height);
            }


            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
                c.drawColor(getResources().getColor(R.color.dividerColorPrimary));
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
            }
        });
        listView.setLayoutManager(layoutManager);
        listView.getRecyclerView().setHasFixedSize(true);
        listView.setLoadingMore(false);
        listView.setRefreshListener(this);
    }

    private void loadData() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        mApplication.netEngine.loadRecommend(getActivity(), mApplication.isLogin() ? mApplication.user.getId() : 0, this);
    }

    private void showData() {
        if (null == posts) {
            loadData();
        } else {
            if (null == adapter) {
                adapter = new RecommendAdapter(getActivity(), this);
                listView.setAdapter(adapter);
            }
            adapter.setData(posts);
        }
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void onLoadRecommendFailure(String msg) {
        isLoading = false;

    }

    @Override
    public void onLoadRecommendSuccess(ArrayList<Post> recommendList) {
        isLoading = false;
        if (null == recommendList) {
            posts = new ArrayList<>(0);
        } else {
            posts = recommendList;
        }
        showData();
    }

    @Override
    public void onItemClicked(int position, Post post) {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.tag_post), post);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onItemUserClicked(int userId) {
        Intent intent = new Intent(getActivity(), UserCenterActivity2.class);
        intent.putExtra(getString(R.string.usercenter_tag_userid), userId);
        startActivity(intent);
    }
}
