package com.jcrawley.webradio.repository;

public class StationEntity {
    private String name, link, description;
    private final String url;
    private final Long id;
    private boolean isFavourite;
    private long timeFavouriteWasEnabled;
    private String genre;


    public StationEntity(String url){
        this(-1L, url);
    }


    public StationEntity(String name, String url){
        this(-1L, name, url);
    }


    private StationEntity(Long id, String url){
        this.id = id;
        this.url = url;
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
        this.isFavourite = builder.isFavourite;
        this.genre = builder.genre;
        this.timeFavouriteWasEnabled = builder.timeFavouriteWasEnabled;
    }

    public String getName(){
        return name;
    }


    public String getUrl(){
        return url;
    }


    public String getLink(){ return link;}


    public long getTimeFavouriteWasEnabled(){
        return timeFavouriteWasEnabled;
    }


    public String getDescription(){ return description;}


    public boolean isFavourite(){
        return this.isFavourite;
    }


    public void toggleFavouriteStatus(){
        isFavourite = !isFavourite;
    }


    public Long getId(){
        return id;
    }


    public void setName(String name){
        this.name = name;}


    public String getGenre(){
        return genre;
    }


    public static class Builder{
        private String name, url, link, description;
        private long id;
        private boolean isFavourite;
        private long timeFavouriteWasEnabled;
        private String genre;

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


        public Builder timeFavouriteWasEnabled(long timeFavouriteWasEnabled){
            this.timeFavouriteWasEnabled = timeFavouriteWasEnabled;
            return this;
        }


        public Builder link(String link){
            this.link = link;
            return this;
        }


        public Builder genre(String genre){
            this.genre = genre;
            return this;
        }


        public Builder setFavourite(boolean isFavourite){
            this.isFavourite = isFavourite;
            return this;
        }

        public StationEntity build(){
            return new StationEntity(this);
        }
    }
}
