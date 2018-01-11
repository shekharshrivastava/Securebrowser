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
 * Created by Shekahar.Shrivastava on 06-Dec-17.
 */

public class DownloadAdapter extends ArrayAdapter<Download> {
    private final Context context;
    private final ArrayList<Download> downloads;


    public DownloadAdapter(Context context, int resource, ArrayList<Download> downloads) {
        super(context, resource);
        this.context = context;
        this.downloads = downloads;
    }

    @Override
    public int getCount() {
        return this.downloads.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout layout = null;
        if (convertView == null) {
            Activity activity = (Activity) context;
            layout = (LinearLayout) activity.getLayoutInflater()
                    .inflate(R.layout.downloading_list_layout, null);

        } else {
            layout = (LinearLayout) convertView;
        }

        Download download = downloads.get(position);
        TextView fileTameTV = (TextView) layout.findViewById(R.id.fileNameTextView);
        TextView fileSize = (TextView) layout.findViewById(R.id.fileSize);
        TextView downloadedDate = (TextView) layout.findViewById(R.id.downloadedDate);

        fileTameTV.setText(download.downloadTitle);
        fileSize.setText(Utils.getFileSize(Long.valueOf(download.downloadedSize)));
        downloadedDate.setText(Utils.getDate(Long.valueOf(download.downloadedDate),"dd MMM "));


        return layout;
    }
}
