package com.example.android.mediasession.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.android.mediasession.R;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicPlaylistActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MUSIC_FOLDER_NAME = "streamingapp_music";

    private List<MediaBrowserCompat.MediaItem> mediaItems;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_playlist);

        checkUserPermission();

        createMusicFolder(MUSIC_FOLDER_NAME);

        initialiseMusicLibrary();
        mediaItems =  MusicLibrary.getMediaItems();

        setFooterElementsOnClickListener();

        for(MediaBrowserCompat.MediaItem item : mediaItems) {
            MediaDescriptionCompat desc = item.getDescription();
            if (desc.getDescription() != null) {
                createTrackEntry(desc.getTitle().toString(), desc.getSubtitle().toString(), desc.getMediaId());
            }
        }
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

    private void setFooterElementsOnClickListener() {

        TextView downloadTrackTextView = (TextView) findViewById(R.id.download_textView);
        ImageButton downloadTrackImageButton = (ImageButton) findViewById(R.id.download_button);
        downloadTrackTextView.setOnClickListener(this);
        downloadTrackImageButton.setOnClickListener(this);

        TextView playlistTextView = (TextView) findViewById(R.id.playlist_textView);
        ImageButton playlistImageButton = (ImageButton) findViewById(R.id.playlist_button);
        playlistTextView.setOnClickListener(this);
        playlistImageButton.setOnClickListener(this);

        TextView parametersTextView = (TextView) findViewById(R.id.parameters_textView);
        ImageButton parametersImageButton = (ImageButton) findViewById(R.id.parameters_button);
        parametersTextView.setOnClickListener(this);
        parametersImageButton.setOnClickListener(this);

        TextView mediaPlayerTextView = (TextView) findViewById(R.id.mediaPlayer_textView);
        ImageButton mediaPlayerImageButton = (ImageButton) findViewById(R.id.mediaPlayer_button);
        mediaPlayerTextView.setOnClickListener(this);
        mediaPlayerImageButton.setOnClickListener(this);

    }


    private void createTrackEntry(String trackName, String author, String mediaId) {

        TableLayout table = findViewById(R.id.table1);

        TableRow newRow = new TableRow(this);

        newRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT));

        TextView newText = new TextView(this);
        String trackInfo = trackName + "\n" + author;
        newText.setText(trackInfo);
        newText.setTextSize(16);

        newRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        newRow.setBackground(getResources().getDrawable(R.drawable.cell_shape));
        newRow.setOnClickListener(this);
        newRow.setTag(mediaId);

        newRow.addView(newText);
        table.addView(newRow);
    }

    public void onClick(View view)
    {
        Intent intent = null;

        switch(view.getId()){
            case R.id.download_button:
            case R.id.download_textView:
                break;
            case R.id.parameters_button:
            case R.id.parameters_textView:
                break;
            case R.id.playlist_button:
            case R.id.playlist_textView:
                break;
            case R.id.mediaPlayer_button:
            case R.id.mediaPlayer_textView:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                // This is a tableRow
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("mediaId", (String)view.getTag());
                startActivity(intent);
                break;
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
