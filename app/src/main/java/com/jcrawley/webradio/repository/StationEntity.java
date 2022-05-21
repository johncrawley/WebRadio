package com.jcrawley.webradio.repository;

public class StationEntity {
    private String name;
    private String url;
    private final Long id;

    public StationEntity(String name, String url){
        this(-1L, name, url);
    }

    public StationEntity(Long id, String name, String url){
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getName(){
        return name;
    }

    public String getUrl(){
        return url;
    }

    public Long getId(){
        return this.id;
    }

    public void setName(String name){
        this.name = name;}

    public void setUrl(String url){
        this.url = url;
    }
}
