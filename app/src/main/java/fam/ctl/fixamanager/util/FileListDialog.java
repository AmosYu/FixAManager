package fam.ctl.fixamanager.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fam.ctl.fixamanager.R;
import fam.ctl.fixamanager.zxing.zxing.decoding.Intents;

/**
 * Created by Amy on 2017-06-02.
 */

public class FileListDialog extends Dialog {
    String CHECK = "CHECK";
    String DIR_NAME ="DIR_NAME";

    ListView filelist;
    TextView all_checkbox;
    Button file_action_btn;
    SimpleAdapter fileNameAdapter;
    private Context context;
    public static List<HashMap<String,Object>> dirNameList=new ArrayList<>();
    /**
     * 无论导入、导出、上传都需要选择文件或目录，此列表存储已选择的文件或目录名称
     */
    public static ArrayList<String> selectDirNameList = new ArrayList<>();
    File[] files;
    public FileListDialog(final Context context, File[] files) {
        super(context);
        this.context=context;
        this.files=files;
        dirNameList.clear();
        for (File file : files) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(CHECK, false);
            map.put(DIR_NAME, file.getName());
            dirNameList.add(map);
        }
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.filelist);

        filelist= (ListView) findViewById(R.id.filelist);
        all_checkbox=(TextView) findViewById(R.id.dir_allcheck);
        file_action_btn=(Button)findViewById(R.id.file_action_btn);
        fileNameAdapter = new SimpleAdapter(context,dirNameList,R.layout.filelist_item,
                new String[]{CHECK,DIR_NAME},new int[]{R.id.dir_check,R.id.dir_name});
        filelist.setAdapter(fileNameAdapter);
        filelist.setOnItemClickListener(dirItemLis);
    }
    public FileListDialog(final Context context, List<String> list) {
        super(context);
        this.context=context;
        dirNameList.clear();
        for (String str : list) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(CHECK, false);
            map.put(DIR_NAME, str);
            dirNameList.add(map);
        }
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.filelist);

        filelist= (ListView) findViewById(R.id.filelist);
        all_checkbox=(TextView) findViewById(R.id.dir_allcheck);
        file_action_btn=(Button)findViewById(R.id.file_action_btn);
        fileNameAdapter = new SimpleAdapter(context,dirNameList,R.layout.filelist_item,
                new String[]{CHECK,DIR_NAME},new int[]{R.id.dir_check,R.id.dir_name});
        filelist.setAdapter(fileNameAdapter);
        filelist.setOnItemClickListener(dirItemLis);
    }
    public void SetOnClickBtn(View.OnClickListener listener){
        file_action_btn.setOnClickListener(listener);
    }
    public void SetTitle(String title){
        all_checkbox.setText(title);
    }
    private void selectDir(String dirName, boolean select){
        if(select){
            for(String name:selectDirNameList){
                if(name.equals(dirName)){
                    return;
                }
            }
            selectDirNameList.add(dirName);
        }else {
            for(int i=0;i<selectDirNameList.size();i++){
                if(dirName.equals(selectDirNameList.get(i))){
                    selectDirNameList.remove(i);
                    return;
                }
            }
        }
    }
    /**
     * 文件或目录列表的点击监听
     */
    AdapterView.OnItemClickListener dirItemLis = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String,Object> map = dirNameList.get((int) id);
            boolean select = !((boolean)map.get(CHECK));
            map.put(CHECK,select);
            fileNameAdapter.notifyDataSetChanged();
            selectDir(map.get(DIR_NAME).toString(),select);
        }
    };
}
