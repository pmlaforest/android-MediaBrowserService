package com.example.android.wifip2p.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.wifip2p.file_transfert.AudioFileClientAsyncTask;
import com.example.android.wifip2p.file_transfert.AudioFileServerAsyncTask;
import com.example.android.wifip2p.file_transfert.FileTransferService;
import com.example.android.wifip2p.fragment.DeviceListFragment.DeviceActionListener;
import com.example.android.wifip2p.WiFiDirectActivity;
import com.example.android.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    private View mContentView = null;

    private WifiP2pDevice device;

    public WifiP2pInfo info;

    ProgressDialog progressDialog = null;

    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";

    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    private   ServerSocket serverSocket = null;

    private  Socket socket = null;

    private static final int SOCKET_TIMEOUT = 5000;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onActivityCreated(): " + e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            mContentView = inflater.inflate(R.layout.device_detail, null);
            mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    config.wps.setup = WpsInfo.PBC;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                            "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                    );
                    ((DeviceActionListener) getActivity()).connect(config);

                }
            });

            mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            ((DeviceActionListener) getActivity()).disconnect();
                        }
                    });
            mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            startClientOnClickListener(v);
                        }
                    });
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onCreateView(): " + e);
        }
        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // User has picked an image. Transfer it to group owner i.e peer using
            // FileTransferService.
            Uri uri = data.getData();
            TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
            statusText.setText("Sending: " + uri);
            Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
            getActivity().startService(serviceIntent);
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onActivityResult(): " + e);
        }
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            this.info = info;
            this.getView().setVisibility(View.VISIBLE);
            // The owner IP is now known.
            TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
            view.setText(getResources().getString(R.string.group_owner_text) + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)));
            // InetAddress from WifiP2pInfo struct.
            view = (TextView) mContentView.findViewById(R.id.device_info);
            view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
            // After the group negotiation, we assign the group owner as the file
            // server. The file server is single threaded, single connection server
            // socket.

            if (info.groupFormed && info.isGroupOwner) {
                new AudioFileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "yes",info.groupOwnerAddress.getHostAddress());
            } else if (info.groupFormed) {
                new AudioFileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"no",info.groupOwnerAddress.getHostAddress());

            }

            // Now we can connect with the client
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);

            // hide the connect button
            mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);

        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onConnectionInfoAvailable(): " + e);
        }
    }

    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        try {
            this.device = device;
            this.getView().setVisibility(View.VISIBLE);
            TextView view = (TextView) mContentView.findViewById(R.id.device_address);
            view.setText(device.deviceAddress);
            view = (TextView) mContentView.findViewById(R.id.device_info);
            view.setText(device.toString());
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_showDetails(): " + e);
        }
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        try {
            mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
            TextView view = (TextView) mContentView.findViewById(R.id.device_address);
            view.setText(R.string.empty);
            view = (TextView) mContentView.findViewById(R.id.device_info);
            view.setText(R.string.empty);
            view = (TextView) mContentView.findViewById(R.id.group_owner);
            view.setText(R.string.empty);
            view = (TextView) mContentView.findViewById(R.id.status_text);
            view.setText(R.string.empty);
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
            this.getView().setVisibility(View.GONE);
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_resetViews(): " + e);
        }
    }

    private void startClientOnClickListener(View v) {
        if (info != null) {
            if (info.groupFormed && info.isGroupOwner) {
                new AudioFileClientAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"yes",info.groupOwnerAddress.getHostAddress());

            } else if (info.groupFormed) {
                new AudioFileClientAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"no",info.groupOwnerAddress.getHostAddress());
            }
        }
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;

        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            try {
                this.context = context;
                this.statusText = (TextView) statusText;
            }catch (Exception e){
                Log.e("JavaInfo","DeviceDetailFragment_FileServerAsyncTask(): " + e);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                try {
                    ServerSocket serverSocket = new ServerSocket(8988);
                    Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                    Socket client = serverSocket.accept();
                    Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                    final File f = new File(context.getExternalFilesDir("received"),
                            "wifip2pshared-" + System.currentTimeMillis()
                                    + ".jpg");

                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();

                    Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                    InputStream inputstream = client.getInputStream();
                    copyFile(inputstream, new FileOutputStream(f));
                    serverSocket.close();
                    return f.getAbsolutePath();
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
                statusText.setText("Opening a server socket");
            }catch (Exception e){
                Log.e("JavaInfo","DeviceDetailFragment_onPreExecute(): " + e);
            }
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