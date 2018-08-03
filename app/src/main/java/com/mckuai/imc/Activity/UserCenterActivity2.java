package com.mckuai.imc.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mckuai.imc.Adapter.CommunityDynamicAdapter;
import com.mckuai.imc.Adapter.CommunityMessageAdapter;
import com.mckuai.imc.Adapter.FriendAdapter_new;
import com.mckuai.imc.Adapter.PostAdapter;
import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.CommunityDynamic;
import com.mckuai.imc.Bean.CommunityMessage;
import com.mckuai.imc.Bean.MCUser;
import com.mckuai.imc.Bean.Page;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.umeng.socialize.media.UMImage;

import java.util.ArrayList;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class UserCenterActivity2 extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener,
        MCNetEngine.OnLoadCommunityDynamicResponseListener,
        MCNetEngine.OnLoadCommunityMessageResponseListener,
        MCNetEngine.OnloadCommunityWorkResponseListener,
        MCNetEngine.OnloadFriendResponseListener,
        MCNetEngine.OnAddFriendResponseListener,
        OnMoreListener,
        SwipeRefreshLayout.OnRefreshListener,
        CommunityMessageAdapter.OnItemClickListener,
        CommunityDynamicAdapter.OnItemClickListener,
        PostAdapter.OnItemClickListener,
        FriendAdapter_new.OnItemClickListener {

    private User user;
    private int contentTypeId = R.id.dynamic;//默认显示社区动态

    private MCKuai mApplication = MCKuai.instence;

    private CommunityDynamicAdapter communityDynamicAdapter;
    private CommunityMessageAdapter communityMessageAdapter;
    private PostAdapter communityWorkAdapter;
    private FriendAdapter_new friendAdapter;
    private Page communityDynamicPage;
    private Page communityMessagePage;
    private Page communityWorkPage;
    private Page friendPage;

    private ArrayList<Post> works;
    private ArrayList<MCUser> friends;
    private ArrayList<CommunityMessage> messages;
    private ArrayList<CommunityDynamic> dynamics;

    private ImageLoader loader;

    private AppCompatImageView userCover;
    private AppCompatTextView userLevel;
    private AppCompatTextView title;
    private AppCompatImageButton setting;
    private RadioGroup subCategory;
    private SuperRecyclerView list;
    private AppCompatRadioButton message, dynamic;
    private Menu menu;
    private MenuItem community, friend, addFriend;

    private LinearLayoutManager layoutManager;

    private boolean checkedFriendship = false;
    private boolean isFriendShip = false;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter_new);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getParams();
        loader = ImageLoader.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == userCover) {
            initView();
        }
        showData();
    }

    private void initView() {
        title = (AppCompatTextView) findViewById(R.id.title);
        userCover = (AppCompatImageView) findViewById(R.id.usercover);
        userLevel = (AppCompatTextView) findViewById(R.id.userlevel);
        subCategory = (RadioGroup) findViewById(R.id.category);
        message = (AppCompatRadioButton) findViewById(R.id.message);
        dynamic = (AppCompatRadioButton) findViewById(R.id.dynamic);
        list = (SuperRecyclerView) findViewById(R.id.list);
        setting = (AppCompatImageButton) findViewById(R.id.setting);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        list.addItemDecoration(new RecyclerView.ItemDecoration() {
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

        list.setLayoutManager(layoutManager);
        list.setLoadingMore(true);
        list.setupMoreListener(this, 1);
        list.setRefreshListener(this);

        findViewById(R.id.share).setOnClickListener(this);
        setting.setOnClickListener(this);
        subCategory.setOnCheckedChangeListener(this);

        changeUIByUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    addFriend();
                    break;
                case 2:
                    startChat(null);
                    break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_usercenter, menu);
        if (null != menu) {
            this.menu = menu;
            community = menu.findItem(R.id.action_community);
            friend = menu.findItem(R.id.action_friend);
            addFriend = menu.findItem(R.id.action_addfriend);
            if (isMySelf()) {
                menu.setGroupVisible(R.id.group_operation, false);
            } else {
                menu.setGroupVisible(R.id.group_operation, true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_community:
                friend.setVisible(true);
                community.setVisible(false);
                if (isMySelf()) {
                    contentTypeId = R.id.message;
                } else {
                    contentTypeId = R.id.dynamic;
                }
                subCategory.setVisibility(View.VISIBLE);
                break;
            case R.id.action_friend:
                community.setVisible(true);
                friend.setVisible(false);
                contentTypeId = R.id.action_friend;
                subCategory.setVisibility(View.GONE);
                break;
            case R.id.action_addfriend:
                addFriend();
                return true;
            case R.id.action_chat:
                startChat(null);
                return true;
        }
        list.setAdapter(null);
        showData();
        return super.onOptionsItemSelected(item);
    }

    private void getParams() {
        Intent intent = getIntent();
        if (null != intent) {
            int userId = intent.getIntExtra(getString(R.string.usercenter_tag_userid), 0);
            if (mApplication.isLogin() && 0 == userId) {
                user = new User(mApplication.user);
            } else {
                user = new User((long) userId);
            }
        }
    }

    private void changeUIByUser() {
        if (isMySelf()) {
            contentTypeId = R.id.message;//自己，默认显示消息
            if (null != menu) menu.setGroupVisible(R.id.group_operation, false);
            message.setVisibility(View.VISIBLE);
            setting.setVisibility(View.VISIBLE);
            message.setChecked(true);
        } else {
            contentTypeId = R.id.dynamic;//它人，默认显示动态
            if (null != menu) menu.setGroupVisible(R.id.group_operation, true);
            message.setVisibility(View.GONE);
            dynamic.setChecked(true);
            setting.setVisibility(View.GONE);
        }
    }

    private boolean isMySelf() {
        return mApplication.isLogin() && mApplication.user.getId() == user.getId();
    }

    private void loadData() {
        isLoading = true;
        switch (contentTypeId) {
            case R.id.message:
                if (null == communityMessagePage) {
                    communityMessagePage = new Page();
                }
                mApplication.netEngine.loadCommunityMessage(this, user.getId().intValue(), communityMessagePage.getNextPage(), this);
                break;
            case R.id.dynamic:
                if (null == communityDynamicPage) {
                    communityDynamicPage = new Page();
                }
                mApplication.netEngine.loadCommunityDynamic(this, user.getId().intValue(), communityDynamicPage.getNextPage(), this);
                break;
            case R.id.work:
                if (null == communityWorkPage) {
                    communityWorkPage = new Page();
                }
                mApplication.netEngine.loadCommunityWork(this, user.getId().intValue(), communityWorkPage.getNextPage(), this);
                break;
            case R.id.friend:
                if (null == friendPage) {
                    friendPage = new Page();
                }
                mApplication.netEngine.loadFriendList(this, friendPage.getNextPage(), this);
                break;
            default:

                break;
        }
    }


    private void showData() {
        switch (contentTypeId) {

            case R.id.message://社区消息
                showCommunityMessages();
                break;
            case R.id.dynamic://社区动态
                showCommunityDynamics();
                break;
            case R.id.work://社区作品
                showWorks();
                break;
            case R.id.action_friend:
                showFridnds();
                break;
        }
    }

    private void showCommunityMessages() {
        if (null == communityMessagePage) {
            loadData();
            return;
        } else if (null == communityMessageAdapter) {
            communityMessageAdapter = new CommunityMessageAdapter(this, this);
        }
        if (null == list.getAdapter()) {
            list.setAdapter(communityMessageAdapter);
        }/* else if (!(list.getAdapter() instanceof  CommunityMessageAdapter)){
            //已经设置有其它的adapter，交换adapter并且删除原有的内容
            list.swapAdapter(communityMessageAdapter,false);
        }
        list.setAdapter(communityMessageAdapter);*/
        communityMessageAdapter.setData(messages, 1 == communityMessagePage.getPage());
        if (1 == communityMessagePage.getPage() && 0 < layoutManager.getChildCount()) {
            layoutManager.scrollToPosition(0);
        }
        showUserInfo();
    }

    private void showCommunityDynamics() {
        if (null == communityDynamicPage) {
            loadData();
            return;
        } else if (null == communityDynamicAdapter) {
            communityDynamicAdapter = new CommunityDynamicAdapter(this, this);
        }
        if (null == list.getAdapter()) {
            list.setAdapter(communityDynamicAdapter);
        }/* else if (!(list.getAdapter() instanceof  CommunityDynamicAdapter)){
            //已经设置有其它的adapter，交换adapter并且删除原有的内容
            list.swapAdapter(communityDynamicAdapter,false);
        }
        list.setAdapter(communityDynamicAdapter);*/
        communityDynamicAdapter.setData(dynamics, 1 == communityDynamicPage.getPage());
        if (1 == communityDynamicPage.getPage() && 0 < layoutManager.getChildCount()) {
            layoutManager.scrollToPosition(0);
        }
        showUserInfo();
    }

    private void showWorks() {
        if (null == communityWorkPage) {
            loadData();
            return;
        } else if (null == communityWorkAdapter) {
            communityWorkAdapter = new PostAdapter(this, this);
        }
        if (null == list.getAdapter()) {
            list.setAdapter(communityWorkAdapter);
        }
        if (1 == communityWorkPage.getPage()) {
            communityWorkAdapter.setData(works);
            layoutManager.scrollToPosition(0);
        } else {
            communityWorkAdapter.addData(works);
        }
        showUserInfo();
    }

    private void showFridnds() {
        if (null == friendPage) {
            loadData();
            return;
        } else if (null == friendAdapter) {
            friendAdapter = new FriendAdapter_new(this, this);
//            list.setAdapter(friendAdapter);
        }
        if (null == list.getAdapter()) {
            list.setAdapter(friendAdapter);
        }
        friendAdapter.setData(friends, 1 == friendPage.getPage());
        if (1 == friendPage.getPage() && 0 < layoutManager.getChildCount()) {
            layoutManager.scrollToPosition(0);
        }
        showUserInfo();
    }

    private void showUserInfo() {
        if (null != user && 0 != user.getId() && null != user.getNick() && null != user.getHeadImage()) {
            if (null == userCover.getTag() || !userCover.getTag().equals(user.getHeadImage()))
                loader.displayImage(user.getHeadImage(), userCover, mApplication.getCircleOptions(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        //super.onLoadingStarted(imageUri, view);
                        userCover.setImageResource(R.mipmap.ic_usercover_default);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (null != loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            userCover.setTag(user.getHeadImage());
                        } else {
                            userCover.setImageResource(R.mipmap.ic_usercover_default);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        userCover.setImageResource(R.mipmap.ic_usercover_default);
                    }
                });
        }
        title.setText(user.getNickEx());
//        ((CollapsingToolbarLayout)findViewById(R.id.toolbar_layout)).setTitle(user.getNick());
        userLevel.setText(getString(R.string.usercenter_userlevel, user.getLevel()));
    }

    private void resetUser(User user) {
        this.user = user;

        isLoading = true;//防止触发加载
        dynamic.setChecked(true);
        isLoading = false;

        communityMessageAdapter = null;
        communityDynamicAdapter = null;
        communityWorkAdapter = null;
        friendAdapter = null;

        communityMessagePage = null;
        communityDynamicPage = null;
        communityWorkPage = null;
        friendPage = null;
        list.setVisibility(View.VISIBLE);
        changeUIByUser();
        checkedFriendship = false;
        isFriendShip = false;
        findViewById(R.id.addfriend).setEnabled(true);
        loadData();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        contentTypeId = checkedId;
        list.setAdapter(null);
        showData();
    }

    private void scrollToFirst() {
        if (0 < layoutManager.getChildCount()) {
            layoutManager.scrollToPosition(0);
            list.setAdapter(null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addfriend:
                addFriend();
                break;
            case R.id.chat:
                startChat(null);
                break;
            case R.id.setting:
                showSetting();
                break;
            case R.id.share:
                share();
                break;
        }
    }

    private void addFriend() {
        if (mApplication.isLogin()) {
            mApplication.netEngine.addFriend(this, user.getId().intValue(), this);
        } else {
            callLogin(1);
        }
    }

    private void showSetting() {
        Intent intent = new Intent(this, ProfileEditerActivity.class);
        startActivity(intent);
    }

    private void share() {
        userCover.setDrawingCacheEnabled(true);
        userCover.buildDrawingCache();
        Bitmap cover = userCover.getDrawingCache();
        userCover.setDrawingCacheEnabled(false);
        UMImage image = null;
        if (null != cover) {
            image = new UMImage(this, cover);
        } else {
            image = new UMImage(this, R.mipmap.ic_share_default);
        }
        String url = "http://m.mckuai.com/u/" + user.getId();
        share("麦块大神",
                "我在麦块上发现了一个大神，大家速来围观",
                url,
                image);
    }

    private void startChat(MCUser chatUser) {
        final User target;
        if (null == chatUser) {
            target = this.user;
        } else {
            target = new User(chatUser);
        }
        if (mApplication.isLogin()) {
            if (RongIM.getInstance().getRongIMClient().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                RongIM.getInstance().startPrivateChat(this, target.getName(), target.getNickEx());
            } else {
                mApplication.loginIM(new MCKuai.IMLoginListener() {
                    @Override
                    public void onInitError() {
                        showMessage("聊天模块功能异常，请重启软件！", null, null);
                    }

                    @Override
                    public void onTokenIncorrect() {
                        showMessage("令牌已过期，需要重新登录，是否重启登录？", "重新登录", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mApplication.logout(UserCenterActivity2.this);
                                callLogin(2);
                            }
                        });
                    }

                    @Override
                    public void onLoginFailure(String msg) {
                        showMessage("登录聊天服务器失败，原因：" + msg, null, null);
                    }

                    @Override
                    public void onLoginSuccess(String msg) {
                        RongIM.getInstance().startPrivateChat(UserCenterActivity2.this, target.getName(), target.getNickEx());
                    }
                });
            }
        } else {
            callLogin(2);
        }

    }

    private void showPostDetailed(Post post) {
        Intent intent = new Intent(UserCenterActivity2.this, PostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.tag_post), post);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Override
    public void onLoadCommunityDynamicSuccess(ArrayList<CommunityDynamic> dynamics, User user, Page page) {
        isLoading = false;
        communityDynamicPage.clone(page);
        this.user = user;
        this.dynamics = dynamics;
        showCommunityDynamics();
    }

    @Override
    public void onLoadCommunityDynamicFailure(String msg) {
        isLoading = false;
        //让listview显示空视图
        if (0 == communityDynamicPage.getPage()) {
            dynamics = new ArrayList<>();
            showCommunityDynamics();
        }
        showMessage(msg, null, null);
    }

    @Override
    public void onLoadCommunityMessageSuccess(ArrayList<CommunityMessage> messages, User user, Page page) {
        isLoading = false;
        communityMessagePage.clone(page);
        this.user = user;
        this.messages = messages;
        showCommunityMessages();
    }

    @Override
    public void onLoadCommunityMessageFailure(String msg) {
        isLoading = false;
        if (0 == communityMessagePage.getPage()) {
            messages = new ArrayList<>();
            showCommunityMessages();
        }
        showMessage(msg, null, null);
    }

    @Override
    public void onLoadCommunityWorkSuccess(ArrayList<Post> works, User user, Page page) {
        isLoading = false;
        communityWorkPage.clone(page);
        this.user = user;
        this.works = works;
        showWorks();
    }

    @Override
    public void onLoadCommunityWorkFailure(String msg) {
        isLoading = false;
        if (0 == communityWorkPage.getPage()) {
            works = new ArrayList<>();
        }
        showMessage(msg, null, null);
    }

    @Override
    public void onLoadFriendSuccess(ArrayList<MCUser> friends, Page page) {
        isLoading = false;
        friendPage.clone(page);
        this.friends = friends;
        showFridnds();
    }

    @Override
    public void OnloadFriendFailure(String msg) {
        isLoading = false;
        if (0 == friendPage.getPage()) {
            friends = new ArrayList<>();
            showFridnds();
        }
        showMessage(msg, null, null);
    }

    @Override
    public void onAddFriendFailure(String msg) {
        isLoading = false;
        addFriend.setChecked(false);
        showMessage(msg, null, null);
    }

    @Override
    public void onAddFriendSuccess() {
        isLoading = false;
        addFriend.setChecked(true);
        showMessage("添加好友成功", null, null);
        findViewById(R.id.addfriend).setEnabled(false);
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        //如果已经是最后一面，则跳出
        switch (contentTypeId) {
            case R.id.message:
                if (communityMessagePage.EOF()) {
                    showMessage("没有更多了！", null, null);
                    return;
                }
                break;
            case R.id.dynamic:
                if (communityDynamicPage.EOF()) {
                    showMessage("没有更多了！", null, null);
                    return;
                }
                break;
            case R.id.work:
                if (communityWorkPage.EOF()) {
                    showMessage("没有更多了！", null, null);
                    return;
                }
                break;
            case R.id.friend:
                if (friendPage.EOF()) {
                    showMessage("没有更多了！", null, null);
                    return;
                }
                break;
        }
        loadData();
    }

    @Override
    public void onRefresh() {
        switch (contentTypeId) {
            case R.id.message:
                if (null != communityMessagePage) {
                    communityMessagePage.setPage(0);
                }
                break;
            case R.id.dynamic:
                if (null != communityDynamicPage) {
                    communityDynamicPage.setPage(0);
                }
                break;
            case R.id.work:
                if (null != communityWorkPage) {
                    communityWorkPage.setPage(0);
                }
                break;
            case R.id.action_friend:
                if (null != friendPage) {
                    friendPage.setPage(0);
                }
                break;
        }
        loadData();
    }

    /**
     * 作品列表里点击头像的回调
     * 因不在个人中心中不会设置此处的点击事件，此回调不可能会被触发
     *
     * @param user
     */
    @Override
    public void onUserClicked(User user) {
        //resetUser(user);
    }

    /**
     * 作品列表里点击作品的回调
     * 打开此帖子
     *
     * @param post
     */
    @Override
    public void onItemClicked(Post post) {
        showPostDetailed(post);
    }

    /**
     * 好友列表里点击私聊的回调
     * 打开聊天界面
     *
     * @param user
     */
    @Override
    public void onChatClicked(MCUser user) {
        startChat(user);
    }

    /**
     * 好友列表里点击好友的回调
     * 打开其个人中心
     *
     * @param user
     */
    @Override
    public void onItemClicked(MCUser user) {
        resetUser(new User(user));
    }

    /**
     * 动态列表里点击动态的回调
     * 将打开此帖子
     *
     * @param dynamic
     */
    @Override
    public void onItemClicked(CommunityDynamic dynamic) {
        Post post = new Post(dynamic.getId());
        showPostDetailed(post);
    }

    /**
     * 消息列表点击消息的回调
     * 打开此帖子
     *
     * @param message
     */
    @Override
    public void onItemClicked(CommunityMessage message) {
        Post post = new Post(message.getId());
        showPostDetailed(post);
    }

    public void callLogin(int requestId) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, requestId);
    }

    public void showMessage(String msg, String action, View.OnClickListener actionClickListener) {
        Snackbar snackbar = Snackbar.make(list, msg, Snackbar.LENGTH_SHORT);
        if (null != action && null != actionClickListener) {
            snackbar.setAction(action, actionClickListener).setActionTextColor(getResources().getColor(R.color.msg_normal)).show();
        }
    }

}
