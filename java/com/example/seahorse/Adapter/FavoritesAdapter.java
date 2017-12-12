package com.example.seahorse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.seahorse.Model.Favorite;
import com.example.seahorse.stockmarket.R;
import com.example.seahorse.stockmarket.TabbedActivity;

import java.util.ArrayList;

/**
 * Created by seahorse on 11/24/2017.
 */

public class FavoritesAdapter extends ArrayAdapter<Favorite>{

    private Context mContext;
    private int mResource;

    public FavoritesAdapter(Context context, int resource, ArrayList<Favorite> objects){
        super(context,resource,objects);
        mContext = context;
        mResource=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String symbol = getItem(position).getSymbol();
        float lastPrice = getItem(position).getLastPrice();
        float change = getItem(position).getChange();
        float changePercent = getItem(position).getChangePercent();

        Favorite favorite = new Favorite(symbol,lastPrice,change,changePercent);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);
        convertView.setLongClickable(true);

        TextView tvSymbol = (TextView) convertView.findViewById(R.id.tv_symbol);
        tvSymbol.setText(symbol);
        TextView tvLastPrice = (TextView) convertView.findViewById(R.id.tv_lastPrice);
        tvLastPrice.setText(String.format("%.2f",lastPrice));
        TextView tvChange = (TextView) convertView.findViewById(R.id.tv_change);
        tvChange.setText(String.format("%.2f (%.2f%%)",change,changePercent));
        if(change>=0){
            tvChange.setTextColor(Color.parseColor("#97c261"));
        }

        else{
            tvChange.setTextColor(Color.parseColor("#c63e38"));
        }

        View.OnClickListener openStock = new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TabbedActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("symbol",symbol);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        };
        convertView.setOnClickListener(openStock);

        return convertView;

    }



}
