package com.jcrawley.webradio.service;

import com.jcrawley.webradio.repository.StationEntity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UrlChecker {

    private final static int OK_RESPONSE = 200;
    private final static String GET = "GET";
    private final static int CONNECT_TIMEOUT = 2500;
    private final static int READ_TIMEOUT = 1000;


    public static boolean isCurrentUrlReachable(String urlStr){
        try {
            HttpURLConnection connection = createConnectionTo(urlStr);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            if(responseCode == OK_RESPONSE){
                //printHeaders(connection);
                return true;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;

    }


    public static StationEntity getMetadata(String urlStr) {
        try {
            HttpURLConnection connection = createConnectionTo(urlStr);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            if(responseCode == OK_RESPONSE){
                return buildStationEntityFromHeaders(connection);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }


    private static StationEntity buildStationEntityFromHeaders(HttpURLConnection connection){
    Map<String, List<String>> headers = connection.getHeaderFields();
    return StationEntity.Builder.newInstance()
            .name(getFirstHeaderFor("icy-name", headers))
            .link(getFirstHeaderFor("icy-url", headers))
            .build();
    }


    private static HttpURLConnection createConnectionTo(String urlStr) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(GET);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        return connection;
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


    private static String getFirstHeaderFor(String key, Map<String, List<String>> headers){
        List<String> header = headers.getOrDefault(key, Collections.singletonList(""));
        return header != null ? header.get(0) : "";
    }

    private static void log(String msg){
        System.out.println("%%%% UrlChecker: " + msg);
    }
}
