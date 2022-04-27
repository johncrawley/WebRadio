package com.jcrawley.webradio.repository;

public class StationEntity {
    private final String name;
    private final String url;
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
}
