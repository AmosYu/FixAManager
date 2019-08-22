package fam.ctl.fixamanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fam.ctl.fixamanager.R;
import fam.ctl.fixamanager.beans.Assets;

public class FixAssetsAdapter extends BaseAdapter {
    private Context context;
    private List<Assets> assetsList;
    public FixAssetsAdapter(Context context,List<Assets> assetsList){
        this.context=context;
        this.assetsList=assetsList;
    }
    @Override
    public int getCount() {
        return assetsList.size();
    }

    @Override
    public Object getItem(int i) {
        return assetsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            viewHolder.id = (TextView) convertView.findViewById(R.id.fix_item_id);
            viewHolder.code = (TextView) convertView.findViewById(R.id.fix_item_code);
            viewHolder.company=convertView.findViewById(R.id.fix_item_company);
            viewHolder.beizhu = (TextView) convertView.findViewById(R.id.fix_item_person);
            viewHolder.isuseless = (TextView) convertView.findViewById(R.id.fix_item_useless);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.id.setText((position+1)+"");
        viewHolder.code.setText(assetsList.get(position).getCode());
        viewHolder.company.setText(assetsList.get(position).getCompany());
        if (assetsList.get(position).getOthers()==null||assetsList.get(position).getOthers().equals("")){
            viewHolder.beizhu.setText("");
        }else {
            viewHolder.beizhu.setText(assetsList.get(position).getOthers()+"");
        }
        int use=assetsList.get(position).getUseless();
        //1在库已核查   0在库未核查   2已核查未在库
        if (use==0){
            viewHolder.isuseless.setText("在库未核查");
            viewHolder.isuseless.setTextColor(Color.BLACK);
        }else if (use==1){
            viewHolder.isuseless.setText("在库已核查");
            viewHolder.isuseless.setTextColor(Color.RED);
        }else if (use==2){
            viewHolder.isuseless.setText("已核查未在库");
            viewHolder.isuseless.setTextColor(Color.BLUE);
        }
        return convertView;
    }
    public class ViewHolder{
        private TextView id;
        private TextView code;
        private TextView company;
        private TextView beizhu;
        private TextView isuseless;
    }
}
