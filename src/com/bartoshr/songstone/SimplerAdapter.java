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
    
    Typeface mTypeface;
    LayoutInflater mInflater;

    public SimplerAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.results = data;
        
        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu.ttf");
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent){
    	ViewHolder holder;
    	
        if (convertView == null) {            
            convertView = mInflater.inflate(R.layout.list_item, null);
            
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.label);
            
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.text.setText(results.get(position).get("songTitle"));
        holder.text.setTypeface(mTypeface);
        return convertView;
    }
    
    private static class ViewHolder {
        public TextView text;
    }
}