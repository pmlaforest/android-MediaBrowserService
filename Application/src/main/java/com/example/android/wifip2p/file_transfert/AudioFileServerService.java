package com.example.android.wifip2p.file_transfert;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class AudioFileServerService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;

    public static final String ACTION_INIT_SERVER = "com.example.android.wifidirect.ACTION_INIT_SERVER";
    public static final String CLOSE_SERVER = "com.example.android.wifidirect.CLOSE_SERVER";

    public static final String OWNER_KEY = "owner";
    public static final String HOSTNAME_KEY = "hostname";

    private String isOwner = null;
    private String hostName = null;
    private int portNumber = -1;

    private ServerSocket serverSocket = null;
    private Socket client = null;


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
        isOwner = intent.getExtras().getString(OWNER_KEY);
        hostName = intent.getExtras().getString(HOSTNAME_KEY);

        try {
            if (intent.getAction().equals(ACTION_INIT_SERVER)) {
                initServer();
                serverSocket.close();
                client.close();
            }
            if (intent.getAction().equals(CLOSE_SERVER)) {
                closeServer();
            }
        }catch (Exception e){
        Log.e("JavaInfo","FileTransferService_onHandleIntent(): " + e);
        e.printStackTrace();
        }

    }

    private void closeServer() {
        try {
            Intent closeClientIntent = new Intent(this, AudioFileClientService.class);
            closeClientIntent.setAction(AudioFileClientService.CLOSE_CLIENT);
            closeClientIntent.putExtra(AudioFileClientService.HOSTNAME_KEY, hostName);
            startService(closeClientIntent);

            serverSocket.close();
            client.close();

        }catch (Exception e){
            Log.e("JavaInfo","FileTransferService_onHandleIntent(): " + e);
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
            Log.i("SERVERASYNCTASK:", "getting ready to read in the data");

            ObjectInputStream inFromClient = new ObjectInputStream(client.getInputStream());

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

            // reading Hello World !
            String helloWorld = inFromClient.readUTF();
            Log.i("SERVERASYNCTASK:", "message received!");
            Log.i("SERVERASYNCTASK:", helloWorld);

            }
            catch(Exception e){
                Log.e("JavaInfo","Server_onHandleIntent(): " + e);
                e.printStackTrace();
            }
    }
}
