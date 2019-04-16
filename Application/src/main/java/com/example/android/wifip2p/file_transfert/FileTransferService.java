package com.example.android.wifip2p.file_transfert;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.example.android.wifip2p.fragment.DeviceDetailFragment;
import com.example.android.wifip2p.WiFiDirectActivity;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;

    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String ACTION_SEND_AUDIO_FILE = "com.example.android.wifidirect.SEND_AUDIO_FILE";
    public static final String ACTION_RECEIVE_AUDIO_FILE = "com.example.android.wifidirect.RECEIVE_AUDIO_FILE";

    public static final String EXTRAS_FILE_PATH = "file_url";

    public static final String IS_OWNER = "no";
    public static final String HOST_NAME = "host";

    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";

    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    private   ServerSocket serverSocket = null;
    private  Socket socket = null;


    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
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

            String isOwner = intent.getExtras().getString(IS_OWNER);

        }catch (Exception e){
            Log.e("JavaInfo","FileTransferService_onHandleIntent(): " + e);
        }
    }
}
