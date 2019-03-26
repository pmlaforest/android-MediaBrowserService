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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.android.mediasession.R;
import com.example.android.mediasession.service.contentcatalogs.MusicDatabase;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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

public class MusicPlaylistActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MUSIC_FOLDER_NAME = "streamingapp_music";



    private MusicDatabase musicDatabase;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_playlist);

        checkUserPermission();

        createMusicFolder(MUSIC_FOLDER_NAME);
        createDataBase(this);
        initialiseMusicLibrary();

        setFooterElementsOnClickListener();
        createListOfTracks();
    }

    /**
     * Création de la base de donnée, si absente de l'appareil, on l'initialise avec toutes les chansons trouvées dans l'appareil également
     */
    private void createDataBase(Context context) {

        musicDatabase = new MusicDatabase(this);

        if (!(MusicDatabase.doesDatabaseExist(this))) {
            initialiseDataBase();
        } else {
            Log.i("MAINACTIVITY", "createDataBase: base de donnée déjà existante.");
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

    /**
     * Crée les entrées dans la BD seulement à la création.
     */
    private void initialiseDataBase() {

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        ContentResolver cResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        boolean isSuccessful = false;

        Cursor cursor = cResolver.query(uri, null, selection, null, null);

        if (cursor == null) {
            throw new RuntimeException("cannot access MediaStore");
        } else if (!cursor.moveToFirst()) {
            Log.i("MAINACTIVITY", "initialiseDataBase: aucune chanson. elseif(!cursor.moveToFirst()");
        } else {
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            do {
                long thisId = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId);

                try {
                    mmr.setDataSource(this, contentUri);
                } catch (Exception e) {
                    Log.i("MAINACTIVITY", "initialiseDataBase, mmr.setDataSource error " + e + ", " + contentUri.toString());
                }
                isSuccessful = musicDatabase.createTrack(
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
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        R.drawable.album_jazz_blues,
                        "album_jazz_blues"
                );

                if (isSuccessful) {
                    Log.i("MAINACTIVITY", "initialiseDataBase: Chanson" + contentUri.toString() + " inserré avec succès.");
                } else {
                    Log.i("MAINACTIVITY", "initialiseDataBase: Chanson" + contentUri.toString() + ": incapabble de faire l'insertion initiale dans la BD.");
                }
            }while (cursor.moveToNext()) ;
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

    private void createListOfTracks() {

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<MediaBrowserCompat.MediaItem>();
        mediaItems =  MusicLibrary.getMediaItems();

        for(MediaBrowserCompat.MediaItem item : mediaItems) {
            MediaDescriptionCompat desc = item.getDescription();
            if (desc.getDescription() != null) {

                if (desc.getMediaId() == null) {
                    continue;
                }

                String title = "unknown";
                if (desc.getTitle().toString() != null) {
                    title = desc.getTitle().toString();
                }

                String artist = "unknown";
                if (desc.getSubtitle().toString() != null) {
                    artist = desc.getSubtitle().toString();
                }

                MediaMetadataCompat currentMetadata = MusicLibrary.getMetadata(this,item.getMediaId());
                int duration = (int) currentMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                
                TableLayout table = findViewById(R.id.table1);
                TableRow newRow = createTrackEntry(title, artist, duration, desc.getMediaId());
                table.addView(newRow);
            }
        }
        // Maybe the garbage collector will reclaim this memory ?
        mediaItems = null;
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

        int seconds = (int) (duration / 1000) % 60 ;
        int minutes = (int) ((duration / (1000*60)) % 60);
        int hours   = (int) ((duration / (1000*60*60)));

        String formattedDurationStr = null;
        if (hours == 0) {
            formattedDurationStr = String.format("%d:%02d", minutes, seconds);
        }
        else {
            formattedDurationStr = String.format("%d:%02d:%02d", hours, minutes, seconds);
        }

        TextView durationTextView = new TextView(this);
        durationTextView.setText(formattedDurationStr);
        durationTextView.setTextSize(16);
        durationTextView.setGravity(Gravity.RIGHT);
        durationTextView.setPadding(0,0,5,0);

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
    /**
     * Récupère les musiques de l'appareille et les ajoute à la MusicLibrary.
     */
    private void initialiseMusicLibrary() {

        List<Integer> chansons = new ArrayList<Integer>();
        chansons = musicDatabase.getAllTracks();
        Cursor chansonPresente;
        TimeUnit unit;

        for (Integer chanson : chansons) {
            chansonPresente = musicDatabase.getTrack(chanson);
            Log.i("MAINACTIVITY1", "initialiseMusicLibrary: " + chansonPresente.toString());
            unit = TimeUnit.valueOf(chansonPresente.getString(chansonPresente.getColumnIndex(KEY_DURATION_UNIT)));
            Log.i("MAINACTIVITY1", "initialiseMusicLibrary: 2");

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
                    chansonPresente.getString(chansonPresente.getColumnIndex(KEY_ALBUM_ART_RES_NAME)));
        }
    }
}
