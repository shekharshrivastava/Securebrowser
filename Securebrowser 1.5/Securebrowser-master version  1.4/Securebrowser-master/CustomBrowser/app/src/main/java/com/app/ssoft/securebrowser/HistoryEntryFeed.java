package com.app.ssoft.securebrowser;

import android.provider.BaseColumns;

/**
 * Created by Shekahar.Shrivastava on 28-Nov-17.
 */

public class HistoryEntryFeed {
    public HistoryEntryFeed(){
    }
    public static abstract class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_ENTRY_ID = "history_id";
        public static final String COLUMN_NAME_TITLE = "history_title";
        public static final String COLUMN_URL = "history_url";
        public static final String COLUMN_FAVICON = "favicon";
    }
}
