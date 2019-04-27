package com.example.android.mediasession.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.R;
import com.example.android.mediasession.ui.DownloadListActivity;
import com.example.android.mediasession.ui.MainActivity;
import com.example.android.mediasession.ui.MusicPlaylistActivity;
import com.example.android.wifip2p.WiFiDirectActivity;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class FooterFragment extends Fragment implements View.OnClickListener {

    private View mContentView = null;

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
            mContentView = inflater.inflate(R.layout.footer, container, false);
            setFooterElementsOnClickListener(mContentView);
        }catch (Exception e){
            Log.e("JavaInfo","DeviceDetailFragment_onCreateView(): " + e);
        }
        return mContentView;
    }

    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
        Activity this_activity = getActivity();
        String caller = "";

        try {
            caller = this_activity.getCallingActivity().getClassName();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if (id == R.id.download_button || id == R.id.download_textView) {
            String target_class_name = DownloadListActivity.class.getName();
            if (this_activity.getClass().getName().equals(target_class_name)) {
                return;
            } else {
                intent = new Intent(this_activity, DownloadListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this_activity.startActivity(intent);
            }

            /*    if (caller.equals(target_class_name)) {
                this_activity.setResult(Activity.RESULT_OK);
                this_activity.finish();
            } else {
                intent = new Intent(this_activity, DownloadListActivity.class);
                this_activity.startActivityForResult(intent, 1);
            }*/
        } else if (id == R.id.parameters_button || id == R.id.parameters_textView) {
            String target_class_name = WiFiDirectActivity.class.getName();
            if (this_activity.getClass().getName().equals(target_class_name)) {
                return;
            } else {
                intent = new Intent(this_activity, WiFiDirectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this_activity.startActivity(intent);
            }

                /*if (caller.equals(target_class_name)) {
                this_activity.setResult(Activity.RESULT_OK);
                this_activity.finish();
            } else {
                intent = new Intent(this_activity, WiFiDirectActivity.class);
                this_activity.startActivityForResult(intent, 1);
            }*/
        } else if (id == R.id.playlist_button || id == R.id.playlist_textView) {
            String target_class_name = MusicPlaylistActivity.class.getName();
            if (this_activity.getClass().getName().equals(target_class_name)) {
                return;
            } else {
                intent = new Intent(this_activity, MusicPlaylistActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this_activity.startActivity(intent);
            }
        } else if (id == R.id.mediaPlayer_button || id == R.id.mediaPlayer_textView) {
            String target_class_name = MainActivity.class.getName();
            if (this_activity.getClass().getName().equals(target_class_name)) {
                return;
            } else {
                intent = new Intent(this_activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this_activity.startActivity(intent);
            }

                /*if (caller.equals(target_class_name)) {
                this_activity.setResult(Activity.RESULT_OK);
                this_activity.finish();
            } else {
                intent = new Intent(this_activity, MainActivity.class);
                this_activity.startActivityForResult(intent, 1);
            }*/
        }
    }

    private void setFooterElementsOnClickListener(View view) {

        TextView downloadTrackTextView = (TextView) mContentView.findViewById(R.id.download_textView);
        ImageButton downloadTrackImageButton = (ImageButton) mContentView.findViewById(R.id.download_button);
        downloadTrackTextView.setOnClickListener(this);
        downloadTrackImageButton.setOnClickListener(this);

        TextView playlistTextView = (TextView) mContentView.findViewById(R.id.playlist_textView);
        ImageButton playlistImageButton = (ImageButton) mContentView.findViewById(R.id.playlist_button);
        playlistTextView.setOnClickListener(this);
        playlistImageButton.setOnClickListener(this);

        TextView parametersTextView = (TextView) mContentView.findViewById(R.id.parameters_textView);
        ImageButton parametersImageButton = (ImageButton) mContentView.findViewById(R.id.parameters_button);
        parametersTextView.setOnClickListener(this);
        parametersImageButton.setOnClickListener(this);

        TextView mediaPlayerTextView = (TextView) mContentView.findViewById(R.id.mediaPlayer_textView);
        ImageButton mediaPlayerImageButton = (ImageButton) mContentView.findViewById(R.id.mediaPlayer_button);
        mediaPlayerTextView.setOnClickListener(this);
        mediaPlayerImageButton.setOnClickListener(this);
    }
}