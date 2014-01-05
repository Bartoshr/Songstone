package com.bartoshr.songstone;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SimplerAdapter extends SimpleAdapter {

    private ArrayList<HashMap<String, String>> results;
    private Context context;

    public SimplerAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.results = data;
    }

    public View getView(int position, View view, ViewGroup parent){

        Typeface localTypeface1 = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu.ttf");
        View v = view;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item, null);
        }
        TextView tt = (TextView) v.findViewById(R.id.label);
        tt.setText(results.get(position).get("songTitle"));
        tt.setTypeface(localTypeface1);
        return v;
    }
}