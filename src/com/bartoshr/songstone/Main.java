package com.bartoshr.songstone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {

	// storage and display songs
	ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	ListView listView;
	
	// real player
    MediaPlayer mp = new MediaPlayer();
    
    // indicates on current played song
    int currentSong = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		

		
		listView = (ListView)findViewById(R.id.listView);
		
		 ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
	
	        SongsManager plm = new SongsManager();
	        this.songsList = plm.ListAllSongs(getApplicationContext());
	 
	        for (int i = 0; i < songsList.size(); i++) {
	            HashMap<String, String> song = songsList.get(i);
	            songsListData.add(song);
	        }
	        
	        
	        ListAdapter adapter = new SimplerAdapter(this, songsListData,
	                R.layout.list_item, new String[] { "songTitle" }, new int[] {
	                        R.id.label });
	        
	        
	        listView.setAdapter(adapter);
	        
	        initListView();
		
	}
	

	
	  public void initListView()
	   {
	        // listening to single list item on click
	        listView.setOnItemClickListener(new OnItemClickListener() {
	          public void onItemClick(AdapterView<?> parent, View view,
	              int position, long id) {
	        	  
	        	  if(currentSong != position)
	        	  	{
	        	  Toast.makeText(getApplicationContext(),songsList.get(position).get("songTitle"), Toast.LENGTH_SHORT).show();
	        	  playSong(position);
	        	  	}
	        	  	else
	        	  		{
	        	  		pause();
	        	  		}
	          }
	        });
	   }
	  
	   public void playSong(int id)
	   {
		   try {
			mp.reset();
			mp.setDataSource(songsList.get(id).get("songPath"));
			mp.prepare();
	  	  	mp.start();
	  	  	
	  	  	currentSong = id;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   public void pause() 
	   {
	   mp.pause();
	   currentSong = -1;
	   }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
