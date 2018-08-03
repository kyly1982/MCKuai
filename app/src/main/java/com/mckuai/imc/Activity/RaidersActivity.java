package com.mckuai.imc.Activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.R;
import com.umeng.analytics.MobclickAgent;

public class RaidersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private WebView webView;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raiders);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.pauseTimers();
        MobclickAgent.onPageEnd("攻略详情");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("攻略详情");
        if (null == webView || null == refreshLayout) {
            initView();
            showRaiders();
        }
        webView.resumeTimers();
    }

    @Override
    public void finish() {
        if (null != webView) {
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (null != parent) {
                parent.removeAllViews();
            }
            webView.removeAllViews();
            webView.destroy();
        }
        super.finish();
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.webView);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        //保证在5.0及以上版本中能显示https中的http图片
        if (Build.VERSION.SDK_INT > 20) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                // 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (100 == newProgress) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (refreshLayout.isRefreshing()) {
                                refreshLayout.setRefreshing(false);
                            }
                        }
                    });
                }
            }
        });
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mTitle.setText("攻略详情");

        refreshLayout.setOnRefreshListener(this);
    }

    private void showRaiders() {
        int id = getIntent().getIntExtra("RAIDERS_ID", 0);
        if (0 != id) {
            String url = "https://api.mckuai.com/gl/" + id;
            webView.loadUrl(url);
        }
    }

    @Override
    public void onRefresh() {
        webView.reload();
    }
}
