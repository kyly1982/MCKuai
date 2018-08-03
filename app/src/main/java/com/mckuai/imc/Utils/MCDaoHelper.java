package com.mckuai.imc.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mckuai.imc.Bean.Forum;
import com.mckuai.imc.Bean.ForumInfo;
import com.mckuai.imc.Bean.MCUser;
import com.mckuai.imc.Bean.PostType;
import com.mckuai.imc.Bean.Type;
import com.mckuai.imc.Bean.User;
import com.mckuai.imc.Utils.MCDao.DaoMaster;
import com.mckuai.imc.Utils.MCDao.DaoSession;
import com.mckuai.imc.Utils.MCDao.ForumDao;
import com.mckuai.imc.Utils.MCDao.TypeDao;
import com.mckuai.imc.Utils.MCDao.UserDao;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by kyly on 2016/1/21.
 */
public class MCDaoHelper {
    private DaoSession daoSession;


    public MCDaoHelper(Context context) {
        MCDBOpenHelper helper = new MCDBOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public void close() {
        if (null != daoSession) {
            SQLiteDatabase db = daoSession.getDatabase();
            //db.close();
            daoSession.clear();
            db.close();
        }
    }

    /**
     * 批量插入版块信息到数据库<BR>
     * 如果数据库中已存在，则更新它<BR>
     *
     * @param forumInfos 要插入的版块<BR>
     */
    public void addForums(ArrayList<ForumInfo> forumInfos) {
        if (null != forumInfos && !forumInfos.isEmpty()) {
            for (ForumInfo forumInfo : forumInfos) {
                addForum(forumInfo);
            }
        }
    }

    /**
     * 插入单个版块信息到数据库<BR>
     * 如果数据库中已存在此版块的记录，则将更新它<BR>
     *
     * @param forumInfo 要插入的版块<BR>
     */
    public void addForum(ForumInfo forumInfo) {
        if (null != forumInfo) {
            ForumDao dao = daoSession.getForumDao();
            dao.insertOrReplace(new Forum(forumInfo));
            addTypes(forumInfo.getIncludeType());//将这个版本的帖子信息写入到数据库
        }
    }

    /**
     * 从数据库中获取所有的版块的信息<BR>
     *
     * @return 返回数据库中所有的版块的信息，如果不存在，则返回空<BR>
     */
    public ArrayList<ForumInfo> getForums() {
        ForumDao dao = daoSession.getForumDao();
        QueryBuilder qb = dao.queryBuilder();
        List<Forum> forums = qb.list();
        ArrayList<ForumInfo> forumInfos = null;
        if (null != forums && !forums.isEmpty()) {
            forumInfos = new ArrayList<>(10);
            for (Forum forum : forums) {
                ForumInfo forumInfo = forum.toForumInfo();
                forumInfo.setIncludeType(getPostTypeByIdS(forum.getType()));//获取帖子类型信息
                forumInfos.add(forumInfo);
            }
        }
        return forumInfos;
    }

    /**
     * 批量插入用户信息到数据库<BR>
     *
     * @param users 要插入的用户列表<BR>
     */
    public void addUsers(ArrayList<User> users) {
        if (null != users && !users.isEmpty()) {
            UserDao dao = daoSession.getUserDao();
            dao.insertOrReplaceInTx(users);
        }
    }

    /**
     * 插入用户信息到数据库<BR>
     *
     * @param user 要插入的用户<BR>
     */
    public void addUser(User user) {
        if (null != user) {
            UserDao dao = daoSession.getUserDao();
            dao.insertOrReplace(user);
        }
    }

    /**
     * 插入用户信息到数据库<BR>
     *
     * @param mcUser 要插入的用户<BR>
     */
    public void addUser(MCUser mcUser) {
        if (null != mcUser) {
            addUser(new User(mcUser));
        }
    }

    /**
     * 能过openId查询用户，主要是供融云查询用户信息<BR>
     *
     * @param name 要查询的用户名(openId)<BR>
     * @return 查询到的用户，如果不存在，则返回空<BR>
     */
    public User getUserByName(String name) {
        if (null != name && 0 < name.length()) {
            UserDao dao = daoSession.getUserDao();
            QueryBuilder queryBuilder = dao.queryBuilder();
            queryBuilder.where(UserDao.Properties.Name.eq(name));
            return (User) queryBuilder.unique();
        }
        return null;
    }

    /**
     * 根据用户id来查询用户<BR>
     *
     * @param userId 要查询的id<BR>
     * @return 查询到的用户，如果不存在则返回空<BR>
     */
    public User getUserById(int userId) {
        if (0 < userId) {
            UserDao dao = daoSession.getUserDao();
            QueryBuilder queryBuilder = dao.queryBuilder();
            queryBuilder.where(UserDao.Properties.Id.eq(userId));
            return (User) queryBuilder.unique();
        }
        return null;
    }

    /**
     * 添加好友，如果数据库中已存在，则更新它<BR>
     *
     * @param friends 要添加的好友<BR>
     */
    public void addFriends(ArrayList<User> friends) {
        if (null != friends && !friends.isEmpty()) {
            for (User user : friends) {
                user.setIsFriend(true);//确保isFriend字段是为true
                addUser(user);
            }
        }
    }

    /**
     * 查询好友，主要是供好友列表使用<BR>
     *
     * @return 返回所有字段isFriend为true的记录<BR>
     */
    public List<User> getFriends() {
        UserDao dao = daoSession.getUserDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(UserDao.Properties.IsFriend.eq(true));
        return queryBuilder.list();
    }

    /**
     * 插入帖子类型到数据库，如果类型已存存，则更新<BR>
     *
     * @param postTypes 要插入的类型列表<BR>
     */
    public void addTypes(ArrayList<PostType> postTypes) {
        if (null != postTypes && !postTypes.isEmpty()) {
            for (PostType postType : postTypes) {
                addType(postType);
            }
        }
    }

    /**
     * &
     * 插入帖子类型到数据库，如果存在，则更新<BR>
     *
     * @param postType 要插入的帖子类型信息<BR>
     */
    public void addType(PostType postType) {
        if (null != postType) {
            Type type = new Type((long) postType.getId(), postType.getSmallId(), postType.getSmallName());
            TypeDao dao = daoSession.getTypeDao();
            dao.insertOrReplace(type);
        }
    }

    /**
     * 根据id获取帖子类型信息<BR>
     *
     * @param typeIds 要获取的类型的Id<BR>
     * @return 查询到的类型信息列表，如果未查询到则返回空
     */
    public ArrayList<PostType> getPostTypeByIdS(String typeIds) {

        if (null != typeIds && typeIds.length() > 0) {
//            String ids[] = typeIds.split("|");
            String ids = typeIds.replace('|', ',');
            TypeDao dao = daoSession.getTypeDao();
            QueryBuilder queryBuilder = dao.queryBuilder();
            queryBuilder.where(TypeDao.Properties.SubId.in(ids));
            List<Type> types = queryBuilder.list();
            if (null != types && !types.isEmpty()) {
                ArrayList<PostType> postTypes = new ArrayList<>(types.size());
                for (Type type : types) {
                    postTypes.add(type.toPostType());
                }
                return postTypes;
            }
        }
        return null;
    }

    public static void initTypeTable() {

    }

    public static void initForumTable() {

    }
}
