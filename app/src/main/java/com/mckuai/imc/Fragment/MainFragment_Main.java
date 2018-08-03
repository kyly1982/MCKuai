package com.mckuai.imc.Fragment;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mckuai.imc.Activity.PostActivity;
import com.mckuai.imc.Activity.RaidersActivity;
import com.mckuai.imc.Activity.UserCenterActivity2;
import com.mckuai.imc.Adapter.RecommandPostAdapter;
import com.mckuai.imc.Base.BaseFragment;
import com.mckuai.imc.Bean.Page;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.RecommendAd;
import com.mckuai.imc.Bean.RecommendItem;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;

import java.util.ArrayList;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static com.mckuai.imc.Bean.RecommendItem.TYPE_AD;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment_Main extends BaseFragment implements RecommandPostAdapter.OnItemClickListener,
        MCNetEngine.onGetRecommendPostListener, SwipeRefreshLayout.OnRefreshListener, OnMoreListener,
        MCNetEngine.onLoadDaShengVideoListener, MCNetEngine.OnGetRecommendAdListener {
    private SuperRecyclerView listView;

    private RecyclerView.LayoutManager layoutManager;

    private RecommandPostAdapter adapter;
    private ArrayList<RecommendItem> items;
    private RecommendItem adItem;
    private Page page = new Page();
    private boolean isVideo = false;
    private boolean isADLoad = false;
    private boolean isInstalled = false;
    private String adFile = null;
    private long downloadId = 0;

    private MainFragment_Main instence = null;

    public MainFragment_Main() {
        instence = this;
    }

    public MainFragment_Main setData(int type) {
        switch (type) {
            case 0:
                mTitle = "推荐";
                isVideo = false;
                break;
            default:
                mTitle = "视频";
                isVideo = true;
                break;
        }
        return instence;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
//            if (isVideo){
//                if (!mApplication.isEnableDaShengVideo){
//                    listView.getEmptyView().setVisibility(View.VISIBLE);
//                    listView.setLoadingMore(false);
//                    listView.setRefreshing(false);
//                    showData();
////                    Intent intent = new Intent(getActivity(), AdActivity.class);
////                    intent.putExtra("AD_TAG",1);
////                    startActivityForResult(intent,214);
//                } else {
//                    showData();
//                }
//            } else {
//                showData();
//            }
        }
    }


    private void initView() {
        listView = (SuperRecyclerView) view.findViewById(R.id.list_content);
        listView.setBackgroundColor(Color.WHITE);
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
        listView.setLoadingMore(true);
        listView.setRefreshListener(this);
        listView.setupMoreListener(this, 1);
    }

    private void showData() {
        if (null == adItem && !isVideo) {
            loadAd();
        }
        if (null == items || items.isEmpty()) {
            loadData();
        } else {
            if (null == adapter) {
                adapter = new RecommandPostAdapter(getActivity(), this);
                listView.setAdapter(adapter);
            }
            if (null != adItem && null != items) {
                if (1 == page.getPage() && items.get(0).getItemType() != TYPE_AD) {
                    items.add(0, adItem);
                }
                adapter.setData(items, page.getPage() == 1);

                if (null != items && !items.isEmpty()) {
                    listView.getEmptyView().setVisibility(View.GONE);
                }
            }
        }

    }

    private void loadData() {
        Log.e("MFM", "loadData,isLoading=" + isLoading);
        if (isLoading) {
            return;
        }
        isLoading = true;
        if (isVideo) {
            mApplication.netEngine.loadVideo(getActivity(), this, page.getNextPage());
        } else {
            mApplication.netEngine.loadRecommendPost(getActivity(), this, page.getNextPage());
        }
    }

    private void loadAd() {
        if (null != mApplication && null != mApplication.netEngine) {
            mApplication.netEngine.getRecommendAd(getActivity(), this);
        }
    }

    private void startDownloadApk(String url) {
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        String fileName = url.substring(url.lastIndexOf('/'));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setMimeType("application/vnd.android.package-archive");//某些机型必须设置，才能在点击通知栏时正确打开安装界面
        request.setVisibleInDownloadsUi(true);
        //定制Notification样式
        if (null != items && !items.isEmpty() && items.get(0).getItemType() == TYPE_AD) {
            request.setTitle(items.get(0).getName());
            request.setDescription("正在下载中，请稍候...");
            request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }


        downloadId = downloadManager.enqueue(request);
    }

    private void cancleDownload() {
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.remove(downloadId);
    }

    private void installApk() {

    }

    @Override
    public void onPostClicked(Post item) {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.tag_post), item);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onRaidersClicked(int id) {
        Intent intent = new Intent(getActivity(), RaidersActivity.class);
        intent.putExtra("RAIDERS_ID", id);
        startActivity(intent);
    }

    @Override
    public void onUserClicked(int userId) {
        Intent intent = new Intent(getActivity(), UserCenterActivity2.class);
        intent.putExtra(getString(R.string.usercenter_tag_userid), userId);
        startActivity(intent);
    }

    @Override
    public void onAdClicked(String downloadUrl) {
        if (0 == downloadId) {
            startDownloadApk(downloadUrl);
        } else {
            showMessage("游戏已添加到下载，点开顶部通知查看详情！", null, null);
        }
    }

    @Override
    public void onRefresh() {
        if (isLoading) {
            listView.setRefreshing(false);
            if (isVideo) {
                mApplication.netEngine.stopLoadVideo();
            } else {
                mApplication.netEngine.stopLoadRecommendPost();
            }
        }
        page.setPage(0);
        if (null != items && !items.isEmpty()) {
            items.clear();
            //adapter.setData(items, page.getPage() == 1);
        }
        loadData();
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (!isLoading) {
            if (!page.EOF()) {
                loadData();
            } else {
                showMessage("没有更多了!", null, null);
            }
        }
    }

    //加载推荐页回调
    @Override
    public void onGetRecommendPostSuccess(ArrayList<RecommendItem> recommendItems, int currentPage, int pageCount) {
        isLoading = false;
        page.setPage(currentPage);
        page.setAllCount(pageCount);
        if (1 == currentPage) {
            if (null == items) {
                items = recommendItems;
            } else {
                items.clear();
                items.addAll(recommendItems);
            }
        } else {
            items = recommendItems;
            //items.addAll(recommendItems);
        }
        showData();
        listView.setLoadingMore(page.EOF());
    }

    @Override
    public void onGetRecommendPostFailure(String msg) {
        isLoading = false;
        showError(msg, null, null);
    }

    //加载视频回调
    @Override
    public void onLoadVideoSuccess(ArrayList<RecommendItem> items, int currentPage, int allCount) {
        isLoading = false;
        page.setPage(currentPage);
        page.setAllCount(allCount);
        if (1 == currentPage) {
            if (null == this.items) {
                this.items = items;
            } else {
                this.items.clear();
                this.items.addAll(items);
            }
        } else {
            this.items.addAll(items);
        }
        showData();
        listView.setLoadingMore(page.EOF());
    }

    @Override
    public void onLoadVideoFailure(String msg) {
        isLoading = false;
        showError(msg, null, null);
    }

    //加载广告回调
    @Override
    public void onGetRecommendADSuccess(RecommendAd ad) {
        adItem = new RecommendItem(ad);
        showData();
    }

    @Override
    public void onGetRecommendAdFail(String msg) {
        showError(msg, null, null);
    }
}
