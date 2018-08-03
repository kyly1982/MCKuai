package com.mckuai.imc.Utils;

import android.content.Context;

import com.mckuai.imc.R;

import org.json.JSONObject;

import okhttp3.Response;

/**
 * Created by kyly on 2016/1/28.
 * 对接口返回的结果进行预处理
 */
public class ParseResponse {
    public static boolean isSuccess = false;
    public String pageBean = null;
    public static String msg = null;

    /**
     * 将JSONObject类型的返回数据解析成预处理后的类
     * 会检查JSONObject的长度
     * 如果长度低于10个字符，则认为是失败
     *
     * @param context
     * @param response
     */
    public ParseResponse(Context context, JSONObject response) {
        this(context, response, false);
    }


    /**
     * 将JSONObject类型的返回数据解析成预处理后的类
     *
     * @param context
     * @param response     返回的JSONObject
     * @param ignoreLength 是否检查返回的长度
     */
    public ParseResponse(Context context, JSONObject response, boolean ignoreLength) {


        if (ignoreLength) {
            if (null != response && checkState(context, response)) {
                setData(context, response);
                setPage(context, response);
            }
        } else {
            if (checkLength(context, response, ignoreLength) && checkState(context, response)) {
                setData(context, response);
                setPage(context, response);
            }
        }

    }

    @Deprecated
    public static boolean checkLength(Context context, JSONObject response, boolean ignoreLength) {
        if ((null == response && response.length() == 0) || (!ignoreLength && response.toString().length() < 10)) {
            isSuccess = false;
            msg = context.getString(R.string.error_pretreatmentres_nullerror);
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkState(Response response, boolean ignoreLength) {
        if (null != response) {
            if (response.isSuccessful()) {
                if (null != response.body()) {
                    if (ignoreLength) {
                        return true;
                    } else {
                        if (response.body().contentLength() > 10) {
                            return true;
                        } else {
                            msg = "返回数据长度不足！";
                        }
                    }
                } else {
                    msg = "返回数据为空！";
                    return false;
                }
            }
        } else {
            msg = "返回数据为空!";
        }
        return false;
    }


    public static boolean checkState(Context context, JSONObject response) {
        if (response.has("state")) {
            try {
                if (response.getString("state").equals("ok")) {
                    isSuccess = true;
                    return true;
                } else {
                    isSuccess = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isSuccess = false;
                msg = context.getString(R.string.error_serverreturn_unkonw);
                return false;
            }
        }
        if (response.has("msg")) {
            try {
                msg = response.getString("msg");
            } catch (Exception e) {
                e.printStackTrace();
                msg = context.getString(R.string.error_serverfalse);
            }

        } else {
            msg = context.getString(R.string.error_serverfalse_unknow);
        }
        return false;
    }

    private boolean setData(Context context, JSONObject response) {
        if (response.has("dataObject")) {
            try {
                msg = response.getString("dataObject");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (response.has("msg")) {
            try {
                msg = response.getString("msg");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    private boolean setPage(Context context, JSONObject response) {
        if (response.has("pageBean")) {
            try {
                pageBean = response.getString("pageBean");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
