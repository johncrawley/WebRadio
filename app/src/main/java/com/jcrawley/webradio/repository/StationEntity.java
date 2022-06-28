package com.jcrawley.webradio.repository;

public class StationEntity {
    private String name, url, link, description;
    private final Long id;
    private boolean isLibraryEntry;
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
        this.isLibraryEntry = builder.isLibraryEntry;
        this.genre = builder.genre;
    }

    public String getName(){
        return name;
    }


    public String getUrl(){
        return url;
    }


    public String getLink(){ return link;}


    public String getDescription(){ return description;}


    public boolean isLibraryEntry(){
        return this.isLibraryEntry;
    }

    public Long getId(){
        return id;
    }

    public void setName(String name){
        this.name = name;}


    public String getGenre(){
        return genre;
    }

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
        private boolean isLibraryEntry;
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

        public Builder link(String link){
            this.link = link;
            return this;
        }


        public Builder genre(String genre){
            this.genre = genre;
            return this;
        }


        public Builder setAsLibraryEntry(boolean isLibraryEntry){
            this.isLibraryEntry = isLibraryEntry;
            return this;
        }

        public StationEntity build(){
            return new StationEntity(this);
        }
    }
}
