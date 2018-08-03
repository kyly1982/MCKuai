package com.mckuai.imc.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;

import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StartActivity extends AppCompatActivity {
    private MCKuai mApplication;
    AppCompatImageView imageView;
    boolean isFirstBoot;
    private ImageLoader loader;
    private boolean isShowNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mApplication = MCKuai.instence;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == imageView) {
            handler.sendEmptyMessageDelayed(1, 1000);
        }
//        init();
    }

    private void init() {
        mApplication.init();
        imageView = (AppCompatImageView) findViewById(R.id.cover);
        loader = ImageLoader.getInstance();
        readdata();
        displayPic();
        handler.sendEmptyMessageDelayed(3, 2000);
    }

    private void displayPic() {
        loader.displayImage("http://cdn.mckuai.com/app_start.png", imageView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageView.setImageResource(R.mipmap.app_start);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (null != loadedImage) {
                    imageView.setImageBitmap(loadedImage);
                }
            }
        });
    }

    private void showNextActivity() {
        if (isShowNext) {
            return;
        }
        isShowNext = true;
        Log.e("Start", "showNextActivity");
        Intent intent;
        if (isFirstBoot) {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
        finish();
    }

    private void readdata() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_filename), 0);
        isFirstBoot = preferences.getBoolean(getString(R.string.preferences_isFirstBoot), true);
        if (true == isFirstBoot) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.preferences_isFirstBoot), false);
            editor.apply();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    init();
                    handler.sendEmptyMessageDelayed(2, 500);
                    break;
                case 2:
                    displayPic();
                    handler.sendEmptyMessageDelayed(3, 2000);
                    break;
                case 3:
                    showNextActivity();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };
}
