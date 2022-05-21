package com.jcrawley.webradio.list;

public class ListItem {

    private String name;
    private long id;

    public ListItem(String name, long id){
        this.name = name;
        this.id = id;
    }

    public String getName(){
        return this.name;
    }


    public long getId(){
        return this.id;
    }

    public void setName(String name){
        this.name = name;
    }
}