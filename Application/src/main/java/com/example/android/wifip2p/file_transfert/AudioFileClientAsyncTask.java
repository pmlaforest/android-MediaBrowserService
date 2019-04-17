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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class AudioFileClientAsyncTask extends AsyncTask<String, Void, String> {

    private Context context;

    private TextView statusText;

    private static final int SOCKET_TIMEOUT = 5000;

    private String isOwner = "no";
    private String hostName = "host";
    private int portNumber = -1;

    private Socket socket = null;

    private int SEND_RECEIVE_BUFFER_SIZE = 1024;

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
                    portNumber  = 8989;
                }

                Log.i("CLIENTASYNCTASK:", "opening client socket...");
                Log.i("CLIENTASYNCTASK:", hostName);
                Log.i("CLIENTASYNCTASK:", Integer.toString(portNumber));

                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostName, portNumber)), SOCKET_TIMEOUT);
                Log.i("CLIENTASYNCTASK:", "connecting to server...");

                ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());

                int len;
                byte buf[] = new byte[SEND_RECEIVE_BUFFER_SIZE];

                if (isOwner.equals("no")) {
                    Log.i("CLIENTASYNCTASK", "Sending IP address");
                    String distantIp = getDottedDecimalIP(getLocalIPAddress());
                    outToServer.writeUTF(distantIp);
                    outToServer.flush();
                }

                String helloWorld = "Hello World !\n";
                outToServer.writeUTF(helloWorld);
                outToServer.flush();

                socket.close();
                return "success";

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }catch (Exception e){
            Log.e("JavaInfo","Client_doInBackground(): " + e);
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
                socket.close();
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
            //statusText.setText("AudioFileClientAsyncTask");
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

}
