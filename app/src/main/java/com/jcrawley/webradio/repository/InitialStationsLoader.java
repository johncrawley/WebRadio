package com.jcrawley.webradio.repository;

import java.util.ArrayList;
import java.util.List;

public class InitialStationsLoader {

    static List<StationEntity> get(){
        List<StationEntity> stations = new ArrayList<>();
        add(stations, "ROCK FM!!!", "http://www.my_rock_example_website.com:9876", "http://www.my_rock.org.com");
        return stations;
    }


    static void add(List<StationEntity> stations, String name, String url, String link){
        StationEntity stationEntity = StationEntity.Builder.newInstance()
                .name(name)
                .url(url)
                .link(link)
                .build();
        stations.add(stationEntity);
    }
}
