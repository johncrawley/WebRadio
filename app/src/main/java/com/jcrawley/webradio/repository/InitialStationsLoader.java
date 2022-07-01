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
import java.util.ArrayList;
import java.util.List;

public class InitialStationsLoader {

    private final SQLiteDatabase db;
    private final SharedPreferences sharedPreferences;
    private final Context context;

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
        parseLines();
    }


    private void parseLines(){
        InputStream is = context.getResources().openRawResource(R.raw.default_stations);

        Reader reader = new InputStreamReader(is);
        try(BufferedReader bufferedReader = new BufferedReader(reader)){
            String line = bufferedReader.readLine();
            while(line != null){
                if(line.startsWith("#")){
                    continue;
                }
                DbUtils.createStation(db, parseLine(line));
                line = bufferedReader.readLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private static StationEntity parseLine(String line){
        String[] strArray = line.split(",");
        return StationEntity.Builder.newInstance()
                .name(strArray[0])
                .url(strArray[1])
                .link(strArray[2])
                .genre(strArray[3])
                .setFavourite(false)
                .build();
    }
}
