package fam.ctl.fixamanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fam.ctl.fixamanager.beans.Login;
import fam.ctl.fixamanager.grrendao.AssetsDaoUtils;
import fam.ctl.fixamanager.util.SharedPreferencesHandler;

public class LoginActivity extends Activity implements View.OnFocusChangeListener{
    private EditText username;
    private EditText password;
    private Button login;
    private Button zhuce;
    private TextView update_pw;

    private Context context;
    private AssetsDaoUtils assetsDaoUtils;
    private String name="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context=this;

        init();
        bindEvent();
    }
    private void init() {
        File fileName = new File(Environment.getExternalStorageDirectory() + "/AdminManager/");
        if (!fileName.exists()){
            fileName.mkdirs();
        }
        assetsDaoUtils=new AssetsDaoUtils(context,"");
        name=SharedPreferencesHandler.getDataFromPref(context,"username",name);
        Bundle receive=getIntent().getExtras();
        //得到随Intent传递过来的Bundle对象
        if (receive!=null){
            name=receive.getString("name");
        }
        username=(EditText)findViewById(R.id.email);
//        if ((!name.equals(""))&&name!=null){
//            username.setText(name);
//        }
        username.setText(name);
        password=(EditText)findViewById(R.id.password);
        login=(Button) findViewById(R.id.email_sign_in_button);
        zhuce=(Button) findViewById(R.id.login_exit_button);
        update_pw=findViewById(R.id.upadte_pw_tv);
        username.setOnFocusChangeListener(this);  //对edit 进行焦点监听
    }
    private void bindEvent() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=username.getText().toString();
                String psword=password.getText().toString();
                if (name.equals("")||psword.equals("")){
                    Toast.makeText(context,"用户名或密码不能为空！",Toast.LENGTH_LONG).show();
                }else {
                    if((name.equals("装基处")||name.equals("装基处"))&&psword.equals("zjc123")){
                        //管理员账户
                        Bundle data=new Bundle();
                        data.putString("admin",name);
                        Intent intent=new Intent(LoginActivity.this,AdminActivity.class);
                        intent.putExtras(data);
                        startActivity(intent);
                        SharedPreferencesHandler.setDataToPref(context,"username",name);
                        finish();
                    }else {
                        if (assetsDaoUtils.QueryLogin(name,psword)){
                            Bundle data=new Bundle();
                            //创建了一个Bundle对象用来存储在两个Activity之间传递的数据
                            data.putString("username",name);
                            Intent intent=new Intent();
                            intent.setClass(LoginActivity.this,TaskActivity.class);
                            intent.putExtras(data);
                            startActivity(intent);
                            SharedPreferencesHandler.setDataToPref(context,"username",name);
                            finish();
                        }else {
                            Toast.makeText(context,"密码输入错误！",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
        zhuce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        update_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data=new Bundle();
                //创建了一个Bundle对象用来存储在两个Activity之间传递的数据
                data.putString("update","update");
                Intent intent=new Intent();
                intent.setClass(LoginActivity.this,RegisterActivity.class);
                intent.putExtras(data);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        assetsDaoUtils.vlose();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            showListPopulWindow(); //调用显示PopuWindow 函数
        }
    }
    private void showListPopulWindow() {
        List<Login> logins=assetsDaoUtils.LoginQuery("装基处");
        final List<String> list=new ArrayList<>();
        for (int i=0;i<logins.size();i++){
            list.add(logins.get(i).getUsername());
        }
        list.add("装基处");
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, list));//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(username);//以哪个控件为基准，在该处以mEditText为基准
        listPopupWindow.setModal(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                username.setText(list.get(i));//把选择的选项内容展示在EditText上
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();//把ListPopWindow展示出来
    }
}
