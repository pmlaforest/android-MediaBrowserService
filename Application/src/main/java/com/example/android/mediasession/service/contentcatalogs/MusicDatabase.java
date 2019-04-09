package com.example.android.mediasession.service.contentcatalogs;

import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MusicDatabase extends SQLiteOpenHelper {

    private static MusicDatabase database;

    private static final String LOG = "DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MUSICDATABASE";

    private static final String TABLE_CHANSON = "CHANSON";
    //table chanson - column names
    public static final String KEY_ID = "ID";
    public static final String KEY_URI_STRING = "URI_STRING";
    public static final String KEY_TITRE = "TITRE";
    public static final String KEY_ARTIST = "ARTIST";
    public static final String KEY_ALBUM = "ALBUM";
    public static final String KEY_GENRE = "GENRE";
    public static final String KEY_DURATION = "DURATION";
    public static final String KEY_DURATION_UNIT = "DURATION_UNIT";
    public static final String KEY_MUSIC_FILENAME = "MUSIC_FILENAME";
    public static final String KEY_ALBUM_ART_RES_ID = "ALBUM_ART_RES_ID";
    public static final String KEY_ALBUM_ART_RES_NAME= "ALBUM_ART_RES_NAME";
    public static final String KEY_CREATED_AT = "CREATED_AT";

    // Statement - Creation de la table
    private static final String CREATE_TABLE_CHANSON = "CREATE TABLE " + TABLE_CHANSON +
            "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_URI_STRING + " TEXT,"
            + KEY_TITRE + " TEXT,"
            + KEY_ARTIST + " TEXT,"
            + KEY_ALBUM + " TEXT,"
            + KEY_GENRE + " TEXT,"
            + KEY_DURATION + " FLOAT,"
            + KEY_DURATION_UNIT + " TEXT,"
            + KEY_MUSIC_FILENAME + " TEXT,"
            + KEY_ALBUM_ART_RES_ID + " INTEGER,"
            + KEY_ALBUM_ART_RES_NAME + " TEXT,"
            + KEY_CREATED_AT + " DATETIME"
            + ")";

    public MusicDatabase(Context context) {
        super(context, DATABASE_NAME , null, 1);
        Log.i(LOG, "Database: " +  DATABASE_NAME + " cree.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_CHANSON);
        Log.i(LOG, "table: " +  TABLE_CHANSON + " cree.");
    }

    //Exécuté si une nouvelle version est plus récente que la présente. On recréera la BD dans ce cas.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +  TABLE_CHANSON);
        onCreate(db);
    }

    /**
     * Code qui provient de https://stackoverflow.com/questions/3386667/query-if-android-database-exists
     * Check if the database exist and can be read.
     *
     * @return true if it exists and can be read, false if it doesn't
     */
    public static boolean doesDatabaseExist(Context context) {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    public boolean createTrack(String uriString, String titre, String artist, String album, String genre, long duration, TimeUnit timeunit, String musicFileName, int albumArtResId, String albumArtResName) {

        //Remplisage des champs nulls
        if(titre == null)
            titre = "Aucun titre";
        if(artist == null)
            artist = "Aucun artiste";
        if(album == null)
            album = "Aucun album";
        if(genre == null)
            genre = "Aucun informations";

        SQLiteDatabase db = this.getWritableDatabase();
        Log.i(LOG, "createTrack, " + "database, " + db.toString());
        long result = -1;

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_URI_STRING, uriString);
        contentValues.put(KEY_TITRE, titre);
        contentValues.put(KEY_ARTIST, artist);
        contentValues.put(KEY_ALBUM, album);
        contentValues.put(KEY_GENRE, genre);
        contentValues.put(KEY_DURATION, (float)duration);
        contentValues.put(KEY_DURATION_UNIT, timeunit.name());
        contentValues.put(KEY_MUSIC_FILENAME, musicFileName);
        contentValues.put(KEY_ALBUM_ART_RES_ID, albumArtResId);
        contentValues.put(KEY_ALBUM_ART_RES_NAME, albumArtResName);
        contentValues.put(KEY_CREATED_AT, getDateTime());

        Log.i(LOG, "CreateChanson: musicFilename: " +  musicFileName);
        Log.i(LOG, "CreateChanson: albumresId: " +  albumArtResId);

        result =  db.insert(TABLE_CHANSON, null, contentValues);
        if(result == -1){
            db.close();
            Log.i(LOG,"Insertion a échouée: " +  musicFileName + ":" + contentValues.toString());
            return false;
        }
        else{
            db.close();
            Log.i(LOG, "CREATE CHANSON: INSERTION DE LA CHANSON " + musicFileName + " RÉUSSIS.");
            return true;
        }
    }

    //Retourne un curseur sur la chanson voulue
    public Cursor getTrack(Integer musicID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_CHANSON
                + " WHERE " + KEY_ID + " = " + musicID;


        Cursor res =  db.rawQuery( selectQuery, null );
        if (res != null)
            res.moveToFirst();
        //Pas certain sur le type de retour encore...Présentement, je retourne un curseur.
        return res;
    }

    //Retourne un curseur sur toutes les chansons
    public Cursor getAllTracks(){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_CHANSON;

        Cursor res =  db.rawQuery( selectQuery, null );
        if (res != null)
            res.moveToFirst();
        //Pas certain sur le type de retour encore...Présentement, je retourne un curseur.
        return res;
    }

    public int getNbOfTracks(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_CHANSON);
        return numRows;
    }

    /*
     * Update une chanson
     */
    public int updateTrack(int id, String uriString, String titre, String artist, String album, String genre, long duration, TimeUnit timeunit, String musicFileName, int albumArtResId, String albumArtResName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ID, id);
        contentValues.put(KEY_URI_STRING, uriString);
        contentValues.put(KEY_TITRE, titre);
        contentValues.put(KEY_ARTIST, artist);
        contentValues.put(KEY_ALBUM, album);
        contentValues.put(KEY_GENRE, genre);
        contentValues.put(KEY_DURATION, duration);
        contentValues.put(KEY_DURATION_UNIT, timeunit.name());
        contentValues.put(KEY_MUSIC_FILENAME, musicFileName);
        contentValues.put(KEY_ALBUM_ART_RES_ID, albumArtResId);
        contentValues.put(KEY_ALBUM_ART_RES_NAME, albumArtResName);
        contentValues.put(KEY_CREATED_AT, getDateTime());

        if(db.update(TABLE_CHANSON, contentValues, KEY_URI_STRING + " = ?", new String[]{uriString} ) == 1) {
            Log.i(LOG, "updateTrack: " + uriString + ": update réussie.");
            return 1;
        }
        else
        {
            Log.i(LOG, "updateTrack: " + uriString + ": impossible d'appliquer l'update.");
            return 0;
        }
    }

    /*
     * Delete une chanson
     */
    public void deleteToDo(String uriString) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHANSON, KEY_URI_STRING + " = ?",
                new String[] { uriString });
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
