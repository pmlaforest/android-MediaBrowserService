package com.example.android.wifip2p.file_transfert;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class AudioFileClientService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;

    public static final String ACTION_INIT_CLIENT = "com.example.android.wifidirect.ACTION_INIT_CLIENT";
    public static final String CLOSE_CLIENT = "com.example.android.wifidirect.CLOSE_CONN";

    public static final String OWNER_KEY = "owner";
    public static final String HOSTNAME_KEY = "hostname";

    private String isOwner = null;
    private String hostName = null;
    private int portNumber = -1;

    private Socket socket = null;

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
                initClientConnection();
                socket.close();
            }
            if (intent.getAction().equals(CLOSE_CLIENT)) {
            }
        }catch (Exception e){
            Log.e("JavaInfo","FileTransferService_onHandleIntent(): " + e);
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

            ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());

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

}
