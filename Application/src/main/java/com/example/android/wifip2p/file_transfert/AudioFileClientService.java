package com.example.android.wifip2p.file_transfert;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.example.android.mediasession.service.contentcatalogs.DownloadLibrary;
import com.example.android.mediasession.ui.MusicPlaylistActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class AudioFileClientService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;

    public static final String ACTION_INIT_CLIENT = "com.example.android.wifidirect.ACTION_INIT_CLIENT";
    public static final String ACTION_CLOSE_CLIENT = "com.example.android.wifidirect.ACTION_CLOSE_CLIENT";
    public static final String ACTION_GET_AUDIO_FILE = "com.example.android.wifidirect.ACTION_GET_AUDIO_FILE";

    public static final String OWNER_KEY = "owner";
    public static final String HOSTNAME_KEY = "hostname";
    public static final String MEDIA_ID_KEY = "mediaid";
    public static final String FILENAME_KEY = "filename";

    private String isOwner = null;
    private String hostName = null;
    private int portNumber = -1;

    private static Socket socket = null;

    private static ObjectOutputStream outToServer = null;
    private static ObjectInputStream inFromServer = null;

    DownloadLibrary downloadLibrary = new DownloadLibrary();

    //public static List<DownloadEntry> downloadableEntries = new ArrayList<DownloadEntry>();

    public AudioFileClientService(String name) {
        super(name);
    }

    public AudioFileClientService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Context context = getApplicationContext();

            isOwner = intent.getExtras().getString(OWNER_KEY);
            hostName = intent.getExtras().getString(HOSTNAME_KEY);

            if (intent.getAction().equals(ACTION_INIT_CLIENT)) {
                if (socket == null) {
                    initClientConnection();
                    receiveDownloadList();
                }
            }
            if (intent.getAction().equals(ACTION_GET_AUDIO_FILE)) {
                String mediaId = intent.getExtras().getString(MEDIA_ID_KEY);
                String filename = intent.getExtras().getString(FILENAME_KEY);
                getAudioFile(mediaId, filename);
            }
            if (intent.getAction().equals(ACTION_CLOSE_CLIENT)) {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            }

        }catch (Exception e){
            Log.e("JavaInfo","FileTransferService_onHandleIntent(): " + e);
            e.printStackTrace();
        }
    }

    private void getAudioFile(String mediaId, String filename) {

        try {
            outToServer.writeUTF(mediaId);
            outToServer.flush();

            String musicFolderPath = Environment.getExternalStorageDirectory() + File.separator + MusicPlaylistActivity.MUSIC_FOLDER_NAME;

            File folder = new File(musicFolderPath);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }

            final File f = new File(musicFolderPath, filename + ".mp3");
            f.createNewFile();

            Log.d("Reading a new file", "server: copying files " + f.toString());
            copyFileFromSocket(inFromServer, new FileOutputStream(f));

        }catch (Exception e){
            Log.e("JavaInfo","ClientService_onHandleIntent(): " + e);
            e.printStackTrace();
        }
    }

    private void receiveDownloadList() {

        try {
            int nbOfDownloadableEntries = inFromServer.readInt();

            for (int entryNb = 0; entryNb  < nbOfDownloadableEntries; entryNb++) {
                DownloadEntry downloadEntry = new DownloadEntry();
                downloadEntry = (DownloadEntry) inFromServer.readObject();

                if (!downloadLibrary.downloadableEntries.contains(downloadEntry)) {
                    downloadLibrary.downloadableEntries.add(downloadEntry);
                }
            }

        }catch (Exception e){
            Log.e("JavaInfo","ClientService_onHandleIntent(): " + e);
            e.printStackTrace();
        }
    }

    private void initClientConnection() {
        try {

            if (isOwner.equals("yes")) {
                portNumber = 8988;
            }
            else if (isOwner.equals("no")) {
                portNumber  = 8989;
            }

            Log.i("CLIENTASYNCTASK:", "opening client socket...");
            Log.i("CLIENTASYNCTASK:", hostName);
            Log.i("CLIENTASYNCTASK:", Integer.toString(portNumber));

            socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostName, portNumber)), SOCKET_TIMEOUT);
            Log.i("CLIENTASYNCTASK:", "connecting to server...");

            outToServer = new ObjectOutputStream(socket.getOutputStream());
            inFromServer = new ObjectInputStream(socket.getInputStream());

            if (isOwner.equals("no")) {
                Log.i("CLIENTASYNCTASK", "Sending IP address");
                String distantIp = getDottedDecimalIP(getLocalIPAddress());
                outToServer.writeUTF(distantIp);
                outToServer.flush();
            }

            Log.i("CLIENTASYNCTASK", "Sending Hello World");
            String helloWorld = "Hello World !\n";
            outToServer.writeUTF(helloWorld);
            outToServer.flush();

        }catch (Exception e){
            Log.e("JavaInfo","ClientService_onHandleIntent(): " + e);
            e.printStackTrace();
        }
    }

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    public static boolean copyFileFromSocket(ObjectInputStream inputStream, OutputStream out) {
        try {
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
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
