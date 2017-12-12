package com.example.seahorse.stockmarket;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.seahorse.Adapter.NewsAdapter;
import com.example.seahorse.Adapter.StockSymbolListAdapter;
import com.example.seahorse.Model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by seahorse on 11/22/2017.
 */

public class NewsFragment extends Fragment {
    private static final String TAG = "NewsFragment";
    NewsAdapter adapter;
    ArrayList<News> newsList = new ArrayList<>();
    RequestQueue requestQueue;
    String symbol;
    ProgressBar newsProgressBar;
    TextView tv_newserror;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment,container,false);

        symbol = getArguments().getString("symbol");

        newsProgressBar =(ProgressBar) view.findViewById(R.id.indi_news);
        tv_newserror = (TextView) view.findViewById(R.id.tv_newserror);
        tv_newserror.setVisibility(View.INVISIBLE);

        ListView mlistView = (ListView) view.findViewById(R.id.listViewNews);
        adapter = new NewsAdapter(this.getContext(),R.layout.news_fragment_layout,newsList);
        mlistView.setAdapter(adapter);
        getNewsList();

        return view;
    }

    public void getNewsList(){
        requestQueue= Volley.newRequestQueue(this.getContext());
        String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/newsfeed?symbol="+symbol;
        newsProgressBar.setVisibility(View.VISIBLE);

        final SimpleDateFormat sdfGet = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        final SimpleDateFormat sdfPut = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        sdfPut.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date publicationDate = null;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    newsList.clear();
                    tv_newserror.setVisibility(View.INVISIBLE);
                    JSONObject rss = response.getJSONObject("rss");
                    JSONArray JsonChannel = rss.getJSONArray("channel");
                    JSONObject channel = JsonChannel.getJSONObject(0);
                    JSONArray items = channel.getJSONArray("item");
                    int length = items.length() < 5 ? items.length() : 5;
                    int i = 0;

                    while (length > 0 && i < items.length()) {
                        JSONObject item = items.getJSONObject(i);
                        String link = item.getJSONArray("link").getString(0);

                        if (link.contains("/article/")) {
                            String title = item.getJSONArray("title").getString(0);
                            String author = item.getJSONArray("sa:author_name").getString(0);
                            String pubDate = item.getJSONArray("pubDate").getString(0);
                            try {
                                Date publicationDate = sdfGet.parse(pubDate);
                                pubDate = sdfPut.format(publicationDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            newsList.add(new News(title,author,pubDate,link));
                            length--;
                        }
                        i++;
                    }

                    newsProgressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    tv_newserror.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                    newsProgressBar.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tv_newserror.setVisibility(View.VISIBLE);
                newsProgressBar.setVisibility(View.GONE);
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }
}
