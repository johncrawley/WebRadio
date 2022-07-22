package com.jcrawley.webradio.service;

import android.media.MediaMetadataRetriever;

import java.util.HashMap;

public class MetadataHandler {


    private HashMap<String, String> metadataMap;
    private MediaMetadataRetriever metaRetriever;
    private int metadataCounter = 0;

    void initMetaDataRetriever(String currentUrl){
        metadataMap = new HashMap<>();
        metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(currentUrl, metadataMap);
    }

    void updateMetadata(){
        int metadataRetrievalInterval = 1;
        metadataCounter++;
        for(String key : metadataMap.keySet()){
            System.out.println("metadata: " + key + " : "  + metadataMap.get(key));
        }

        if(metadataCounter >= metadataRetrievalInterval){
            metadataCounter = 0;
            String artist =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String albumArtist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
            String composer = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
            String author = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            String bitrate = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

            System.out.println("Artist: " + artist
                    + " title: " + title
                    + " albumArtist: " + albumArtist
                    + " composer: " + composer
                    + " author: " +  author
                    + " bitrate: " + bitrate);
        }
    }

}
