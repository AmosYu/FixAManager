package fam.ctl.fixamanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fam.ctl.fixamanager.adapter.TaskAdapter;
import fam.ctl.fixamanager.grrendao.AssetsDaoUtils;

public class TaskActivity extends Activity {
    private Context context;
    private ListView listView;
    private ImageView add_imageview;
    private List<String> list=new ArrayList<>();
    private TaskAdapter taskAdapter;
    private AssetsDaoUtils assetsDaoUtils;
    AssetsDaoUtils assetsDaoUtils1;
    PowerManager.WakeLock mWakeLock=null;
    public static String username="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_layout);
        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        }

        context=this;
        init();
        bindEvent();
    }

    private void init() {
        Bundle receive=getIntent().getExtras();
        if (receive!=null){
            //得到随Intent传递过来的Bundle对象
            username=receive.getString("username");
        }
        //创建以用户名为名的文件夹
        File file = new File(Environment.getExternalStorageDirectory() + "/AdminManager/"+username+"/");
        if (!file.exists()){
            file.mkdirs();
        }
        assetsDaoUtils=new AssetsDaoUtils(context,"");
        list=assetsDaoUtils.loadCase(username);
        add_imageview=findViewById(R.id.task_add);
        listView=findViewById(R.id.task_listview);
        taskAdapter=new TaskAdapter(context,list);
        listView.setAdapter(taskAdapter);
    }

    private void bindEvent() {
        add_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.case_input_edit, null);
                new AlertDialog.Builder(context)
                        .setTitle("新建任务")
                        .setView(layout)
                        .setPositiveButton("确定",new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialoginterface, int i)
                            {
                                EditText caseNameEdit = (EditText) layout.findViewById(R.id.case_edit_name);
                                String caseNameStr = (caseNameEdit.getText().toString()).replaceAll(" ", "");
                                if (caseNameStr.length() > 0)
                                {
                                    if(list.contains(caseNameStr)) {
                                        Toast msg = Toast.makeText(context,"此案件已经存在,请重新命名",Toast.LENGTH_LONG);
                                        msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
                                        msg.show();
                                    }
                                    else {
                                        try{
                                            assetsDaoUtils.insertCase(caseNameStr,username);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        list.add(0,caseNameStr);
                                        taskAdapter.notifyDataSetChanged();
                                    }
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "案件名不能为空！",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).setNegativeButton("取消",null).show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("温馨提示")
                        .setMessage("确定要删除此任务吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String casename=list.get(position);
                                assetsDaoUtils1=new AssetsDaoUtils(context);
                                MainActivity.assetsDaoUtils=assetsDaoUtils1;
                                assetsDaoUtils.deleteCase(assetsDaoUtils1,casename,username);
                                list.clear();
                               List<String> cases=assetsDaoUtils.loadCase(username);
                                for (int j=0;j<cases.size();j++){
                                    list.add(cases.get(j));
                                }
                                taskAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消",null);
                builder.show();
                return true;
            }
        });
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

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN) {
           AlertDialog.Builder builder=new AlertDialog.Builder(context)
                    .setTitle("温馨提示：")
                    .setMessage("您是否要退出程序？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                           TaskActivity.this.finish();
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
