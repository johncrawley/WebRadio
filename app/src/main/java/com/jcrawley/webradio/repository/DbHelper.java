package com.jcrawley.webradio.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.jcrawley.webradio.repository.DbContract.StationsEntry;
import static com.jcrawley.webradio.repository.DbContract.GenresEntry;
import static com.jcrawley.webradio.repository.DbContract.StationsGenresEntry;


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
                    + StationsEntry.COL_DESCRIPTION + TEXT + COMMA
                    + StationsEntry.IS_FAVOURITE + INTEGER + COMMA
                    + StationsEntry.TIME_FAVOURITE_ENABLED + INTEGER
                    + CLOSING_BRACKET;

    private static final String SQL_CREATE_GENRES_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + GenresEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + GenresEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + GenresEntry.COL_GENRE_NAME + TEXT
                    + CLOSING_BRACKET;

    private static final String SQL_CREATE_STATIONS_GENRES_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + StationsGenresEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + StationsGenresEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + StationsGenresEntry.COL_STATION_ID + INTEGER + COMMA
                    + StationsGenresEntry.COL_GENRE_ID + INTEGER
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
        db.execSQL(SQL_CREATE_GENRES_TABLE);
        db.execSQL(SQL_CREATE_STATIONS_GENRES_TABLE);
    }



    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
