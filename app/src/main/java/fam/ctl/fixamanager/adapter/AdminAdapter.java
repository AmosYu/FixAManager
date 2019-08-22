package fam.ctl.fixamanager.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fam.ctl.fixamanager.MainActivity;
import fam.ctl.fixamanager.R;
import fam.ctl.fixamanager.TaskActivity;
import fam.ctl.fixamanager.beans.Assets;
import fam.ctl.fixamanager.beans.Login;
import fam.ctl.fixamanager.grrendao.AssetsDaoUtils;

public class AdminAdapter extends BaseAdapter {
    private Context context;
    private List<Login> loginList;
    private AssetsDaoUtils assetsDaoUtils;
    private String admimN="";
    public AdminAdapter(AssetsDaoUtils assetsDaoUtils,Context context, List<Login> loginList,String admin) {
        this.context = context;
        this.loginList = loginList;
        this.assetsDaoUtils=assetsDaoUtils;
        this.admimN=admin;
    }

    @Override
    public int getCount() {
        return loginList.size();
    }

    @Override
    public Object getItem(int i) {
        return loginList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_list_item, null);
            viewHolder.username = (TextView) convertView.findViewById(R.id.admin_item_name);
            viewHolder.password = (TextView) convertView.findViewById(R.id.admin_item_pwd);
            viewHolder.num=convertView.findViewById(R.id.admin_item_num);
            viewHolder.task_enter = (TextView) convertView.findViewById(R.id.admin_item_enter);
//            viewHolder.delete=convertView.findViewById(R.id.admin_delete);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.username.setText(loginList.get(position).getUsername()+"");
        viewHolder.password.setText(loginList.get(position).getPassword()+"");
        int num=assetsDaoUtils.queryByRecord(loginList.get(position).getUsername()).size();
        viewHolder.num.setText("数据量："+num);
        viewHolder.task_enter.setText("▶");
        viewHolder.task_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.task_name=loginList.get(position).getUsername();
                Bundle data=new Bundle();
                //创建了一个Bundle对象用来存储在两个Activity之间传递的数据
                data.putString("username", loginList.get(position).getUsername());
                data.putString("adminAdapter", admimN);
                data.putString("flag", "admin");
                Intent intent=new Intent();
                intent.setClass(context, MainActivity.class);
                intent.putExtras(data);
                context.startActivity(intent);
            }
        });
//        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                assetsDaoUtils.DeleteLogin(assetsDaoUtils,loginList.get(position).getUsername(),loginList.get(position).getPassword());
//            }
//        });
        return convertView;
    }
    public class ViewHolder{
        private TextView username;
        private TextView password;
        private TextView num;
        private TextView task_enter;
//        private ImageView delete;
    }
}
