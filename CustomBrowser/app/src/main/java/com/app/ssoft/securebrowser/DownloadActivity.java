package com.app.ssoft.securebrowser;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private DownloadAdapter adapter;
    private ArrayList<Bookmark> bookmarkList;
    private String bookmarkURL;
    private ListView downloadRV;
    private ArrayList<Download> downloadList;
    private ProgressBar downloadingProgress;
    private String downloadFileTitle;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        downloadList = new ArrayList<Download>();
        downloadRV = (ListView) findViewById(R.id.downloadRV);
        downloadingProgress = (ProgressBar) findViewById(R.id.downloadingProgress);
        downloadRV.setOnItemClickListener(this);
        adapter = new DownloadAdapter(this, android.R.layout.simple_list_item_1, downloadList);
        downloadRV.setAdapter(adapter);
        showDownloadList();
    }

    private void showDownloadList() {


        try {
            db = DBHelper.getInstance(this).getReadableDatabase();
            Cursor downloadCursor = db.rawQuery("SELECT * FROM download", null);
            if (!downloadCursor.isAfterLast()) {
                downloadCursor.moveToFirst();
                while (!downloadCursor.isAfterLast()) {
                    Download download = new Download();
                    download.downloadTitle = downloadCursor.getString(downloadCursor.getColumnIndex(DownloadEntryFeed.DownloadEntry.COLUMN_NAME_TITLE));
                    download.downloadedDate = downloadCursor.getString(downloadCursor.getColumnIndex(DownloadEntryFeed.DownloadEntry.COLUMN_DOWNLOADED_DATE));
                    download.downloadedSize = downloadCursor.getString(downloadCursor.getColumnIndex(DownloadEntryFeed.DownloadEntry.COLUMN_TOTAL_SIZE));

                    downloadList.add(download);
                    downloadCursor.moveToNext();

                }
            }
            downloadCursor.close();
            db.close();
            adapter.notifyDataSetChanged();
        } catch (Exception SQLException) {

        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            db = DBHelper.getInstance(this).getReadableDatabase();
        } catch (Exception SQLException) {

        }
        Cursor downloadCursor = db.rawQuery("SELECT download_title FROM download where " + DownloadEntryFeed.DownloadEntry.COLUMN_NAME_ENTRY_ID + " = " + (position + 1), null);
        if (!downloadCursor.isAfterLast()) {
            downloadCursor.moveToFirst();
            downloadFileTitle = downloadCursor.getString(downloadCursor.getColumnIndex(DownloadEntryFeed.DownloadEntry.COLUMN_NAME_TITLE));
            downloadCursor.moveToNext();


        }
        downloadCursor.close();
        db.close();
        openFile(Environment.DIRECTORY_DOWNLOADS + File.separator + "secure downloads" + File.separator + downloadFileTitle);

      /*  Intent intent = new Intent();
        intent.putExtra("title", historyFileTitle);
        setResult(1, intent);
        finish();*/
    }

    protected void openFile(String filePath) {
        File file = new File(filePath);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "*/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(file);

        intent.setDataAndType(data, type);

        startActivity(intent);
    }
}