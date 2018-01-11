package com.app.ssoft.securebrowser;

import android.provider.BaseColumns;

/**
 * Created by Shekahar.Shrivastava on 06-Dec-17.
 */

public class DownloadEntryFeed {
    public DownloadEntryFeed(){
    }
    public static abstract class DownloadEntry implements BaseColumns {
        public static final String TABLE_NAME = "download";
        public static final String COLUMN_NAME_ENTRY_ID = "download_id";
        public static final String COLUMN_NAME_TITLE = "download_title";
        public static final String COLUMN_DOWNLOAD_URL = "download_url";
        public static final String COLUMN_TOTAL_SIZE = "downloaded_size";
        public static final String COLUMN_DOWNLOADED_DATE = "downloaded_date";
    }
}
