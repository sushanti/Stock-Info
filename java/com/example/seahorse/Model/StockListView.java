package com.example.seahorse.Model;

/**
 * Created by seahorse on 11/22/2017.
 */

public class StockListView {
    public String name;
    public String value;

    public StockListView(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

