package com.bartoshr.songstone;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StoneAdapter extends BaseAdapter {

	private ArrayList<Song> list;
	
	LayoutInflater inflater;
	Context context;
	
	Typeface typeface;
	
	public StoneAdapter(Context context, ArrayList<Song> list) {
		this.context = context;
		this.list = list;
		
		typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu.ttf");
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder;
    	
        if (convertView == null) {            
            convertView = inflater.inflate(R.layout.list_item, null);
            
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.label);
            
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.text.setText(list.get(position).getTitle());
        holder.text.setTypeface(typeface);
        return convertView;
	}
	

	private static class ViewHolder {
        public TextView text;
    }

}
