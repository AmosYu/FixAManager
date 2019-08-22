package fam.ctl.fixamanager.grrendao;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import fam.ctl.fixamanager.MainActivity;
import fam.ctl.fixamanager.beans.DaoMaster;
import fam.ctl.fixamanager.beans.DaoSession;

public class DaoManager extends Application{
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    public DaoManager(Context context) {
        this.context=context;
        init();
    }
//    /**
//     * 静态内部类，实例化对象使用
//     */
//    private static class SingleInstanceHolder {
//        private static final DaoManager INSTANCE = new DaoManager();
//    }
//    /**
//     * 对外唯一实例的接口
//     *
//     * @return
//     */
//    public  static DaoManager getInstance() {
//        return SingleInstanceHolder.INSTANCE;
//    }

    /**
     * 初始化数据
     */
    private void init() {
        try {
            DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context, "FixAssets.db",null);
//            context.deleteDatabase("FixAssets");
            sqLiteDatabase=devOpenHelper.getWritableDatabase();
            mDaoMaster = new DaoMaster(sqLiteDatabase);
            mDaoSession = mDaoMaster.newSession();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }

    public DaoMaster getDaoMaster() {
        return mDaoMaster;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

}
