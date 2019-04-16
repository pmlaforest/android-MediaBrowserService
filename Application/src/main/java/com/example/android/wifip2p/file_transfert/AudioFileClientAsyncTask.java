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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioFileClientAsyncTask extends AsyncTask<String, Void, String> {

    private Context context;

    private TextView statusText;

    private static final int SOCKET_TIMEOUT = 5000;

    private String isOwner = "no";
    private String hostName = "host";
    private int portNumber = -1;

    /**
     * @param context
     * @param statusText
     */
    public AudioFileClientAsyncTask(Context context, View statusText) {
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
                hostName = params[1];

                if (isOwner.equals("yes")) {
                    portNumber = 8988;
                }
                else if (isOwner.equals("no")) {
                    portNumber = 8989;
                }

                Log.i("CLIENTASYNCTASK:", "opening client socket...");
                Socket socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostName, portNumber)), SOCKET_TIMEOUT);
                Log.i("CLIENTASYNCTASK:", "connecting to OWNER OF GROUP...");

                OutputStream outputStream = socket.getOutputStream();

                int len;
                byte buf[] = new byte[1024];
                buf = "Hello World".getBytes("UTF-8");

                Log.i("CLIENTASYNCTASK:", "Sending Hello World!");

                outputStream.write(buf, 0, buf.length);

                return "success";

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
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        try{
            statusText.setText("AudioFileClientAsyncTask");
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
