package com.example.seahorse.stockmarket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.seahorse.Adapter.FavoritesAdapter;
import com.example.seahorse.Adapter.SuggestionAdapter;
import com.example.seahorse.BL.StockBL;
import com.example.seahorse.Model.Favorite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StockSearchActivity extends AppCompatActivity {
    AutoCompleteTextView acTextView;
    RequestQueue requestQueue;
    FavoritesAdapter favoritesAdapter;
    ArrayList<Favorite> favoritesList = new ArrayList<>();
    ArrayList<Favorite> newList = new ArrayList<>();
    int requestSize;
    Spinner sortBySpinner,orderBySpinner;
    Toast validationToast = null;
    ProgressBar favoriteProgressBar;
    Switch autorefreshSwitch;
    ImageButton refresh;
    Handler handler = new Handler();
    int delay = 5000; //milliseconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_search);
        getSupportActionBar().hide();

        setOrderByDropdown();
        setSortByDropdown();

        acTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteText);
        acTextView.setThreshold(1);
        acTextView.setAdapter(new SuggestionAdapter(this,acTextView.getText().toString()));

        refresh = (ImageButton) findViewById(R.id.ib_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRefreshList();
            }
        });

        autorefreshSwitch = (Switch) findViewById(R.id.switch_autorefresh);

        autorefreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

               if(isChecked){
                   handler.postDelayed(new Runnable(){
                       public void run(){
                           getRefreshList();
                           handler.postDelayed(this, delay);
                       }
                   }, delay);
               }

               else{
                   handler.removeCallbacksAndMessages(null);
               }
            }
        });


        favoriteProgressBar =(ProgressBar) findViewById(R.id.indi_favoriteList);
        ListView mlistView = (ListView) findViewById(R.id.lv_favoriteList);
        favoritesAdapter = new FavoritesAdapter(this,R.layout.favorite_list_layout,favoritesList);
        mlistView.setAdapter(favoritesAdapter);
        getFavoriteList();
        registerForContextMenu(mlistView);

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo)
    {

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Remove from Favorites?");
        menu.add(0, v.getId(), 0, "No");
        menu.add(0, v.getId(), 0, "Yes");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        //  info.position will give the index of selected item
        int IndexSelected=info.position;
        if("YES".equals(item.toString().toUpperCase()))
        {
            Toast toast = Toast.makeText(this, "Selected Yes", Toast.LENGTH_LONG);
            toast.show();
            SharedPreferences preferences = this.getSharedPreferences("com.example.seahorse", Context.MODE_PRIVATE);
            preferences.edit().remove(favoritesAdapter.getItem(info.position).getSymbol()).commit();
            favoritesList.remove(favoritesAdapter.getItem(info.position));
            favoritesAdapter.notifyDataSetChanged();
        }
        return true;

    }





    public void setOrderByDropdown(){
        List<String> orderBy = new ArrayList<String>();
        orderBy.add("Order");
        orderBy.add("Ascending");
        orderBy.add("Descending");

        orderBySpinner = (Spinner) findViewById(R.id.spinner_orderBy);
        final ArrayAdapter<String> orderByAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderBy){
            @Override
            public boolean isEnabled(int position){
                if(position==0 || position == orderBySpinner.getSelectedItemPosition())
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                // TODO Auto-generated method stub
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                if (position==0 || position == orderBySpinner.getSelectedItemPosition()) {
                    mTextView.setTextColor(Color.GRAY);
                } else {
                    mTextView.setTextColor(Color.BLACK);
                }
                return mView;
            }
        };
        orderByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderBySpinner.setAdapter(orderByAdapter);

        orderBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sortDropdown(sortBySpinner.getSelectedItem().toString().toUpperCase(),adapterView.getItemAtPosition(i).toString().toUpperCase());
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


    }

    public void setSortByDropdown(){
        List<String> sortBy = new ArrayList<String>();
        sortBy.add("Sort By");
        sortBy.add("Default");
        sortBy.add("Symbol");
        sortBy.add("Price");
        sortBy.add("Change");
        sortBy.add("Change Percent");


        sortBySpinner = (Spinner) findViewById(R.id.spinner_sortBy);
        final ArrayAdapter<String> sortByAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortBy){
            @Override
            public boolean isEnabled(int position){
                if(position==0 || position == sortBySpinner.getSelectedItemPosition())
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                // TODO Auto-generated method stub
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                if (position==0 || position == sortBySpinner.getSelectedItemPosition()) {
                    mTextView.setTextColor(Color.GRAY);
                } else {
                    mTextView.setTextColor(Color.BLACK);
                }
                return mView;
            }
        };
        sortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(sortByAdapter);

        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sortDropdown(adapterView.getItemAtPosition(i).toString().toUpperCase(),orderBySpinner.getSelectedItem().toString().toUpperCase());
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

    }

    public void sortDropdown(String sortBy,String orderBy){
        int order = orderBy.equals("ASCENDING") ? 0 : orderBy.equals("DESCENDING")? 1 : 2;
        switch(sortBy){
            case "SYMBOL":
                switch(order){
                    case 0:
                        Collections.sort(favoritesList,Favorite.SymbolAscendingComparator);
                        break;
                    case 1:
                        Collections.sort(favoritesList,Favorite.SymbolDescendingComparator);
                        break;
                }
                orderBySpinner.setEnabled(true);
                break;

            case "PRICE":
                switch(order){
                    case 0:
                        Collections.sort(favoritesList,Favorite.LastPriceAscendingComparator);
                        break;
                    case 1:
                        Collections.sort(favoritesList,Favorite.LastPriceDescendingComparator);
                        break;
                }
                orderBySpinner.setEnabled(true);
                break;

            case "CHANGE":
                switch(order){
                    case 0:
                        Collections.sort(favoritesList,Favorite.ChangeAscendingComparator);
                        break;
                    case 1:
                        Collections.sort(favoritesList,Favorite.ChangeDescendingComparator);
                        break;
                }
                orderBySpinner.setEnabled(true);
                break;

            case "CHANGE PERCENT":
                switch(order){
                    case 0:
                        Collections.sort(favoritesList,Favorite.ChangePercentAscendingComparator);
                        break;
                    case 1:
                        Collections.sort(favoritesList,Favorite.ChangePercentDescendingComparator);
                        break;
                }
                orderBySpinner.setEnabled(true);
                break;

            case "DEFAULT":
                orderBySpinner.setEnabled(false);
                break;
        }

        favoritesAdapter.notifyDataSetChanged();

    }

    public void populateAutoComplete(View view){
        requestQueue= Volley.newRequestQueue(this);
        String symbol = "AAPL";
        String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/autocomplete?symbol="+symbol;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                try{
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject symbolData = jsonArray.getJSONObject(i);
                        String symbol = symbolData.getString("Symbol");
                        String name = symbolData.getString("Name");
                        String exchange = symbolData.getString("Exchange");
                        Log.d("Volley",exchange);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.d("Volley","catch");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d("Volley","error");
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    public void getStockDetails(View view){
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteText);
        String actText = textView.getText().toString();

        if(actText==null || actText.trim().length() == 0){
            validationToast = Toast.makeText(this, "Please enter a stock name or symbol", Toast.LENGTH_LONG);
            validationToast.show();
        }
        else{
            Intent intent = new Intent(this, TabbedActivity.class);
            String parts[] = actText.split("-");
            Bundle bundle = new Bundle();
            bundle.putString("symbol",parts[0].trim());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void clearDetails(View view){
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteText);
        textView.setText("");

        if(validationToast!=null)
            validationToast.cancel();
    }

    public void getFavoriteList() {
        List<String> favoriteSymbols = new ArrayList<String>();
        favoriteProgressBar.setVisibility(View.VISIBLE);
        SharedPreferences preferences = this.getSharedPreferences("com.example.seahorse", Context.MODE_PRIVATE);
        Map<String, ?> keys = preferences.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            favoriteSymbols.add(entry.getKey());
        }
        requestSize = favoriteSymbols.size();

        requestQueue = Volley.newRequestQueue(this);
        favoritesList.clear();
        for (int i = 0; i < favoriteSymbols.size(); i++) {
            String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/stockdetails?symbol=" + favoriteSymbols.get(i);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        JSONObject metaData = response.getJSONObject("Meta Data");
                        JSONObject timeSeries = response.getJSONObject("Time Series (Daily)");

                        String firstDate = timeSeries.names().getString(0);
                        String previousDate = timeSeries.names().getString(1);
                        JSONObject latestData = timeSeries.getJSONObject(firstDate);
                        JSONObject previousData = timeSeries.getJSONObject(previousDate);

                        String symbol = metaData.getString("2. Symbol");
                        String close = latestData.getString("4. close");
                        String previousclose = previousData.getString("4. close");

                        float change = Float.parseFloat(close) - Float.parseFloat(previousclose);
                        float changePercent = change / Float.parseFloat(previousclose) * 100;

                        favoritesList.add(new Favorite(symbol, Float.parseFloat(close), change, changePercent));
                        requestSize--;

                        if (requestSize <= 0) {
                            favoriteProgressBar.setVisibility(View.GONE);
                        }

                        favoritesAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    requestSize--;

                    if (requestSize <= 0) {
                        favoriteProgressBar.setVisibility(View.GONE);
                    }
                    Log.d("VOLLEY", error.toString());
                }
            });

            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsObjRequest);

        }
    }

    public void getRefreshList(){
        List<String> favoriteSymbols = new ArrayList<String>();
        favoriteProgressBar.setVisibility(View.VISIBLE);
        SharedPreferences preferences = this.getSharedPreferences("com.example.seahorse", Context.MODE_PRIVATE);
        Map<String,?> keys = preferences.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            favoriteSymbols.add(entry.getKey());
        }
        requestSize = favoriteSymbols.size();

        requestQueue= Volley.newRequestQueue(this);
        newList.clear();
        for(int i=0;i<favoriteSymbols.size();i++){
            String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/stockdetails?symbol="+favoriteSymbols.get(i);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        JSONObject metaData = response.getJSONObject("Meta Data");
                        JSONObject timeSeries = response.getJSONObject("Time Series (Daily)");

                        String firstDate = timeSeries.names().getString(0);
                        String previousDate = timeSeries.names().getString(1);
                        JSONObject latestData = timeSeries.getJSONObject(firstDate);
                        JSONObject previousData = timeSeries.getJSONObject(previousDate);

                        String symbol = metaData.getString("2. Symbol");
                        String close = latestData.getString("4. close");
                        String previousclose = previousData.getString("4. close");

                        float change = Float.parseFloat(close)  - Float.parseFloat(previousclose);
                        float changePercent = change / Float.parseFloat(previousclose) * 100;

                        newList.add(new Favorite(symbol,Float.parseFloat(close),change,changePercent));
                        requestSize--;

                        if(requestSize<=0){
                            favoriteProgressBar.setVisibility(View.GONE);
                            favoritesList.clear();
                            favoritesList.addAll(newList);
                            favoritesAdapter.notifyDataSetChanged();
                        }



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    requestSize--;

                    if(requestSize<=0){
                        favoriteProgressBar.setVisibility(View.GONE);
                        favoritesList.clear();
                        favoritesList.addAll(newList);
                        favoritesAdapter.notifyDataSetChanged();
                    }
                    Log.d("VOLLEY",error.toString());
                }
            });

            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsObjRequest);

        }



    }
}
