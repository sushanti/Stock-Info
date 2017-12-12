package com.example.seahorse.Model;

/**
 * Created by seahorse on 11/20/2017.
 */

public class StockSymbol {
    public String Symbol,Name,Exchange;

    public StockSymbol(){

    }

    public StockSymbol(String Symbol, String Name,String Exchange){
        this.Symbol = Symbol;
        this.Name = Name;
        this.Exchange = Exchange;
    }

}
