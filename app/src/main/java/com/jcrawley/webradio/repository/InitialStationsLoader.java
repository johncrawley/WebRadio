package com.jcrawley.webradio.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.jcrawley.webradio.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InitialStationsLoader {

    private final SQLiteDatabase db;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private String genre;

    public InitialStationsLoader(Context context, SharedPreferences sharedPreferences){
        this.context = context;
        DbHelper dbHelper = DbHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
        this.sharedPreferences = sharedPreferences;
    }


    public void load(){
        String isFirstRunKey = "isFirstRun";
        if(sharedPreferences.getBoolean(isFirstRunKey, false)){
            return;
        }
        sharedPreferences.edit().putBoolean(isFirstRunKey, true).apply();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this::parseLines);
    }


    private void parseLines(){
        try(InputStream is = context.getResources().openRawResource(R.raw.default_stations);
            Reader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader)){
            String line = bufferedReader.readLine();
            while(line != null){
                if(line.startsWith("#")){
                    genre = line.substring(1);
                }
                else {
                    DbUtils.createStation(db, parseLine(line));
                }
                line = bufferedReader.readLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private StationEntity parseLine(String line){
        String[] strArray = line.split(",");
        StationEntity.Builder stationEntity = StationEntity.Builder.newInstance()
                .name(strArray[1])
                .url(strArray[0])
                .genre(genre)
                .setFavourite(false);

                if(strArray.length==3){
                    stationEntity.link(strArray[2]);
                }
                return stationEntity.build();
    }
}
