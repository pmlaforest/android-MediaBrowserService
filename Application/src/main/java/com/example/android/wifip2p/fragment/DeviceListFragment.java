package com.example.android.wifip2p.fragment;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.android.wifip2p.WiFiDirectActivity;
import java.util.ArrayList;
import java.util.List;

import com.example.android.R;

/**
 * A ListFragment that displays available peers on discovery and requests the
 * parent activity to handle user interaction events
 */
public class DeviceListFragment extends ListFragment implements PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    ProgressDialog progressDialog = null;

    View mContentView = null;

    private WifiP2pDevice device;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
            this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_onActivityCreated(): " + e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            mContentView = inflater.inflate(R.layout.device_list, null);
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_onCreateView(): " + e);
        }
        return mContentView;
    }

    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        try {
            Log.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
            switch (deviceStatus) {
                case WifiP2pDevice.AVAILABLE:
                    return "Available";
                case WifiP2pDevice.INVITED:
                    return "Invited";
                case WifiP2pDevice.CONNECTED:
                    return "Connected";
                case WifiP2pDevice.FAILED:
                    return "Failed";
                case WifiP2pDevice.UNAVAILABLE:
                    return "Unavailable";
                default:
                    return "Unknown";
            }
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_getDeviceStatus(): " + e);
        }
        return "Unknown";
    }

    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        try {
            WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
            ((DeviceActionListener) getActivity()).showDetails(device);
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_onListItemClick(): " + e);
        }
    }

    /**
     * Update UI for this device.
     * 
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        try {
            this.device = device;
            TextView view = (TextView) mContentView.findViewById(R.id.my_name);
            view.setText(device.deviceName);
            view = (TextView) mContentView.findViewById(R.id.my_status);
            view.setText(getDeviceStatus(device.status));
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_updateThisDevice(): " + e);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            if (peers.size() == 0) {
                Log.d(WiFiDirectActivity.TAG, "No devices found");
                return;
            }
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_onPeersAvailable(): " + e);
        }
    }

    public void clearPeers() {
        try {
            peers.clear();
            ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_clearPeers(): " + e);
        }
    }

    /**
     * 
     */
    public void onInitiateDiscovery() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
                    true, new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    });
        }catch (Exception e){
            Log.e("JavaInfo","DeviceListFragment_onInitiateDiscovery(): " + e);
        }
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            try {
                items = objects;
            }catch (Exception e){
                Log.e("JavaInfo","DeviceListFragment_WiFiPeerListAdapter(): " + e);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = null;
            try {
                v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row_devices, null);
                }
                WifiP2pDevice device = items.get(position);
                if (device != null) {
                    TextView top = (TextView) v.findViewById(R.id.device_name);
                    TextView bottom = (TextView) v.findViewById(R.id.device_details);
                    if (top != null) {
                        top.setText(device.deviceName);
                    }
                    if (bottom != null) {
                        bottom.setText(getDeviceStatus(device.status));
                    }
                }
            }catch (Exception e){
                Log.e("JavaInfo","DeviceListFragment_WiFiPeerListAdapter(): " + e);
            }
            return v;
        }
    }

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }
}
