package com.app.ssoft.securebrowser;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Shekahar.Shrivastava on 27-Nov-17.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper instance;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "browser.sqlite";

    private static final String TEXT_TYPE = " TEXT";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BookmarkEntryFeed.BookmarkEntry.TABLE_NAME + " (" +
                    BookmarkEntryFeed.BookmarkEntry.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BookmarkEntryFeed.BookmarkEntry.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
                    BookmarkEntryFeed.BookmarkEntry.COLUMN_NAME_SUBTITLE + TEXT_TYPE + "," +
                    BookmarkEntryFeed.BookmarkEntry.COLUMN_URL + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_HISTORY_ENTRIES =
            "CREATE TABLE " + HistoryEntryFeed.HistoryEntry.TABLE_NAME + " (" +
                    HistoryEntryFeed.HistoryEntry.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    HistoryEntryFeed.HistoryEntry.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
                    HistoryEntryFeed.HistoryEntry.COLUMN_URL + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_DOWNLOAD_ENTRIES =
            "CREATE TABLE " + DownloadEntryFeed.DownloadEntry.TABLE_NAME + " (" +
                    DownloadEntryFeed.DownloadEntry.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DownloadEntryFeed.DownloadEntry.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
                    DownloadEntryFeed.DownloadEntry.COLUMN_DOWNLOADED_DATE + TEXT_TYPE + "," +
                    DownloadEntryFeed.DownloadEntry.COLUMN_TOTAL_SIZE + TEXT_TYPE + "," +
                    DownloadEntryFeed.DownloadEntry.COLUMN_DOWNLOAD_URL + TEXT_TYPE +
                    " )";


    private static final String SQL_DELETE_HISTORY_ENTRIES =
            "DROP TABLE IF EXISTS " + HistoryEntryFeed.HistoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BookmarkEntryFeed.BookmarkEntry.TABLE_NAME;
    private static final String SQL_DELETE_DOWNLOAD_ENTRIES =
            "DROP TABLE IF EXISTS " + DownloadEntryFeed.DownloadEntry.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static public synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

  /*  public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_HISTORY_ENTRIES);
        db.execSQL(SQL_CREATE_DOWNLOAD_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_HISTORY_ENTRIES);
        db.execSQL(SQL_DELETE_DOWNLOAD_ENTRIES);
        onCreate(db);
    }*/

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_HISTORY_ENTRIES);
        db.execSQL(SQL_CREATE_DOWNLOAD_ENTRIES);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_HISTORY_ENTRIES);
        db.execSQL(SQL_DELETE_DOWNLOAD_ENTRIES);
        onCreate(db);
    }
}
