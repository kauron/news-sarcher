package com.kauron.newssarcher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class QueryAdapter extends ArrayAdapter<Query> {

    QueryAdapter(@NonNull Context context, @NonNull ArrayList<Query> objects) {
        super(context, R.layout.query_list_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Query q = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (convertView == null)
            convertView = inflater.inflate(R.layout.query_list_item, parent, false);
        TextView query   = (TextView) convertView.findViewById(R.id.query_text);
        TextView options = (TextView) convertView.findViewById(R.id.options_text);
        TextView result  = (TextView) convertView.findViewById(R.id.result_text);

        if (q != null) {
            query.setText(q.getQuery());
            options.setText(q.getOptions());
            result.setText(q.getShortAnswer());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
