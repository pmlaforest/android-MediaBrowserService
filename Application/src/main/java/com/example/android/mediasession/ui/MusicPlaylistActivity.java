package com.example.android.mediasession.ui;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.mediasession.R;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicPlaylistActivity extends AppCompatActivity {

    private static final String MUSIC_FOLDER_NAME = "streamingapp_music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_playlist);

        createMusicFolder(MUSIC_FOLDER_NAME);
        initialiseMusicLibrary();

        List<MediaBrowserCompat.MediaItem> mediaItems = MusicLibrary.getMediaItems();
        for(MediaBrowserCompat.MediaItem item : mediaItems) {
            MediaDescriptionCompat desc = item.getDescription();
            Log.d("title", desc.getTitle().toString());
            Log.d("subtitle", desc.getSubtitle().toString());
            Log.d("description", desc.getDescription().toString());
        }
    }

    private void createTrackEntry(MediaBrowserCompat.MediaItem item) {

    }

    private void copySampleToMusicFolder(File sampleFile, String assetsFilename) {
        try {
            sampleFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException se) {
            Log.d("user permission:",  "file creation denied");
        }

        AssetFileDescriptor sampleFileFd = null;
        try {

            sampleFileFd = this.getAssets().openFd(assetsFilename);
            try (InputStream in = sampleFileFd.createInputStream()) {
                try (OutputStream out = new FileOutputStream(sampleFile.getAbsolutePath())) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createMusicFolder(String folderName) {
        String musicFolderPath = Environment.getExternalStorageDirectory() + File.separator + folderName;

        File folder = new File(musicFolderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        String jazzInParisFullpath = musicFolderPath + File.separator + "jazz_in_paris.mp3";
        String theColdestShoulderFullpath = musicFolderPath + File.separator + "the_coldest_shoulder.mp3";

        File jazzInParisFile = new File(jazzInParisFullpath);
        File theColdestShoulderFile = new File(theColdestShoulderFullpath);

        if (!jazzInParisFile.exists()) {
            copySampleToMusicFolder(jazzInParisFile, "jazz_in_paris.mp3");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(jazzInParisFile)));
        }
        if (!theColdestShoulderFile.exists()) {
            copySampleToMusicFolder(theColdestShoulderFile, "the_coldest_shoulder.mp3");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(theColdestShoulderFile)));
        }

        return musicFolderPath;
    }

    /**
     * Récupère les musiques de l'appareil et les ajoute à la MusicLibrary.
     */
    private void initialiseMusicLibrary() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        ContentResolver cResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = cResolver.query(uri, null, selection, null, null);
        if (cursor == null) {
            throw new RuntimeException("cannot access MediaStore");
        } else if (!cursor.moveToFirst()) {
            // no medias
        } else {
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            //int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            //int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            //int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            //int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            do {
                long thisId = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId);
                mmr.setDataSource(this, contentUri);

                MusicLibrary.createMediaMetadataCompat(
                        contentUri.toString(),
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
                        contentUri.toString(),
                        R.drawable.album_jazz_blues,
                        "album_jazz_blues"
                );

            } while (cursor.moveToNext());
        }
    }
}
