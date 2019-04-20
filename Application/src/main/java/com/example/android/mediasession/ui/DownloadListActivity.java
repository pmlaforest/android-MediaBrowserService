package com.example.android.mediasession.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.android.R;
import com.example.android.mediasession.service.contentcatalogs.MusicDatabase;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class DownloadListActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download_list);
        createListOfTracks();
    }

    private void createListOfTracks() {

        TableLayout table = findViewById(R.id.table1);

        for (String key : MusicLibrary.keySet()) {

            MediaMetadataCompat mmc = MusicLibrary.getMetadataWithoutBitmap(key);
            String mediaId = mmc.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            if (mediaId == null) {
                continue;
            }
            String title = mmc.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            if (title == null) {
                title = "unknown";
            }
            String artist = mmc.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            if (artist == null) {
                artist = "unknown";
            }
            long duration = mmc.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            if (duration == 0L) {
                //do something?
            }

            TableRow newRow = createTrackEntry(title, artist, (int) duration, mediaId);
            table.addView(newRow);
        }
    }

    private TableRow createTrackEntry(String trackName, String author, int duration, String mediaId) {

        TableRow newRow = new TableRow(this);

        TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);

        newRow.setBackground(getResources().getDrawable(R.drawable.table_row_bg));
        newRow.setOnClickListener(this);
        newRow.setTag(mediaId);

        LinearLayout newLayout = new LinearLayout(this);
        newLayout.setOrientation(LinearLayout.VERTICAL);

        newLayout.setLayoutParams(tlparams);

        TextView trackNameTextView = new TextView(this);
        trackNameTextView.setText(trackName);
        trackNameTextView.setTextSize(16);
        trackNameTextView.setTypeface(null, Typeface.BOLD);

        TextView artistTextView = new TextView(this);
        artistTextView.setText(author);
        artistTextView.setTextSize(16);
        artistTextView.setTypeface(null, Typeface.ITALIC);

        int seconds = (int) (duration / 1000) % 60;
        int minutes = (int) ((duration / (1000 * 60)) % 60);
        int hours = (int) ((duration / (1000 * 60 * 60)));

        String formattedDurationStr = null;
        if (hours == 0) {
            formattedDurationStr = String.format("%d:%02d", minutes, seconds);
        } else {
            formattedDurationStr = String.format("%d:%02d:%02d", hours, minutes, seconds);
        }

        TextView durationTextView = new TextView(this);
        durationTextView.setText(formattedDurationStr);
        durationTextView.setTextSize(16);
        durationTextView.setGravity(Gravity.RIGHT);
        durationTextView.setPadding(0, 0, 5, 0);

        durationTextView.setLayoutParams(tlparams);
        TableRow.LayoutParams params2 = (TableRow.LayoutParams) durationTextView.getLayoutParams();
        params2.rightMargin = 50;
        params2.gravity = Gravity.CENTER_VERTICAL;
        durationTextView.setLayoutParams(params2);

        newLayout.addView(trackNameTextView);
        newLayout.addView(artistTextView);
        newRow.addView(newLayout);
        newRow.addView(durationTextView);

        return newRow;
    }

    public void onClick(View view) {
        Intent intent = null;

        // This is a tableRow
        intent = new Intent(this, MainActivity.class);
        intent.putExtra("mediaId", (String) view.getTag());
        startActivity(intent);
    }
}
