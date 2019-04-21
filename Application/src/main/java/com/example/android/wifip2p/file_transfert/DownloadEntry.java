package com.example.android.wifip2p.file_transfert;

import java.io.Serializable;

public class DownloadEntry implements Serializable {
    static final long serialVersionUID = 1L;
    public String mediaId;
    public String title;
    public String artist;

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        DownloadEntry dEntry = (DownloadEntry) o;

        if (dEntry.mediaId.equals(this.mediaId)) {
            return true;
        }

        return false;
    }
}
