package com.example.android.mediasession.service.contentcatalogs;

import android.database.Cursor;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_ALBUM;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_ALBUM_ART_RES_ID;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_ALBUM_ART_RES_NAME;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_ARTIST;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_DURATION;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_DURATION_UNIT;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_GENRE;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_MUSIC_FILENAME;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_TITRE;
import static com.example.android.mediasession.service.contentcatalogs.MusicDatabase.KEY_URI_STRING;

public class MusicLibraryLoader {

    /*public static void loadFromDB(MusicDatabase dbMusic) {
        try (Cursor chansonPresente = dbMusic.getAllTracks()){
            if (chansonPresente == null) {
                Log.i("MUSICLIBRARYLOADER", "db null cursor, no music was loaded");
                return;
            }
            do {
                MusicLibrary.createMediaMetadataCompat(
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_URI_STRING)),
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_TITRE)),
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_ARTIST)),
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_ALBUM)),
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_GENRE)),
                    chansonPresente.getLong(chansonPresente.getColumnIndex(KEY_DURATION)),
                    TimeUnit.valueOf(chansonPresente.getString(chansonPresente.getColumnIndex(KEY_DURATION_UNIT))),
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_MUSIC_FILENAME)),
                    chansonPresente.getInt(chansonPresente.getColumnIndex(KEY_ALBUM_ART_RES_ID)),
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_ALBUM_ART_RES_NAME))
                );
            } while (chansonPresente.moveToNext());
        }
    }*/

    public static void loadFromDB(MusicDatabase dbMusic) {
        for (HashMap<String,String>chanson : dbMusic.getTrackList()) {
            MusicLibrary.createMediaMetadataCompat(
                    chanson.get(KEY_URI_STRING),
                    chanson.get(KEY_TITRE),
                    chanson.get(KEY_ARTIST),
                    chanson.get(KEY_ALBUM),
                    chanson.get(KEY_GENRE),
                    Long.valueOf(chanson.get(KEY_DURATION)),
                    TimeUnit.valueOf(chanson.get(KEY_DURATION_UNIT)),
                    chanson.get(KEY_MUSIC_FILENAME),
                    Integer.valueOf(chanson.get(KEY_ALBUM_ART_RES_ID)),
                    chanson.get(KEY_ALBUM_ART_RES_NAME)
            );
        }
    }

}
