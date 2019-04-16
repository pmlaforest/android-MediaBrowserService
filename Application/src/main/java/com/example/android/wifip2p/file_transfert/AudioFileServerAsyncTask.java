package com.example.android.wifip2p.file_transfert;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.wifip2p.WiFiDirectActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioFileServerAsyncTask extends AsyncTask<String, Void, String> {

    private Context context;
    private TextView statusText;
    private String isOwner = "no";
    private int portNumber = -1;

    /**
     * @param context
     * @param statusText
     */
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

                ServerSocket serverSocket = new ServerSocket(portNumber);
                Log.i("SERVERASYNCTASK:", "opening server socket...");
                Socket client = serverSocket.accept();
                Log.i("SERVERASYNCTASK:", "getting ready to read in the data");
                InputStream inputstream = client.getInputStream();

                int len;
                byte buf[] = new byte[1024];

                // The server reads a request (from the client) for the list of audio files of the server
                while ((len = inputstream.read(buf)) != -1) {
                    Log.i("SERVERASYNCTASK:", "message received!");
                    Log.i("SERVERASYNCTASK:", new String(buf, "UTF-8"));
                }

                return "Success";

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_doInBackground(): " + e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
    /*
        try {
            if (result != null) {
                statusText.setText("File copied - " + result);
                File recvFile = new File(result);
                Uri fileUri = FileProvider.getUriForFile(context, "com.example.android.wifidirect.fileprovider", recvFile);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "image/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onPostExecute(): " + e);
        }
        */
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
