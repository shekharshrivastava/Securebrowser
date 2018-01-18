package com.app.ssoft.securebrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashSet;


public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private HistoryAdapter adapter;
    private ListView historyLV;
    private History history;
    private ArrayList<History> historyList;
    private String historyURL;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyList = new ArrayList<History>();
        LinkedHashSet<History> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(historyList);
        historyList.clear();
        historyList.addAll(linkedHashSet);
        historyLV = (ListView) findViewById(R.id.historyLV);
        historyLV.setOnItemClickListener(this);
        adapter = new HistoryAdapter(this, android.R.layout.simple_list_item_1, historyList);
        historyLV.setAdapter(adapter);
        showHistoryList();
    }

    private void showHistoryList() {
        try {
            db = DBHelper.getInstance(this).getReadableDatabase();

            //we used rawQuery(sql, selectionargs) for fetching all the employees
            Cursor historyCursor = db.rawQuery("SELECT * FROM history", null);
            if (!historyCursor.isAfterLast()) {
                historyCursor.moveToFirst();
                while (!historyCursor.isAfterLast()) {
                    History history = new History();
                    //if the cursor has some data
                    history.historyTitle = historyCursor.getString(historyCursor.getColumnIndex(HistoryEntryFeed.HistoryEntry.COLUMN_NAME_TITLE));
                    history.historyURL = historyCursor.getString(historyCursor.getColumnIndex(HistoryEntryFeed.HistoryEntry.COLUMN_URL));
                    history.favicon = historyCursor.getString(historyCursor.getColumnIndex(HistoryEntryFeed.HistoryEntry.COLUMN_FAVICON));
                    historyList.add(history);
                    historyCursor.moveToNext();

                }
            }
            historyCursor.close();
            db.close();
            adapter.notifyDataSetChanged();
        } catch (Exception SQLException) {

        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            db = DBHelper.getInstance(this).getReadableDatabase();

            Cursor historyCursor = db.rawQuery("SELECT * FROM history where " + HistoryEntryFeed.HistoryEntry.COLUMN_NAME_ENTRY_ID + " = " + (position + 1), null);
            if (!historyCursor.isAfterLast()) {
                historyCursor.moveToFirst();
                historyURL = historyCursor.getString(historyCursor.getColumnIndex(HistoryEntryFeed.HistoryEntry.COLUMN_URL));
                historyCursor.moveToNext();


            }
            historyCursor.close();
            db.close();
            Intent intent = new Intent();
            intent.putExtra("url", historyURL);
            setResult(2, intent);
            finish();
        } catch (Exception SQLException) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation");
            builder.setMessage("Are you sure you want to clear history ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeAll();
                    historyList.clear();
                    MainActivity.clearHistory();
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void removeAll() {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will delete all rows.
        SQLiteDatabase db = DBHelper.getInstance(this).getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        db.delete(HistoryEntryFeed.HistoryEntry.TABLE_NAME, null, null);

    }
}
