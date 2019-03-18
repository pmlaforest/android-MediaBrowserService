/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.mediasession.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.mediasession.R;
import com.example.android.mediasession.client.MediaBrowserHelper;
import com.example.android.mediasession.service.MusicService;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageView mAlbumArt;
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private ImageView mMediaControlsImage;
    private MediaSeekBar mSeekBarAudio;

    private MediaBrowserHelper mMediaBrowserHelper;

    private boolean mIsPlaying;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkUserPermission();

        mTitleTextView = findViewById(R.id.song_title);
        mArtistTextView = findViewById(R.id.song_artist);
        mAlbumArt = findViewById(R.id.album_art);
        mMediaControlsImage = findViewById(R.id.media_controls);
        mSeekBarAudio = findViewById(R.id.seekbar_audio);

        final ClickListener clickListener = new ClickListener();
        findViewById(R.id.button_previous).setOnClickListener(clickListener);
        findViewById(R.id.button_play).setOnClickListener(clickListener);
        findViewById(R.id.button_next).setOnClickListener(clickListener);

        createMusicFolder();
        initialiseMusicLibrary();

        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
    }

    /**
     * Vérifie les permissions nécessaire pour l'accès aux médias 
     * (nécessaire pour API 22+) et les demandes à l'utilisateur au besoin.
     */
    private void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return;
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return;
            }
        }

    }
    
    /**
     * Récupère les musiques de l'appareille et les ajoute à la MusicLibrary.
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

    private String createMusicFolder() {
        String musicFolderPath = Environment.getExternalStorageDirectory() + File.separator + "streamingapp_music";

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

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSeekBarAudio.disconnectController();
        mMediaBrowserHelper.onStop();
    }

    /**
     * Convenience class to collect the click listeners together.
     * <p>
     * In a larger app it's better to split the listeners out or to use your favorite
     * library.
     */
    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_previous:
                    mMediaBrowserHelper.getTransportControls().skipToPrevious();
                    break;
                case R.id.button_play:
                    if (mIsPlaying) {
                        mMediaBrowserHelper.getTransportControls().pause();
                    } else {
                        mMediaBrowserHelper.getTransportControls().play();
                    }
                    break;
                case R.id.button_next:
                    mMediaBrowserHelper.getTransportControls().skipToNext();
                    break;
            }
        }
    }

    /**
     * Customize the connection to our {@link android.support.v4.media.MediaBrowserServiceCompat}
     * and implement our app specific desires.
     */
    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, MusicService.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            mSeekBarAudio.setMediaController(mediaController);
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            final MediaControllerCompat mediaController = getMediaController();

            // Queue up all media items for this simple sample.
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                mediaController.addQueueItem(mediaItem.getDescription());
            }

            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
        }
    }

    /**
     * Implementation of the {@link MediaControllerCompat.Callback} methods we're interested in.
     * <p>
     * Here would also be where one could override
     * {@code onQueueChanged(List<MediaSessionCompat.QueueItem> queue)} to get informed when items
     * are added or removed from the queue. We don't do this here in order to keep the UI
     * simple.
     */
    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
            mMediaControlsImage.setPressed(mIsPlaying);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
            mTitleTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mArtistTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mAlbumArt.setImageBitmap(MusicLibrary.getAlbumBitmap(
                    MainActivity.this,
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    }
}
