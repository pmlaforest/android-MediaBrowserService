package com.example.android.mediasession.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.R;
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
        switch (view.getId()) {
            case R.id.download_button:
            case R.id.download_textView:
                break;
            case R.id.parameters_button:
            case R.id.parameters_textView:
                startActivity(new Intent(getActivity(), WiFiDirectActivity.class));
                break;
            case R.id.playlist_button:
            case R.id.playlist_textView:
                startActivity(new Intent(getActivity(), MusicPlaylistActivity.class));
                break;
            case R.id.mediaPlayer_button:
            case R.id.mediaPlayer_textView:
                startActivity(new Intent(getActivity(), MainActivity.class));
                break;
            default:
                break;
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