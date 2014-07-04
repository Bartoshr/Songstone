package com.bartoshr.songstone;

import java.util.ArrayList;
import java.util.HashMap;

import android.media.AudioManager;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
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
	
	public static TextView songLabel;
	
	public static Intent songService;
	
	private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;
	
    // indicates on current played song
    public static int currentSong = -1; 
	
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
		updateSongs();
		setListView();
		
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(getPackageName(),
                RemoteControlReceiver.class.getName());
        
 
	}
	
	
	@Override
	protected void onStart() {
		
		super.onStart();
	}
	
	  @Override
	protected void onResume() {
		super.onResume();
		mAudioManager.registerMediaButtonEventReceiver(
                mRemoteControlResponder);
	}

	  

	public void setListView()
	   {
		  listView = (ListView)findViewById(R.id.listView);
  
	        ListAdapter adapter = new SimplerAdapter(this, songsList,
	                R.layout.list_item, new String[] { "songTitle" }, new int[] {
	                        R.id.label });
	        	        
	        listView.setAdapter(adapter); 
		  
	        // listening to single list item on click
	        listView.setOnItemClickListener(new OnItemClickListener() {
	          public void onItemClick(AdapterView<?> parent, View view,
	              int position, long id) {
	      	    Log.i("Songstone","service running = "+SongService.is_running);
	      	  Log.i("Songstone","currentSong = "+currentSong);
	        	  if(currentSong != position)
	        	  	{
	        		  openPanel();
	        		  playSong(getApplicationContext(),position);
	        	  	}
	        	  	else
	        	  		{
	        	  		switchSong(getApplicationContext());
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
	  
	  public void updateSongs()
	  {
		  SongsManager plm = new SongsManager();
	        this.songsList = plm.ListAllSongs(getApplicationContext());
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
		   //Log.d("S",scale(30)+" - wysokość");
	       lp.height = screenHeight-scale(150);
	       listView.setLayoutParams(lp);
	   }
	  
	   
	   
	   /* Song control functions*/
	   
	   
	   public static void playSong(Context context,int id)
	   {
		// for the record
		   currentSong = id;
		   currentSong = (currentSong != -1) ? currentSong : 0;
		  
		   songLabel.setText(songsList.get(currentSong).get("songTitle"));
		   Log.i("Songstone", "id - "+id+", currentSong - "+ currentSong);
		   
			//SongSerice
		   songService.putExtra("action", 0 /* play*/);
		   songService.putExtra("id", currentSong);
		   context.startService(songService);
		   
	   }
	   
	   public static void powerButton(Context context, int id)
	   {
		   if (id != currentSong || id == -1)
		   {
			   playSong(context, id);
		   }
		   else
		   {
			   switchSong(context);
		   }
	   }
	   
	   public static void switchSong(Context context) 
	   {
		   Log.i("Songstone", "SwitchSong, currentSong ="+currentSong);
			songService.putExtra("action", 1 /* pause or resume*/);
			context.startService(songService); 
	   }
	  
	   public static void nextSong(Context context) {
			// Check if last song or not   
			if (++currentSong >= songsList.size()) {
				currentSong = -1;
			} else {
				playSong(context,currentSong);
			}
		}
	   
		public static void prevSong(Context context) {
			if (--currentSong >= 0) {
				playSong(context, currentSong);
			} else {
				playSong(context, songsList.size()-1);
			}
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
		   Log.i("Songstone", "Restore CurrentSong ="+currentSong);
	   }

	   
	   @Override
	protected void onDestroy() {
		savePreferences();
		super.onDestroy();
		 mAudioManager.unregisterMediaButtonEventReceiver(
	                mRemoteControlResponder);
	}
	   
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
