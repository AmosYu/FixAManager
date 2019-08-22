package fam.ctl.fixamanager;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fam.ctl.fixamanager.adapter.FixAssetsAdapter;
import fam.ctl.fixamanager.beans.Assets;
import fam.ctl.fixamanager.beans.Login;
import fam.ctl.fixamanager.grrendao.AssetsDaoUtils;
import fam.ctl.fixamanager.util.AnimUtil;
import fam.ctl.fixamanager.util.FileListDialog;
import fam.ctl.fixamanager.zxing.CaptureActivity;

public class MainActivity extends Activity {

    private Context context;
    public static String task_name=null;
    private String username="";
    private String flag_admin_task="";
    private Spinner spinner;
    private ArrayAdapter FindAdapter;
    private String[] finds={"编码"};//,"地点","责任人","类别"
    private int findSpinner=0;

    private ImageView import_imageview,sao_imageview;
//    private RelativeLayout add_layout;
    private TextView tv_import,tv_sao,tv_export,tv_delete;
    private PopupWindow mPopupWindow;
    private AnimUtil animUtil;
    private float bgAlpha = 1f;
    private boolean bright = false;
    private static final long DURATION = 500;
    private static final float START_ALPHA = 0.7f;
    private static final float END_ALPHA = 1f;

    private EditText content_edt;
    private Button find_btn;
    private ListView listView;
    private FixAssetsAdapter adapter;
    private List<Assets> assetsList=new ArrayList<>();
    public static AssetsDaoUtils assetsDaoUtils;
    private int count=1;
    PowerManager.WakeLock mWakeLock=null;
    private int useless_0=0;
    private int useless_1=0;
    private int useless_2=0;//0在库未核查  1在库已核查  2已核查未在库
    private String adminNMane="";//处级账户名
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        }

        context=this;
        init();
        isGrantExternalRW(this);
    }
    private void init() {
        Bundle receive=getIntent().getExtras();
        if (receive!=null){
            //得到随Intent传递过来的Bundle对象
            username=receive.getString("username");
            flag_admin_task=receive.getString("flag");
            if (flag_admin_task.equals("admin")){
                adminNMane=receive.getString("adminAdapter");
            }
        }
        if (assetsDaoUtils==null){
            assetsDaoUtils=new AssetsDaoUtils(context);
        }
        List<Assets> list=assetsDaoUtils.queryAllAssets();
        count=list.size()+1;
        assetsList.addAll(assetsDaoUtils.queryByRecord(task_name));
        listView=findViewById(R.id.fix_list);
        adapter=new FixAssetsAdapter(context,assetsList);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final Assets assets_p=assetsList.get(position);
                final EditText et = new EditText(context);
                AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle("请输入需要添加的备注信息：")
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String beizhu=et.getText().toString();
                                if ((!beizhu.equals(""))&&beizhu!=null){
                                    assets_p.setOthers(beizhu);
                                }
                                assetsDaoUtils.updateAssets(assets_p);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNeutralButton("取消", null);
                builder.show();
                return true;
            }
        });
        mPopupWindow = new PopupWindow(this);
        animUtil = new AnimUtil();
        spinner=(Spinner)findViewById(R.id.activity_find_spinner);
        initSpinner();
        content_edt=(EditText)findViewById(R.id.activity_edit);
        find_btn=(Button)findViewById(R.id.activity_find_btn);
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message=content_edt.getText().toString();
                if (findSpinner==0){
                    if (message!=null&&(!message.equals(""))){
                        assetsList.clear();
                        List<Assets> assets_List=assetsDaoUtils.queryByAreaCode(message,task_name);
                        if (assets_List.size()==0){
                            //已核查未在库    添加进数据库
                            Assets asset = new Assets(count,message,null,null,null,null,2,task_name,null,null);
                            assetsDaoUtils.insertOrReplaceAssets(asset);
                        }else if (assets_List.size()>0){
                            //查询出结果  状态改为在库已核查
                            for (int i=0;i<assets_List.size();i++){
                                //若查询出的结果是已核查未在库的，状态不更改；只有是在库未核查的状态才会改变为在库已核查
                                if (assets_List.get(i).getUseless()==0){
                                    assets_List.get(i).setUseless(1);
                                    assetsDaoUtils.updateAssets(assets_List.get(i));
                                }
                            }
                        }
                        assetsList.addAll(assetsDaoUtils.queryByAreaCode(message,task_name));
                        adapter.notifyDataSetChanged();
                    }else {
                        assetsList.clear();
                        assetsList.addAll(assetsDaoUtils.queryByRecord(task_name));
                        adapter.notifyDataSetChanged();
                    }
                }
//                switch (findSpinner){
//                    case 0://编码
//                        if (message!=null&&(!message.equals(""))){
//                            assetsList.clear();
//                            List<Assets> assets_List=assetsDaoUtils.queryByAreaCode(message,task_name);
//                            if (assets_List.size()==0){
//                                //已核查未在库    添加进数据库
//                                Assets asset = new Assets(count,message,null,null,null,null,2,task_name,null,null);
//                                assetsDaoUtils.insertOrReplaceAssets(asset);
//                            }else if (assets_List.size()>0){
//                                //查询出结果  状态改为在库已核查
//                                for (int i=0;i<assets_List.size();i++){
//                                    //若查询出的结果是已核查未在库的，状态不更改；只有是在库未核查的状态才会改变为在库已核查
//                                    if (assets_List.get(i).getUseless()==0){
//                                        assets_List.get(i).setUseless(1);
//                                        assetsDaoUtils.updateAssets(assets_List.get(i));
//                                    }
////                                    assets_List.get(i).setUseless(1);
////                                    assetsDaoUtils.updateAssets(assets_List.get(i));
//                                }
//                            }
//                            assetsList.addAll(assetsDaoUtils.queryByAreaCode(message,task_name));
//                            adapter.notifyDataSetChanged();
//                        }else {
//                            assetsList.clear();
//                            assetsList.addAll(assetsDaoUtils.queryByRecord(task_name));
//                            adapter.notifyDataSetChanged();
//                        }
//                        break;
//                    case 1://存放地点
//                        if (message!=null&&(!message.equals(""))){
//                            assetsList.clear();
//                            List<Assets> assets_company=assetsDaoUtils.queryByCompany(message,task_name);
//                            assetsList.addAll(assets_company);
//                            adapter.notifyDataSetChanged();
//                        }else {
//                            assetsList.clear();
//                            assetsList.addAll(assetsDaoUtils.queryByRecord(task_name));
//                            adapter.notifyDataSetChanged();
//                        }
//                        break;
//                    case 2://责任人
//                        if (message!=null&&(!message.equals(""))){
//                            assetsList.clear();
//                            assetsList.addAll(assetsDaoUtils.queryByPerson(message,task_name));
//                            adapter.notifyDataSetChanged();
//                        }else {
//                            assetsList.clear();
//                            assetsList.addAll(assetsDaoUtils.queryByRecord(task_name));
//                            adapter.notifyDataSetChanged();
//                        }
//                        break;
//                    case 3://类别
//                        if (message!=null&&(!message.equals(""))){
//                            assetsList.clear();
//                            if (message.equals("在库已核查")){
//                                assetsList.addAll(assetsDaoUtils.queryByUseless(1,task_name));
//                            }else if (message.equals("在库未核查")){
//                                assetsList.addAll(assetsDaoUtils.queryByUseless(0,task_name));
//                            }else if (message.equals("已核查未在库")){
//                                assetsList.addAll(assetsDaoUtils.queryByUseless(2,task_name));
//                            }
//                            adapter.notifyDataSetChanged();
//                        }else {
//                            assetsList.clear();
//                            assetsList.addAll(assetsDaoUtils.queryByRecord(task_name));
//                            adapter.notifyDataSetChanged();
//                        }
//                        break;
//                }
            }
        });
        import_imageview=findViewById(R.id.import_main);
        import_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //导入
                File fileName = new File(Environment.getExternalStorageDirectory() + "/AdminManager/"+username+"/");
//                File fileName = new File(Environment.getExternalStorageDirectory() + "/Excels/");
                File[] files = fileName.listFiles();     //本方法返回该文件夹展开后的所有文件的数组
                FileListDialog dialog=null;
                if (files.length>0){
                    dialog = new FileListDialog(context, files);
                    dialog.show();
                    final FileListDialog finalDialog = dialog;
                    dialog.SetOnClickBtn(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            List<Assets> assets=PoiReadDataFromExcel();
                            //写入数据库
                            assetsDaoUtils.insertOrReplaceAssetList(assets);
                            assetsList.clear();
                            assetsList.addAll(assetsDaoUtils.queryByRecord(task_name));
                            Message message=new Message();
                            message.arg1=0;
                            message.obj= finalDialog;
                            handler.sendMessage(message);
                        }
                    });
                }else {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("该账户文件下没有任何文件，请先导入excel表")
                            .setPositiveButton("确定",null);
                    builder.show();
                }
            }
        });
        sao_imageview=findViewById(R.id.sao_main);
        sao_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 打开扫描界面扫描条形码或二维码
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // 申请CAMERA权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                }else {
                    Intent openCameraIntent = new Intent(MainActivity.this,CaptureActivity.class);
                    startActivityForResult(openCameraIntent, 1);
                }
            }
        });
    }

    private void initSpinner() {
        FindAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,finds);
        //设置下拉列表的风格
        FindAdapter.setDropDownViewResource(R.layout.drop_down_item);
        //将adapter 添加到spinner中
        spinner.setAdapter(FindAdapter);
        spinner.setSelection(0,true);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(FindSelectLis);
    }
    private AdapterView.OnItemSelectedListener FindSelectLis = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            findSpinner=0;
//            switch (position){
//                case 0:
//                    findSpinner=0;
//                    break;
//                case 1:
//                    findSpinner=1;
//                    break;
//                case 2:
//                    findSpinner=2;
//                    break;
//                case 3:
//                    findSpinner=3;
//                    break;
//            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1){
                case 0:
                    adapter.notifyDataSetChanged();
                    FileListDialog dialog= (FileListDialog) msg.obj;
                    dialog.dismiss();
                    FileListDialog.selectDirNameList.clear();
                    break;
                case 1:
                    break;
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        task_name=null;
    }
    private String getCurrentTime(){
        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss");
        String time=dateFormat.format(calendar.getTime());
        return time;
    }
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
//                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
//                File fileName = new File(path + "Excels" + File.separator);
                File fileName = new File(Environment.getExternalStorageDirectory() + "/AdminManager/"+username+"/");
                File file = new File(fileName, name);
                String str=name.substring(name.length()-3,name.length());
                int excel_code=0;//卡片编号
                int excel_company=0;//存放地点
                int excel_person=0;//责任人
                int excel_mc=0;//资产名称
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
                        }else if (cell.contains("名称")){
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
                        String zichanming=null;
                        if (r.getCell(excel_company)!= null){
                            company = r.getCell(excel_company).toString();
                        }
                        if (r.getCell(excel_person)!=null){
                            person = r.getCell(excel_person).toString();
                        }
                        if (r.getCell(excel_mc)!=null){
                            zichanming = r.getCell(excel_mc).toString();
                        }
                        Assets assets = new Assets(count,code,company,person,zichanming,null,0,task_name,null,null);
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
                        }else if (cell.contains("名称")){
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
                        String zichanming=null;
                        if (r.getCell(excel_company)!= null){
                            company = r.getCell(excel_company).toString();
                        }
                        if (r.getCell(excel_person)!=null){
                            person = r.getCell(excel_person).toString();
                        }
                        if (r.getCell(excel_mc)!=null){
                            zichanming = r.getCell(excel_mc).toString();
                        }
                        Assets assets = new Assets(count,code,company,person,zichanming,null,0,task_name,null,null);
                        cellDataContainer.add(assets);
                        count++;
                    }
                }
            }
            return cellDataContainer;
        }
    }
    private void toggleBright() {
        // 三个参数分别为：起始值 结束值 时长，那么整个动画回调过来的值就是从0.5f--1f的
        animUtil.setValueAnimator(START_ALPHA, END_ALPHA, DURATION);
        animUtil.addUpdateListener(new AnimUtil.UpdateListener() {
            @Override
            public void progress(float progress) {
                // 此处系统会根据上述三个值，计算每次回调的值是多少，我们根据这个值来改变透明度
                bgAlpha = bright ? progress : (START_ALPHA + END_ALPHA - progress);
                backgroundAlpha(bgAlpha);
            }
        });
        animUtil.addEndListner(new AnimUtil.EndListener() {
            @Override
            public void endUpdate(Animator animator) {
                // 在一次动画结束的时候，翻转状态
                bright = !bright;
            }
        });
        animUtil.startAnimator();
    }
    /**
     * 此方法用于改变背景的透明度，从而达到“变暗”的效果
     */
    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
        // everything behind this window will be dimmed.
        // 此方法用来设置浮动层，防止部分手机变暗无效
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(1 == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //扫码
                // todo 做相应的处理逻辑
                Intent openCameraIntent = new Intent(MainActivity.this,CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            } else {
                Toast.makeText(this, "你拒绝了权限申请，无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            final String scanResult = bundle.getString("result");
            content_edt.setText(scanResult);
            //扫出结果进行查询
            if (scanResult!=null&&(!scanResult.equals(""))){
                assetsList.clear();
                final List<Assets> assets=assetsDaoUtils.queryByCode(scanResult,adminNMane);//扫描之后查询出的结果
                if (assets.size()==0){
                    //已核查未在库    添加进数据库
                    Assets asset = new Assets(count,scanResult,null,adminNMane,task_name,null,2,username,null,null);
                    assetsDaoUtils.insertOrReplaceAssets(asset);
//                    AlertDialog.Builder builder=new AlertDialog.Builder(context)
//                            .setTitle("查询结果：")
//                            .setMessage("已核查未在库，将该数据添加进当前科室中")
//                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    List<Assets> assets1=assetsDaoUtils.queryByRecord(username);
//                                    String person=assets1.get(0).getPerson();
//                                    Assets asset = new Assets(count,scanResult,null,person,null,null,2,task_name,null,null);
//                                    assetsDaoUtils.insertOrReplaceAssets(asset);
//                                }
//                            });
//                    builder.show();
                }else if (assets.size()>0){
                    //查询出结果  状态改为在库已核查
                    for (int i=0;i<assets.size();i++){
                        //若查询出的结果是已核查未在库的，状态不更改
                        if (assets.get(i).getUseless()==0){
                            if (assets.get(i).getRecord()==null||assets.get(i).getRecord().equals("")){
                                assets.get(i).setRecord(task_name);
                                assets.get(i).setUseless(1);
                                assetsDaoUtils.updateAssets(assets.get(i));
                                Toast.makeText(context,"该编号在库但无科室，已加入当前科室，可添加备注标示",Toast.LENGTH_LONG);
                            }else {
                                assets.get(i).setUseless(1);
                                assetsDaoUtils.updateAssets(assets.get(i));
                            }
                        }
                    }
                }
                assetsList.addAll(assetsDaoUtils.queryByAreaCode(scanResult,task_name));
                adapter.notifyDataSetChanged();
            }
        }
    }
    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }
}
