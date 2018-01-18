package com.app.ssoft.securebrowser;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Shekahar.Shrivastava on 06-Dec-17.
 */

public class DownloadReciever extends BroadcastReceiver {
    private SQLiteDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cursor = manager.query(query);
            if (cursor.moveToFirst()) {
                if (cursor.getCount() > 0) {
                    int totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    int downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Toast.makeText(context, "Download completed", Toast.LENGTH_SHORT).show();
                        String fileTitle = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        String fileDescription = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
                        try {
                            insertDownloadingData(context, fileTitle, fileDescription, String.valueOf(Calendar.getInstance().getTimeInMillis()), String.valueOf(totalBytes));
                        } catch (Exception SQLException) {

                        }
                        // So something here on success
                    } else {
                        int message = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                        // So something here on failed.
                    }
                }
            }
        }
    }

    private void insertDownloadingData(Context context, String title, String url, String date, String fileSize) {
        db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DownloadEntryFeed.DownloadEntry.COLUMN_NAME_TITLE, title);
        values.put(DownloadEntryFeed.DownloadEntry.COLUMN_DOWNLOAD_URL, url);
        values.put(DownloadEntryFeed.DownloadEntry.COLUMN_DOWNLOADED_DATE, date);
        values.put(DownloadEntryFeed.DownloadEntry.COLUMN_TOTAL_SIZE, fileSize);
        db.insert(DownloadEntryFeed.DownloadEntry.TABLE_NAME, null, values);
        db.close();
    }
}
