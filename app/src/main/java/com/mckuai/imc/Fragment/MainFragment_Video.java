package com.mckuai.imc.Fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mckuai.imc.Activity.PostActivity;
import com.mckuai.imc.Activity.UserCenterActivity2;
import com.mckuai.imc.Adapter.VideoAdapter;
import com.mckuai.imc.Base.BaseFragment;
import com.mckuai.imc.Bean.Page;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.VideoBean;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/7/20.
 */
public class MainFragment_Video extends BaseFragment implements
        MCNetEngine.OnLoadVideoListener, VideoAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, OnMoreListener {
    private SuperRecyclerView listView;
    private RecyclerView.LayoutManager layoutManager;

    private Page page = new Page();
    private VideoAdapter adapter;
    private ArrayList<Post> posts;

    private String orderType = "new";
    public String videoType = "动画";//默认显示动画的

    public MainFragment_Video() {
        mTitle = "视频";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == view) {
            view = inflater.inflate(R.layout.fragment_list, container, false);
            listView = (SuperRecyclerView) view.findViewById(R.id.list_content);
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
        if (!hidden && null != view) {
            showData(false);
        }
    }


    private void initView() {
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
        listView.setLoadingMore(true);
        listView.setRefreshListener(this);
        listView.setupMoreListener(this, 1);
    }


    public void setVideoType(@IdRes int actionId) {
        switch (actionId) {
            case R.id.action_new:
                orderType = "new";
                break;
            case R.id.action_hot:
                orderType = "hot";
                break;
            case R.id.action_cartoon:
                videoType = "动画";
                break;
            case R.id.action_knowledge:
                videoType = "教程";
                break;
            case R.id.action_map:
                videoType = "地图";
                break;
            case R.id.action_mod:
                videoType = "模组";
                break;
            case R.id.action_show:
                videoType = "鉴赏";
                break;
            case R.id.action_online:
                videoType = "联机";
                break;
        }
        page.setPage(0);
        loadData();
    }

    private void showData(boolean isRefresh) {
        if (0 == page.getPage()) {
            loadData();
        } else {
            if (null == adapter) {
                adapter = new VideoAdapter(getActivity(), this);
                adapter.setDisplayOperations(mApplication.getCircleOptions(), mApplication.getNormalOptions());
                listView.setAdapter(adapter);
            }
            adapter.setData(posts, isRefresh);
            if (isRefresh) {
                layoutManager.scrollToPosition(0);
            }
        }
    }

    private void loadData() {
        if (!isLoading) {
            if (0 == page.getPage() || page.getPage() < page.getAllCount()) {
                mApplication.netEngine.loadVideoList(getActivity(),
                        videoType,
                        orderType,
                        null == page ? 1 : page.getNextPage(),
                        this);
                isLoading = true;
            } else {
                showMessage("没有更多了！", null, null);
            }
        }
    }

    @Override
    public void onRefresh() {
        if (null != page) {
            page.setPage(0);
        }
        loadData();
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (!page.EOF()) {
            loadData();
        } else {
            showMessage("没有更多了！", null, null);
        }
    }

    @Override
    public void onLoadVideoFailure(String msg) {
        isLoading = false;
        listView.hideProgress();
        listView.hideMoreProgress();
        showMessage(msg, null, null);
    }

    @Override
    public void onLoadVideoSuccess(VideoBean video) {
        isLoading = false;
        listView.hideMoreProgress();
        listView.hideProgress();
        this.page = video.getPageInfo();
        posts = video.getData();
        if (1 == page.getPage()) {
            showData(true);//刷新
        } else {
            showData(false);//添加
        }

    }

    @Override
    public void onItemClicked(int position, Post item) {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.tag_post), item);
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
