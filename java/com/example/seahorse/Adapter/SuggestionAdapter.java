package com.example.seahorse.Adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.seahorse.BL.StockBL;
import com.example.seahorse.Model.StockSymbol;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seahorse on 11/20/2017.
 */

public class SuggestionAdapter extends ArrayAdapter<String> {

    protected static final String TAG = "SuggestionAdapter";
    private List<String> suggestions;
    RequestQueue requestQueue;

    public SuggestionAdapter(Activity context, String nameFilter) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        suggestions = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public String getItem(int index) {
        return suggestions.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                StockBL stockBL=new StockBL();
                if (constraint != null) {
                    // A class that queries a web API, parses the data and
                    // returns an ArrayList<GoEuroGetSet>
                    List<StockSymbol> new_suggestions = stockBL.getStockSymbol(constraint.toString());
                    suggestions.clear();
                    int autoCompleteLength = new_suggestions.size() < 5 ? new_suggestions.size() : 5;
                    for (int i=0;i<autoCompleteLength;i++) {
                        String text = String.format("%s - %s (%s)",new_suggestions.get(i).Symbol,new_suggestions.get(i).Name,new_suggestions.get(i).Exchange);
                        suggestions.add(text);
                    }

                    // Now assign the values and count to the FilterResults
                    // object
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint,
                                          FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }


    public List<StockSymbol> getAutoCompleteData(String symbol){
        requestQueue= Volley.newRequestQueue(this.getContext());
        String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/autocomplete?symbol="+symbol;
        final List<StockSymbol> ListData = new ArrayList<StockSymbol>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                try{
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject symbolData = jsonArray.getJSONObject(i);
                        ListData.add(new StockSymbol(symbolData.getString("Symbol"),symbolData.getString("Name"),symbolData.getString("Exchange")));

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
        return ListData;
    }

}
