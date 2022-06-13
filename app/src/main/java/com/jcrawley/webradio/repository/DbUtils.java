package com.jcrawley.webradio.repository;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbUtils {

    static void addValuesToTable(SQLiteDatabase db, ContentValues contentValues){
        db.beginTransaction();
        try {
            db.insertOrThrow(DbContract.StationsEntry.TABLE_NAME, null, contentValues);
            db.setTransactionSuccessful();
        }catch(SQLException e){
            e.printStackTrace();
        }
        db.endTransaction();
    }


    static void createStation(SQLiteDatabase db, StationEntity stationEntity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.StationsEntry.COL_STATION_NAME, stationEntity.getName());
        contentValues.put(DbContract.StationsEntry.COL_URL, stationEntity.getUrl());
        contentValues.put(DbContract.StationsEntry.COL_LINK, stationEntity.getLink());
        contentValues.put(DbContract.StationsEntry.COL_DESCRIPTION, stationEntity.getDescription());
        addValuesToTable(db, contentValues);
    }
}