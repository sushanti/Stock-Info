package com.example.seahorse.Model;

import java.util.Comparator;

/**
 * Created by seahorse on 11/24/2017.
 */

public class Favorite {
    private String Symbol;
    private float LastPrice;
    private float Change;
    private float ChangePercent;

    public static Comparator SymbolAscendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getSymbol().compareToIgnoreCase(f2.getSymbol());
        }
    };

    public static Comparator SymbolDescendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f2.getSymbol().compareToIgnoreCase(f1.getSymbol());
        }
    };

    public static Comparator LastPriceAscendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getLastPrice() < f2.getLastPrice() ? -1 : f1.getLastPrice() == f2.getLastPrice() ? 0 : 1;
        }
    };

    public static Comparator LastPriceDescendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getLastPrice() < f2.getLastPrice() ? 1 : f1.getLastPrice() == f2.getLastPrice() ? 0 : -1;
        }
    };

    public static Comparator ChangeAscendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getChange() < f2.getChange() ? -1 : f1.getChange() == f2.getChange() ? 0 : 1;
        }
    };

    public static Comparator ChangeDescendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getChange() < f2.getChange() ? 1 : f1.getChange() == f2.getChange() ? 0 : -1;
        }
    };

    public static Comparator ChangePercentAscendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getChangePercent() < f2.getChangePercent() ? -1 : f1.getChangePercent() == f2.getChangePercent() ? 0 : 1;
        }
    };

    public static Comparator ChangePercentDescendingComparator= new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getChangePercent() < f2.getChangePercent() ? 1 : f1.getChangePercent() == f2.getChangePercent() ? 0 : -1;
        }
    };


    public Favorite(String symbol, float lastPrice, float change, float changePercent) {
        Symbol = symbol;
        LastPrice = lastPrice;
        Change = change;
        ChangePercent = changePercent;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public float getLastPrice() {
        return LastPrice;
    }

    public void setLastPrice(float lastPrice) {
        LastPrice = lastPrice;
    }

    public float getChange() {
        return Change;
    }

    public void setChange(float change) {
        Change = change;
    }

    public float getChangePercent() {
        return ChangePercent;
    }

    public void setChangePercent(float changePercent) {
        ChangePercent = changePercent;
    }
}
