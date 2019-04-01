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

import java.util.List;

import com.example.android.R;
import com.example.android.mediasession.client.MediaBrowserHelper;
import com.example.android.mediasession.service.MusicService;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;
import com.example.android.wifip2p.WiFiDirectActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.mediasession.R;
import com.example.android.mediasession.client.MediaBrowserHelper;
import com.example.android.mediasession.service.MusicService;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView mAlbumArt;
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private ImageView mMediaControlsImage;
    private MediaSeekBar mSeekBarAudio;
    private MediaBrowserHelper mMediaBrowserHelper;
    private boolean mIsPlaying;
    private String trackIdToPlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTitleTextView = findViewById(R.id.song_title);
        mArtistTextView = findViewById(R.id.song_artist);
        mAlbumArt = findViewById(R.id.album_art);
        mMediaControlsImage = findViewById(R.id.media_controls);
        mSeekBarAudio = findViewById(R.id.seekbar_audio);

        final ClickListener clickListener = new ClickListener();
        findViewById(R.id.button_previous).setOnClickListener(clickListener);
        findViewById(R.id.button_play).setOnClickListener(clickListener);
        findViewById(R.id.button_next).setOnClickListener(clickListener);

        setFooterElementsOnClickListener();

        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());

        trackIdToPlay = null;
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            trackIdToPlay = savedInstanceState.getString("trackIdToPlay");
        }

        if (intent != null) {
            trackIdToPlay = intent.getStringExtra("mediaId");
        }


    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("trackIdToPlay", trackIdToPlay);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }catch (Exception e){
            Log.e("JavaInfo","Error MainActivity_onCreateOptionsMenu(): " + e);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shortcut_wifip2p:
                startActivity(new Intent(MainActivity.this, WiFiDirectActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFooterElementsOnClickListener() {

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch(view.getId()){
                    case R.id.download_button:
                        startActivity(new Intent(MainActivity.this, WiFiDirectActivity.class));
                        break;
                    case R.id.download_textView:
                        startActivity(new Intent(MainActivity.this, WiFiDirectActivity.class));
                        break;
                    case R.id.parameters_button:
                    case R.id.parameters_textView:
                        break;
                    case R.id.playlist_button:
                        startActivity(new Intent(MainActivity.this, MusicPlaylistActivity.class));
                        break;
                    case R.id.playlist_textView:
                        startActivity(new Intent(MainActivity.this, MusicPlaylistActivity.class));
                        break;
                    case R.id.mediaPlayer_button:
                    case R.id.mediaPlayer_textView:
                    default:
                        break;
                }
            }
        };

        TextView downloadTrackTextView = findViewById(R.id.download_textView);
        ImageButton downloadTrackImageButton = findViewById(R.id.download_button);
        downloadTrackTextView.setOnClickListener(listener);
        downloadTrackImageButton.setOnClickListener(listener);

        TextView playlistTextView = findViewById(R.id.playlist_textView);
        ImageButton playlistImageButton = findViewById(R.id.playlist_button);
        playlistTextView.setOnClickListener(listener);
        playlistImageButton.setOnClickListener(listener);

        TextView parametersTextView = findViewById(R.id.parameters_textView);
        ImageButton parametersImageButton = findViewById(R.id.parameters_button);
        parametersTextView.setOnClickListener(listener);
        parametersImageButton.setOnClickListener(listener);

        TextView mediaPlayerTextView = findViewById(R.id.mediaPlayer_textView);
        ImageButton mediaPlayerImageButton = findViewById(R.id.mediaPlayer_button);
        mediaPlayerTextView.setOnClickListener(listener);
        mediaPlayerImageButton.setOnClickListener(listener);
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

            if (mIsPlaying) {
                if (trackIdToPlay != null) {
                    mMediaBrowserHelper.getTransportControls().stop();
                }
                else {
                    MediaMetadataCompat metadata = mediaController.getMetadata();
                    trackIdToPlay = metadata.getDescription().getMediaId();
                }
                mediaController.getTransportControls().prepareFromMediaId(trackIdToPlay,null);
            }
            else {
                if (trackIdToPlay != null) {

                    if (getMediaController().getPlaybackState() == null) {
                        // Queue up all media items for this simple sample.
                        for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                            mediaController.addQueueItem(mediaItem.getDescription());
                        }
                    }
                    mediaController.getTransportControls().prepareFromMediaId(trackIdToPlay,null);
                }
                else {
                    mediaController.getTransportControls().prepare();
                }
            }
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
