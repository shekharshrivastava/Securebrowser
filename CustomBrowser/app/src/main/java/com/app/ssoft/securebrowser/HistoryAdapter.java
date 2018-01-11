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
 * Created by Shekahar.Shrivastava on 28-Nov-17.
 */

public class HistoryAdapter extends ArrayAdapter<History> {
    private final Context context;
    private final ArrayList<History> history;


    public HistoryAdapter(Context context, int resource, ArrayList<History> history) {
        super(context, resource);
        this.context = context;
        this.history = history;
    }

    @Override
    public int getCount() {
        return this.history.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout layout = null;
        if (convertView == null) {
            Activity activity = (Activity) context;
            layout = (LinearLayout) activity.getLayoutInflater()
                    .inflate(R.layout.history_list, null);

        } else {
            layout = (LinearLayout) convertView;
        }

        History h = history.get(position);
        TextView textURLTitle = (TextView) layout.findViewById(R.id.urlTitle);
        TextView textURL = (TextView) layout.findViewById(R.id.urlText);

        textURLTitle.setText(h.historyTitle);
        textURL.setText(h.historyURL);


        return layout;
    }
}
