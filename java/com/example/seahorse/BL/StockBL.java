package com.example.seahorse.BL;

import com.example.seahorse.Model.StockSymbol;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seahorse on 11/20/2017.
 */

public class StockBL {

             public List<StockSymbol> getStockSymbol(String symbol){
             List<StockSymbol> ListData = new ArrayList<StockSymbol>();

             String url = "http://stockinfo.us-east-2.elasticbeanstalk.com/autocomplete?symbol="+symbol;

                 try {
                     URL js = new URL(url);
                     URLConnection jc = js.openConnection();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(jc.getInputStream()));
                     String line = reader.readLine();
                     JSONArray jsonArray = new JSONArray(line);
                     for(int i = 0; i < jsonArray.length(); i++){
                         JSONObject r = jsonArray.getJSONObject(i);
                         ListData.add(new StockSymbol(r.getString("Symbol"),r.getString("Name"),r.getString("Exchange")));
                     }
                 } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 return ListData;
         }

}

