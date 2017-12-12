package com.example.seahorse.stockmarket;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by seahorse on 11/22/2017.
 */

public class HistoricalFragment extends Fragment {
    private static final String TAG = "HistoricalFragment";
    WebView historicalWebView;
    String symbol;
    View view;
    ProgressBar historicalProgressBar;
    TextView tv_histerror;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.historical_fragment,container,false);

        historicalProgressBar =(ProgressBar) view.findViewById(R.id.indi_historical);
        historicalProgressBar.setVisibility(View.VISIBLE);
        tv_histerror = (TextView) view.findViewById(R.id.tv_histerror);
        tv_histerror.setVisibility(View.INVISIBLE);

        symbol = getArguments().getString("symbol");

        RequestQueue RQ = Volley.newRequestQueue(this.getContext());
        String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/stockdetails?symbol="+symbol;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
            @SuppressLint("NewApi")
            @Override
            public void onResponse(final JSONObject response) {
                historicalWebView = (WebView) view.findViewById(R.id.webView_historical);
                historicalWebView.loadUrl("file:///android_asset/historical-chart.html");
                historicalWebView.getSettings().setJavaScriptEnabled(true);
                historicalProgressBar.setVisibility(View.GONE);
                tv_histerror.setVisibility(View.INVISIBLE);
                historicalWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        view.loadUrl(String.format("javascript:loadHighStockChart("+response+")"));
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tv_histerror.setVisibility(View.VISIBLE);
                historicalProgressBar.setVisibility(View.GONE);
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RQ.add(jsObjRequest);

        return view;
    }


}
