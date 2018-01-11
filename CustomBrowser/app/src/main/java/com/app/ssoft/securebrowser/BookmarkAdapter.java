package com.app.ssoft.securebrowser;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Shekahar.Shrivastava on 27-Nov-17.
 */

public class BookmarkAdapter extends ArrayAdapter<Bookmark> {
    private final Context context;
    private final ArrayList<Bookmark> bookmarks;


    public BookmarkAdapter(Context context, int resource, ArrayList<Bookmark> bookmarks) {
        super(context, resource);
        this.context = context;
        this.bookmarks = bookmarks;
    }

    @Override
    public int getCount() {
        return this.bookmarks.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout layout = null;
        if (convertView == null) {
            Activity activity = (Activity) context;
            layout = (LinearLayout) activity.getLayoutInflater()
                    .inflate(R.layout.bookmark_list, null);

        } else {
            layout = (LinearLayout) convertView;
        }

        Bookmark e = bookmarks.get(position);
        TextView textURLTitle = (TextView) layout.findViewById(R.id.urlTitle);
        TextView textURL = (TextView) layout.findViewById(R.id.urlText);

        textURLTitle.setText(e.bookmarkTitle);
        textURL.setText(e.bookmarkURL);


        return layout;
    }
}
