package com.example.android.wifip2p.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.wifip2p.file_transfert.AudioFileClientService;
import com.example.android.wifip2p.file_transfert.AudioFileServerService;
import com.example.android.wifip2p.fragment.DeviceListFragment.DeviceActionListener;
import com.example.android.R;

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
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onCreateView(): " + e);
        }
        return mContentView;
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

            Intent serverIntent = new Intent(getActivity(), AudioFileServerService.class);
            if (info.groupFormed && info.isGroupOwner) {
                serverIntent.putExtra(AudioFileServerService.OWNER_KEY, "yes");
            } else if (info.groupFormed) {
                serverIntent.putExtra(AudioFileServerService.OWNER_KEY, "no");
            }
            serverIntent.setAction(AudioFileServerService.ACTION_INIT_SERVER);
            serverIntent.putExtra(AudioFileClientService.HOSTNAME_KEY,  info.groupOwnerAddress.getHostAddress());
            getActivity().startService(serverIntent);

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
            this.getView().setVisibility(View.GONE);

        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_resetViews(): " + e);
        }
    }
}