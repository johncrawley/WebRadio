package com.jcrawley.webradio.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import static com.jcrawley.webradio.repository.DbContract.StationsEntry;


public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper instance;
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "WebRadioStations.db";

    private static final String OPENING_BRACKET = " (";
    private static final String CLOSING_BRACKET = " );";
    private static final  String INTEGER = " INTEGER";
    private static final String TEXT = " TEXT";
    private static final String COMMA = ",";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";

    private static final String SQL_CREATE_STATIONS_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + StationsEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + StationsEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + StationsEntry.COL_STATION_NAME + TEXT + COMMA
                    + StationsEntry.COL_URL + TEXT + COMMA
                    + StationsEntry.COL_LINK + TEXT + COMMA
                    + StationsEntry.COL_DESCRIPTION + TEXT
                    + CLOSING_BRACKET;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static DbHelper getInstance(Context context){
        if(instance == null){
            instance = new DbHelper(context);
        }
        return instance;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STATIONS_TABLE);
        addInitialStations(db);
    }


    private void addInitialStations(SQLiteDatabase  db){
        List<StationEntity> stations = InitialStationsLoader.get();
        for(StationEntity station : stations){
            DbUtils.createStation(db, station);
        }
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
