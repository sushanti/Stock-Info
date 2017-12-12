package com.example.seahorse.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.seahorse.Model.StockListView;
import com.example.seahorse.stockmarket.R;
import com.google.zxing.common.StringUtils;

import java.util.ArrayList;

/**
 * Created by seahorse on 11/22/2017.
 */

public class StockSymbolListAdapter extends ArrayAdapter<StockListView> {
    private Context mContext;
    private int mResource;

    public StockSymbolListAdapter(Context context, int resource, ArrayList<StockListView> objects){
        super(context,resource,objects);
        mContext = context;
        mResource=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();
        String value = getItem(position).getValue();

        StockListView stock = new StockListView(name,value);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);


        TextView tvName = (TextView) convertView.findViewById(R.id.textView1);
        tvName.setText(name);
        TextView tvValue = (TextView) convertView.findViewById(R.id.textView2);
        tvValue.setText(value);

        if("CHANGE".equals(name.toUpperCase())){
            String[] split = value.split("\\(");
            Float change = Float.parseFloat(split[0].trim());
            Drawable drawable = change >= 0 ? getContext().getResources().getDrawable(R.drawable.green) : getContext().getResources().getDrawable(R.drawable.red) ;
            drawable.setBounds(0,0,50,50);
            tvValue.setCompoundDrawables(drawable,null,null ,null);

        }
        return convertView;

    }
}
