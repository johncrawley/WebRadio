package com.jcrawley.webradio.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class UrlChecker {

    static boolean isCurrentUrlReachable(String urlStr){
        URL url;
        try {
            url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(1000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            if(responseCode == 200){
                printHeaders(connection);
                return true;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    private static void printHeaders(HttpURLConnection connection){
        Map<String, List<String>> headers = connection.getHeaderFields();
        log("^^^^^^^^^^^^^^ HEADERS ^^^^^^^^^^^^^^^ ");
        for( String key : headers.keySet()){
            List<String> header = headers.get(key);
            if(header == null || header.isEmpty()){
                continue;
            }
            for(String headerStr: header){
                log("^^^ Header: " + key + " : " + headerStr);
            }
        }
    }

    private static void log(String msg){
        System.out.println("%%%% UrlChecker: " + msg);
    }
}
