package com.app.ssoft.securebrowser;

import android.provider.BaseColumns;

/**
 * Created by Shekahar.Shrivastava on 27-Nov-17.
 */

public class BookmarkEntryFeed {
    public BookmarkEntryFeed(){
    }
    public static abstract class BookmarkEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookmark";
        public static final String COLUMN_NAME_ENTRY_ID = "bookmark_id";
        public static final String COLUMN_NAME_TITLE = "bookmark_title";
        public static final String COLUMN_NAME_SUBTITLE = "bookmark_subtitle";
        public static final String COLUMN_URL = "bookmark_url";
    }
}
