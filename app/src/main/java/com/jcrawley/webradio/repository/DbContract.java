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
    }
}