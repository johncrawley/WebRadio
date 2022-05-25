package com.jcrawley.webradio.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import com.jcrawley.webradio.repository.DbContract.StationsEntry;

public class StationsRepositoryImpl implements StationsRepository{


    private final SQLiteDatabase db;


    public StationsRepositoryImpl(Context context){
        DbHelper dbHelper = DbHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
    }


    @Override
    public void createStation(StationEntity stationEntity) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(StationsEntry.COL_STATION_NAME, stationEntity.getName());
            contentValues.put(StationsEntry.COL_URL, stationEntity.getUrl());
            addValuesToTable(db, contentValues);
    }

    @Override
    public void update(StationEntity stationEntity){
        System.out.println("^^^ StationsRepositoryImpl: entered update()");
        ContentValues contentValues = new ContentValues();
        contentValues.put(StationsEntry.COL_STATION_NAME, stationEntity.getName());
        contentValues.put(StationsEntry.COL_URL, stationEntity.getUrl());

        db.update(StationsEntry.TABLE_NAME,
                contentValues,
                "_id=?",
                new String[]{String.valueOf(stationEntity.getId())});
    }


    @Override
    public List<StationEntity> getAll() {
       List<StationEntity> list = new ArrayList<>();
        Cursor cursor;
        String query = "SELECT * FROM " + StationsEntry.TABLE_NAME;

        try {
            cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()){
                String name = getString(cursor, StationsEntry.COL_STATION_NAME);
                String url = getString(cursor, StationsEntry.COL_URL);
                long id = getLong(cursor, StationsEntry._ID);
                list.add(new StationEntity(id, name, url));
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return list;
        }
        cursor.close();
        return list;
    }


    @Override
    public void delete(long id) {
        try {
            db.delete(StationsEntry.TABLE_NAME,
                    "_id=?",
                    new String[]{String.valueOf(id)});
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }



    static void addValuesToTable(SQLiteDatabase db, ContentValues contentValues){
        db.beginTransaction();
        try {
            db.insertOrThrow(StationsEntry.TABLE_NAME, null, contentValues);
            db.setTransactionSuccessful();
        }catch(SQLException e){
            e.printStackTrace();
        }
        db.endTransaction();
    }


    private String getString(Cursor cursor, String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    private long getLong(Cursor cursor, String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }

}
