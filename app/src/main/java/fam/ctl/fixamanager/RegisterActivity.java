package fam.ctl.fixamanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.poi.ss.formula.functions.T;

import java.util.List;

import fam.ctl.fixamanager.grrendao.AssetsDaoUtils;

public class RegisterActivity extends Activity {
    private Context context;
    private Button register,exit;
    private EditText usename,password,password_again;
    private AssetsDaoUtils assetsDaoUtils;
    private String update="";
    private String adminAName="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        context=this;
        init();
        bindEvent();
    }

    private void init() {
        assetsDaoUtils=new AssetsDaoUtils(context,"");
        register=findViewById(R.id.register_button);
        exit=findViewById(R.id.register_exit_button);
        usename=findViewById(R.id.register_user);
        password=findViewById(R.id.register_password);
        password_again=findViewById(R.id.register_password_again);
        Bundle receive=getIntent().getExtras();
        //得到随Intent传递过来的Bundle对象
        if (receive!=null){
            update=receive.getString("update");
            adminAName=receive.getString("adminActivity");
            if (update.equals("update")){
                register.setText("修改");
                password.setHint("原密码");
                password_again.setHint("新密码");
            }else {
                register.setText("注册");
                password.setHint("密码");
                password_again.setHint("再次确认密码");
            }
        }else {
            register.setText("注册");
            password.setHint("密码");
            password_again.setHint("再次确认密码");
        }
    }

    private void bindEvent() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=usename.getText().toString();
                String pasword=password.getText().toString();
                String pw_again=password_again.getText().toString();
                    if (name.equals("")||name==null){
                        Toast.makeText(context,"用户名不可以为空！",Toast.LENGTH_LONG);
                    }else  if (pasword.equals("")||pasword==null){
                        Toast.makeText(context,"密码不可以为空！",Toast.LENGTH_LONG);
                    }else if (pw_again.equals("")||pw_again==null){
                        Toast.makeText(context,"确认密码不可以为空！",Toast.LENGTH_LONG);
                    }else {
                        if (update.equals("update")){
                            //修改密码
                            if (assetsDaoUtils.QueryLogin(name,pasword)){
                                assetsDaoUtils.updateLoginPwd(name,pw_again);
                                Bundle data=new Bundle();
                                data.putString("name",name);
                                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                                intent.putExtras(data);
                                startActivity(intent);
                                finish();
                            }else {
                                AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle("提示")
                                        .setMessage("输入的用户名或原密码错误，请重新输入")
                                        .setPositiveButton("确定",null);
                                builder.show();
                            }
                        }else if (update.equals("admin")){
                            if (!pasword.equals(pw_again)){
                                AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle("提示")
                                        .setMessage("输入的密码不一致，请重新输入")
                                        .setPositiveButton("确定",null);
                                builder.show();
                            }else {
                                //添加进数据库并登陆
                                List<String> names=assetsDaoUtils.LoginNameQuery(adminAName);
                                if (names.size()==0){
                                    assetsDaoUtils.insertLogin(name,pw_again,adminAName);
                                    Intent intent=new Intent(RegisterActivity.this,AdminActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    boolean flag=false;
                                    for (int i=0;i<names.size();i++){
                                        if (names.get(i).equals(name)){
                                            Toast.makeText(context,"此用户名已被注册",Toast.LENGTH_LONG).show();
                                            flag=true;
                                            break;
                                        }
                                    }
                                    for (int i=0;i<names.size();i++){
                                        if (!flag){
                                            assetsDaoUtils.insertLogin(name,pw_again,adminAName);
                                            Intent intent=new Intent(RegisterActivity.this,AdminActivity.class);
                                            startActivity(intent);
                                            finish();
                                            break;
                                        }
                                    }
                                }
                            }
                        }else{
                            //注册
                            if (!pasword.equals(pw_again)){
                                AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle("提示")
                                        .setMessage("输入的密码不一致，请重新输入")
                                        .setPositiveButton("确定",null);
                                builder.show();
                            }else {
                                //添加进数据库并登陆
                                List<String> names=assetsDaoUtils.LoginNameQuery(adminAName);
                                if (names.size()==0){
                                    assetsDaoUtils.insertLogin(name,pw_again,adminAName);
                                    Bundle data=new Bundle();
                                    //创建了一个Bundle对象用来存储在两个Activity之间传递的数据
                                    data.putString("name",name);
                                    //添加进Bundle对象里面两个String类型的数据和一个int类型的数据
                                    Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                                    //创建了一个从MainActivity跳转到Main2Activity的Intent
                                    intent.putExtras(data);
                                    //将存储了数据的Bundle对象put进Intent里面
                                    startActivity(intent);
                                    finish();
                                }else {
                                    boolean flag=false;
                                    for (int i=0;i<names.size();i++){
                                        if (names.get(i).equals(name)){
                                            Toast.makeText(context,"此用户名已被注册",Toast.LENGTH_LONG).show();
                                            flag=true;
                                            break;
                                        }
                                    }
                                    for (int i=0;i<names.size();i++){
                                        if (!flag){
                                            assetsDaoUtils.insertLogin(name,pw_again,adminAName);
                                            Bundle data=new Bundle();
                                            //创建了一个Bundle对象用来存储在两个Activity之间传递的数据
                                            data.putString("name",name);
                                            //添加进Bundle对象里面两个String类型的数据和一个int类型的数据
                                            Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                                            //创建了一个从MainActivity跳转到Main2Activity的Intent
                                            intent.putExtras(data);
                                            //将存储了数据的Bundle对象put进Intent里面
                                            startActivity(intent);
                                            finish();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (update.equals("update")){
                    Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                    startActivity(intent);
                }else if (update.equals("admin")){
                    Intent intent=new Intent(RegisterActivity.this,AdminActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        assetsDaoUtils.vlose();
    }
}
