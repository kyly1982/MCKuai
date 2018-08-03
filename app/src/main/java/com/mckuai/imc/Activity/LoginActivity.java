package com.mckuai.imc.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;

import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.MCUser;
import com.mckuai.imc.Bean.Token;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, MCNetEngine.OnLoginServerResponseListener {
    private AppCompatTextView loginMsg;

    private UMShareAPI api;

    private String title = "登录";
    private final String TAG = "LoginActivity";
    private boolean isFullLoginNeed = false;
    private Token loginToken;

    private UMAuthListener mUMAuthListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            loginMsg.setText(getString(R.string.login_auth_getinfo));
        }

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
            if (share_media == SHARE_MEDIA.QQ) {
                if (map.size() > 5) {
                    loginToken = new Token();
                    loginToken.setBirthday(System.currentTimeMillis() - 600000);
                    loginToken.setToken(map.get("accessToken"));
                    loginToken.setExpires(Long.parseLong(map.get("expiration")));

                    MCUser user = new MCUser();
                    user.setLoginToken(loginToken);
                    user.setNike(map.get("name"));
                    user.setAddr(map.get("city"));
                    user.setGender(map.get("gender"));
                    user.setHeadImg(map.get("iconurl"));
                    user.setName(map.get("uid"));
                    user.setUserType("QQ");

                    MobclickAgent.onEvent(LoginActivity.this, "qqLogin_S");
                    if (null == mApplication.user) {
                        mApplication.user = user;
                    } else {
                        mApplication.user.clone(user);
                    }
                    loginToMC(user);
                    return;
                } else {
                    Log.e(TAG, getString(R.string.login_auth_response_to_short));
                }
            } else {
                Log.e(TAG, getString(R.string.login_auth_platform_fail));
            }

        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
            MobclickAgent.onEvent(LoginActivity.this, "qqLogin_F");
            loginMsg.setText(getString(R.string.mc_Err, throwable.getLocalizedMessage()));
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {
            MobclickAgent.onEvent(LoginActivity.this, "qqLogin_F");
            loginMsg.setText(getString(R.string.login_auth_cancle));
        }

    };

    private MCKuai.IMLoginListener mIMLoginListener = new MCKuai.IMLoginListener() {
        @Override
        public void onInitError() {
            MobclickAgent.onEvent(LoginActivity.this, "chatLogin_F");
            handler.sendEmptyMessage(1);
        }

        @Override
        public void onTokenIncorrect() {
            MobclickAgent.onEvent(LoginActivity.this, "chatLogin_F");
            mApplication.user.setLoginToken(null);
            handler.sendEmptyMessage(2);
        }

        @Override
        public void onLoginFailure(String msg) {
            MobclickAgent.onEvent(LoginActivity.this, "chatLogin_F");
            handler.sendEmptyMessage(3);
        }

        @Override
        public void onLoginSuccess(String msg) {
            handler.sendEmptyMessage(4);
            MobclickAgent.onEvent(LoginActivity.this, "chatLogin_S");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login);
//        initToolbar(R.id.toolbar, 0, this);
//        mTencent = Tencent.createInstance("101155101", getApplicationContext());
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResult(false);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(title);
        initView();
    }

    private void initView() {
        mTitle.setText(title);
        findViewById(R.id.login_qqlogin).setOnClickListener(this);
        findViewById(R.id.login_anonymous).setOnClickListener(this);
        loginMsg = (AppCompatTextView) findViewById(R.id.loginmsg);
    }

    private void logoutQQ() {
        if (null == api) {
            api = UMShareAPI.get(this);
        }
        api.deleteOauth(this, SHARE_MEDIA.QQ, mUMAuthListener);
    }


    private void loginToQQ() {
        loginMsg.setText(R.string.login_auth_getinfo);
        MobclickAgent.onEvent(this, "qqLogin");
        if (null == api) {
            api = UMShareAPI.get(this);
        }
        if (api.isInstall(this, SHARE_MEDIA.QQ)) {
            api.getPlatformInfo(this, SHARE_MEDIA.QQ, mUMAuthListener);
        } else {
            loginMsg.setText("未安装QQ，先安装QQ再登录！");
        }
    }

    private void loginToMC(MCUser user) {
        MobclickAgent.onEvent(this, "login");
        loginMsg.setText(R.string.login_MCServer);
        mApplication.netEngine.loginServer(this, user, this);
    }

    public void loginIM() {
        MobclickAgent.onEvent(this, "loginChatServer");
        loginMsg.setText(R.string.login_RongIM);
        mApplication.loginIM(mIMLoginListener);
    }

    private void handleResult(Boolean result) {
        setResult(true == result ? RESULT_OK : RESULT_CANCELED);
        this.finish();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_qqlogin:
                if (null != mApplication.user && mApplication.user.isUserTokenValid() && !isFullLoginNeed) {
                    loginToMC(mApplication.user);
                } else {
                    loginToQQ();
                }
                break;
            default:
                handleResult(false);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 服务器返回登录成功
     *
     * @param user
     */
    @Override
    public void onLoginSuccess(MCUser user) {
        MobclickAgent.onEvent(this, "login_S");
        mApplication.user.clone(user);
        mApplication.saveProfile();
        loginIM();
    }

    /**
     * 服务器返回登录失败
     *
     * @param msg
     */
    @Override
    public void onLoginFailure(String msg) {
        MobclickAgent.onEvent(this, "Login_F");
        if (null != msg) {
            loginMsg.setText(getResources().getString(R.string.qq_err, msg));
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    loginMsg.setText(R.string.login_IM_Err_UnInit);
                    break;
                case 2:
                    loginMsg.setText(getResources().getString(R.string.login_IM_Err, getResources().getString(R.string.im_Err_TokenIncorrect)));
                    showError(R.string.tryReLogin, R.string.reLogin, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mApplication.logout(LoginActivity.this);
                            isFullLoginNeed = true;
//                            onClick(findViewById(R.id.login_qqlogin));
                            loginToQQ();
                        }
                    });
                    break;
                case 3:
                    loginMsg.setText(getResources().getString(R.string.login_IM_Err, msg));
                    break;
                case 4:
                    handleResult(true);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    };
}

