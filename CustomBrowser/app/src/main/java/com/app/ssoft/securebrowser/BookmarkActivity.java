package com.app.ssoft.securebrowser;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private BookmarkAdapter adapter;
    private ListView bookmarkRV;
    private Bookmark bookmark;
    private ArrayList<Bookmark> bookmarkList;
    private String bookmarkURL;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        bookmarkList = new ArrayList<Bookmark>();
        bookmarkRV = (ListView) findViewById(R.id.bookmarkRV);
        bookmarkRV.setOnItemClickListener(this);
        adapter = new BookmarkAdapter(this, android.R.layout.simple_list_item_1, bookmarkList);
        bookmarkRV.setAdapter(adapter);
        showBookmarkList();
    }

    private void showBookmarkList() {


        try {
            db = DBHelper.getInstance(this).getReadableDatabase();

            Cursor bookmarkCursor = db.rawQuery("SELECT * FROM bookmark", null);
            if (!bookmarkCursor.isAfterLast()) {
                bookmarkCursor.moveToFirst();
                while (!bookmarkCursor.isAfterLast()) {
                    Bookmark bookmark = new Bookmark();
                    bookmark.bookmarkTitle = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarkEntryFeed.BookmarkEntry.COLUMN_NAME_TITLE));
                    bookmark.bookmarkURL = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarkEntryFeed.BookmarkEntry.COLUMN_URL));
                    bookmarkList.add(bookmark);
                    bookmarkCursor.moveToNext();

                }
            }
            bookmarkCursor.close();
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
        Cursor bookmarkCursor = db.rawQuery("SELECT bookmark_url FROM bookmark where " + BookmarkEntryFeed.BookmarkEntry.COLUMN_NAME_ENTRY_ID + " = " + (position + 1), null);
        if (!bookmarkCursor.isAfterLast()) {
            bookmarkCursor.moveToFirst();
            bookmarkURL = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarkEntryFeed.BookmarkEntry.COLUMN_URL));
            bookmarkCursor.moveToNext();


        }
        bookmarkCursor.close();
        db.close();
        Intent intent = new Intent();
        intent.putExtra("url", bookmarkURL);
        setResult(1, intent);
        finish();
    }
}
