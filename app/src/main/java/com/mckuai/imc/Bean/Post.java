/**
 *
 */
package com.mckuai.imc.Bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kyly
 */
public class Post implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2843054425964962633L;
    private int id;// 帖子id
    private int forumId;//版块id
    private int userId;// 发帖人id
    private int replyNum;// 回复数
    private int smallId;//帖子类型id,发帖用
    private String forumName;// 版块名字
    private String talkTitle;// 标题
    private String talkType;//直播类型
    private String insertTime;// 话题添加时间
    private String replyTime;// 回帖时间
    private String userName;// 发帖人姓名
    private String mobilePic;// 帖子封面图片
    private String headImg;//发帖人头像
    private String icon;//版块的封面
    private String content;//内容,发帖时用
    private String smallName;//帖子类型,发帖用

    private int hasVideo; //视频数量
    private int isNew;//
    private int hasImg;//有图片
    private int isDing;//置顶
    private int isJing;//加精
    private int isLive;//正在直播

    public Post() {
    }

    public Post(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTalkTitle() {
        return talkTitle;
    }

    public void setTalkTitle(String talkTitle) {
        this.talkTitle = talkTitle;
    }

    public String getForumName() {
        return forumName;
    }

    public void setForumName(String forumName) {
        this.forumName = forumName;
    }

    /**
     * getLastReplyTime:获取最新回帖的时间<br>
     * TODO 返回的是一个转换成常用语表述的时间<br/>
     *
     * @return
     */
    public String getLastReplyTime() {
        if (null == replyTime) {
            return "未知";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = null;
        try {
            Date reply = sdf.parse(replyTime);
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
        return null == time ? replyTime : time;
    }

    public void setReplyTime(String replyTime) {
        this.replyTime = replyTime;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    public int getReplyNum() {
        return replyNum;
    }

    /**
     * getReplyNumEx:获取回帖数<br>
     * TODO 返回的回帖数是长度固定的string，方便排版<br/>
     *
     * @return
     */
    public String getReplyNumEx() {
        String count = "0";
        if (10 > replyNum) {
            count = replyNum + "      ";
        } else if (100 > replyNum) {
            count = replyNum + "    ";
        } else if (1000 > replyNum) {
            count = replyNum + "  ";
        } else {
            count = replyNum + "";
        }
        return count;
    }

    public String getMobilePic() {
        return mobilePic;
    }

    public void setMobilePic(String mobilePic) {
        this.mobilePic = mobilePic;
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

    public int isHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(int hasVideo) {
        this.hasVideo = hasVideo;
    }

    public int isNew() {
        return isNew;
    }

    public void setNew(int isNew) {
        this.isNew = isNew;
    }

    public int isHasImg() {
        return hasImg;
    }

    public void setHasImg(int hasImg) {
        this.hasImg = hasImg;
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

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public int getIsDing() {
        return isDing;
    }

    public boolean isTop() {
        return 3 == isDing;
    }

    public void setIsDing(int isDing) {
        this.isDing = isDing;
    }

    public int getIsJing() {
        return isJing;
    }

    public void setIsJing(int isJing) {
        this.isJing = isJing;
    }

    public boolean isEssence() {
        return 2 == this.isJing;
    }

    public int getIsLive() {
        return isLive;
    }

    public void setIsLive(int isLive) {
        this.isLive = isLive;
    }

  /*  public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        this.postType = postType;
    }*/

    public String getTalkType() {
        return talkType;
    }

    public void setTalkType(String talkType) {
        this.talkType = talkType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSmallName() {
        return smallName;
    }

    public void setSmallName(String smallName) {
        this.smallName = smallName;
    }

    public int getSmallId() {
        return smallId;
    }

    public void setSmallId(int smallId) {
        this.smallId = smallId;
    }

}
