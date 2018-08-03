package com.mckuai.imc.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mckuai.imc.R;
import com.mckuai.imc.Utils.MCDao.DaoMaster;

/**
 * Created by kyly on 2016/1/21.
 */
public class MCDBOpenHelper extends DaoMaster.OpenHelper {
    public MCDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    public MCDBOpenHelper(Context context) {
        super(context, context.getString(R.string.dbname), null);
    }

    /**
     * 处理升级数据库
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                //创建新表，注意createTable()是静态方法
                // SchoolDao.createTable(db, true);

                // 加入新字段
                // db.execSQL("ALTER TABLE 'moments' ADD 'audio_path' TEXT;");

                // TODO
                break;
        }
    }
}
