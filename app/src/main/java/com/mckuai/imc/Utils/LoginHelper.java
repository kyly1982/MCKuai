package com.mckuai.imc.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.mckuai.imc.Bean.MCUser;
import com.mckuai.imc.Bean.Token;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

public class LoginHelper {

    public interface OnLoginListener {
        public void onLoginSuccess(boolean isLogin, SHARE_MEDIA platform, MCUser user);

        public void onLoginFail(SHARE_MEDIA platform, String message);

        public void onLoginProcess(SHARE_MEDIA platform, String message);
    }

    public interface OnIMLoginListener {
        public void onIMLoginSuccess();

        public void onIMLoginFail();
    }

    public OnLoginListener mLoginListener;
    private UMAuthListener mListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            if (null != mLoginListener) {
                mLoginListener.onLoginProcess(share_media, "开始");
            }
        }

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
            if (null != mLoginListener) {
                if (map.size() > 5) {
                    Token token = new Token();
                    token.setBirthday(System.currentTimeMillis() - 600000);
                    token.setToken(map.get("accessToken"));
                    token.setExpires(Long.parseLong(map.get("expiration")));

                    MCUser user = new MCUser();
                    user.setLoginToken(token);
                    user.setNike(map.get("name"));
                    user.setAddr(map.get("city"));
                    user.setGender(map.get("gender"));
                    user.setHeadImg(map.get("iconurl"));

                    switch (share_media) {
                        case QQ:
                            user.setName(map.get("uid"));
                            user.setUserType("QQ");

                            break;
                        case WEIXIN:
                            user.setName(map.get("openid"));
                            user.setUserType("WX");
                            break;
                    }

                    mLoginListener.onLoginSuccess(true, share_media, user);
                } else {
                    Log.e("LH", "监听接口收到成功，但返回的map长度过小!");
                }
            } else {
                Log.e("LH", "监听接口收到成功，但未设置接收监听器");
            }
        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
            if (null != mLoginListener) {
                mLoginListener.onLoginFail(share_media, throwable.getLocalizedMessage());
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {
            if (null != mLoginListener) {
                mLoginListener.onLoginFail(share_media, "用户取消登录！");
            }
        }
    };


    public boolean login(Context context, SHARE_MEDIA platform, final OnLoginListener listener) {
        mLoginListener = listener;
        UMShareAPI api = UMShareAPI.get(context);
        if (null != api) {
            if (api.isInstall((Activity) context, platform)) {
                api.getPlatformInfo((Activity) context, platform, mListener);
            } else {
                listener.onLoginFail(platform, "当前未安装" + platform + "平台!");
                Log.e("LH", "当前未安装" + platform + "平台!");
            }
        } else {
            listener.onLoginFail(platform, "初始化实例失败");
            Log.e("LH", "初始化实例失败！");
        }
        return false;
    }

    public void logout(Context context, SHARE_MEDIA platform, final OnLoginListener listener) {
        mLoginListener = listener;
        UMShareAPI.get(context).deleteOauth(((Activity) context), platform, mListener);
    }

}
