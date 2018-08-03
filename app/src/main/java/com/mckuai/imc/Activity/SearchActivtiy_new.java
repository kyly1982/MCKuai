package com.mckuai.imc.Activity;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mckuai.imc.Adapter.FriendAdapter_new;
import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.R;

/**
 * Created by kyly on 2016/2/1.
 */
public class SearchActivtiy_new extends BaseActivity {

    private MenuItem type_post, type_friend;
    private SuperRecyclerView listview;
    private StaggeredGridLayoutManager layoutManager;
    private FriendAdapter_new adapter;


    private int mode = 0;
    public static int MODE_PACKAGE = 0;
    public static int MODE_SEARCH = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);
        mode = getIntent().getIntExtra("TYPE", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == listview) {
            initView();
        }
        showData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mode == MODE_SEARCH) {
            getMenuInflater().inflate(R.menu.menu_search, menu);
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        listview = (SuperRecyclerView) findViewById(R.id.list_content);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        listview.setLayoutManager(layoutManager);

        if (mode == MODE_SEARCH) {
            setSearchMode();
        }


        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (mode == MODE_SEARCH) {
            mTitle.setText("搜索");
            listview.hideProgress();
        } else {
            mTitle.setText("背包");
        }
    }

    private void setSearchMode() {

    }

    private void showData() {

    }
}
