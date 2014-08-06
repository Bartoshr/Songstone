package com.bartoshr.songstone;

import java.util.ArrayList;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {

	// storage and display songs
	public static ArrayList<Song> songsList = new ArrayList<Song>();
	public static ListView listView;
	public static StoneAdapter stone;
	
	// Showing current Song and controls
	public static TextView songLabel;
	public static RelativeLayout layout;
	
	public static Intent songService;
	
	private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;

    //Display parameters
    public static Display display;
    public static Point displaySize;
	   
    //Getting the songs
    SongsManager plm;
    Context context;

    //Bluetooth
    BluetoothAdapter bluetoothAdapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		context = getApplicationContext();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				
		//get Display size
		getDisplay();
		
		songService = new Intent(context, SongService.class);
		
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
		

		updateSongs();
		
		// Set panel on last played Song
		
		Log("ONSTART = "+SongService.currentSong);
		if (SongService.currentSong != -1)
			{
			songLabel.setText(songsList.get(SongService.currentSong).getTitle());
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
  	      
		  if(stone == null) stone = new StoneAdapter(getApplicationContext(), songsList);		  
	        listView.setAdapter(stone); 
		  
	        // listening to single list item on click
	        listView.setOnItemClickListener(new OnItemClickListener() {
	          public void onItemClick(AdapterView<?> parent, View view,
	              int position, long id) {
	        	  playSong(context, position);
	          }
	        });
	        
	        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) {
					openDialog(position);
					return false;
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
					
					powerButton(context, SongService.currentSong);
					
				}
			});
	  }
	  
	  public void updateSongs()
	  {
			  plm = new SongsManager();
			  
			  Log("on UpdateSong songList = "+songsList.isEmpty());
			  
			  if (SongService.currentSong != -1 && !songsList.isEmpty()) {
				  
				 				 
				 updateListView();
			  
			  } else if(listView != null) {
				  
				  songsList = plm.ListAllSongs(context);
				  updateListView();
				  
			  } else {
				  songsList = plm.ListAllSongs(context);
			  }
			  
			  Log("UPDATE_LIST - "+songsList.size());
	  }
	  
	  public void updateListView()
	  {
		  stone = new StoneAdapter(context, songsList);
		  listView.setAdapter(stone);
		  ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
	  }
	  
	  public void refresh()
	  {
		  sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				  Uri.parse("file://" + Environment.getExternalStorageDirectory())));
		  
		  SystemClock.sleep(1000);
		  
		  plm = new SongsManager();
		  songsList = plm.ListAllSongs(context);
		  Log("REFRESH - "+songsList.size());
		  
		  updateListView();
	  }
	  
	  // Songlist info functions 
	  
	  public static String getSongTitle(int id)
	  {
		  return songsList.get(id).getTitle();
	  }
	  
	  
	  public static String getSongPath(int id)
	  {
		  return songsList.get(id).getPath();
	  }


     public static String getSongArtist(int id)
    {
        return songsList.get(id).getArtist();
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
		   songLabel.setText(songsList.get(id).getTitle());
		   Log("PLAYSONG = "+id);
		   
		   SongService.currentSong = id;
			//SongSerice
		   songService.setAction(SongService.ACTION_PLAY);
		   context.startService(songService);	   
	   }
	   
	   public static void powerButton(Context context, int id)
	   {
		   songService.setAction(SongService.ACTION_FLOW);
			context.startService(songService); 
	   }
	   
	   public static void switchSong(Context context) 
	   {
			songService.setAction(SongService.ACTION_PAUSE);
			context.startService(songService); 
	   }
	  

	   @Override
	protected void onDestroy() {
		super.onDestroy();
		 mAudioManager.unregisterMediaButtonEventReceiver(
	                mRemoteControlResponder);
	}
	   
	
	   
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		 
	        switch (item.getItemId()) {
	 
	        case R.id.action_refresh:
	        	 Log("ACTION_REFRESH");
	        	 refresh();
	            break;
	        case R.id.action_settings:
	        	 Log("ACTION_SETTINGS");
	            break;
                case R.id.action_bluetooth:
                 turnOnBluetooth();
                break;
	        default:
	 
	        }
	        
		return super.onOptionsItemSelected(item);
	}

	public void openDialog(int position)
	{
		    FragmentTransaction ft = getFragmentManager().beginTransaction();
		    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		    if (prev != null) {
		        ft.remove(prev);
		    }
		    ft.addToBackStack(null);
	    
		    // Create and show the dialog.
		    SongDialog newFragment = SongDialog.newInstance(0);

		    Tagger tag = new Tagger(getSongPath(position));
		    newFragment.setTag(tag);

		    newFragment.show(ft, "dialog");
		    

	}

    public void turnOnBluetooth()
    {
        if(bluetoothAdapter.isEnabled()) bluetoothAdapter.disable();
        else bluetoothAdapter.enable();
    }
	
	// Just for a while
	public static void Log(String s)
	{
		Log.i("Songstone", s);
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
}
