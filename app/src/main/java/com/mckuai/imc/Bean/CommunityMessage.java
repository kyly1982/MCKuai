package com.mckuai.imc.Bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommunityMessage implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 9201548077186523373L;
    private int id;
    private int userId;// 触发创建消息操作的人ID
    private int replyNum;// 回复数
    private String userName;// 触发创建消息操作的人名字
    private String headImg;// 触发创建消息操作的人的头像
    private String type;// 消息/动态的类型
    private String cont1;// 消息字段1
    private String cont2;// 消息字段2
    private String cont3;// 消息字段3
    private int operUserId;// 消息中被操作的人ID，可以为0
    private String operUserName;// 消息中被操作的人姓名,可以为空
    private String insertTime;// 消息的添加时间
    private String cont;// 内容
    private String talkTitle;// 被回复时的帖子标题
    private String smallImgs;// 临时字段 内容有图片的情况存放图片
    private String samllVideo;// 临时字段 内容有视频的情况存放视频
    private String showText;// app需要的字段
    private boolean isSystemMessage = false;

    /**
     * 回复
     */
    public static final int TYPE_REPLY = 0;
    /**
     * @你
     */
    public static final int TYPE_AT = 1;
    /**
     * 系统消息
     */
    public static final int TYPE_SYSTEM = 2;
    /**
     * 动态中的回复
     */
    public static final int TYPE_REPLY_DYNAMIC = 4;
    /**
     * 动态中的创造
     */
    public static final int TYPE_CREAT_DYNAMIC = 5;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public int getTypeEx() {
        switch (type) {
            case "at_by_other":
                return TYPE_AT;
            case "pub_talk_moderator":
                return TYPE_SYSTEM;
            case "digest_talk_moderator":
                return TYPE_SYSTEM;
            case "move_talk_moderator":
                return TYPE_SYSTEM;
            case "del_talk_moderator":
                return TYPE_SYSTEM;
            case "talk_deleted":
                return TYPE_SYSTEM;
            case "user_report_talk_success":
                return TYPE_SYSTEM;
            default:
                return TYPE_REPLY;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCont1() {
        return cont1;
    }

    public void setCont1(String cont1) {
        this.cont1 = cont1;
    }

    public String getCont2() {
        return cont2;
    }

    public void setCont2(String cont2) {
        this.cont2 = cont2;
    }

    public String getCont3() {
        return cont3;
    }

    public void setCont3(String cont3) {
        this.cont3 = cont3;
    }

    public int getOperUserId() {
        return operUserId;
    }

    public void setOperUserId(int operUserId) {
        this.operUserId = operUserId;
    }

    public String getOperUserName() {
        return operUserName;
    }

    public void setOperUserName(String operUserName) {
        this.operUserName = operUserName;
    }

    public String getInsertTime() {
        if (null != insertTime) {
            return insertTime.substring(0, 19);
        }
        return insertTime;
    }

    public String getInsertTimeEx() {
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

    public String getSmallImgs() {
        return smallImgs;
    }

    public void setSmallImgs(String smallImgs) {
        this.smallImgs = smallImgs;
    }

    public String getSamllVideo() {
        return samllVideo;
    }

    public void setSamllVideo(String samllVideo) {
        this.samllVideo = samllVideo;
    }

    public String getShowText() {
        return showText;
    }

    public void setShowText(String showText) {
        this.showText = showText;
    }

    public int getReplyNum() {
        return replyNum;
    }

    public void setReplyNum(int replyNum) {
        this.replyNum = replyNum;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public void setSystemMessage(boolean isSystemMessage) {
        this.isSystemMessage = isSystemMessage;
    }
}
