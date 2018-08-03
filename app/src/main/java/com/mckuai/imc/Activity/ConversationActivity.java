package com.mckuai.imc.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCDaoHelper;
import com.mckuai.imc.Utils.MCNetEngine;

import java.util.Locale;

import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.model.Conversation;

public class ConversationActivity extends BaseActivity implements MCNetEngine.OnLoadUserInfoResponseListener {
    private MCDaoHelper daoHelper;
    private boolean isLoginCalled = false;

    /**
     * 目标 Id
     */
    private String mTargetId;

    /**
     * 刚刚创建完讨论组后获得讨论组的id 为targetIds，需要根据 为targetIds 获取 targetId
     */
    // private String mTargetIds;

    /**
     * 会话类型
     */
    private Conversation.ConversationType mConversationType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //title = getString(R.string.activity_conversation);
        setContentView(R.layout.conversation);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        daoHelper = mApplication.daoHelper;
        Intent intent = getIntent();
        if (null != intent) {
            getIntentDate(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mApplication.isLogin()) {
            if (mApplication.isIMLogined) {
                showConversation(mConversationType, mTargetId);
            } else {
                /*mApplication.loginIM(new RongIMClient.ConnectCallback() {
                    @Override
                    public void onTokenIncorrect() {
                        showMessage("令牌过期，需要重新登录", "重新登录", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mApplication.user = null;
                                callLogin(5);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(String s) {
                        showConversation(mConversationType,mTargetId);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        showMessage("登录失败，原因"+errorCode.getMessage(),null,null);
                    }
                });*/
                mApplication.loginIM(new MCKuai.IMLoginListener() {
                    @Override
                    public void onInitError() {
                        showMessage("聊天模块功能异常，请重启软件！", null, null);
                    }

                    @Override
                    public void onTokenIncorrect() {
                        showMessage("令牌过期，需要重新登录", "重新登录", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mApplication.user = null;
                                callLogin(5);
                            }
                        });
                    }

                    @Override
                    public void onLoginFailure(String msg) {
                        showMessage("登录失败，原因" + msg, null, null);
                    }

                    @Override
                    public void onLoginSuccess(String msg) {
                        showConversation(mConversationType, mTargetId);
                    }
                });
            }
        } else {
            //未登录，则进入登录
            if (!isLoginCalled) {
                callLogin(5);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            showMessage("需要登录后才能进行聊天功能", null, null);
            finish();
        }
    }

    /**
     * 展示如何从 Intent 中得到 融云会话页面传递的 Uri
     */
    private void getIntentDate(Intent intent) {

        mTargetId = intent.getData().getQueryParameter("targetId");
        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));
        getUser();
    }

    private void getUser() {
        if (null != mTargetId) {
            User user = daoHelper.getUserByName(mTargetId);
            if (null == user) {
                //从网络获取用户信息
                mApplication.netEngine.loadUserInfo(this, mTargetId, this);
            } else {
                mTitle.setText(user.getNickEx());
            }
        }
    }


    /**
     * 加载会话页面 ConversationFragment
     *
     * @param mConversationType 会话类型
     * @param mTargetId         目标 Id
     */
    private void showConversation(Conversation.ConversationType mConversationType, String mTargetId) {

        ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversation);

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
                .appendQueryParameter("targetId", mTargetId).build();

        fragment.setUri(uri);
    }

    @Override
    public void onLoadUserInfoSuccess(User user) {
        mTitle.setText(user.getNickEx());
    }

    @Override
    public void onLoadUserInfoFailure(String msg) {
        mTitle.setText("未知");
        showMessage(msg, null, null);
    }
}
