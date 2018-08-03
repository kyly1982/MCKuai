package com.mckuai.imc.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.Fragment.MainFragment_Chat;
import com.mckuai.imc.Fragment.MainFragment_Community;
import com.mckuai.imc.Fragment.MainFragment_Main;
import com.mckuai.imc.Fragment.MainFragment_Video;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.media.UMImage;

import java.util.ArrayList;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        RadioGroup.OnCheckedChangeListener,
        RongIMClient.OnReceiveMessageListener,
        RongIM.OnReceiveUnreadCountChangedListener,
        RongIM.UserInfoProvider {

    private DrawerLayout slidingView;
    private AppCompatImageButton userCover;
    private AppCompatTextView userName;
    private AppCompatTextView userLevel;
    private RadioGroup radioGroup;
    private AppCompatRadioButton nav_video, nav_chat;
    private View msgIndicator;

    private ImageLoader loader;
    private DisplayImageOptions circleDisplayOption;


    private FragmentManager fragmentManager;
    private ArrayList<Fragment> fragments;
    private int currentFragmentIndex = 0;

    private Menu menu;
    private MenuItem type_new, type_hot;

    private final int LOGIN_USERCENTER = 0;
    private final int LOGIN_SETTING = 1;
    private final int LOGIN_PACKAGE = 2;
    private long lastBackClick = 0;

//    private boolean isShowNew = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        initSlidingMenuView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == radioGroup) {
            initView();
            initImageLoader();
            initFragment();
            initIMListener();
        }

        if (null != fragmentManager && null != fragments) {
            fragmentManager.beginTransaction().show(fragments.get(currentFragmentIndex)).commit();
        }

        if (mApplication.isLogin()) {
            refreshUser();
        }
    }

    @Override
    protected void onPause() {
        mApplication.netEngine.exit();
        mApplication.saveProfile();
        super.onPause();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 214) {
            mApplication.isEnableDaShengVideo = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 设置侧边栏
     */
    public void initSlidingMenuView() {

        //设置侧边栏
        slidingView = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, slidingView, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        slidingView.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                Log.e("OHC", "onChildViewAdded");
            }

            @Override
            public void onChildViewRemoved(View view, View view1) {
                Log.e("OHC", "onChildViewAdded");
            }
        });

        navigationView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                Log.e("OHC", "onHover");
                return false;
            }
        });


        View view = navigationView.inflateHeaderView(R.layout.nav_header_base);
        userCover = (AppCompatImageButton) view.findViewById(R.id.slh_userCover);
        userName = (AppCompatTextView) view.findViewById(R.id.slh_userName);
        userLevel = (AppCompatTextView) view.findViewById(R.id.slh_userLevel);
        userCover.setOnClickListener(this);

    }


    private void initView() {
        radioGroup = (RadioGroup) findViewById(R.id.nav);
        nav_video = (AppCompatRadioButton) findViewById(R.id.nav_video);
        nav_chat = (AppCompatRadioButton) findViewById(R.id.nav_chat);
        msgIndicator = findViewById(R.id.msgIndicator);

        radioGroup.setOnCheckedChangeListener(this);
        mTitle.setText("推荐");

        ViewTreeObserver vto = nav_chat.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) msgIndicator.getLayoutParams();
                layoutParams.leftMargin = nav_chat.getRight() - msgIndicator.getWidth() - (nav_chat.getWidth() / 5);
                layoutParams.topMargin = nav_chat.getHeight() / 10;
                msgIndicator.setLayoutParams(layoutParams);
                msgIndicator.postInvalidate();
                nav_chat.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void initImageLoader() {
        if (null == loader) {
            loader = ImageLoader.getInstance();
            circleDisplayOption = mApplication.getCircleOptions();
        }
    }

    private void initIMListener() {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            RongIM.setUserInfoProvider(this, true);
            RongIM.setOnReceiveMessageListener(this);
            RongIM.getInstance().setOnReceiveUnreadCountChangedListener(this, Conversation.ConversationType.PRIVATE);
        }
    }

    private void initFragment() {
        fragments = new ArrayList<>(4);
        fragments.add(new MainFragment_Main().setData(0));
        //fragments.add(new MainFragment_Main().setData(1));
        fragments.add(new MainFragment_Video());
        fragments.add(new MainFragment_Chat());
        fragments.add(new MainFragment_Community());

        LinearLayout contentView = (LinearLayout) findViewById(R.id.fragment_content);

        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.add(contentView.getId(), fragment);
            transaction.hide(fragment);
        }
        transaction.commit();
        currentFragmentIndex = 0;
    }


    @Override
    public void onBackPressed() {
        if (slidingView.isDrawerOpen(GravityCompat.START)) {
            slidingView.closeDrawer(GravityCompat.START);
        } else {

            if (3000 < System.currentTimeMillis() - lastBackClick) {
                lastBackClick = System.currentTimeMillis();
                showMessage("再次点击返回退出程序！", null, null);
            } else {
                MCKuai.instence.handleExit();
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_videocategory, menu);
//        this.menu = menu;
//        type_hot = menu.findItem(R.id.action_hot);
//        type_new = menu.findItem(R.id.action_new);
        return true;
    }

    /**
     * toolbar中的菜单
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        item.setChecked(true);
//        if (nav_video.isChecked()){
//            ((MainFragment_Video)fragments.get(1)).setVideoType(item.getItemId());
//        }
        return true;
    }

    /**
     * 侧边栏点击事件
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        closeSlidingMenu();
        switch (item.getItemId()) {
            case R.id.nav_search:
                showSearch();
                break;
            case R.id.nav_package:
                showPackage();
                break;
            case R.id.nav_setting:
                showSetting();
                break;
            case R.id.nav_logout:
                logout();
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_prise:
                priseApp();
                break;
            case R.id.nav_upgread:
                checkUpgread(false);
                break;
        }
        return true;
    }

    /*
    底部导航栏事件
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (null != fragments && !fragments.isEmpty()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (0 <= currentFragmentIndex) {
                transaction.hide(fragments.get(currentFragmentIndex));
            }

//            if (1 == currentFragmentIndex){
//                menu.setGroupVisible(R.id.action_videotype,false);
//                menu.setGroupVisible(R.id.action_ordertype,false);
//            } else if (1 != currentFragmentIndex && R.id.nav_video == checkedId){
//                menu.setGroupVisible(R.id.action_videotype,true);
//                menu.setGroupVisible(R.id.action_ordertype,true);
//            }

            switch (checkedId) {
                case R.id.nav_recommend:
                    MobclickAgent.onEvent(this, "clickCartoon");
                    mTitle.setText("推荐");
                    currentFragmentIndex = 0;
                    break;
                case R.id.nav_video:
                    MobclickAgent.onEvent(this, "clickChat");
                    mTitle.setText("视频");
                    currentFragmentIndex = 1;
                    break;
                case R.id.nav_chat:
                    MobclickAgent.onEvent(this, "clickChat");
                    mTitle.setText("聊天");
                    currentFragmentIndex = 2;
                    break;
                case R.id.nav_community:
                    MobclickAgent.onEvent(this, "clickMine");
                    mTitle.setText("社区");
                    currentFragmentIndex = 3;
                    break;
            }
            transaction.show(fragments.get(currentFragmentIndex)).commit();
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slh_userCover:
                closeSlidingMenu();
                showUserCenter();
                break;
        }
    }

    @Override
    public boolean onReceived(Message message, int i) {
        if (null != message)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkUnReadMsg();
                    ((MainFragment_Chat) fragments.get(1)).onNewMsgRecived();
                }

            });
        return false;
    }

    @Override
    public void onMessageIncreased(int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkUnReadMsg();
            }
        });
    }

    @Override
    public UserInfo getUserInfo(String id) {
        if (id.equals(mApplication.user.getName())) {
            UserInfo userInfo = new UserInfo(id, mApplication.user.getNike(), Uri.parse(mApplication.user.getHeadImg()));
            return userInfo;
        }
        User user = mApplication.daoHelper.getUserByName(id);
        if (null == user) {
            mApplication.netEngine.loadUserInfo(this, id, new MCNetEngine.OnLoadUserInfoResponseListener() {
                @Override
                public void onLoadUserInfoSuccess(User user) {
                }

                @Override
                public void onLoadUserInfoFailure(String msg) {
                    showMessage(msg, null, null);
                }
            });
            return null;
        } else {
            UserInfo userInfo = new UserInfo(id, user.getNickEx(), Uri.parse(user.getHeadImage()));
            return userInfo;
        }
    }

    private void refreshUser() {
        if (mApplication.isLogin()) {
            loader.displayImage(mApplication.user.getHeadImg(), userCover, circleDisplayOption);
            userName.setText(mApplication.user.getNike());
            userLevel.setText("LV " + mApplication.user.getLevel());
        } else {
            userCover.setImageResource(R.mipmap.ic_usercover_default);
            userName.setText("未登录");
            userLevel.setText("点击头像登录");
        }
    }

    private void showSearch() {
        Intent intent = new Intent(this, SearchActivtiy_new.class);
        intent.putExtra("TYPE", 1);
        startActivity(intent);
    }

    private void showPackage() {
        if (mApplication.isLogin()) {
            Intent intent = new Intent(this, SearchActivtiy_new.class);
            Bundle bundle = new Bundle();

            bundle.putSerializable("PACKAGE", new User(mApplication.user));
            intent.putExtras(bundle);
            intent.putExtra("TYPE", 0);
            startActivity(intent);
        } else {
            showMessage("登录后才能使用背包，是否登录？", "登录", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callLogin(LOGIN_PACKAGE);
                }
            });
        }
    }

    private void showSetting() {
        if (mApplication.isLogin()) {
            Intent intent = new Intent(this, ProfileEditerActivity.class);
            startActivity(intent);
        } else {
            showMessage("登录后才能设置，是否登录？", "登录", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callLogin(LOGIN_SETTING);
                }
            });
        }
    }

    private void showUserCenter() {
        if (mApplication.isLogin()) {
            Intent intent = new Intent(this, UserCenterActivity2.class);
            startActivity(intent);
        } else {
            callLogin(LOGIN_USERCENTER);
        }
    }

    private void shareApp() {
        share("变大神，用麦块，你也可以",
                "自从用了麦块我的世界盒子，游戏玩的特别6，妈妈再也不用担心我变不成大神了,666",
                "http://www.mckuai.com/down.html",
                new UMImage(this, R.mipmap.ic_share_default));
    }

    private void priseApp() {
        try {
            Uri uri = Uri.parse("market://details?id=com.tars.mckuai");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
            showMessage("你还没安装有应用市场，安装后才能评分！", null, null);
        }
    }

    private void checkUpgread(boolean isSlience) {

    }

    private void logout() {
        mApplication.logout(this);
        refreshUser();
    }

    private void closeSlidingMenu() {
        if (null != slidingView) {
            slidingView.closeDrawer(GravityCompat.START);
        }
    }

    private void checkUnReadMsg() {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && RongIM.getInstance().getRongIMClient().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            switch (RongIM.getInstance().getRongIMClient().getTotalUnreadCount()) {
                case 0:
                    if (null != msgIndicator) {
                        msgIndicator.setVisibility(View.GONE);
                    }
                    break;
                default:
                    if (null != msgIndicator) {
                        msgIndicator.setVisibility(View.VISIBLE);
                        msgIndicator.postInvalidate();
                    }
                    break;
            }
        }
    }

}
