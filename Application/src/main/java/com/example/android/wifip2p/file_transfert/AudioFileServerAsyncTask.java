package com.example.android.wifip2p.file_transfert;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.wifip2p.WiFiDirectActivity;
import com.example.android.wifip2p.fragment.onIpAddressReceived;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioFileServerAsyncTask extends AsyncTask<String, String, String> {

    private Context context;
    private TextView statusText;
    private String isOwner = "no";
    private int portNumber = -1;
    private onIpAddressReceived ipReceivedCallback;
    private ServerSocket serverSocket = null;
    private Socket client = null;

    private int SEND_RECEIVE_BUFFER_SIZE = 1024;

    /**
     * @param ipReceivedCallback
     * @param statusText
     */

    public AudioFileServerAsyncTask(onIpAddressReceived ipReceivedCallback, View statusText) {
        try {
            this.ipReceivedCallback = ipReceivedCallback;
            this.statusText = (TextView) statusText;
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_FileServerAsyncTask(): " + e);
        }
    }

    public AudioFileServerAsyncTask(Context context, View statusText) {
        try {
            this.context = context;
            this.statusText = (TextView) statusText;
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_FileServerAsyncTask(): " + e);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            try {

                isOwner = params[0];

                if (isOwner.equals("yes")) {
                    portNumber = 8989;
                }
                else if (isOwner.equals("no")) {
                    portNumber = 8988;
                }

                serverSocket = new ServerSocket(portNumber);
                Log.i("SERVERASYNCTASK:", "opening server socket...");
                client = serverSocket.accept();
                Log.i("SERVERASYNCTASK:", "getting ready to read in the data");

                //BufferedReader inFromClient = new BufferedReader (new InputStreamReader(client.getInputStream()));
                ObjectInputStream inFromClient = new ObjectInputStream(client.getInputStream());


                int len;
                byte buf[] = new byte[SEND_RECEIVE_BUFFER_SIZE];
                int lenOfData = 0;

                if (isOwner.equals("yes")) {
                    // reading IP address
                    String distantIp = inFromClient.readUTF();
                    Log.i("SERVERASYNCTASK:", "message received!");
                    Log.i("SERVERASYNCTASK:",distantIp);
                    ipReceivedCallback.onIpReceivedFromClient(distantIp);
                }

                // reading Hello World !
                String helloWorld = inFromClient.readUTF();
                Log.i("SERVERASYNCTASK:", "message received!");
                Log.i("SERVERASYNCTASK:", helloWorld);


                serverSocket.close();
                client.close();

                return "Success";

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }catch (Exception e){
            Log.e("JavaInfo","Server_doInBackground(): " + e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {

        try {
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onPostExecute(): " + e);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        try{
            statusText.setText("AudioFileServerAsyncTask");
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onPreExecute(): " + e);
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        try {
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);

                }
                out.close();
                inputStream.close();
            } catch (IOException e) {
                Log.d(WiFiDirectActivity.TAG, e.toString());
                return false;
            }
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_copyFile(): " + e);
        }
        return true;
    }
}
