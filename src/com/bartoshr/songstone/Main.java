package com.bartoshr.songstone;

import java.util.ArrayList;
import java.util.HashMap;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
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
import android.view.View.OnClickListener;
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
	public static ListView listView;
	
	// Showing current Song and controls
	public static TextView songLabel;
	public static RelativeLayout layout;
	
	public static Intent songService;
	
	private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;
	
    //Display parameters
    public static Display display;
    public static Point displaySize;
    
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
		
		//get Display size
		getDisplay();

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
		// Set panel on last played Song
		Context context = getApplicationContext();
		
		if (currentSong != -1)
			{
			songLabel.setText(songsList.get(currentSong).get("songTitle"));
			openPanel(context);
			}
		
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
	      	  
	      	  	Context context = getApplicationContext();
	      	  
	        	  powerButton(context, position);
	          }
	        });
	   }
	
	
	  public void setLabel()
	  {
		   	layout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
		   	
		  
			Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ubuntu.ttf");
			songLabel = (TextView) findViewById(R.id.songLabel);
			songLabel.setTypeface(font);
			
			songLabel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					Context context = getApplicationContext();
					powerButton(context, currentSong);
					
				}
			});
	  }
	  
	  public void updateSongs()
	  {
		  SongsManager plm = new SongsManager();
	        this.songsList = plm.ListAllSongs(getApplicationContext());
	  }
	  
	  // Songlist info functions 
	  
	  public static String getSongTitle(int id)
	  {
		  return songsList.get(id).get("songTitle");
	  }
	  
	  
	  public static String getSongPath(int id)
	  {
		  return songsList.get(id).get("songPath");
	  }
	  
	  // Metrics funtions - get display parameters
	  
	  public static int scale(Context context,float dp)
	  {
		  DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		  float fpixels = metrics.density * dp;
		  return  (int) (metrics.density * dp + 0.5f);
	  }
	  
	  private void getDisplay()
	  {
		   display = getWindowManager().getDefaultDisplay();
		   displaySize = new Point();
		   display.getSize(displaySize);
	  }
	  
	  
	   public static void openPanel(Context context)
	   {		  
		   layout.setVisibility(0);

		   LayoutParams lp = (LayoutParams) listView.getLayoutParams();
	       lp.height = displaySize.y-scale(context, 125);
	       listView.setLayoutParams(lp);
	   }
	    
	   /* Song control functions*/
	   
	   
	   public static void playSong(Context context,int id)
	   {
		// for the record
		   currentSong = id;
		   currentSong = (currentSong != -1) ? currentSong : 0;
		  
		   songLabel.setText(songsList.get(currentSong).get("songTitle"));
		   Log("PLAYSONG = "+currentSong);
		   
			//SongSerice
		   songService.setAction(SongService.ACTION_PLAY);
		   context.startService(songService);
		   
	   }
	   
	   public static void powerButton(Context context, int id)
	   {
		   if (id != currentSong || id == -1)
		   {
			   openPanel(context);
			   playSong(context, id);
		   }
		   else
		   {
			   switchSong(context);
		   }
	   }
	   
	   public static void switchSong(Context context) 
	   {
		   Log("SWITCHSONG = "+currentSong);
			songService.setAction(SongService.ACTION_PAUSE);
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
		   Log.i("Songstone", "RESTORE = "+currentSong);
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
	
	// Just for a while
	public static void Log(String s)
	{
		Log.i("Songstone", s);
	}

}
