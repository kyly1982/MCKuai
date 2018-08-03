package com.mckuai.imc.Fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mckuai.imc.Activity.PostActivity;
import com.mckuai.imc.Activity.PublishPostActivity;
import com.mckuai.imc.Activity.UserCenterActivity2;
import com.mckuai.imc.Adapter.ForumAdapter;
import com.mckuai.imc.Adapter.PostAdapter;
import com.mckuai.imc.Base.BaseFragment;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.ForumInfo;
import com.mckuai.imc.Bean.Page;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;

import java.util.ArrayList;


public class MainFragment_Community extends BaseFragment
        implements View.OnClickListener, MCNetEngine.OnForumListResponseListener, MCNetEngine.OnPostListResponseListener, ForumAdapter.OnItemClickListener, PostAdapter.OnItemClickListener, RadioGroup.OnCheckedChangeListener {
    private Page page;
    private ArrayList<ForumInfo> mForums;
    private ArrayList<Post> mPosts;
    private String[] postType = {"lastChangeTime", "isJing", "isDing"};
    private int typeindex = 0;
    private ForumAdapter forumAdapter;
    private PostAdapter postAdapter;
    private MCNetEngine mNetEngine;
    private int currentForumIndex = 0;

    private SuperRecyclerView mForumList;
    private SuperRecyclerView mPostList;
    private AppCompatImageButton mCreatePost;
    private RadioGroup postGroup;

    private LinearLayoutManager postLayoutManager;


    public MainFragment_Community() {
        mTitle = "社区";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == view) {
            view = inflater.inflate(R.layout.fragment_main_community, container, false);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null == mForumList) {
            initView();
            mNetEngine = MCKuai.instence.netEngine;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (null != view) {
            if (!hidden) {
                showForum();
            }
        }
    }

    private void initView() {
        mForumList = (SuperRecyclerView) view.findViewById(R.id.community_forumlist);
        mPostList = (SuperRecyclerView) view.findViewById(R.id.community_postlist);
        mCreatePost = (AppCompatImageButton) view.findViewById(R.id.community_createpost);
        postGroup = (RadioGroup) view.findViewById(R.id.posttype_indicator);

        mPostList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int height = getResources().getDimensionPixelOffset(R.dimen.dividerSecondary);
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

        mForumList.getRecyclerView().setHasFixedSize(true);
        mPostList.getRecyclerView().setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mForumList.setLayoutManager(manager);
        postLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mPostList.setLayoutManager(postLayoutManager);
        mForumList.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadForumList();
            }
        });

        mPostList.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (null == page) {
                    page = new Page();
                } else page.setPage(0);
                loadPostList();
            }
        });

        mPostList.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
                loadPostList();
            }
        }, 1);

        postGroup.setOnCheckedChangeListener(this);
        mCreatePost.setOnClickListener(this);
    }

    private void loadForumList() {
        if (null == mNetEngine) {
            MCKuai.instence.init();
        }
        mNetEngine.loadFroumList(getActivity(), this);
    }

    private void loadPostList() {
        if (null != mForums) {
            mNetEngine.loadPostList(getActivity(), mForums.get(currentForumIndex).getId(), postType[typeindex], page.getNextPage(), this);
        }
    }

    private void showForum() {
        if (null == mForums) {
            loadForumList();
        } else {
            if (null == forumAdapter) {
                forumAdapter = new ForumAdapter(getActivity(), this);
                mForumList.setAdapter(forumAdapter);
            }
            forumAdapter.setData(mForums);
        }
    }

    private void showPost() {
        if (null == mPosts) {
            loadPostList();
        } else {
            if (null == postAdapter) {
                postAdapter = new PostAdapter(getActivity(), this);
                mPostList.setAdapter(postAdapter);
            }
            postAdapter.setData(mPosts);
            if (1 == page.getPage() && postLayoutManager.getChildCount() > 0) {
                postLayoutManager.scrollToPosition(0);
            }
        }
    }

    private void hideProgress() {
        mForumList.hideProgress();
        mForumList.hideMoreProgress();
        mPostList.hideProgress();
        mPostList.hideMoreProgress();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.community_createpost:
                Intent intent = new Intent(getActivity(), PublishPostActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("FORUM_LIST", mForums);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.lastpost:
                typeindex = 0;
                break;
            case R.id.essencepost:
                typeindex = 1;
                break;
            case R.id.toppost:
                typeindex = 2;
                break;
        }
        if (null != page) {
            page.setPage(0);
        } else {
            page = new Page();
        }
        loadPostList();
    }

    @Override
    public void onLoadForumListFailure(String msg) {
        showMessage("加载板块失败，原因：" + msg, null, null);
        hideProgress();
    }

    @Override
    public void onLoadForumListSuccess(ArrayList<ForumInfo> forums) {
        if (null == page) {
            this.mForums = forums;
            page = new Page(0, 0, 20);
            mCreatePost.setVisibility(View.VISIBLE);
            showForum();
            showPost();
        }
    }

    @Override
    public void onLoadPostListSuccess(ArrayList<Post> posts, Page page) {
        this.page = page;
        if (1 == page.getPage()) {
            mPosts = posts;
        } else if (null != posts) {
            mPosts.addAll(posts);
        }
        showPost();
    }

    @Override
    public void onLoadPostListFailure(String msg) {
        showMessage("加载失败，向下拉动以重新加载", null, null);
        hideProgress();
    }


    @Override
    public void onItemClicked(int forumPosition) {
        currentForumIndex = forumPosition;
        forumAdapter.notifyDataSetChanged();
        page.setPage(0);
        loadPostList();
    }

    @Override
    public void onItemClicked(Post post) {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.tag_post), post);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getActivity(), UserCenterActivity2.class);
        intent.putExtra(getString(R.string.usercenter_tag_userid), user.getId().intValue());
        startActivity(intent);
    }
}
