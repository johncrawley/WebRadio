package com.jcrawley.webradio.repository;

import java.util.ArrayList;
import java.util.List;

public class InitialStationsLoader {

    static List<StationEntity> get(){
        List<StationEntity> stations = new ArrayList<>();
        add(stations, "ROCK FM!!!", "http://www.my_rock_example_website.com:9876", "http://www.my_rock.org.com");
        add(stations, "Jazzy FM!!!", "http://www.jazzy_example_website.com:9876", "http://www.my_jazz.org.com,", "Jazz");
        add(stations, "Metal FM!!!", "http://www.metal1_example_website.com:9876", "http://www.my_metalzz.org.com,", "Metal");
        add(stations, "Country FM!!!", "http://www.country1_example_website.com:9876", "http://www.my_countryzz.org.com,", "Country");
        add(stations, "Metal Forever!!!!!!", "http://www.metal2_example_website.com:9876", "http://www.my_metalzz2.org.com,", "Metal");
        return stations;
    }


    static void add(List<StationEntity> stations, String name, String url, String link){
        StationEntity stationEntity = StationEntity.Builder.newInstance()
                .name(name)
                .url(url)
                .link(link)
                .setFavourite(false)
                .build();
        stations.add(stationEntity);
    }


    static void add(List<StationEntity> stations, String name, String url, String link, String genre){
        StationEntity stationEntity = StationEntity.Builder.newInstance()
                .name(name)
                .url(url)
                .link(link)
                .genre(genre)
                .build();
        stations.add(stationEntity);
    }
}
