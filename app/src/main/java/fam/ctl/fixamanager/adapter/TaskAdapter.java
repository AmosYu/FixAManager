package fam.ctl.fixamanager.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import fam.ctl.fixamanager.MainActivity;
import fam.ctl.fixamanager.R;
import fam.ctl.fixamanager.TaskActivity;
import fam.ctl.fixamanager.beans.Assets;

public class TaskAdapter extends BaseAdapter {
    private Context context;
    private List<String> tasks;
    public TaskAdapter(Context context,List<String> task_lists){
        this.context=context;
        this.tasks=task_lists;
    }
    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int i) {
        return tasks.get(i);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.tasklist_item, null);
            viewHolder.task_name = (TextView) convertView.findViewById(R.id.task_item_name);
            viewHolder.task_enter = (TextView) convertView.findViewById(R.id.task_item_enter);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.task_name.setText(tasks.get(position));
        viewHolder.task_enter.setText("▶");
        viewHolder.task_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.task_name=tasks.get(position);
                Bundle data=new Bundle();
                //创建了一个Bundle对象用来存储在两个Activity之间传递的数据
                data.putString("username", TaskActivity.username);
                data.putString("flag", "task");
                Intent intent=new Intent();
                intent.setClass(context, MainActivity.class);
                intent.putExtras(data);
                context.startActivity(intent);
            }
        });
        return convertView;
    }
    public class ViewHolder{
        private TextView task_name;
        private TextView task_enter;
    }
}
