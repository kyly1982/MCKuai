package com.mckuai.imc.Base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.mckuai.imc.Activity.LoginActivity;
import com.mckuai.imc.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

/**
 * Created by kyly on 2016/7/12.
 */
public class BaseActivity extends AppCompatActivity {
    protected AppCompatTextView mTitle;
    protected Toolbar mToolbar;
    protected MCKuai mApplication = MCKuai.instence;
    private FrameLayout mContentRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setContentView(@LayoutRes int contentViewLayoutResID) {
        if (getLocalClassName().equals("Activity.MainActivity")) {
            super.setContentView(R.layout.activity_with_sildingmenu);
        } else {
            super.setContentView(R.layout.activity_with_toolbar);
        }
        //添加内容视图
        mContentRootView = (FrameLayout) findViewById(R.id.content);
//        if (null != mContentRootView && (0 < contentViewLayoutResID)){
        if (null != mContentRootView) {
            View contentView = LayoutInflater.from(this).inflate(contentViewLayoutResID, mContentRootView, false);
            if (null != contentView) {
                mContentRootView.addView(contentView);
            }
        }
        //设置toolbar
        initToolbar();
    }

    protected void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        String classname = getLocalClassName();
        if (!classname.equals("Activity.MainActivity")) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mTitle = (AppCompatTextView) findViewById(R.id.toolbar_title);
    }


    public void callLogin(int requestId) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, requestId);
    }


    public void showMessage(@StringRes int msgResId, @StringRes int actionResid, View.OnClickListener actionClicklistener) {
        showMessage(0, msgResId, actionResid, actionClicklistener);
    }

    public void showWarning(@StringRes int msgResId, @StringRes int actionResid, View.OnClickListener actionClicklistener) {
        showMessage(1, msgResId, actionResid, actionClicklistener);
    }

    public void showError(@StringRes int errorResId, @StringRes int actionResId, View.OnClickListener actionClicklistener) {
        showMessage(2, errorResId, actionResId, actionClicklistener);
    }

    private void showMessage(int type, @StringRes int msgResId, @StringRes int actionResid, View.OnClickListener actionClicklistener) {
        Snackbar snackbar = Snackbar.make(mContentRootView, msgResId, 0 == type ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG);
        if (null != actionClicklistener) {
            snackbar.setAction(actionResid, actionClicklistener);
            switch (type) {
                case 0:
                    snackbar.setActionTextColor(getResources().getColor(R.color.msg_normal));
                    break;
                case 1:
                    snackbar.setActionTextColor(getResources().getColor(R.color.msg_warning));
                    break;
                case 2:
                    snackbar.setActionTextColor(getResources().getColor(R.color.msg_error));
                    break;
            }
        }
        snackbar.show();
    }

    public void showMessage(String msg, String action, View.OnClickListener actionClickListener) {
        Snackbar snackbar = Snackbar.make(mContentRootView, msg, Snackbar.LENGTH_SHORT);
        if (null != action && null != actionClickListener) {
            snackbar.setAction(action, actionClickListener).setActionTextColor(getResources().getColor(R.color.msg_normal));
        }
        snackbar.show();
    }

    public void showError(String msg, String action, View.OnClickListener actionClickListener) {
        Snackbar snackbar = Snackbar.make(mContentRootView, msg, Snackbar.LENGTH_LONG);
        if (null != action && null != actionClickListener) {
            snackbar.setAction(action, actionClickListener).setActionTextColor(getResources().getColor(R.color.msg_error));
        }
        snackbar.show();
    }

    /**
     * 分享
     *
     * @param title   分享的标题
     * @param content 分享内容
     * @param url     链拉地址
     * @param image   图片
     */
    public void share(String title, String content, String url, UMImage image) {
        SHARE_MEDIA[] displayList = new SHARE_MEDIA[]{
                SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
        };
        ShareAction action = new ShareAction(this).setDisplayList(displayList);

        if (null != url) {
            //分享连接
            UMWeb web = new UMWeb(url);
            web.setTitle(title);
            if (null != content && !content.isEmpty()) {
                web.setDescription(content);
            }
            if (null != image) {
                web.setThumb(image);
            }
            action.withMedia(web);
        } else if (null != content) {
            //分享文本
            action.withText(content);
            if (null != image) {
                //带有图片
                action.withMedia(image);
            }
        } else if (null != image) {
            //纯图片
            action.withMedia(image);
        }

        action.setCallback(new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onResult(SHARE_MEDIA share_media) {
                //closeSlidmenu();
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                if (null != throwable) {
                    showMessage("分享到" + share_media.toString() + "失败，原因：" + throwable.getLocalizedMessage(), null, null);
                } else {
                    showMessage("分享到" + share_media.toString() + "失败，原因未知", null, null);
                }
                //closeSlidmenu();
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {

            }
        });

        action.open();
    }


}
