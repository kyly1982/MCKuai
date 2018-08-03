package com.mckuai.imc.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mckuai.imc.Adapter.ForumAdapter_Publish;
import com.mckuai.imc.Adapter.PostTypeAdapter_Publish;
import com.mckuai.imc.Base.BaseActivity;
import com.mckuai.imc.Bean.ForumInfo;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.PostType;
import com.mckuai.imc.R;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class PublishPostActivity extends BaseActivity implements OnClickListener,
        OnFocusChangeListener, TextWatcher, ForumAdapter_Publish.OnItemClickListener, PostTypeAdapter_Publish.OnItemClickListener {

    private AppCompatTextView tvselectedType, tvForum, tvType;
    private RecyclerView listForum, listType;
    private AppCompatEditText edtTitle, edtContent;
    private LinearLayout layoutPics;
    private AppCompatImageButton btnAddPic;

    private ForumAdapter_Publish mFroumAdapter;
    private PostTypeAdapter_Publish mTypeAdapter;

    private ArrayList<Bitmap> picsList;// 存储图片之用
    private String picUrl;// 图片上传后的路径
    private String postTitle;
    private String postContent;
    private boolean isPublish = false;
    private ArrayList<ForumInfo> mForums;

    private static boolean isUploading = false;
    private static final int LOGIN = 0;
    private static final int GETPIC = 1;

    private AsyncHttpClient mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_post);
        mClient = new AsyncHttpClient();
        Intent intent = getIntent();
        mForums = (ArrayList<ForumInfo>) intent.getSerializableExtra("FORUM_LIST");
        removeInvalidForum();
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("发帖");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart("发帖");
        if (null == listForum) {
            initView();
        }
        showData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
                case LOGIN:
                    publishPost();
                    break;
                case GETPIC:
                    addpic(data);
                    break;

                default:

                    break;
            }
        } else {
            showError("未登录,不能发帖!", null, null);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd("发帖");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_publish) {
            publishPost();
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // TODO Auto-generated method stub
        if (!hasFocus) {
            showTypeLayout();
        }
        if (!edtContent.hasFocus() && !edtTitle.hasFocus()) {
            hideTypeLayout();
        }
    }

    @Override
    public void onItemClicked(ForumInfo forumInfo) {
        setSelectedTextViewColor(forumInfo.getName(), forumInfo.getIncludeType().get(0).getSmallName());
        tvselectedType.setTag(new SelectedInfo(forumInfo.getName(), forumInfo.getId(), forumInfo.getIncludeType().get(0)));
        mTypeAdapter.setData(forumInfo.getIncludeType());
    }

    @Override
    public void onItemClicked(PostType postType, boolean isManual) {
        SelectedInfo info = (SelectedInfo) tvselectedType.getTag();
        if (null != info) {
            info.setType(postType);
            tvselectedType.setTag(info);
            setSelectedTextViewColor(info.forumName, info.typeName);
            if (!isManual) {
                hideTypeLayout();
            }
        }
    }

    private void setSelectedTextViewColor(String forumName, String typeName) {
        for (int i = 0; i < 4 + (8 - forumName.length()); i++) {
            forumName += " ";
        }

        String text = getString(R.string.selectedType, forumName, typeName);
        int forumStart = text.indexOf(forumName);
        int typeStart = text.indexOf(typeName);
        ForegroundColorSpan fcValue = new ForegroundColorSpan(getResources().getColor(R.color.color_white));
//        ForegroundColorSpan fcKey = new ForegroundColorSpan(getResources().getColor(R.color.color_white));
        BackgroundColorSpan bcValue = new BackgroundColorSpan(getResources().getColor(R.color.text_green));
//        BackgroundColorSpan bcKey = new BackgroundColorSpan(getResources().getColor(R.color.color_white));

        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(bcValue, forumStart, forumStart + forumName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(bcValue, typeStart, typeStart + typeName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(fcValue, forumStart, forumStart + forumName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(fcValue, typeStart, typeStart + typeName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvselectedType.setText(spannable);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.addpic:
                MobclickAgent.onEvent(this, "addPic_Publish");
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GETPIC);
                break;


            case R.id.selectedType:
                showTypeLayout();
                break;

            default:
                break;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        int lenght = s.length();
        if (edtTitle.hasFocus()) {
            if (4 > lenght) {
                edtTitle.setError("还需要输入" + (4 - lenght) + "个字");
            } else if (20 < lenght) {
                edtTitle.setError("已超出" + (lenght - 20) + "个字");
            }
        } else if (edtContent.hasFocus()) {
            if (15 > lenght) {
                edtContent.setError("还需要输入" + (15 - lenght) + "个字");
            } else if (3000 < s.length()) {
                edtContent.setError("已超出" + (lenght - 3000) + "个字");
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private void removeInvalidForum() {
        if (null != mForums && !mForums.isEmpty()) {
            Iterator<ForumInfo> iterator = mForums.iterator();
            while (iterator.hasNext()) {
                ForumInfo forumInfo = iterator.next();
                if (forumInfo.getName().equals("公告与反馈")) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private void initView() {
        tvselectedType = (AppCompatTextView) findViewById(R.id.selectedType);
        tvForum = (AppCompatTextView) findViewById(R.id.lableforum);
        tvType = (AppCompatTextView) findViewById(R.id.labletype);
        listForum = (RecyclerView) findViewById(R.id.listforum);
        listType = (RecyclerView) findViewById(R.id.listtype);
        layoutPics = (LinearLayout) findViewById(R.id.layoutpic);
        btnAddPic = (AppCompatImageButton) findViewById(R.id.addpic);
        edtContent = (AppCompatEditText) findViewById(R.id.edt_content);
        edtTitle = (AppCompatEditText) findViewById(R.id.edt_title);

        StaggeredGridLayoutManager forumManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        StaggeredGridLayoutManager typeManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        listForum.setLayoutManager(forumManager);
        listType.setLayoutManager(typeManager);

        edtTitle.setOnFocusChangeListener(this);
        edtContent.setOnFocusChangeListener(this);
        edtTitle.addTextChangedListener(this);
        edtContent.addTextChangedListener(this);

        btnAddPic.setOnClickListener(this);
        tvselectedType.setOnClickListener(this);
        mTitle.setText("发帖");
    }


    private void showData() {
        if (null != mForums && !mForums.isEmpty()) {
            if (null == mFroumAdapter) {
                mFroumAdapter = new ForumAdapter_Publish(this, mForums, this);
                listForum.setAdapter(mFroumAdapter);

                mTypeAdapter = new PostTypeAdapter_Publish(this, this);
                listType.setAdapter(mTypeAdapter);
                mTypeAdapter.setData(mForums.get(0).getIncludeType());
                showTypeLayout();
            }
        } else {
            showMessage("未获取到版块信息！", null, null);
        }

    }

    private void showTypeLayout() {
//        tvselectedType.setVisibility(View.GONE);
        tvForum.setVisibility(View.VISIBLE);
        tvType.setVisibility(View.VISIBLE);
        listForum.setVisibility(View.VISIBLE);
        listType.setVisibility(View.VISIBLE);
    }

    private void hideTypeLayout() {
//        tvselectedType.setVisibility(View.VISIBLE);
        tvForum.setVisibility(View.GONE);
        tvType.setVisibility(View.GONE);
        listForum.setVisibility(View.GONE);
        listType.setVisibility(View.GONE);
    }

    private void publishPost() {
        if (!isPublish) {
            isPublish = true;
            if (checkPublishInfo()) {
                postTitle = edtTitle.getText().toString();
                postContent = edtContent.getText().toString();

                if (null != picsList && !picsList.isEmpty()) {
                    MobclickAgent.onEvent(this, "picCount_Publish");
                    uploadPic();
                } else {
                    uploadText();
                }
            }
        } else {
            showMessage("正在发布，请稍候！", null, null);
        }
    }

    private void uploadPic() {
        String url = "http://www.mckuai.com/" + getString(R.string.interface_uploadimage);
        RequestParams params = new RequestParams();
        params.put("upload", Bitmap2IS(picsList.get(0)), "01.jpg", "image/jpeg");
        mClient.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                isUploading = true;
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // TODO Auto-generated method stub
                isUploading = false;
                super.onSuccess(statusCode, headers, response);
                if (response.has("state")) {
                    try {
                        if (response.getString("state").equals("ok")) {
                            picUrl = response.getString("msg");
                            if (null != picUrl) {
                                isUploading = false;
                                uploadText();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
                showMessage("上传图片失败，是否重试？", "重试", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadPic();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // TODO Auto-generated method stub
                isUploading = false;
                super.onFailure(statusCode, headers, responseString, throwable);
                showError("上传图片失败，原因：" + throwable.getLocalizedMessage() + "\r\n是否重试？", "重试", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadPic();
                    }
                });
            }
        });
    }

    private InputStream Bitmap2IS(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
        return sbs;
    }

    private void uploadText() {
        String url = getString(R.string.interface_domainName) + getString(R.string.interface_uploadpost);
        RequestParams params = new RequestParams();
        SelectedInfo info = (SelectedInfo) tvselectedType.getTag();
        if (null == info) {
            showTypeLayout();
            return;
        }
        params.put("userId", mApplication.user.getId());
        params.put("forumId", info.forumId + "");
        params.put("forumName", info.forumName);
        params.put("talkTypeid", info.typeId + "");
        params.put("talkTypeName", info.typeName);
        params.put("talkTitle", postTitle);
        if (null != picUrl && 0 < picUrl.length()) {
            params.put("content", postContent + picUrl);
        } else {
            params.put("content", postContent);
        }
        params.put("device", "android");
        mClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // TODO Auto-generated method stub
                isPublish = false;
                int id = 0;
                if (response.has("state")) {
                    try {
                        if (response.getString("state").equalsIgnoreCase("ok")) {
                            id = Integer.parseInt(response.getString("msg"));
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        Toast.makeText(PublishPostActivity.this, "发帖失败!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (0 != id) {
                        MobclickAgent.onEvent(PublishPostActivity.this, "publishPost_Success");
                        Post post = new Post();
                        post.setId(id);
                        Intent intent = new Intent(PublishPostActivity.this, PostActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(getString(R.string.tag_post), post);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
                Toast.makeText(PublishPostActivity.this, "发帖失败!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // TODO Auto-generated method stub
                isPublish = false;
                Toast.makeText(PublishPostActivity.this, "发帖失败!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub
                super.onCancel();
                isPublish = false;
            }
        });
    }

    private boolean checkPublishInfo() {
        if (null == mApplication.user || 0 == mApplication.user.getId()) {
            callLogin();
            return false;
        }
        if (null == tvselectedType.getTag()) {
            showMessage("请选择要发帖的板块！", null, null);
            showTypeLayout();
            return false;
        } else if (5 > edtTitle.getText().toString().length() || 20 < edtTitle.getText().length()) {
            showMessage("标题长度为5-20个字!", null, null);
            edtTitle.setError("标题长度为5-20个字!");
            edtTitle.requestFocus();
            return false;
        } else if (edtContent.getText().length() < 15 || edtContent.getText().length() > 3000) {
            showMessage("内容长度为15-3000字！", null, null);
            edtContent.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void callLogin() {
        Intent intent = new Intent(PublishPostActivity.this, LoginActivity.class);
        startActivityForResult(intent, LOGIN);
    }

    private void addpic(Intent data) {
        if (null != data) {
            // 取出所选图片的路径
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // 获取图片
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, 128 * 128);
            opts.inJustDecodeBounds = false;
            final Bitmap bmp;
            try {
                bmp = BitmapFactory.decodeFile(picturePath, opts);
            } catch (OutOfMemoryError err) {
                // showNotification("图片过大!");
                Toast.makeText(this, "图片过大!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (null == picsList) {
                picsList = new ArrayList<Bitmap>(4);
            }
            picsList.add(bmp);

            // 将图片贴到imageview
            final ImageView image = new ImageView(PublishPostActivity.this);
            LayoutParams params = (LayoutParams) btnAddPic.getLayoutParams();
            params.width = btnAddPic.getWidth();
            params.height = btnAddPic.getHeight();
            image.setScaleType(ScaleType.CENTER_CROP);
            image.setLayoutParams(params);
            image.setImageBitmap(bmp);
            image.setClickable(true);
            image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (null != layoutPics && layoutPics.getChildCount() > 0) {
                        layoutPics.removeView(image);
                        picsList.remove(bmp);
                        layoutPics.postInvalidate();
                        if (layoutPics.getChildCount() < 5) {
                            btnAddPic.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                    return false;
                }
            });
            if (4 == picsList.size()) {
                btnAddPic.setVisibility(View.GONE);
            }
            layoutPics.addView(image, picsList.size() - 1);
            layoutPics.postInvalidate();
        }
    }


 /*   Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            RadioButton radioButton = (RadioButton) (mFroums.getChildAt(0));
            if (null != radioButton) {
                radioButton.setChecked(true);
            } else {
                sendMessageDelayed(mHandler.obtainMessage(0), 100);
            }
        }
    };*/


    // 加载大图时,计算缩放比例,以免出现OOM
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    class SelectedInfo {
        public String forumName;
        public String typeName;
        public int forumId;
        public int typeId;

        public SelectedInfo(String forumName, int forumId, PostType type) {

            this.forumName = forumName;
            this.forumId = forumId;
            if (null != type) {
                typeName = type.getSmallName();
                typeId = type.getSmallId();
            }
        }

        public void setType(PostType type) {
            if (null != type) {
                this.typeName = type.getSmallName();
                this.typeId = type.getSmallId();
            }
        }
    }
}
