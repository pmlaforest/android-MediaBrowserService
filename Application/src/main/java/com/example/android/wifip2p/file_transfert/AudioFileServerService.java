package com.example.android.wifip2p.file_transfert;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.widget.TableRow;

import com.example.android.mediasession.service.contentcatalogs.MusicDatabase;
import com.example.android.mediasession.service.contentcatalogs.MusicLibrary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class AudioFileServerService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;

    public static final String ACTION_INIT_SERVER = "com.example.android.wifidirect.ACTION_INIT_SERVER";
    public static final String ACTION_CLOSE_SERVER = "com.example.android.wifidirect.ACTION_CLOSE_SERVER";

    public static final String OWNER_KEY = "owner";
    public static final String HOSTNAME_KEY = "hostname";

    private String isOwner = null;
    private String hostName = null;
    private int portNumber = -1;

    private static ServerSocket serverSocket = null;
    private static Socket client = null;
    private static ObjectInputStream inFromClient = null;
    private static ObjectOutputStream outToClient = null;

    private MusicLibrary musicLibrary = new MusicLibrary();

    public AudioFileServerService(String name) {
        super(name);
    }

    public AudioFileServerService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        try {
            if (intent.getAction().equals(ACTION_INIT_SERVER)) {
                isOwner = intent.getExtras().getString(OWNER_KEY);
                hostName = intent.getExtras().getString(HOSTNAME_KEY);

                if (serverSocket == null && client == null) {
                    initServer();
                    sendDownloadList();
                    while (true) {
                        sendAudioFile();
                    }
                }
            }
            if (intent.getAction().equals(ACTION_CLOSE_SERVER)) {
                closeServer();
            }
        }catch (Exception e){
        Log.e("JavaInfo","FileTransferService_onHandleIntent(): " + e);
        e.printStackTrace();
        }
    }

    private void sendAudioFile()  {

        try {
            String fileUri = inFromClient.readUTF();
            Log.i("SENDAUDIOFILE", "must send a file " + fileUri);

            ContentResolver cr = this.getContentResolver();
            InputStream is = null;
            try {
                is = cr.openInputStream(Uri.parse(fileUri));
            } catch (FileNotFoundException e) {
                Log.d("SENDAUDIOFILE", e.toString());
            }

            copyFileToSocket(is, outToClient);

        }catch(Exception e) {
            Log.e("SENDAUDIOFILE", "read/write file exception");
            e.printStackTrace();
        }
    }

    private void closeServer() {
        try {
            if (serverSocket != null && client != null) {
                // close client
                Intent closeClientIntent = new Intent(this, AudioFileClientService.class);
                closeClientIntent.setAction(AudioFileClientService.ACTION_CLOSE_CLIENT);
                closeClientIntent.putExtra(AudioFileClientService.HOSTNAME_KEY, hostName);
                startService(closeClientIntent);

                // close server
                serverSocket.close();
                client.close();

                serverSocket = null;
                client = null;
            }
        }catch (Exception e){
            Log.e("JavaInfo","FileTransferService_onHandleIntent(): " + e);
            e.printStackTrace();
        }
    }

    private void sendDownloadList() {

        try {
            outToClient.writeInt(MusicLibrary.keySet().size());

            for (String key : MusicLibrary.keySet()) {

                MediaMetadataCompat mmc = MusicLibrary.getMetadataWithoutBitmap(key);
                DownloadEntry downloadEntry = new DownloadEntry();

                String mediaId = mmc.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                if (mediaId == null) {
                    continue;
                }
                downloadEntry.mediaId = mediaId;

                String title = mmc.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
                if (title == null) {
                    title = "unknown";
                }
                downloadEntry.title = title;

                String artist = mmc.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                if (artist == null) {
                    artist = "unknown";
                }
                downloadEntry.artist = artist;

                outToClient.writeObject(downloadEntry);
            }

        }catch (Exception e){
            Log.e("JavaInfo","ClientService_onHandleIntent(): " + e);
            e.printStackTrace();
        }
    }

    private void initServer() {
        try {
            if (isOwner.equals("yes")) {
                portNumber = 8989;
            }
            else if (isOwner.equals("no")) {
                portNumber = 8988;

                Intent msgIntent = new Intent(this, AudioFileClientService.class);
                msgIntent.putExtra(AudioFileClientService.OWNER_KEY, isOwner);
                msgIntent.putExtra(AudioFileClientService.HOSTNAME_KEY, hostName);
                msgIntent.setAction(AudioFileClientService.ACTION_INIT_CLIENT);
                this.startService(msgIntent);
            }

            serverSocket = new ServerSocket(portNumber);
            Log.i(":", "opening server socket...");
            client = serverSocket.accept();
            Log.i("SERVERASYNCTASK:", "getting ready to read in the data...");

            inFromClient = new ObjectInputStream(client.getInputStream());
            outToClient = new ObjectOutputStream(client.getOutputStream());

            if (isOwner.equals("yes")) {
                // reading IP address
                Thread.sleep(100);
                hostName = inFromClient.readUTF();
                Log.i("SERVERASYNCTASK:", "message received!");
                Log.i("SERVERASYNCTASK:",hostName);

                Intent msgIntent = new Intent(this, AudioFileClientService.class);
                msgIntent.putExtra(AudioFileClientService.OWNER_KEY, isOwner);
                msgIntent.putExtra(AudioFileClientService.HOSTNAME_KEY, hostName);
                msgIntent.setAction(AudioFileClientService.ACTION_INIT_CLIENT);
                this.startService(msgIntent);
            }

            }
            catch(Exception e){
                Log.e("JavaInfo","Server_onHandleIntent(): " + e);
                e.printStackTrace();
            }
    }

    public static boolean copyFileToSocket(InputStream inputStream, ObjectOutputStream out) {
        try {
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    out.flush();
                }

            } catch (IOException e) {
                Log.d("AUDIOFILESERVERSERVICE", e.toString());
                return false;
            }
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_copyFile(): " + e);
        }
        return true;
    }
}
