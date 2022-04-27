package com.jcrawley.webradio.repository;

import java.util.List;

public interface StationsRepository {

    void createStation(StationEntity stationEntity);
    List<StationEntity> getAll();
    void delete(long id);


}
