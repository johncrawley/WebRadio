package com.jcrawley.webradio.repository;

public class StationEntity {
    private String name, url, link, description;
    private final Long id;

    public StationEntity(String name, String url){
        this(-1L, name, url);
    }


    public StationEntity(Long id, String name, String url){
        this.id = id;
        this.name = name;
        this.url = url;
    }


    public StationEntity(Builder builder){
        this.id = builder.id;
        this.name = builder.name;
        this.url = builder.url;
        this.link = builder.link;
        this.description = builder.description;
    }

    public String getName(){
        return name;
    }

    public String getUrl(){
        return url;
    }

    public String getLink(){ return link;}

    public String getDescription(){ return description;}

    public Long getId(){
        return id;
    }

    public void setName(String name){
        this.name = name;}

    public void setLink(String link){
        this.link = link;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public static class Builder{
        private String name, url, link, description;
        private long id;

        public static Builder newInstance(){
            return new Builder();
        }

        private Builder(){
        }

        public Builder id(long id){
            this.id = id;
            return this;
        }

        public Builder description(String description){
            this.description = description;
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder url(String url){
            this.url = url;
            return this;
        }

        public Builder link(String link){
            this.link = link;
            return this;
        }

        public StationEntity build(){
            return new StationEntity(this);
        }
    }
}
