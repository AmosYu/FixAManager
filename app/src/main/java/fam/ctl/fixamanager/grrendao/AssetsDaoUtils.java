package fam.ctl.fixamanager.grrendao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fam.ctl.fixamanager.beans.Assets;
import fam.ctl.fixamanager.beans.AssetsDao;
import fam.ctl.fixamanager.beans.DaoMaster;
import fam.ctl.fixamanager.beans.DaoSession;
import fam.ctl.fixamanager.beans.Login;

public class AssetsDaoUtils {

    private DaoManager daoManager;
    private AssetsDao assetsDao;
    private DaoSession daoSession;
    private SQLiteDatabase db;
    private SQLiteDatabase db1;
    private SQLiteDatabase db2;
    public AssetsDaoUtils(Context context) {
        daoManager=new DaoManager(context);
        daoSession=daoManager.getDaoSession();
        assetsDao=daoSession.getAssetsDao();
    }
    public AssetsDaoUtils(Context context,String name){
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context, "task.db",null);
        db1=devOpenHelper.getWritableDatabase();
        db2=devOpenHelper.getReadableDatabase();
//        db1.execSQL("DROP TABLE IF EXISTS " + "Login");
//        db1.execSQL("DROP TABLE IF EXISTS " + "Anjian");
        //判断表名是否存在
        if (!tabIsExist("Anjian")){
            db1.execSQL("create table  Anjian(CaseName text,UserName text)");
        }
        if (!tabIsExist("Login")){
            db1.execSQL("create table  Login(UserName text,Password text,Person text)");
        }
    }
    public boolean tabIsExist(String tabName){
        boolean result=false;
        if (tabName==null){
            return false;
        }
        Cursor cursor=null;
        try {
            String sql="select count(*) as c from sqlite_master where type='table' and name='"+tabName.trim()+"'";
            cursor=db2.rawQuery(sql,null);
            if (cursor.moveToNext()){
                int count=cursor.getInt(0);
                if (count>0){
                    result=true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        cursor.close();
//        db2.close();
        return result;
    }
    /**
     * 单条插入
     * @param assets
     */
    public void insertOrReplaceAssets(Assets assets) {
        daoSession.insertOrReplace(assets);
    }

    /**
     * 单条插入
     * @param assets
     */
    public void inserAssets(Assets assets) {
        daoSession.insert(assets);
    }
    /**
     * 批量增加,重复替换
     */
    public void insertOrReplaceAssetList(List<Assets> assetsList) {
        assetsDao.insertOrReplaceInTx(assetsList);
    }
    /**
     * 根据ID删除
     *
     * @param id id
     */
    public void deleteAsset(long id) {
        assetsDao.deleteByKey(id);
    }

    /**
     * 根据任务名进行删除
     * @param assets
     */
    public void deleteByRecord(Assets assets) {
        assetsDao.delete(assets);
    }
    /**
     * 删除所有的数据
     */
    public void deleteAll() {
        daoSession.deleteAll(Assets.class);
    }
    /**
     * 更新某条数据
     * @param assets
     */
    public void updateAssets(Assets assets){
        daoSession.update(assets);
    }

    /**
     * 查询所有
     *
     * @return List
     */
    public List<Assets> queryAllAssets() {
        return daoSession.loadAll(Assets.class);
    }

    /**
     * 按条件查询
     *
     * @param code 国家码
     * @return List
     */
    public List<Assets> queryByAreaCode(String code,String record) {
        return daoSession.queryBuilder(Assets.class)
                .where(AssetsDao.Properties.Code.eq(code),AssetsDao.Properties.Record.eq(record))
                .list();
    }

    /**
     * 根据处级账号及编码进行查询
     * @param code
     * @param person
     * @return
     */
    public List<Assets> queryByCode(String code,String person) {
        return daoSession.queryBuilder(Assets.class)
                .where(AssetsDao.Properties.Code.eq(code),AssetsDao.Properties.Person.eq(person))
                .list();
    }

    /**
     * 根据编码进行查询
     * @param code
     * @return
     */
    public List<Assets> queryCode(String code) {
        return daoSession.queryBuilder(Assets.class)
                .where(AssetsDao.Properties.Code.eq(code))
                .list();
    }
    /**
     * 查询预留二中所有不重复的字段名称
     * @return
     */
    public List<String> queryRecord(){//可能要去查处级账户下的所有不重复的预留二
        final String SQL_DISTINCT_ENAME = "SELECT DISTINCT "+AssetsDao.Properties.Record.columnName+" FROM "+AssetsDao.TABLENAME;
        List<String> records=new ArrayList<>();
        Cursor cur=daoSession.getDatabase().rawQuery(SQL_DISTINCT_ENAME,null);
        while (cur.moveToNext()) {
            String record = cur.getString(cur.getColumnIndex("RECORD"));
            records.add(record);
        }
        cur.close();
//        try {
//            if (c.moveToFirst()){
//                do {
//                    records.add(c.getString(0));
//                }while (c.moveToNext());
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            c.close();
//        }
        return  records;
    }
    /**
     * 根据存放地点进行查询
     * @param company
     * @param record
     * @return
     */
    public List<Assets> queryByCompany(String company,String record) {
        return daoSession.queryBuilder(Assets.class)
                .where(AssetsDao.Properties.Company.eq(company),AssetsDao.Properties.Record.eq(record))
                .list();
    }

    /**
     * 根据在库状态进行查询
     * @param use
     * @param record
     * @return
     */
    public List<Assets> queryByUseless(int use,String record) {
        return daoSession.queryBuilder(Assets.class)
                .where(AssetsDao.Properties.Useless.eq(use),AssetsDao.Properties.Address.eq(record))
                .list();
    }
    public List<Assets> queryByPerson(String person,String record) {
        return daoSession.queryBuilder(Assets.class)
                .where(AssetsDao.Properties.Person.eq(person),AssetsDao.Properties.Address.eq(record))
                .list();
    }
    /**
     * 根据任务名进行查询
     * @param record
     * @return
     */
    public List<Assets> queryByRecord(String record) {
        List<Assets> list=new ArrayList<>();
        List<Assets> assets_0=queryByUseless(0,record);
        List<Assets> assets_2=queryByUseless(2,record);
        List<Assets> assets_1=queryByUseless(1,record);
        for (int i=0;i<assets_0.size();i++){
            list.add(assets_0.get(i));
        }
        for (int i=0;i<assets_2.size();i++){
            list.add(assets_2.get(i));
        }
        for (int i=0;i<assets_1.size();i++){
            list.add(assets_1.get(i));
        }
        return list;
//        return daoSession.queryBuilder(Assets.class)
//                .where(AssetsDao.Properties.Record.eq(record))
//                .list();
    }

    public synchronized void insertCase(String name,String username) {
        List<String> list=loadCase(username);
        boolean flag=false;
        for (int i=0;i<list.size();i++){
            if (list.get(i).equals(name)){
                flag=true;
                break;
            }
        }
        if (!flag){
            ContentValues values = new ContentValues();
            values.put("CaseName", name);
            values.put("UserName", username);
            db1.insert("Anjian", null, values);
        }
//        db1.close();
    }
    /**
     * 根据用户名查询该用户下所有的任务名
     * @param username
     * @return
     */
    public List<String> loadCase(String username) {
        List<String> list = new ArrayList<>();
        Cursor cur = db2.query("Anjian", new String[] { "CaseName,UserName"}, "UserName like ?",new String[]{username}, null, null, null);
        while (cur.moveToNext()) {
            String caseName = cur.getString(cur.getColumnIndex("CaseName"));
            list.add(caseName);
        }
        cur.close();
//        db1.close();
        return list;
    }
    /**
     * 删除某任务
     * @param utils
     * @param name
     * @param username
     */
    public synchronized void deleteCase(AssetsDaoUtils utils,String name,String username) {
        try {
//            db.beginTransaction();
            List<Assets> assets=utils.queryByRecord(name);
            for (int i=0;i<assets.size();i++){
                utils.deleteByRecord(assets.get(i));
            }
            db1.execSQL("delete from   Anjian  where UserName='"+username+"' and CaseName='" + name + "'");
//            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            //MyLog.printStackTraceString(e);
        }
//        finally {
//            db.endTransaction(); //处理完成
//            db.close();
//        }
    }
    public void vlose(){
        if (db1!=null){
            db1.close();
        }
        if (db2!=null){
            db2.close();
        }
    }
    //登录表
   // db1.execSQL("create table  Login(UserName text,Password text,Person text)");
    /**
     * 注册用户，添加用户名密码
     * @param name
     * @param password
     */
    public synchronized void insertLogin(String name,String password,String person) {
        List<String> list=LoginNameQuery(person);
        boolean flag=false;
        for (int i=0;i<list.size();i++){
            if (list.get(i).equals(name)){
                flag=true;
                break;
            }
        }
        if (!flag){
            ContentValues values = new ContentValues();
            values.put("UserName", name);
            values.put("Password", password);
            values.put("Person", person);
            db1.insert("Login", null, values);
        }
    }

    /**
     * 查询登陆表中所有的用户名
     * @return
     */
    public List<String> LoginNameQuery(String person) {
        List<String> list = new ArrayList<>();
        Cursor cur = db2.query("Login", new String[] { "UserName"}, "Person like ?",new String[]{person}, null, null, null);
        while (cur.moveToNext()) {
            String caseName = cur.getString(cur.getColumnIndex("UserName"));
            list.add(caseName);
        }
        cur.close();
//        db1.close();
        return list;
    }

    /**
     * 查询所有用户名和密码
     * @return
     */
    public List<Login> LoginQuery(String person) {
        List<Login> list = new ArrayList<>();
        Cursor cur = db2.query("Login", null, null,null, null, null, null);
        if (cur!=null){
            while (cur.moveToNext()) {
                String userName = cur.getString(cur.getColumnIndex("UserName"));
                String password = cur.getString(cur.getColumnIndex("Password"));
                Login login=new Login(userName,password);
                list.add(login);
            }
        }
        cur.close();
//        db1.close();
        return list;
    }
    /**
     * 修改登录密码
     * @param username
     * @param pwd
     */
    public void updateLoginPwd(String username,String pwd){
        db2.execSQL("update Login set Password=? where UserName=?",new Object[]{pwd,username});
    }

    /**
     * 删除用户
     * @param username
     * @param pwd
     */
    public void DeleteLogin(AssetsDaoUtils utils,String username,String pwd){
        List<String> tasks=loadCase(username);//查询该用户下所有的任务
        for (int i=0;i<tasks.size();i++){
            deleteCase(utils,tasks.get(i),username);
        }
        db1.execSQL("delete from Login where UserName='"+username+"' and Password='" + pwd + "'");
    }
    /**
     * 判断登陆是否成功
     * @param name
     * @param password
     * @return
     */
    public boolean QueryLogin(String name,String password) {
        boolean flag=false;
        List<String> list = new ArrayList<>();
        Cursor cur = db2.query("Login", new String[] { "UserName,Password"}, "UserName like ?",new String[]{name}, null, null, null);
        while (cur.moveToNext()) {
//            String caseName = cur.getString(cur.getColumnIndex("UserName"));
            String pasw=cur.getString(cur.getColumnIndex("Password"));
            if (pasw.equals(password)){
                flag=true;
            }
        }
        cur.close();
//        db1.close();
        return flag;
    }
}
