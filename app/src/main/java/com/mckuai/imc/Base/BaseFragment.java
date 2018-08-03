package com.mckuai.imc.Base;


import android.app.Fragment;
import android.view.View;

import com.umeng.analytics.MobclickAgent;


public class BaseFragment extends Fragment {
    protected String mTitle;
    protected MCKuai mApplication = MCKuai.instence;
    protected View view;

    protected boolean isLoading = false;

    @Override
    public void onResume() {
        super.onResume();
        if (null != mTitle) {
            MobclickAgent.onPageStart(mTitle);
        } else {
            MobclickAgent.onPageStart(this.getClass().getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mTitle) {
            MobclickAgent.onPageEnd(mTitle);
        } else {
            MobclickAgent.onPageEnd(this.getClass().getName());
        }
    }

    public String getTitle() {
        return null == mTitle ? "未知" : mTitle;
    }

    protected void showMessage(String msg, String action, View.OnClickListener listener) {
        if (null != getActivity()) {
            ((BaseActivity) getActivity()).showMessage(msg, action, listener);
        }
    }

    protected void showError(String msg, String action, View.OnClickListener listener) {
        if (null != getActivity()) {
            ((BaseActivity) getActivity()).showError(msg, action, listener);
        }
    }

}
