package com.mckuai.imc.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.Ad;
import com.mckuai.imc.Bean.CommunityDynamic;
import com.mckuai.imc.Bean.CommunityDynamicBean;
import com.mckuai.imc.Bean.CommunityMessage;
import com.mckuai.imc.Bean.CommunityMessageBean;
import com.mckuai.imc.Bean.CommunityMessageList;
import com.mckuai.imc.Bean.CommunityWorkBean;
import com.mckuai.imc.Bean.ForumInfo;
import com.mckuai.imc.Bean.FriendBean;
import com.mckuai.imc.Bean.MCUser;
import com.mckuai.imc.Bean.Page;
import com.mckuai.imc.Bean.Post;
import com.mckuai.imc.Bean.PostListBean;
import com.mckuai.imc.Bean.Recommend;
import com.mckuai.imc.Bean.RecommendAd;
import com.mckuai.imc.Bean.RecommendItem;
import com.mckuai.imc.Bean.RecommendsBean;
import com.mckuai.imc.Bean.SearchPost;
import com.mckuai.imc.Bean.SearchUser;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.Bean.VideoBean;
import com.mckuai.imc.R;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;

/**
 * Created by kyly on 2016/1/22.
 */
public class MCNetEngine {
    private OkHttpClient client;
    private Gson gson;
    private JsonCache cache;
    AsyncHttpClient httpClient;
    private final String domainName = "http://api.mckuai.com/";
    //    private final String domainName = "http://221.237.152.39:8081/";
//    private final String domainName = "http://192.168.10.66/";
    private MCDaoHelper daoHelper;

    public MCNetEngine() {
        client = new OkHttpClient();
        httpClient = new AsyncHttpClient();
        httpClient.setTimeout(10);

        cache = new JsonCache();
        gson = new Gson();
        daoHelper = MCKuai.instence.daoHelper;
    }

    public void exit() {
        cache.saveCacheFile();
        cancle();
        if (null != cache) {
            cache.saveCacheFile();
        }
    }

    public void cancle() {
        if (null != httpClient) {
            httpClient.cancelAllRequests(true);
        }
        if (null != client) {
            client.dispatcher().cancelAll();
        }
    }

    /***************************************************************************
     * 收藏的帖子
     ***************************************************************************/

    public interface OnLoadFavoritesListener {
        void onLoadFavoritesSuccess(ArrayList<Post> posts, Page page);

        void OnLoadFavoritesFailure(String msg);
    }

    public void loadFavorites(final Context context, int userId, int page, final OnLoadFavoritesListener listener) {
        String url = domainName + "interface.do?act=collectTalk";
        RequestParams params = new RequestParams();
        params.put("id", userId);
        params.put("page", page);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                SearchPost favorites = gson.fromJson(result.msg, SearchPost.class);
                if (null != favorites) {
                    Page temp = new Page(favorites.getAllCount(), favorites.getPage(), favorites.getPageSize());
                    listener.onLoadFavoritesSuccess(favorites.getData(), temp);
                } else {
                    listener.OnLoadFavoritesFailure(context.getString(R.string.error_parsefalse));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                listener.OnLoadFavoritesFailure(context.getString(R.string.error_requestfalse, throwable.getLocalizedMessage()));
            }
        });

    }

    /***************************************************************************
     * 搜索
     ***************************************************************************/

    public interface OnSearchListener {
        void onSearchUserSuccess(ArrayList<MCUser> posts, Page page);

        void onSearchPostSuccess(ArrayList<Post> posts, Page page);

        void onSearchFailure(String msg);
    }

    public void search(final Context context, final boolean isSearchPost, String key, final int page, final OnSearchListener listener) {
        String url = domainName + "interface.do?act=search";
        RequestParams params = new RequestParams();
        params.put("key", key);
        params.put("type", isSearchPost ? "talk" : "people");
        httpClient.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    if (isSearchPost) {
                        SearchPost post = gson.fromJson(result.msg, SearchPost.class);
                        if (null != post) {
                            Page temp = new Page(post.getAllCount(), post.getPage(), post.getPageSize());
                            listener.onSearchPostSuccess(post.getData(), temp);
                        } else {
                            listener.onSearchFailure(context.getString(R.string.error_parsefalse));
                        }
                    } else {
                        SearchUser user = gson.fromJson(result.msg, SearchUser.class);
                        if (null != user) {
                            Page temp = new Page(user.getAllCount(), user.getPage(), user.getPageSize());
                            listener.onSearchUserSuccess(user.getData(), temp);
                        } else {
                            listener.onSearchFailure(context.getString(R.string.error_parsefalse));
                        }
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                listener.onSearchFailure(context.getString(R.string.error_requestfalse, throwable.getLocalizedMessage()));
            }
        });
    }


    /***************************************************************************
     * 推荐页接口
     ***************************************************************************/

    public interface OnLoadRecommendListener {
        void onLoadRecommendSuccess(ArrayList<Post> recommendList);

        void onLoadRecommendFailure(String msg);
    }

    public void loadRecommend(final Context context, int userId, final OnLoadRecommendListener listener) {
        final String url = "http://api.mckuai.com/interface.do?act=indexRec";
        final RequestParams params = new RequestParams();
        if (0 < userId) {
            params.put("id", userId);
        }
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                if (null != listener) {
                    String result = cache.get(url, params);
                    if (null != result && result.length() > 10) {
                        ArrayList<Post> recommendList = gson.fromJson(result, new TypeToken<ArrayList<Post>>() {
                        }.getType());
                        listener.onLoadRecommendSuccess(recommendList);
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    Recommend recommend = gson.fromJson(result.msg, Recommend.class);
                    ArrayList<Post> postList = recommend.getAllPostList();
                    listener.onLoadRecommendSuccess(postList);
                    cache.put(url, params, gson.toJson(postList));
                } else {
                    listener.onLoadRecommendFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                listener.onLoadRecommendFailure(context.getString(R.string.error_requestfalse, throwable.getLocalizedMessage()));
            }
        });
    }

    /***************************************************************************
     * 视频
     ***************************************************************************/
    public interface OnLoadVideoListener {
        void onLoadVideoSuccess(VideoBean video);

        void onLoadVideoFailure(String msg);
    }

    public void loadVideoList(final Context context, String videoType, String orderType, final int page, final OnLoadVideoListener listener) {
        final String url = "http://api.mckuai.com/interface.do?act=live";
        final RequestParams params = new RequestParams();
        params.put("forumId", 100);//取视频标识
        params.put("type", URLEncoder.encode(videoType));
        params.put("orderField", orderType);
        params.put("page", page);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                if (1 == page && null != listener) {
                    String result = cache.get(url, params);
                    if (null != result && result.length() > 10) {
                        VideoBean video = gson.fromJson(result, VideoBean.class);
                        listener.onLoadVideoSuccess(video);
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse pr = new ParseResponse(context, response);
                if (pr.isSuccess) {
                    VideoBean video = gson.fromJson(pr.msg, VideoBean.class);
                    if (null != video) {
                        listener.onLoadVideoSuccess(video);
                    } else {
                        listener.onLoadVideoFailure(context.getString(R.string.error_parsefalse));
                    }
                } else {
                    listener.onLoadVideoFailure(pr.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                listener.onLoadVideoFailure(context.getString(R.string.error_requestfalse, throwable.getLocalizedMessage()));
            }
        });
    }

    /***************************************************************************
     * 登录
     ***************************************************************************/


    public interface OnLoginServerResponseListener {
        void onLoginSuccess(MCUser user);

        void onLoginFailure(String msg);
    }


    public void loginServer(@NonNull final Context context, @NonNull final MCUser user, @NonNull final OnLoginServerResponseListener listener) {
        String url = domainName + context.getString(R.string.interface_login);
        RequestParams params = new RequestParams();
        params.put("accessToken", user.getLoginToken().getToken());
        params.put("openId", user.getName());
        params.put("nickName", user.getNike());
        params.put("gender", user.getGender());
        params.put("headImg", user.getHeadImg());

        httpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    MCUser userinfo = gson.fromJson(result.msg, MCUser.class);
                    if (null != userinfo && userinfo.getName().equals(user.getName())) {
                        listener.onLoginSuccess(userinfo);
                    } else {
                        listener.onLoginFailure(context.getString(R.string.error_parsefalse));
                    }
                } else {
                    listener.onLoginFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                listener.onLoginFailure(context.getString(R.string.error_requestfalse, throwable.getLocalizedMessage()));
            }
        });
    }


    /***************************************************************************
     * 获取版块列表
     ***************************************************************************/

    public interface OnForumListResponseListener {
        void onLoadForumListSuccess(ArrayList<ForumInfo> forums);

        void onLoadForumListFailure(String msg);
    }

    public void loadFroumList(final Context context, final OnForumListResponseListener listener) {
        final String url = domainName + context.getString(R.string.interface_forumlist);
        httpClient.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                String result = cache.get(url);
                if (null != result && result.length() > 10) {
                    ArrayList<ForumInfo> forums = gson.fromJson(result, new TypeToken<ArrayList<ForumInfo>>() {
                    }.getType());
                    listener.onLoadForumListSuccess(forums);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    ArrayList<ForumInfo> forums = gson.fromJson(result.msg, new TypeToken<ArrayList<ForumInfo>>() {
                    }.getType());
                    listener.onLoadForumListSuccess(forums);
                    cache.put(url, result.msg);
                } else {
                    listener.onLoadForumListFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onLoadForumListFailure(throwable.getLocalizedMessage());
            }
        });
    }


    /***************************************************************************
     * 获取帖子列表
     ***************************************************************************/

    public interface OnPostListResponseListener {
        void onLoadPostListSuccess(ArrayList<Post> posts, Page page);

        void onLoadPostListFailure(String msg);
    }

    public void loadPostList(final Context context, int forumId, String postType, int nextPage, final OnPostListResponseListener listener) {
        final String url = domainName + context.getString(R.string.interface_postlist);
        final RequestParams params = new RequestParams();
        params.put("forumId", forumId);
        params.put("page", nextPage);
        if (null != postType) {
            params.put("type", postType);
        }
        httpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                String result = cache.get(url, params);
                if (null != result && result.length() > 10) {
                    PostListBean bean = gson.fromJson(result, PostListBean.class);
                    Page page = new Page(bean.getAllCount(), bean.getPage(), bean.getPageSize());
                    listener.onLoadPostListSuccess(bean.getdata(), page);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    PostListBean bean = gson.fromJson(result.msg, PostListBean.class);
                    Page page = new Page(bean.getAllCount(), bean.getPage(), bean.getPageSize());
                    listener.onLoadPostListSuccess(bean.getdata(), page);
                    if (1 == page.getPage()) {
                        cache.put(url, params, result.msg);
                    }
                } else {
                    listener.onLoadPostListFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onLoadPostListFailure(throwable.getLocalizedMessage());
            }
        });
    }


    /***************************************************************************
     * 获取人物列表
     ***************************************************************************/

    public interface OnCharacterListResponseListener {
        void onLoadCharacterListSuccess(ArrayList<String> characters);

        void onLoadCharacterListFailure(String msg);
    }

    public void loadCharacterList(final Context context, Page page, final OnCharacterListResponseListener listener) {

    }


    /***************************************************************************
     * 对获取工具列表
     ***************************************************************************/

    public interface OnToolListResponseListener {
        void onLoadToolListSuccess(ArrayList<String> tools);

        void onLoadToolListFailure(String msg);
    }

    public void loadToolList(final Context context, Page page, final OnToolListResponseListener listener) {

    }

    /***************************************************************************
     * 上传图片
     ***************************************************************************/

    public interface OnUploadImageResponseListener {
        void onImageUploadSuccess(String url);

        void onImageUploadFailure(String msg);
    }

    public void uploadImage(final Context context, ArrayList<Bitmap> bitmaps, final OnUploadImageResponseListener listener) {
        String url = domainName + context.getString(R.string.interface_uploadimage);
        RequestParams params = new RequestParams();
        if (null != bitmaps && !bitmaps.isEmpty()) {
            String fileName = null;
            for (int i = 0; i < bitmaps.size(); i++) {
                fileName = i + ".jpg";
                params.put("upload", Bitmap2IS(bitmaps.get(i)), fileName, "image/jpeg");
            }
        }
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    listener.onImageUploadSuccess(result.msg);
                } else {
                    listener.onImageUploadFailure(result.msg);
                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onImageUploadFailure(throwable.getLocalizedMessage());
            }
        });
    }


    /***************************************************************************
     * 个人中心社区消息
     ***************************************************************************/
    public interface OnLoadCommunityMessageResponseListener {
        void onLoadCommunityMessageSuccess(ArrayList<CommunityMessage> messages, User user, Page page);

        void onLoadCommunityMessageFailure(String msg);
    }

    public void loadCommunityMessage(final Context context, int userId, int nextPage, final OnLoadCommunityMessageResponseListener listener) {
        final String url = domainName + context.getString(R.string.interface_usercenter);
        final RequestParams params = new RequestParams();
        params.put("type", "message");
        params.put("id", userId);
        params.put("messageType", "all");
        params.put("page", nextPage);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                String result = cache.get(url, params);
                if (null != result && result.length() > 10) {
                    CommunityMessageBean bean = gson.fromJson(result, CommunityMessageBean.class);
                    CommunityMessageList list = bean.getList();
                    Page page = new Page(list.getAllCount(), list.getPage(), list.getPageSize());
                    listener.onLoadCommunityMessageSuccess(list.getData(), bean.getUser(), page);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    CommunityMessageBean bean = gson.fromJson(result.msg, CommunityMessageBean.class);
                    if (null != bean && null != bean.getList() && null != bean.getUser()) {
                        CommunityMessageList list = bean.getList();
                        Page page = new Page(list.getAllCount(), list.getPage(), list.getPageSize());
                        listener.onLoadCommunityMessageSuccess(list.getData(), bean.getUser(), page);
                        daoHelper.addUser(bean.getUser());
                        cache.put(url, params, result.msg);
                    } else {
                        listener.onLoadCommunityMessageFailure("转换数据失败！");
                    }
                } else {
                    listener.onLoadCommunityMessageFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onLoadCommunityMessageFailure(throwable.getLocalizedMessage());
            }
        });
    }

    /***************************************************************************
     * 个人中心社区动态
     ***************************************************************************/
    public interface OnLoadCommunityDynamicResponseListener {
        void onLoadCommunityDynamicSuccess(ArrayList<CommunityDynamic> dynamics, User user, Page page);

        void onLoadCommunityDynamicFailure(String msg);
    }

    public void loadCommunityDynamic(final Context context, int userId, int nextPage, final OnLoadCommunityDynamicResponseListener listener) {
        final String url = domainName + context.getString(R.string.interface_usercenter);
        final RequestParams params = new RequestParams();
        params.put("type", "dynamic");
        params.put("id", userId);
        params.put("page", nextPage);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                String result = cache.get(url, params);
                if (null != result && result.length() > 10) {
                    CommunityDynamicBean bean = gson.fromJson(result, CommunityDynamicBean.class);
                    Page page = new Page(bean.getList().getAllCount(), bean.getList().getPage(), bean.getList().getPageSize());
                    listener.onLoadCommunityDynamicSuccess(bean.getList().getData(), new User(bean.getUser()), page);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    CommunityDynamicBean bean = gson.fromJson(result.msg, CommunityDynamicBean.class);
                    if (null != bean && null != bean.getList() && null != bean.getUser()) {
                        Page page = new Page(bean.getList().getAllCount(), bean.getList().getPage(), bean.getList().getPageSize());
                        listener.onLoadCommunityDynamicSuccess(bean.getList().getData(), new User(bean.getUser()), page);
                        daoHelper.addUser(bean.getUser());
                        if (page.getPage() == 1) {
                            cache.put(url, params, result.msg);
                        }
                    } else {
                        listener.onLoadCommunityDynamicFailure("转换数据失败！");
                    }
                } else {
                    listener.onLoadCommunityDynamicFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onLoadCommunityDynamicFailure(throwable.getLocalizedMessage());
            }
        });
    }


    /***************************************************************************
     * 个人中心社区作品
     ***************************************************************************/
    public interface OnloadCommunityWorkResponseListener {
        void onLoadCommunityWorkSuccess(ArrayList<Post> works, User user, Page page);

        void onLoadCommunityWorkFailure(String msg);
    }

    public void loadCommunityWork(final Context context, int userId, int nextPage, final OnloadCommunityWorkResponseListener listener) {
        final String url = domainName + context.getString(R.string.interface_usercenter);
        final RequestParams params = new RequestParams();
        params.put("type", "work");
        params.put("id", userId);
        params.put("page", nextPage);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                String result = cache.get(url, params);
                if (null != result && result.length() > 10) {
                    CommunityWorkBean bean = gson.fromJson(result, CommunityWorkBean.class);
                    Page page = new Page(bean.getList().getAllCount(), bean.getList().getPage(), bean.getList().getPageSize());
                    listener.onLoadCommunityWorkSuccess(bean.getList().getdata(), bean.getUser(), page);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    CommunityWorkBean bean = gson.fromJson(result.msg, CommunityWorkBean.class);
                    if (null != bean && null != bean.getList() && null != bean.getUser()) {
                        Page page = new Page(bean.getList().getAllCount(), bean.getList().getPage(), bean.getList().getPageSize());
                        listener.onLoadCommunityWorkSuccess(bean.getList().getdata(), bean.getUser(), page);
                        daoHelper.addUser(bean.getUser());
                        cache.put(url, params, result.msg);
                    } else {
                        listener.onLoadCommunityWorkFailure("转换数据失败！");
                    }
                } else {
                    listener.onLoadCommunityWorkFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onLoadCommunityWorkFailure(throwable.getLocalizedMessage());
            }
        });
    }

    /***************************************************************************
     * 添加好友
     ***************************************************************************/

    public interface OnAddFriendResponseListener {
        void onAddFriendSuccess();

        void onAddFriendFailure(String msg);
    }

    public void addFriend(final Context context, int userId, final OnAddFriendResponseListener listener) {
        String url = domainName + "interface.do?act=attentionUser";
        RequestParams params = new RequestParams();
        params.put("ownerId", MCKuai.instence.user.getId());
        params.put("id", userId);
        httpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    listener.onAddFriendSuccess();
                } else {
                    listener.onAddFriendFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onAddFriendFailure(throwable.getLocalizedMessage());
            }
        });
    }


    /***************************************************************************
     * 个人中心好友
     ***************************************************************************/
    public interface OnloadFriendResponseListener {
        void onLoadFriendSuccess(ArrayList<MCUser> friends, Page page);

        void OnloadFriendFailure(String msg);
    }

    public void loadFriendList(final Context context, final int nextPage, final OnloadFriendResponseListener listener) {
        final String url = domainName + context.getString(R.string.interface_fellowuserlist);
        final RequestParams params = new RequestParams();
        params.put("id", MCKuai.instence.user.getId());
        params.put("page", nextPage);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                String result = cache.get(url, params);
                if (null != url && result.length() > 10) {
                    FriendBean bean = gson.fromJson(result, FriendBean.class);
                    listener.onLoadFriendSuccess(bean.getData(), new Page(20, 1, 20));
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    FriendBean bean = gson.fromJson(result.msg, FriendBean.class);
                    if (null != bean) {
                        Page page = new Page(bean.getAllCount(), bean.getPage(), bean.getPageSize());
                        ArrayList<MCUser> users = bean.getData();
                        listener.onLoadFriendSuccess(bean.getData(), page);
                        if (null != users && !users.isEmpty()) {
                            for (MCUser user : users) {
                                User tempuser = new User(user);
                                tempuser.setIsFriend(true);
                                daoHelper.addUser(tempuser);
                            }
                        }
                        if (1 == nextPage) {
                            cache.put(url, params, result.msg);
                        }
                    } else {
                        listener.OnloadFriendFailure("转换数据失败！");
                    }
                } else {
                    listener.OnloadFriendFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.OnloadFriendFailure(throwable.getLocalizedMessage());
            }
        });
    }

    /***************************************************************************
     * 上传头像图片
     ***************************************************************************/

    public interface OnUploadUserCoverListener {
        void onUploadCoverSuccess(String url);

        void onUploadCoverFailure(String msg);
    }

    public void uploadUserCover(final Context context, Bitmap cover, final OnUploadUserCoverListener listener) {
        String url = "http://www.mckuai.com/" + context.getString(R.string.interface_uploadimage);
        RequestParams params = new RequestParams();
        params.put("fileHeadImg", Bitmap2IS(cover), "cover.jpg", "image/jpeg");
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    listener.onUploadCoverSuccess(result.msg);
                } else {
                    listener.onUploadCoverFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onUploadCoverFailure(throwable.getLocalizedMessage());
            }
        });
    }

    public void uploadUserCover(final Context context, String fileName, final OnUploadUserCoverListener listener) {
        String url = "http://www.mckuai.com/" + context.getString(R.string.interface_uploadimage);
        File file = new File(fileName);
        if (null != file && file.exists() && file.isFile()) {
            RequestParams params = new RequestParams();
            try {
                params.put("fileHeadImg", file, "image/jpeg");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                listener.onUploadCoverFailure(e.getLocalizedMessage());
                return;
            }
            httpClient.post(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ParseResponse result = new ParseResponse(context, response);
                    if (result.isSuccess) {
                        listener.onUploadCoverSuccess(result.msg);
                    } else {
                        listener.onUploadCoverFailure(result.msg);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    listener.onUploadCoverFailure(throwable.getLocalizedMessage());
                }
            });

        }

    }

    /***************************************************************************
     * 更新头像url
     ***************************************************************************/

    public interface OnUpdateUserCoverListener {
        void onUpdateUserCoverSuccess();

        void onUpdateUserCoverFailure(String msg);
    }

    public void updateUserCover(final Context context, int userId, String coverUrl, final OnUpdateUserCoverListener listener) {
        String url = domainName + context.getString(R.string.interface_update_userinfo);
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("flag", "headImg");
        params.put("headImg", coverUrl);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    listener.onUpdateUserCoverSuccess();
                } else {
                    listener.onUpdateUserCoverFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onUpdateUserCoverFailure(throwable.getLocalizedMessage());
            }
        });
    }

    /***************************************************************************
     * 更新昵称
     ***************************************************************************/

    public interface OnUpdateUserNickListener {
        void onUpdateUserNickSuccess();

        void onUpdateUserNickFailure(String msg);
    }

    public void updateUserNick(final Context context, int userId, String nick, final OnUpdateUserNickListener listener) {
        String url = domainName + context.getString(R.string.interface_update_userinfo);
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("flag", "name");
        params.put("nickName", nick);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    listener.onUpdateUserNickSuccess();
                } else {
                    listener.onUpdateUserNickFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onUpdateUserNickFailure(throwable.getLocalizedMessage());
            }
        });
    }

    /***************************************************************************
     * 更新地址
     ***************************************************************************/

    public interface OnUpdateUserAddressResponseListener {
        void onUpdateAddressSuccess();

        void onUpdateAddressFailure(String msg);
    }

    public void updateUserAddress(final Context context, int userId, String address, final OnUpdateUserAddressResponseListener listener) {
        String url = domainName + context.getString(R.string.interface_updateLocation);
        RequestParams params = new RequestParams();
        params.put("id", userId);
        params.put("addr", address);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    listener.onUpdateAddressSuccess();
                } else {
                    listener.onUpdateAddressFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onUpdateAddressFailure(throwable.getLocalizedMessage());
            }
        });
    }


    /***************************************************************************
     * 推荐用户
     ***************************************************************************/
    public interface OnLoadRecommendUserListener {
        void onLoadUserSuccess(ArrayList<User> users);

        void onLoadUserFailure(String msg);
    }

    public void loadRecommendUser(final Context context, Integer userId, final OnLoadRecommendUserListener listener) {
        String url = domainName + "interface.do?act=recPeople";
        if (null != userId) {
            url += ("&userId=" + userId);
        }
        final String finalUrl = url;
        httpClient.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                String result = cache.get(finalUrl);
                if (null != result && result.length() > 10) {
                    ArrayList<User> users = gson.fromJson(result, new TypeToken<ArrayList<User>>() {
                    }.getType());
                    listener.onLoadUserSuccess(users);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onLoginSuccess(statusCode, headers, response);
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    ArrayList<User> users = gson.fromJson(result.msg, new TypeToken<ArrayList<User>>() {
                    }.getType());
                    listener.onLoadUserSuccess(users);
                    for (User user : users) {
                        daoHelper.addUser(user);
                    }
                    cache.put(finalUrl, result.msg);
                } else {
                    listener.onLoadUserFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //super.onFailure(statusCode, headers, responseString, throwable);
                listener.onLoadUserFailure(throwable.getLocalizedMessage());
            }
        });
    }

    /**
     * 获取用户信息
     */
    public interface OnLoadUserInfoResponseListener {
        void onLoadUserInfoSuccess(User user);

        void onLoadUserInfoFailure(String msg);
    }

    public void loadUserInfo(final Context context, String userName, final OnLoadUserInfoResponseListener listener) {
        String url = domainName + "interface.do?act=newUserInfo";
        RequestParams params = new RequestParams();
        params.put("userName", userName);
        httpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    MCUser user = gson.fromJson(result.msg, MCUser.class);
                    if (null != user) {
                        listener.onLoadUserInfoSuccess(new User(user));
                        daoHelper.addUser(user);
                    } else {
                        listener.onLoadUserInfoFailure("返回数据不正确！");
                    }
                } else {
                    listener.onLoadUserInfoFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onLoadUserInfoFailure(throwable.getLocalizedMessage());
            }

        });
    }

    public interface OnCheckFriendshipResponseListener {
        void onIsFriendShip();

        void onIsStrangerShip();

        void onError(String msg);
    }

    public void checkFriendship(final Context context, int userId, final OnCheckFriendshipResponseListener listener) {
        String url = domainName + "interface.do?act=isAttention";
        RequestParams params = new RequestParams();
        params.put("ownerId", MCKuai.instence.user.getId());
        params.put("otherId", userId);
        httpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    listener.onIsFriendShip();
                } else {
                    listener.onIsStrangerShip();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onError(throwable.getLocalizedMessage());
            }
        });
    }


    /**
     * 获取推荐区广告
     */
    public interface OnGetRecommendAdListener {
        void onGetRecommendADSuccess(RecommendAd ad);

        void onGetRecommendAdFail(String msg);
    }

    public void getRecommendAd(final Context context, final OnGetRecommendAdListener listener) {
        String url = "http://www.mckuai.com/interface.do?act=appadv";
        httpClient.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                RecommendAd ad = gson.fromJson(response.toString(), RecommendAd.class);
                if (null != listener) {
                    if (null != ad) {
                        listener.onGetRecommendADSuccess(ad);
                    } else {
                        listener.onGetRecommendAdFail("返回数据解析失败！");
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (null != listener) {
                    listener.onGetRecommendAdFail(throwable.getLocalizedMessage());
                }
            }
        });
    }


    /**
     * 获取退出广告
     */
    public interface OnGetAdResponse {
        void onGetAdSuccess(Ad ad);

        void onGetAdFailure(String msg);
    }

    public void getAd(final Context context, final OnGetAdResponse listener) {
        String url = "http://api.mckuai.com/interface.do?act=adv";
        httpClient.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    Gson gson = new Gson();
                    Ad ad = gson.fromJson(result.msg, Ad.class);
                    if (null != ad) {
                        listener.onGetAdSuccess(ad);
                    } else {
                        listener.onGetAdFailure("返回数据解析失败！");
                    }
                } else {
                    listener.onGetAdFailure(result.msg);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onGetAdFailure(throwable.getLocalizedMessage());
            }
        });
    }

    /**
     * 获取插入广告列表
     */

    public interface OnGetAdsResponse {
        void onGetAdSuccess(ArrayList<Ad> ads);

        void onGetAdFailure(String msg);
    }

    public void getAds(final Context context, final OnGetAdsResponse listener) {
        String url = "http://api.mckuai.com/interface.do?act=timeAdv";
        httpClient.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    Gson gson = new Gson();
                    ArrayList<Ad> ads = gson.fromJson(result.msg, new TypeToken<ArrayList<Ad>>() {
                    }.getType());
                    if (null != ads) {
                        listener.onGetAdSuccess(ads);
                    } else {
                        listener.onGetAdFailure("返回数据解析失败！");
                    }
                } else {
                    listener.onGetAdFailure(result.msg);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onGetAdFailure(throwable.getLocalizedMessage());
            }
        });
    }

    private static InputStream Bitmap2IS(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
        return sbs;
    }

    /***************************************************************************
     * 新推荐页
     ***************************************************************************/
    public static interface onGetRecommendPostListener {
        void onGetRecommendPostSuccess(ArrayList<RecommendItem> recommendItems, int currentPage, int pageCount);

        void onGetRecommendPostFailure(String msg);
    }

    public void loadRecommendPost(final Context context, final onGetRecommendPostListener listener, int requestPage) {
        String url = "http://api.mckuai.com/interface.do?act=info";
        RequestParams params = new RequestParams();
        params.add("page", requestPage + "");
        httpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    Gson gson = new Gson();
                    RecommendsBean bean = gson.fromJson(result.msg, RecommendsBean.class);
                    if (null != bean) {
                        if (null != bean.getData() && !bean.getData().isEmpty()) {
                            listener.onGetRecommendPostSuccess(bean.getData(), bean.getPage(), bean.getAllCount());
                        } else {
                            listener.onGetRecommendPostFailure("暂时没有内容或已经到末尾！");
                        }
                    } else {
                        listener.onGetRecommendPostFailure("返回数据解析失败！");
                    }
                } else {
                    listener.onGetRecommendPostFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onGetRecommendPostFailure(throwable.getLocalizedMessage());
            }
        }).setTag("loadRecommendPost");
    }

    public void stopLoadRecommendPost() {
        httpClient.cancelRequestsByTAG("loadRecommendPost", true);
    }


    /***************************************************************************
     * 新视频页
     ***************************************************************************/
    public interface onLoadDaShengVideoListener {
        void onLoadVideoSuccess(ArrayList<RecommendItem> items, int currentPage, int allCount);

        void onLoadVideoFailure(String msg);
    }

    public void loadVideo(final Context context, final onLoadDaShengVideoListener listener, int requestPage) {
        String url = "http://api.mckuai.com/interface.do?act=info";
        RequestParams params = new RequestParams();
        params.add("page", requestPage + "");
        params.add("type", "dashen");
        httpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ParseResponse result = new ParseResponse(context, response);
                if (result.isSuccess) {
                    Gson gson = new Gson();
                    RecommendsBean bean = gson.fromJson(result.msg, RecommendsBean.class);
                    if (null != bean) {
                        if (null != bean.getData() && !bean.getData().isEmpty()) {
                            listener.onLoadVideoSuccess(bean.getData(), bean.getPage(), bean.getAllCount());
                        } else {
                            listener.onLoadVideoFailure("暂时没有内容或已经到末尾！");
                        }
                    } else {
                        listener.onLoadVideoFailure("返回数据解析失败！");
                    }
                } else {
                    listener.onLoadVideoFailure(result.msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onLoadVideoFailure(throwable.getLocalizedMessage());
            }
        }).setTag("loadVideos");
    }

    public void stopLoadVideo() {
        httpClient.cancelRequestsByTAG("loadVideos", true);
    }
}
