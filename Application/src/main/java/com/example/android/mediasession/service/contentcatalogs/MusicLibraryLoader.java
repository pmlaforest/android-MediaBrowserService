package com.example.android.mediasession.service.contentcatalogs;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import android.media.MediaScannerConnection;

import com.example.android.R;

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
                    chanson.get(MusicDatabase.KEY_URI_STRING),
                    chanson.get(MusicDatabase.KEY_TITRE),
                    chanson.get(MusicDatabase.KEY_ARTIST),
                    chanson.get(MusicDatabase.KEY_ALBUM),
                    chanson.get(MusicDatabase.KEY_GENRE),
                    Long.valueOf(chanson.get(MusicDatabase.KEY_DURATION)),
                    TimeUnit.valueOf(chanson.get(MusicDatabase.KEY_DURATION_UNIT)),
                    chanson.get(MusicDatabase.KEY_MUSIC_FILENAME),
                    Integer.valueOf(chanson.get(MusicDatabase.KEY_ALBUM_ART_RES_ID)),
                    chanson.get(MusicDatabase.KEY_ALBUM_ART_RES_NAME)
            );
        }
    }

    public static void loadFromNewFile(Context context, File file) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{file.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                        mmr.setDataSource(path);
                        MusicLibrary.createMediaMetadataCompat(
                                uri.toString(),
                                mmr.extractMetadata(mmr.METADATA_KEY_TITLE),
                                mmr.extractMetadata(mmr.METADATA_KEY_ARTIST),
                                mmr.extractMetadata(mmr.METADATA_KEY_ALBUM),
                                mmr.extractMetadata(mmr.METADATA_KEY_GENRE),
                                TimeUnit.MILLISECONDS.toSeconds(
                                        Long.parseLong(
                                                mmr.extractMetadata(mmr.METADATA_KEY_DURATION)
                                        )
                                ),
                                TimeUnit.SECONDS,
                                path,
                                R.drawable.album_jazz_blues,
                                "album_jazz_blues"
                        );
                        Log.v("MUSICLIBRARYLOADER",
                                "file " + path + " was scanned seccessfully: " + uri);
                    }
                });


    }

    public static void loadFromNewFile(Context context, ArrayList<File> files) {
        ArrayList<String> paths = new ArrayList<>();
        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }
        MediaScannerConnection.scanFile(
                context,
                paths.toArray(new String[0]),
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                        mmr.setDataSource(path);
                        MusicLibrary.createMediaMetadataCompat(
                                uri.toString(),
                                mmr.extractMetadata(mmr.METADATA_KEY_TITLE),
                                mmr.extractMetadata(mmr.METADATA_KEY_ARTIST),
                                mmr.extractMetadata(mmr.METADATA_KEY_ALBUM),
                                mmr.extractMetadata(mmr.METADATA_KEY_GENRE),
                                TimeUnit.MILLISECONDS.toSeconds(
                                        Long.parseLong(
                                                mmr.extractMetadata(mmr.METADATA_KEY_DURATION)
                                        )
                                ),
                                TimeUnit.SECONDS,
                                path,
                                R.drawable.album_jazz_blues,
                                "album_jazz_blues"
                        );
                        Log.v("MUSICLIBRARYLOADER",
                                "file " + path + " was scanned seccessfully: " + uri);
                    }
                });
    }

}
