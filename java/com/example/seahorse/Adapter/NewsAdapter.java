package com.example.seahorse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seahorse.Model.News;
import com.example.seahorse.stockmarket.R;

import java.util.ArrayList;


/**
 * Created by seahorse on 11/23/2017.
 */

public class NewsAdapter  extends ArrayAdapter<News> {
    private Context mContext;
    private int mResource;

    public NewsAdapter(Context context, int resource, ArrayList<News> objects){
        super(context,resource,objects);
        mContext = context;
        mResource=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String title = getItem(position).getTitle();
        String author = getItem(position).getAuthor();
        String pubDate = getItem(position).getPublishDate();
        final String articleLink = getItem(position).getArticleLink();

        News news = new News(title,author,pubDate,articleLink);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
        tvTitle.setText(title);
        TextView tvAuthor = (TextView) convertView.findViewById(R.id.textViewAuthor);
        tvAuthor.setText(author);
        TextView tvDate = (TextView) convertView.findViewById(R.id.textViewDate);
        tvDate.setText(pubDate);

        View.OnClickListener openArticle = new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(articleLink));
                mContext.startActivity(intent);
            }
        };

        convertView.setOnClickListener(openArticle);
        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    @Override
    public boolean isEnabled(int arg0)
    {
        return true;
    }


}
