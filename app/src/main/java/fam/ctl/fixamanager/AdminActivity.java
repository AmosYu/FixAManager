package fam.ctl.fixamanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import fam.ctl.fixamanager.adapter.AdminAdapter;
import fam.ctl.fixamanager.adapter.TaskAdapter;
import fam.ctl.fixamanager.beans.Assets;
import fam.ctl.fixamanager.beans.Login;
import fam.ctl.fixamanager.grrendao.AssetsDaoUtils;
import fam.ctl.fixamanager.util.FileListDialog;
import fam.ctl.fixamanager.zxing.CaptureActivity;

public class AdminActivity extends Activity {
    private Context context;
    private TextView num_tv;
    private AssetsDaoUtils assetsDaoUtils;
    private  AssetsDaoUtils assetsDaoUtils1;
    private ImageView add_admin,import_admin,sao_admin;
    private ListView listView;
    private AdminAdapter adminAdapter;
    private List<Login> loginList=new ArrayList<>();
    private int count=1;
    private String admin="";
    private TextView code_tv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout);

        context=this;
        Bundle receive=getIntent().getExtras();
        if (receive!=null){
            //得到随Intent传递过来的Bundle对象
            admin=receive.getString("admin");
        }
        init();
    }

    private void init() {
        assetsDaoUtils=new AssetsDaoUtils(context,"");
        assetsDaoUtils1=new AssetsDaoUtils(context);
        num_tv=findViewById(R.id.num_main);
        List<Assets> list=assetsDaoUtils1.queryAllAssets();
        num_tv.setText("总数据量："+list.size());
        count=list.size()+1;
        listView=findViewById(R.id.admin_listview);
        loginList=assetsDaoUtils.LoginQuery(admin);
        adminAdapter=new AdminAdapter(assetsDaoUtils1,context,loginList,admin);
        listView.setAdapter(adminAdapter);
        add_admin=findViewById(R.id.admin_add);
        add_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data=new Bundle();
                data.putString("update","admin");
                data.putString("adminActivity",admin);
                Intent intent=new Intent();
                intent.setClass(AdminActivity.this,RegisterActivity.class);
                intent.putExtras(data);
                startActivity(intent);
                finish();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("确定要删除此用户吗？该用户下所有数据将一并删除!")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Login login=loginList.get(position);
                                assetsDaoUtils.DeleteLogin(assetsDaoUtils1,login.getUsername(),login.getPassword());
                                //同时删除文件夹
                                File fileName = new File(Environment.getExternalStorageDirectory() + "/AdminManager/"+login.getUsername());
                                deleteFile(fileName);
                                loginList.remove(login);
                                adminAdapter.notifyDataSetChanged();
                                List<Assets> list=assetsDaoUtils1.queryAllAssets();
                                num_tv.setText("总数据量："+list.size());
                                Toast.makeText(context,"删除用户成功!",Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("取消",null);
                builder.show();
                return true;
            }
        });
        import_admin=findViewById(R.id.import_admin);
        import_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //导入
                File fileName = new File(Environment.getExternalStorageDirectory() + "/AdminManager/");
                File[] files = fileName.listFiles();     //本方法返回该文件夹展开后的所有文件的数组
                FileListDialog dialog=null;
                if (files.length>0){
                    dialog = new FileListDialog(context, files);
                    dialog.show();
                }
                final FileListDialog finalDialog = dialog;
                dialog.SetOnClickBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<Assets> assets=PoiReadDataFromExcel();
                        //写入数据库
                        assetsDaoUtils1.insertOrReplaceAssetList(assets);
                        List<String> records=assetsDaoUtils1.queryRecord();
                        for (int i=0;i<records.size();i++){
                            //写入Login表
                            assetsDaoUtils.insertLogin(records.get(i),"keshi1",admin);
                            //写入任务表
                            assetsDaoUtils.insertCase(records.get(i),records.get(i));
                            //创建用户文件夹
                            File file = new File(Environment.getExternalStorageDirectory() + "/AdminManager/"+records.get(i)+"/");
                            if (!file.exists()){
                                file.mkdirs();
                            }
                        }
                        loginList.clear();
                        loginList=assetsDaoUtils.LoginQuery(admin);
                        Message message=new Message();
                        message.arg1=0;
                        message.arg2=assets.size();
                        message.obj= finalDialog;
                        handler.sendMessage(message);
                    }
                });
            }
        });
        code_tv=findViewById(R.id.code_main);
        sao_admin=findViewById(R.id.sao_admin);
        sao_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 打开扫描界面扫描条形码或二维码
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // 申请CAMERA权限
                    ActivityCompat.requestPermissions(AdminActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                }else {
                    Intent openCameraIntent = new Intent(AdminActivity.this,CaptureActivity.class);
                    startActivityForResult(openCameraIntent, 1);
                }
            }
        });
    }
    public static boolean deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {

            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }

        return dirFile.delete();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            final String scanResult = bundle.getString("result");
            code_tv.setText(scanResult);
            //扫出结果进行查询
            if (scanResult!=null&&(!scanResult.equals(""))){
                List<Assets> assets=assetsDaoUtils1.queryByCode(scanResult,admin);//扫描之后查询出的结果
                if (assets.size()==0){
                    AlertDialog.Builder builder=new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("编码号"+scanResult+"不在数据库中,是否新建一个已核查未在库的账号？")
                            .setPositiveButton("新建", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File file = new File(Environment.getExternalStorageDirectory() + "/AdminManager/已核查未在库/");
                                    if (!file.exists()){
                                        file.mkdirs();
                                    }
                                    assetsDaoUtils.insertLogin("已核查未在库","keshi1",admin);
                                    assetsDaoUtils.insertCase("已核查未在库","已核查未在库");
                                    //已核查未在库    添加进数据库
                                    Assets asset = new Assets(count,scanResult,null,admin,"已核查未在库",null,2,"已核查未在库",null,null);
                                    assetsDaoUtils1.insertOrReplaceAssets(asset);
                                    loginList.clear();
                                    loginList=assetsDaoUtils.LoginQuery(admin);
                                    adminAdapter=new AdminAdapter(assetsDaoUtils1,context,loginList,admin);
                                    listView.setAdapter(adminAdapter);
                                    List<Assets> list=assetsDaoUtils1.queryAllAssets();
                                    num_tv.setText("总数据量："+list.size());
                                }
                            })
                            .setNegativeButton("取消",null);
                    builder.show();
                }else if (assets.size()>0){
                    //查询出结果  状态改为在库已核查
                    for (int i=0;i<assets.size();i++){
                        //若查询出的结果是已核查未在库的，状态不更改
                        if (assets.get(i).getUseless()==0){
                            assets.get(i).setUseless(1);
                            assetsDaoUtils1.updateAssets(assets.get(i));
                        }
                    }
                    if (assets.get(0).getRecord()==null||assets.get(0).getRecord().equals("")){
                        AlertDialog.Builder builder=new AlertDialog.Builder(context)
                                .setTitle(scanResult+"的扫描结果")
                                .setMessage("在库中发现"+assets.size()+"条数据，但没有科室信息")
                                .setPositiveButton("确定",null);
                        builder.show();
                    }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder(context)
                                .setTitle(scanResult+"的扫描结果")
                                .setMessage(assets.get(0).getRecord()+"中发现"+assets.size()+"条数据")
                                .setPositiveButton("确定",null);
                        builder.show();
                    }
                }
            }
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1){
                case 0:
                    int num=msg.arg2;
                    num_tv.setText("总数据量："+num);
                    adminAdapter=new AdminAdapter(assetsDaoUtils1,context,loginList,admin);
                    listView.setAdapter(adminAdapter);
//                    adminAdapter.notifyDataSetChanged();
                    FileListDialog dialog= (FileListDialog) msg.obj;
                    dialog.dismiss();
                    FileListDialog.selectDirNameList.clear();
                    break;
                case 1:
                    break;
            }
        }
    };
    private List<Assets> PoiReadDataFromExcel() {
        if (FileListDialog.selectDirNameList.size() == 0) {
            Toast.makeText(context, "请选择一个文件", Toast.LENGTH_SHORT).show();
            return null;
        } else if (FileListDialog.selectDirNameList.size()>1){
            Toast.makeText(context,"只能选择一个文件",Toast.LENGTH_SHORT).show();
            return null;
        }else {
            List<Assets> cellDataContainer = new ArrayList<>();
            //循环选择的文件，根据文件名读取文件内容去写证书
            for (String name : FileListDialog.selectDirNameList) {
                File fileName = new File(Environment.getExternalStorageDirectory() + "/AdminManager/");
                File file = new File(fileName, name);
                String str=name.substring(name.length()-3,name.length());
                int excel_code=0;//卡片编号
                int excel_company=0;//存放地点
                int excel_person=0;//责任人
                int excel_mc=0;//预留二
                if (str.equals("xls")){
                    HSSFWorkbook mWorkbook = null;
                    try {
                        FileInputStream is = new FileInputStream(file);
                        mWorkbook = new HSSFWorkbook(is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    HSSFSheet mSheet = mWorkbook.getSheetAt(0);
                    int rowNumber = mSheet.getLastRowNum() + 1;
                    HSSFRow hssfRow = mSheet.getRow(0);
                    int columnNum=hssfRow.getPhysicalNumberOfCells();//总列数
                    for (int j=0;j<columnNum;j++){
                        String cell=hssfRow.getCell(j).toString();
                        if (cell.contains("编号")){
                            excel_code=j;
                        }else if (cell.contains("地点")){
                            excel_company=j;
                        }else if (cell.contains("预留二")){
                            excel_mc=j;
                        }else if (cell.contains("责任人")){
                            excel_person=j;
                        }
                    }
                    for (int row = 1; row < rowNumber; row++) {
                        HSSFRow r = mSheet.getRow(row);
                        String code = r.getCell(excel_code).toString();
                        String company = null;
                        String person=null;
                        String yuliu=null;
                        if (r.getCell(excel_company)!= null){
                            company = r.getCell(excel_company).toString();
                        }
                        if (r.getCell(excel_person)!=null){
                            person = r.getCell(excel_person).toString();
                        }
                        if (r.getCell(excel_mc)!=null){
                            yuliu = r.getCell(excel_mc).toString();
                        }
                        Assets assets = new Assets(count,code,company,admin,yuliu,null,0,yuliu,null,null);
                        cellDataContainer.add(assets);
                        count++;
                    }
                }else if (str.equals("lsx")){
                    XSSFWorkbook xssfWorkbook=null;
                    try{
                        FileInputStream is = new FileInputStream(file);
                        xssfWorkbook = new XSSFWorkbook(is);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    XSSFSheet mSheet=xssfWorkbook.getSheetAt(0);
                    int rowNumber = mSheet.getLastRowNum() + 1;
                    XSSFRow xssfRow = mSheet.getRow(0);
                    int columnNum=xssfRow.getPhysicalNumberOfCells();//总列数
                    for (int j=0;j<columnNum;j++){
                        String cell=xssfRow.getCell(j).toString();
                        if (cell.contains("编号")){
                            excel_code=j;
                        }else if (cell.contains("地点")){
                            excel_company=j;
                        }else if (cell.contains("预留二")){
                            excel_mc=j;
                        }else if (cell.contains("责任人")){
                            excel_person=j;
                        }
                    }
                    for (int row = 1; row < rowNumber; row++) {
                        XSSFRow r = mSheet.getRow(row);
                        String code = r.getCell(excel_code).toString();
                        String company = null;
                        String person=null;
                        String yuliu=null;
                        if (r.getCell(excel_company)!= null){
                            company = r.getCell(excel_company).toString();
                        }
                        if (r.getCell(excel_person)!=null){
                            person = r.getCell(excel_person).toString();
                        }
                        if (r.getCell(excel_mc)!=null){
                            yuliu = r.getCell(excel_mc).toString();
                        }
                        Assets assets = new Assets(count,code,company,admin,yuliu,null,0,yuliu,null,null);
                        cellDataContainer.add(assets);
                        count++;
                    }
                }
            }
            return cellDataContainer;
        }
    }
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN) {
            AlertDialog.Builder builder=new AlertDialog.Builder(context)
                    .setTitle("温馨提示：")
                    .setMessage("您是否要退出程序？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    AdminActivity.this.finish();
                                    if (assetsDaoUtils!=null){
                                        assetsDaoUtils.vlose();
                                    }
                                    if (assetsDaoUtils1!=null){
                                        assetsDaoUtils1.vlose();
                                    }
                                }
                            })
                    .setNegativeButton("取消",null);
            builder.show();
        }
        return true;
    }
}
