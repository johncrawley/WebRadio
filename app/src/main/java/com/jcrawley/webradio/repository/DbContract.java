package com.jcrawley.webradio.repository;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract() {
    }

    static class StationsEntry implements BaseColumns {
        static final String TABLE_NAME = "Stations";
        static final String COL_STATION_NAME = "name";
        static final String COL_URL = "url";
        static final String COL_LINK = "link";
        static final String COL_DESCRIPTION = "description";
        static final String IS_FAVOURITE = "is_library_entry";
        static final String TIME_FAVOURITE_ENABLED = "time_favourite_enabled";
    }


    static class GenresEntry implements BaseColumns {
        static final String TABLE_NAME = "Genres";
        static final String COL_GENRE_NAME = "genre";
    }


    static class StationsGenresEntry implements BaseColumns {
        static final String TABLE_NAME = "StationGenre";
        static final String COL_STATION_ID = "station_id";
        static final String COL_GENRE_ID = "genre_id";
    }
}