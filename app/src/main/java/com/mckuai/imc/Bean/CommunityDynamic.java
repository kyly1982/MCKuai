package com.mckuai.imc.Bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommunityDynamic implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -8490916867758783008L;
    private int id;// 动态id
    private int userId;// 触发创建动态操作的人ID
    private int replyNum;
    private String type;// 消息/动态的类型
    private int operUserId;// 消息中被操作的人ID，可以为0
    private String insertTime;// 消息的添加时间
    private String cont;// 内容
    private String talkTitle;// 帖子标题
    public static final int TYPE_REPLY = 0;
    public static final int TYPE_CREATE = 1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public int getTypeEx() {
        if (type.equalsIgnoreCase("talk_reply")) {
            return TYPE_REPLY;
        } else {
            return TYPE_CREATE;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOperUserId() {
        return operUserId;
    }

    public void setOperUserId(int operUserId) {
        this.operUserId = operUserId;
    }

    public String getInsertTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = null;
        try {
            Date reply = sdf.parse(insertTime);
            Date curDate = new Date(System.currentTimeMillis());

            int diff = (int) ((curDate.getTime() - reply.getTime()) / 60000);
            if (5 > diff) {
                time = "刚刚";
            } else if (60 > diff) {
                time = diff + "分钟前";
            } else {
                diff = diff / 60;
                if (24 > diff) {
                    time = diff + "小时前";
                } else {
                    diff = diff / 24;
                    if (1 == diff) {
                        time = "昨天";
                    } else if (2 == diff) {
                        time = "前天";
                    } else if (30 > diff) {
                        time = diff + "天前";
                    } else {
                        diff = diff / 30;
                        if (12 > diff) {
                            time = diff + "月前";
                        } else {
                            diff = diff / 12;
                            time = diff + "年前";
                        }
                    }
                }
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null == time ? insertTime : time;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    public String getCont() {
        return cont;
    }

    public void setCont(String cont) {
        this.cont = cont;
    }

    public String getTalkTitle() {
        return talkTitle;
    }

    public void setTalkTitle(String talkTitle) {
        this.talkTitle = talkTitle;
    }
}
