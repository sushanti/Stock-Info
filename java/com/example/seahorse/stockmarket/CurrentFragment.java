package com.example.seahorse.stockmarket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.seahorse.Adapter.StockSymbolListAdapter;
import com.example.seahorse.Model.StockListView;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by seahorse on 11/22/2017.
 */

public class CurrentFragment extends Fragment {
    private static final String TAG = "CurrentFragment";
    private Button btnTest;
    RequestQueue requestQueue;
    StockSymbolListAdapter adapter;
    WebView indicatorWebView;
    ArrayList<StockListView> stockList = new ArrayList<>();
    LoginButton loginButton;
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    String symbol;
    String chart="PRICE";
    TextView Change,tv_stockerror,tv_charterror;
    Spinner indicatorSpinner;
    Button favorite;
    String chartUrl;
    Button facebookShare;
    ProgressBar stockDetailsProgressBar,indicatorChartsProgressBar;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.current_fragment,container,false);

        symbol = getArguments().getString("symbol");

        favorite = (Button) view.findViewById(R.id.b_star);
        setFavoriteBackground();

        stockDetailsProgressBar =(ProgressBar) view.findViewById(R.id.indi_stockDetails);
        indicatorChartsProgressBar =(ProgressBar) view.findViewById(R.id.indi_indicator);
        tv_stockerror = (TextView) view.findViewById(R.id.tv_stockerror);
        tv_stockerror.setVisibility(View.INVISIBLE);
        tv_charterror = (TextView) view.findViewById(R.id.tv_charterror);
        tv_charterror.setVisibility(View.INVISIBLE);

        ListView mlistView = (ListView) view.findViewById(R.id.listView);
        adapter = new StockSymbolListAdapter(this.getContext(),R.layout.adapter_view_layout,stockList);
        mlistView.setAdapter(adapter);
        getStockDetails();

        indicatorSpinner = (Spinner) view.findViewById(R.id.spinner_chart);
        setDropdown(view);

        Change = (TextView) view.findViewById(R.id.tv_change);
        Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chart = indicatorSpinner.getSelectedItem().toString().toUpperCase();
                loadChart(chart);
                indicatorWebView.reload();


                Change.setClickable(false);
                Change.setTextColor(Color.parseColor("#bbbbbb"));
            }
        });


        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertOrDeleteFromFavoriteList();
            }
        });



        indicatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!chart.equals(adapterView.getItemAtPosition(i).toString().toUpperCase())){
                    Change.setClickable(true);
                    Change.setTextColor(Color.parseColor("#000000"));
                }

                else{
                    Change.setClickable(false);
                    Change.setTextColor(Color.parseColor("#bbbbbb"));
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });



        indicatorWebView = (WebView) view.findViewById(R.id.webView_indicator);

        loadChart("PRICE");

        facebookShare = (Button) view.findViewById(R.id.share_button);
        facebookShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareChart();
            }
        });

        setRetainInstance(true);
        return view;
    }

    private void loadChart(final String chartType){
        RequestQueue RQ = Volley.newRequestQueue(this.getContext());
        indicatorWebView.setVisibility(View.GONE);
        indicatorChartsProgressBar.setVisibility(View.VISIBLE);

        String url = "PRICE".equals(chartType.toUpperCase()) ? "http://stockinfo.us-east-2.elasticbeanstalk.com/stockdetails?symbol="+symbol : "http://stockinfo.us-east-2.elasticbeanstalk.com/indicatorData?indicator="+chartType+"&symbol="+symbol;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
            @SuppressLint("NewApi")
            @Override
            public void onResponse(final JSONObject response) {
                indicatorWebView.loadUrl("file:///android_asset/indicator-chart.html");
                indicatorWebView.getSettings().setJavaScriptEnabled(true);
                tv_charterror.setVisibility(View.INVISIBLE);
                indicatorWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        switch(chartType.toUpperCase()){
                            case "PRICE":
                                view.loadUrl("javascript:loadPriceVolumeChart("+response+")");
                                break;
                            default:
                                view.loadUrl("javascript:loadIndicatorChart('"+chartType+"',"+response+")");
                                break;
                        }
                        indicatorChartsProgressBar.setVisibility(View.GONE);
                        indicatorWebView.setVisibility(View.VISIBLE);
                    }
                });


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLEY",error.toString());
                indicatorChartsProgressBar.setVisibility(View.GONE);
                tv_charterror.setVisibility(View.VISIBLE);
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RQ.add(jsObjRequest);
    }

    @SuppressLint("NewApi")
    private void getChartOptions(JSONObject response){
        indicatorWebView.loadUrl("file:///android_asset/indicator-chart.html");

        switch (chart.toUpperCase()){
            case "PRICE":
                String test1 = "getPriceVolumeChartOptions("+response+")";
                indicatorWebView.evaluateJavascript("getPriceVolumeChartOptions("+response+")", new ValueCallback<String>() {
                    @Override public void onReceiveValue(String value) {
                        Log.d("URL",value);
                        getChartUrl(value);
                    }
                });
                break;
            default:
                String test = "getIndicatorChartOptions('"+chart+"',"+response+")";
                indicatorWebView.evaluateJavascript(test, new ValueCallback<String>() {
                    @Override public void onReceiveValue(String value) {
                        Log.d("URL",value);
                        getChartUrl(value);
                    }
                });
                break;

        }

    }

    private void getChartUrl(final String options){

       RequestQueue RQ = Volley.newRequestQueue(this.getContext());
       final String exportUrl = "http://export.highcharts.com/";
       final JSONObject data = new JSONObject();
        try {
            data.put("options",options);
            data.put("filename","test.png");
            data.put("type","image/png");
            data.put("async",true);

            StringRequest postRequest = new StringRequest(Request.Method.POST, exportUrl,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            shareImageToFacebook(exportUrl+response);
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    HashMap<String, String> params2 = new HashMap<String, String>();
                    return data.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            RQ.add(postRequest);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void shareChart(){
        RequestQueue RQ = Volley.newRequestQueue(this.getContext());
        String url = "PRICE".equals(chart.toUpperCase()) ? "http://stockinfo.us-east-2.elasticbeanstalk.com/stockdetails?symbol="+symbol : "http://stockinfo.us-east-2.elasticbeanstalk.com/indicatorData?indicator="+chart+"&symbol="+symbol;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                getChartOptions(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLEY",error.toString());
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RQ.add(jsObjRequest);

    }

    private void shareImageToFacebook(String url){
        shareDialog = new ShareDialog(this);  // initialize facebook shareDialog.
        callbackManager = CallbackManager.Factory.create();
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(url))
                    .build();
            shareDialog.show(linkContent);  // Show facebook ShareDialog
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void setDropdown(View view){
        List<String> charts = new ArrayList<String>();
        charts.add("Price");
        charts.add("SMA");
        charts.add("EMA");
        charts.add("MACD");
        charts.add("RSI");
        charts.add("ADX");
        charts.add("CCI");

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner_chart);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, charts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void insertOrDeleteFromFavoriteList(){
        SharedPreferences preferences = this.getContext().getSharedPreferences("com.example.seahorse", Context.MODE_PRIVATE);
        if(preferences.contains(symbol)){
            preferences.edit().remove(symbol).commit();
        }
        else{
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(symbol,symbol);
            editor.commit();
        }
        setFavoriteBackground();
    }

    public void setFavoriteBackground(){
        SharedPreferences preferences = this.getContext().getSharedPreferences("com.example.seahorse", Context.MODE_PRIVATE);
        if(preferences.contains(symbol)){
            favorite.setBackgroundDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.filled));
        }
        else{
            favorite.setBackgroundDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.empty));
        }
    }

    public void getStockDetails(){
        requestQueue= Volley.newRequestQueue(this.getContext());
        String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/stockdetails?symbol="+symbol;
        stockDetailsProgressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) {
                try {
                    tv_stockerror.setVisibility(View.INVISIBLE);
                    JSONObject metaData = response.getJSONObject("Meta Data");
                    JSONObject timeSeries = response.getJSONObject("Time Series (Daily)");

                    String symbol = metaData.getString("2. Symbol");
                    String firstDate = timeSeries.names().getString(0);
                    String previousDate = timeSeries.names().getString(1);

                    JSONObject latestData = timeSeries.getJSONObject(firstDate);
                    JSONObject previousData = timeSeries.getJSONObject(previousDate);

                    float open = Float.parseFloat(latestData.getString("1. open"));
                    float high = Float.parseFloat(latestData.getString("2. high"));
                    float close = Float.parseFloat(latestData.getString("4. close"));
                    float low = Float.parseFloat(latestData.getString("3. low"));
                    String volume = latestData.getString("5. volume");
                    float previousclose = Float.parseFloat(previousData.getString("4. close"));
                    float change = close  - previousclose;
                    float changePercent = change / previousclose * 100;

                    String currentTime="";
                    String endTime ="";
                    String timestamp="";
                    Date currentDate = new Date();
                    Date endDate = null;

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                    //SimpleDateFormat sdfPut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                    sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));

                    try {
                        endDate =sdf.parse(firstDate+" 13:00:00 PST");
                        currentTime = sdf.format(currentDate);
                        endTime =sdf.format(endDate);
                        timestamp = currentDate.compareTo(endDate) < 0 ? currentTime : endTime;
                        int test = currentDate.compareTo(endDate);
                    } catch (ParseException e) {
                        tv_stockerror.setVisibility(View.VISIBLE);
                        e.printStackTrace();
                    }



                    StockListView stockSymbol = new StockListView("Stock Symbol",symbol);
                    StockListView stockLastPrice = new StockListView("Last Price",String.format("%.2f",close));
                    StockListView stockChange = new StockListView("Change",String.format("%.2f",change) + " (" + String.format("%.2f",changePercent)+"%)");
                    StockListView stockTimestamp = new StockListView("TimeStamp",timestamp);
                    StockListView stockOpen = new StockListView("Open",String.format("%.2f",open));
                    StockListView stockClose = new StockListView("Close",String.format("%.2f",currentDate.compareTo(endDate) < 0 ? previousclose : close));
                    StockListView stockRange = new StockListView("Day's Range",String.format("%.2f",low)+" - "+String.format("%.2f",high));
                    StockListView stockVolume = new StockListView("Volume",volume);

                    stockList.clear();
                    stockList.add(stockSymbol);
                    stockList.add(stockLastPrice);
                    stockList.add(stockChange);
                    stockList.add(stockTimestamp);
                    stockList.add(stockOpen);
                    stockList.add(stockClose);
                    stockList.add(stockRange);
                    stockList.add(stockVolume);

                    stockDetailsProgressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    tv_stockerror.setVisibility(View.VISIBLE);
                    stockDetailsProgressBar.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tv_stockerror.setVisibility(View.VISIBLE);
                stockDetailsProgressBar.setVisibility(View.GONE);
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

}