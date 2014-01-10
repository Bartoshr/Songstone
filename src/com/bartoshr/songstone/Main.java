package com.bartoshr.songstone;

import java.util.ArrayList;
import java.util.HashMap;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {

	// storage and display songs
	public static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	ListView listView;
	
	TextView songLabel;
	
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
		
		setLabel(); 
		
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
	        
	        setListView();
		
	}
	

	
	  public void setListView()
	   {
	        // listening to single list item on click
	        listView.setOnItemClickListener(new OnItemClickListener() {
	          public void onItemClick(AdapterView<?> parent, View view,
	              int position, long id) {
	        	  
	        	  if(currentSong != position)
	        	  	{
	        		  openPanel();
	        	  playSong(position);
	        	  	}
	        	  	else
	        	  		{
	        	  		pause_resume();
	        	  		}
	          }
	        });
	   }
	  
	  public void setLabel()
	  {

			Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ubuntu.ttf");
			songLabel = (TextView) findViewById(R.id.songLabel);
			songLabel.setTypeface(font);
			
	  }
	  
	  // change to 
	  private int scale(float dp)
	  {
		  DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
		  float fpixels = metrics.density * dp;
		  return  (int) (metrics.density * dp + 0.5f);
	  }
	  
	   private void openPanel()
	   {
		   Display display = getWindowManager().getDefaultDisplay();
		   Point size = new Point();
		   display.getSize(size);
		   int screenHeight = size.y;
		   
		   RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
		   layout.setVisibility(0);

		   
		   LayoutParams lp = (LayoutParams) listView.getLayoutParams();
		  Log.d("S",scale(30)+" - wysokoœæ");
	       lp.height = screenHeight-scale(150);
	       listView.setLayoutParams(lp);
	   }
	  
	   
	   
	   /* Song control functions*/
	   
	   
	   public void playSong(int id)
	   {
		   songLabel.setText(songsList.get(id).get("songTitle"));
		   
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
	   
	   
	   /* Preferences functions*/
	   
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
