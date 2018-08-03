package com.mckuai.imc.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.Bean.MCUser;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProfileEditerActivity extends BaseActivity
        implements View.OnClickListener,
        TextView.OnEditorActionListener,
        View.OnFocusChangeListener,
        MCNetEngine.OnUpdateUserNickListener,
        MCNetEngine.OnUpdateUserAddressResponseListener,
        MCNetEngine.OnUploadUserCoverListener,
        MCNetEngine.OnUpdateUserCoverListener,
        TextWatcher {

    private AppCompatImageView userCover;
    private TextInputLayout nickWrapper, addressWrapper;
    private TextInputEditText nickEdit;
    private AppCompatAutoCompleteTextView addressEdit;
    private View progress;

    private ImageLoader loader;
    private DisplayImageOptions circleOption;
    private MCUser user;

    private LocationClient locationClient;

    private boolean isUserChange = true;//用于防止设置昵称和城市时触发事件响应
    private String fileName, coverUrl, city;
    private int colorChanged, colorUnChange;
    private final int GETPIC = 12410;
    private Bitmap bmp_Cover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editer);

        mToolbar.setNavigationOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitle.setText("设置");
        loader = ImageLoader.getInstance();
        circleOption = mApplication.getCircleOptions();
        user = mApplication.user;
        colorChanged = getResources().getColor(R.color.textGreen);
        colorUnChange = getResources().getColor(R.color.textColorPrimary);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == userCover) {
            initView();
        }
        showData();
        location();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case GETPIC:
                    getNewCover(data);
                    break;

                default:
                    break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                upload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        userCover = (AppCompatImageView) findViewById(R.id.usercover);
        nickWrapper = (TextInputLayout) findViewById(R.id.usernick_wrapper);
        addressWrapper = (TextInputLayout) findViewById(R.id.useraddress_wrapper);
        nickEdit = (TextInputEditText) findViewById(R.id.usernick);
        addressEdit = (AppCompatAutoCompleteTextView) findViewById(R.id.useraddress);
        progress = findViewById(R.id.uploadview);

        nickEdit.setOnEditorActionListener(this);
        addressEdit.setOnEditorActionListener(this);
        nickEdit.addTextChangedListener(this);
        addressEdit.addTextChangedListener(this);
        nickEdit.setOnFocusChangeListener(this);
        addressEdit.setOnFocusChangeListener(this);
        userCover.setOnClickListener(this);


        String[] citys = getResources().getStringArray(R.array.citylist);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, citys);
        addressEdit.setAdapter(cityAdapter);
    }

    private void showData() {
        String url = (String) userCover.getTag();
        if (null == url || url.isEmpty() || !url.equals(user.getHeadImg())) {
            loader.displayImage(user.getHeadImg(), userCover, circleOption);
            userCover.setTag(user.getHeadImg());
            isUserChange = false;
            nickEdit.setText(user.getNike());
            addressEdit.setText(user.getAddr());
            isUserChange = true;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            if (v.getId() == addressEdit.getId()) {
                if (addressEdit.getText().toString().equals(user.getAddr())) {
                    addressEdit.setTextColor(colorUnChange);
                } else {
                    addressEdit.setTextColor(colorChanged);
                }
            } else if (v.getId() == nickEdit.getId()) {
                if (nickEdit.getText().toString().equals(user.getNike())) {
                    nickEdit.setTextColor(colorUnChange);
                } else {
                    nickEdit.setTextColor(colorChanged);
                }
            }
        } else {
            if (v.getId() == R.id.useraddress) {
                if (null != city) {
                    addressEdit.setText(city);
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isUserChange) {
            if (addressEdit.hasFocus()) {
                if (0 == s.length()) {
                    addressEdit.setError("城市不能为空");
                } else if (16 <= s.length()) {
                    addressEdit.setError("不能超过16个字符");
                }
            } else if (nickEdit.hasFocus()) {
                if (0 == s.length()) {
                    nickEdit.setError("昵称不能为空");
                } else if (16 <= s.length()) {
                    nickEdit.setError("不能超过16个字符");
                }
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.usernick:
                updateNick();
                break;
            case R.id.useraddress:
                updateAddress();
                break;
        }
        mApplication.hideSoftKeyboard(v);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.usercover:
                changeCover();
                break;
            default:
                if (colorChanged == addressEdit.getCurrentTextColor() || colorChanged == nickEdit.getCurrentTextColor() || null != fileName || null != coverUrl) {
                    showMessage("当前内容还未保存，是否退出？", "退出", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                } else {
                    finish();
                }
                break;
        }
    }

    private void changeCover() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GETPIC);
    }

    private void getNewCover(Intent data) {
        if (null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            fileName = cursor.getString(columnIndex);
            cursor.close();
            String url = selectedImage.toString();
            loader.displayImage(url, userCover, circleOption);
            uploadCover();

            // 获取图片
            /*BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, 1080 * 700);
            opts.inJustDecodeBounds = false;
            Bitmap bmp = null;
            try
            {
                bmp = BitmapFactory.decodeFile(picturePath, opts);
            } catch (OutOfMemoryError err)
            {
                showMessage("图片过大!",null,null);
                return;
            }
            civ_cover.setImageBitmap(bmp);
            // isCoverChanged = true;
            bmp_Cover = bmp;
            uploadUserCover();*/
        }
    }

    private void location() {
        if (null == locationClient) {
            locationClient = new LocationClient(getApplicationContext());

            initLocationOption();
            locationClient.registerLocationListener(new BDLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    int result = bdLocation.getLocType();
                    if (result == BDLocation.TypeGpsLocation ||
                            result == BDLocation.TypeNetWorkLocation ||
                            result == BDLocation.TypeOffLineLocation) {
                        if (!addressEdit.getText().toString().equals(bdLocation.getCity())) {
                            city = bdLocation.getCity();
                            locationClient.unRegisterLocationListener(this);
                            locationClient.stop();
                        }
                    }
                }
            });
            locationClient.start();
        }
        locationClient.requestLocation();
    }

    private void initLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度定位
        option.setCoorType("bd0911");
        option.setScanSpan(0);//仅定位一次
        option.setIsNeedAddress(true);//需要地址信息
        //option.setIsNeedLocationDescribe(true);//设置需要位置语义化结果
        option.setIgnoreKillProcess(true);//设置在stop的时候杀死这个进程
        locationClient.setLocOption(option);
    }

    private void showProgress() {
        if (null != progress) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        if (null != progress) {
            progress.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        showMessage("隐藏软键盘", null, null);
    }

    private void upload() {
        if (colorChanged == nickEdit.getCurrentTextColor()) {
            updateNick();
        }
        if (colorChanged == addressEdit.getCurrentTextColor()) {
            updateAddress();
        }
        if (null != fileName) {
            uploadCover();
        } else if (null != coverUrl) {
            updateCoverUrl();
        }
    }

    private void updateNick() {
        showProgress();
        mApplication.netEngine.updateUserNick(this, user.getId(), nickEdit.getText().toString(), this);
    }

    private void updateAddress() {
        showProgress();
        mApplication.netEngine.updateUserAddress(this, user.getId(), addressEdit.getText().toString(), this);
    }

    private void uploadCover() {
        showProgress();
        if (null != fileName && !fileName.isEmpty()) {
            mApplication.netEngine.uploadUserCover(this, fileName, this);
        }
    }

    private void updateCoverUrl() {
        if (null != coverUrl && 10 < coverUrl.length()) {
            mApplication.netEngine.updateUserCover(this, user.getId(), coverUrl, this);
        }
    }

    @Override
    public void onUpdateAddressSuccess() {
        hideProgress();
        addressEdit.setTextColor(colorUnChange);
        user.setAddr(addressEdit.getText().toString());
        mApplication.user.setAddr(addressEdit.getText().toString());
    }

    @Override
    public void onUpdateAddressFailure(String msg) {
        hideProgress();
        showMessage("更新地址失败，原因：" + msg, "重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAddress();
            }
        });
    }

    @Override
    public void onUpdateUserNickSuccess() {
        hideProgress();
        nickEdit.setTextColor(colorUnChange);
        user.setNike(nickEdit.getText().toString());
        mApplication.user.setNike(nickEdit.getText().toString());
    }

    @Override
    public void onUpdateUserNickFailure(String msg) {
        hideProgress();
        showMessage("更新昵称失败，原因：" + msg, "重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNick();
            }
        });
    }

    @Override
    public void onUploadCoverSuccess(String url) {
        hideProgress();
        if (null != url && url.length() > 10) {
            fileName = null;
            coverUrl = url;
            updateCoverUrl();
        }
    }

    @Override
    public void onUploadCoverFailure(String msg) {
        hideProgress();
        showMessage("上传头像失败，原因" + msg, "重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCover();
            }
        });
    }

    @Override
    public void onUpdateUserCoverSuccess() {
        hideProgress();
        user.setHeadImg(coverUrl);
        mApplication.user.setHeadImg(coverUrl);
        coverUrl = null;
    }

    @Override
    public void onUpdateUserCoverFailure(String msg) {
        hideProgress();
        showMessage("更新头像时失败，原因：" + msg, "重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCoverUrl();
            }
        });
    }

}
