package com.jcrawley.webradio.repository;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbUtils {

    static long addValuesToTable(SQLiteDatabase db, String tableName, ContentValues contentValues){
        db.beginTransaction();
        long id = -1;
        try {
            id = db.insertOrThrow(tableName, null, contentValues);
            db.setTransactionSuccessful();
        }catch(SQLException e){
            e.printStackTrace();
        }
        db.endTransaction();
        return id;
    }


    static void createStation(SQLiteDatabase db, StationEntity stationEntity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.StationsEntry.COL_STATION_NAME, stationEntity.getName());
        contentValues.put(DbContract.StationsEntry.COL_URL, stationEntity.getUrl());
        contentValues.put(DbContract.StationsEntry.COL_LINK, stationEntity.getLink());
        contentValues.put(DbContract.StationsEntry.COL_DESCRIPTION, stationEntity.getDescription());
        addValuesToTable(db, DbContract.StationsEntry.TABLE_NAME, contentValues);
    }

    static void createGenre(SQLiteDatabase db, StationEntity stationEntity){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.StationsEntry.COL_STATION_NAME, stationEntity.getGenre() );
        addValuesToTable(db, DbContract.GenresEntry.TABLE_NAME, contentValues);
    }
}
