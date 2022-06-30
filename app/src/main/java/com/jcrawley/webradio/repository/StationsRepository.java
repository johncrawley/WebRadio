package com.jcrawley.webradio.repository;

import java.util.List;

public interface StationsRepository {

    void createStation(StationEntity stationEntity);
    List<StationEntity> getAllForStationsList();
    List<StationEntity> getAllLibrary();
    List<StationEntity> getFromLibraryWithGenre(String genre);
    void delete(long id);
    void update(StationEntity stationEntity);
    void setAsFavourite(StationEntity stationEntity, boolean isFavourite);
    List<String> getAllGenres();



}
