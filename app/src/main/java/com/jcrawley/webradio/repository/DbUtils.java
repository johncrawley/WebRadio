package com.jcrawley.webradio.repository;

import android.content.ContentValues;
import android.database.Cursor;
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
        contentValues.put(DbContract.StationsEntry.IS_FAVOURITE, stationEntity.isFavourite() ? 1 : 0);
        contentValues.put(DbContract.StationsEntry.TIME_FAVOURITE_ENABLED, System.currentTimeMillis());
        long stationId = DbUtils.addValuesToTable(db, DbContract.StationsEntry.TABLE_NAME, contentValues);
        addGenreData(db, stationEntity, stationId);
    }


    static void addGenreData(SQLiteDatabase db, StationEntity stationEntity, long stationId){
        if(stationEntity.getGenre() == null || stationId == -1){
            return;
        }
        String genreName = stationEntity.getGenre();
        long existingGenreId = getGenreId(db, stationEntity.getGenre());
        long genreId = existingGenreId != -1 ? existingGenreId : createGenre(db, genreName);
        addStationGenreData(db, stationId, genreId);
    }


    static void addStationGenreData(SQLiteDatabase db, long stationId, long genreId){
        if(genreId == -1){
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.StationsGenresEntry.COL_STATION_ID, stationId);
        contentValues.put(DbContract.StationsGenresEntry.COL_GENRE_ID, genreId);
        DbUtils.addValuesToTable(db, DbContract.StationsGenresEntry.TABLE_NAME, contentValues);
    }


    static long createGenre(SQLiteDatabase db, String genreName){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.GenresEntry.COL_GENRE_NAME, genreName);
        return DbUtils.addValuesToTable(db, DbContract.GenresEntry.TABLE_NAME, contentValues);
    }


    static long getGenreId(SQLiteDatabase db, String genreName){
            long id = -1;
            String query = "SELECT " + DbContract.GenresEntry._ID
                    + " FROM " + DbContract.GenresEntry.TABLE_NAME
                    + " WHERE " + DbContract.GenresEntry.COL_GENRE_NAME
                    + " = '" + genreName + "';";
            Cursor cursor;
            try {
                cursor = db.rawQuery(query, null);
                while(cursor.moveToNext()){
                   id = getLong(cursor, DbContract.GenresEntry._ID);
                }
            }
            catch(SQLException e){
                e.printStackTrace();
                return id;
            }
            cursor.close();
            return id;
    }


    private  static long getLong(Cursor cursor, String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }
}
