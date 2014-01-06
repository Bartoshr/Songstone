package com.bartoshr.songstone;

import java.util.ArrayList;
import java.util.HashMap;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {

	// storage and display songs
	public static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	ListView listView;
	
	Intent songService;
	
    // indicates on current played song
    int currentSong = -1;
	
    //Store settings after restart
    private static final String PREFERENCES_NAME = "StongStonePref";
    private static final String CURRENT_SONG = "CurrentSongPref";
    private SharedPreferences preferences;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// bring back previous settings
		preferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
		restorePreferences();
		
		songService = new Intent(getApplicationContext(), SongService.class);
		
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
	        	  		pause_resume();
	        	  		}
	          }
	        });
	   }
	  
	   public void playSong(int id)
	   {
		   // for the record
		   currentSong = id;
			//SongSerice
		songService.putExtra("action", 0 /* play*/);
		songService.putExtra("id", id);
		getApplicationContext().startService(songService); 
	   }
	   
	   public void pause_resume() 
	   {
		  // for the record
			songService.putExtra("action", 1 /* pause*/);
			getApplicationContext().startService(songService); 
	   }
	   
	   private void savePreferences() {
		    SharedPreferences.Editor preferencesEditor = preferences.edit();
		    preferencesEditor.putInt(CURRENT_SONG, currentSong);
		    preferencesEditor.commit();
		}
	   
	   private void restorePreferences()
	   {
		   currentSong = preferences.getInt(CURRENT_SONG, -1);
	   }

	   
	   @Override
	protected void onDestroy() {
		savePreferences();
		super.onDestroy();
	}
	   
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
