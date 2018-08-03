package com.mckuai.imc.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mckuai.imc.Activity.LoginActivity;
import com.mckuai.imc.Adapter.ConversationAdapter;
import com.mckuai.imc.Base.BaseFragment;
import com.mckuai.imc.Base.MCKuai;
import com.mckuai.imc.Bean.Conversation;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCNetEngine;

import java.util.ArrayList;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class MainFragment_Chat extends BaseFragment implements ConversationAdapter.OnItemClickListener {
    private ArrayList<Conversation> conversations;
    private SuperRecyclerView conversationList;
    private ConversationAdapter adapter;

    public MainFragment_Chat() {
        mTitle = "聊天";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == view) {
            view = inflater.inflate(R.layout.fragment_list, container, false);
            conversationList = (SuperRecyclerView) view.findViewById(R.id.list_content);
        }
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (null != view) {
            initView();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (null != view && null == conversationList) {
            initView();
        }
        if (!hidden) {
            showData(false);
        }
    }

    private void initView() {
//        conversationList = (SuperRecyclerView) view.findViewById(R.id.conversationlist);
        conversationList.getRecyclerView().setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        conversationList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int height = getResources().getDimensionPixelOffset(R.dimen.dividerSecondary);
                outRect.set(0, 0, 0, height);
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
                c.drawColor(getResources().getColor(R.color.dividerColorPrimary));
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
            }
        });
        conversationList.setLayoutManager(manager);


        conversationList.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mApplication.isLogin()) {
                    getConversation();
                }
            }
        });
    }

    private void showData(boolean isRefresh) {
        if (null != conversations && !isRefresh) {
            if (null == adapter) {
                adapter = new ConversationAdapter(getActivity(), this);
                conversationList.setAdapter(adapter);
            }
            adapter.setData(conversations);
        } else {
            getConversation();
        }
    }


    private void getConversation() {
        conversations = new ArrayList<Conversation>(0);
        if (!mApplication.isLogin()) {
            callLogin(1);
            return;
        } else if (null != RongIM.getInstance() && null != RongIM.getInstance().getRongIMClient()) {
            if (RongIM.getInstance().getRongIMClient().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                ArrayList<io.rong.imlib.model.Conversation> list = (ArrayList<io.rong.imlib.model.Conversation>) RongIM.getInstance().getRongIMClient().getConversationList();

                if (null != list) {
                    conversations = new ArrayList<>(list.size());
                    for (io.rong.imlib.model.Conversation imConversation : list) {
                        Conversation conversation = new Conversation();
                        conversation.setConversation(imConversation);
                        //聊天对象信息
                        String id = imConversation.getTargetId();
                        User tempUser = mApplication.daoHelper.getUserByName(id);
                        if (null == tempUser) {
                            tempUser = new User();
                            tempUser.setName(id);
                            conversation.setTarget(tempUser);
                            mApplication.netEngine.loadUserInfo(getActivity(), id, new MCNetEngine.OnLoadUserInfoResponseListener() {
                                @Override
                                public void onLoadUserInfoSuccess(User user) {
                                    if (null != user) {
                                        updateUserInfo(user);
                                    }
                                }

                                @Override
                                public void onLoadUserInfoFailure(String msg) {

                                }
                            });
                        } else {
                            conversation.setTarget(tempUser);
                        }
                        if (conversation.getConversation().getUnreadMessageCount() > 0) {
                            conversations.add(0, conversation);
                        } else {
                            conversations.add(0 == conversations.size() ? 0 : conversations.size() - 1, conversation);
                        }
                    }
                    if (null != conversationList) {
                        conversationList.hideProgress();
                    }
                }
            } else {
                //聊天服务器未连接上
                RongIM.connect(MCKuai.instence.user.getToken(), new RongIMClient.ConnectCallback() {
                    @Override
                    public void onTokenIncorrect() {
                        showMessage("用户令牌失效，需要重新登录，是否登录？", "登录", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                callLogin(0);
                                return;
                            }
                        });
                    }

                    @Override
                    public void onSuccess(String s) {
                        getConversation();
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        showMessage("连接聊天服务器失败，原因：" + errorCode.getMessage(), null, null);
                        return;
                    }
                });
            }
        } else {
            showMessage("聊天服务故障，请重新启动软件！", null, null);
        }
        showData(false);
    }

    private void callLogin(int requestcode) {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        getActivity().startActivityForResult(intent, requestcode);
    }

    @Override
    public void onItemClicked(Conversation conversation) {
        String id = conversation.getConversation().getTargetId();
        RongIM.getInstance().startPrivateChat(getActivity(), id, id);
    }


    private void updateUserInfo(User user) {
        if (null != adapter && null != user) {
            adapter.refreshItem(user);
        }
        for (int i = 0; i < conversations.size(); i++) {
            User tempUser = conversations.get(i).getTarget();
            if (tempUser.getId() == user.getId()) {
                tempUser.update(user);
                break;
                /*if (null != adapter) {
                    adapter.refreshItem(user);
                    adapter.setData(conversations);
                    adapter.notifyItemChanged(i);
                }*/
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 0:
                    showData(false);
                    break;
           /*     case 1:
                    onItemClicked(user);
                    break;*/
                case 2:
                    getConversation();
                    break;
            }
        }
    }

    public void onNewMsgRecived() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showData(true);
            }
        });
    }


}
