/**
 *
 */
package com.mckuai.imc.Utils;

import android.util.Log;
import android.util.LruCache;

import com.loopj.android.http.RequestParams;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author kyly
 */
public class JsonCache {

    private LruCache<String, String> mCache;
    private String mCacheFile;// 缓存文件
    private final int CACHE_SIZE = 512 * 1024;// 使用512K的内存来做为缓存

    private static final String TAG = "JsonCache";

    /**
     *
     */

    public JsonCache() {
        // TODO Auto-generated constructor stub
        mCacheFile = MCKuai.instence.getJsonFile();
        mCache = new LruCache<String, String>(CACHE_SIZE);
        initFromFile();
    }

    /**
     * initFromFile:用缓存文件中的内容来初始化缓存<br>
     * 此函数会检查程序版本是否一致，如果不一致会删除当前的缓存文件再重新创建<br/>
     */
    private void initFromFile() {
        File file = new File(mCacheFile);
        if (file.exists()) {
            String key = null;// url
            String value;// 返回的内容
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        new FileInputStream(mCacheFile), "UTF-8");
                BufferedReader reader = new BufferedReader(inputStreamReader);
                // 检查缓存的版本是否与当前的版本相同，仅版本一致时才读取，否则将删除已存在的缓存文件

                if (reader.readLine().equals(BuildConfig.VERSION_NAME)) {

//                    Long startTime = System.nanoTime();
                    try {
                        key = reader.readLine().toString();
                    } catch (Exception e) {
                        key = null;
                        // TODO: handle exception
                    }
                    while (null != key && !key.isEmpty()) {
                        value = reader.readLine();
                        if (null != value) {
                            mCache.put(key, value);
                            try {
                                key = reader.readLine().toString();
                            } catch (Exception e) {
                                // TODO: handle exception
                                key = null;
                            }
                        } else {
                            Log.e(TAG, "\"" + key + "\"的值为空");
                        }
                    }
                    //Log.e(TAG, "解析文件用时" + (System.nanoTime() - startTime));
                    reader.close();
                    inputStreamReader.close();
                } else {
                    // 版本号不相同，删除文件
                    deleteFile();
                    reader.close();
                    inputStreamReader.close();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                deleteFile();
            }
        }
    }

    public void put(String url, String value) {
        String temp = mCache.get(url);
        if (null != temp && temp.equalsIgnoreCase(value)) {
            if (!temp.equalsIgnoreCase(value)) {
                mCache.remove(url);
                mCache.put(url, value);
            }
        } else {
            mCache.put(url, value);
        }
    }

    public void put(String url, RequestParams params, String value) {
        if (null != params) {
            url += "&" + params.toString();
        }
        mCache.put(url, value);
    }

    public String get(String url) {
        return mCache.get(url);
    }

    public String get(String url, RequestParams params) {
        if (null != params) {
            url += "&" + params.toString();
        }
        String result = mCache.get(url);
        return null == result ? "" : result;
    }

    /**
     * saveCacheFile:将缓存保存到文件<br>
     */
    public void saveCacheFile() {
        if (0 == mCache.size()) {
            return;
        }
        File file = new File(mCacheFile);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        } else {
            file.delete();
        }
        try {
            long startTime = System.nanoTime();
            OutputStreamWriter streamWriter = new OutputStreamWriter(
                    new FileOutputStream(file), "UTF-8");
            BufferedWriter bw = new BufferedWriter(streamWriter);
            bw.write(BuildConfig.VERSION_NAME);
            HashMap<String, String> data = (HashMap<String, String>) mCache.snapshot();
            String key;
            StringBuilder strData = new StringBuilder();
            for (Iterator<String> it = data.keySet().iterator(); it.hasNext(); ) {
                key = it.next();
                strData.append("\n" + key + "\n" + data.get(key));
                //bw.write("\n" + key + "\n" + data.get(key));
            }
            bw.write(strData.toString());
            bw.close();
            streamWriter.close();
            // Log.e(TAG, "保存用时：" + (System.nanoTime() - startTime));
            // Log.e(TAG, "文件大小：" + file.length());
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * deleteFile:删除缓存文件<br>
     */
    private void deleteFile() {
        File file = new File(mCacheFile);
        file.delete();
        file = null;
    }
}
