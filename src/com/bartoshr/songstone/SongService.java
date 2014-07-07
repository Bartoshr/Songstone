package com.bartoshr.songstone;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.RemoteViews;

public class SongService extends Service {

	static enum action {play,pause}
	static boolean is_running = false; // indicate if service running
	
	//Notifications stuff
	NotificationCompat.Builder noteBuilder;
	NotificationManager noteManager;
	RemoteViews noteView;
	int noteID = 1337;
    Notification note;
	
    // Buttons
    Button next;
    Button prev;
    
	// real player
    MediaPlayer mp = new MediaPlayer();
    
    // Foreground
    boolean isForegroundStarted = false;
    
    public SongService() {
		Log.i("Songstone", "Start SongService");
		is_running = true;
	}
    
    
    
    @Override
	public void onCreate() {
		setForeground();
		super.onCreate();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) { 
   
    	int actionId = intent.getIntExtra("action", 0);
    	action act = action.values()[actionId];
 	
    	switch(act)
    	{ 
    	case play:
        	playSong(intent.getIntExtra("id", 0));
    		break;
    	case pause:
    		switchState();
    		break;
    	}
    	
    	return Service.START_NOT_STICKY;
    }

	   public void playSong(int id)
	   {
		   try {
			mp.reset();
			mp.setDataSource(Main.getSongPath(id));
			mp.prepare();
	  	  	mp.start();
	  	  	
		    if(isForegroundStarted)startForeground(1337, note);
		    updateNote(id);
	  	  	
	  	  	mp.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					Main.nextSong(getApplicationContext());
				}
			});
	  	  	
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
	   
		private void setForeground()
		{
			isForegroundStarted = true;
			
			noteView = new RemoteViews(getPackageName(),
	                R.layout.notification);
			
			
			long eventtime = SystemClock.uptimeMillis(); 
			Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null); 
			KeyEvent upEvent = new KeyEvent(eventtime, eventtime, 
			KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0); 
			intent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent); 
			  
			PendingIntent pendingIntent = 
					PendingIntent.getBroadcast(this, 0, intent, 0);
		
			noteManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			
			noteBuilder = new NotificationCompat.Builder(
	                this);
	        note = noteBuilder
	                .setSmallIcon(R.drawable.ic_launcher)
	                .setWhen(System.currentTimeMillis())
	                .setAutoCancel(true)
	                .setContentTitle("Songstone")
	                .setContentText("This text")
	                .setContent(noteView)
	                .build();
	        
	        noteView.setImageViewResource(R.id.imagenotileft,R.drawable.arrow_left);
	        noteView.setImageViewResource(R.id.imagenotiright,R.drawable.arrow_right);
	        
	        noteView.setOnClickPendingIntent(R.id.imagenotiright, pendingIntent);
	        
	        noteView.setTextViewText(R.id.title,"Mr. Sandman - The Chordettes");
		}
	
		
		private void setNoteButtons()
		{
			//next = noteView.
		}
	   
		private void updateNote(int id)
		{
			String title = Main.getSongTitle(id);
			
			note = noteBuilder
		    .setContentTitle(title)
		    .setContentText("is played")
		    .setAutoCancel(true)
		    .setWhen(System.currentTimeMillis()).build();
			
			noteView.setTextViewText(R.id.title, title);
			
			 noteManager.notify(noteID, noteBuilder.build());
		}
		
	   public void switchState() 
	   {
		   if(mp.isPlaying())
		   {
			   mp.pause();
		   	}
		   else {
			  mp.start(); 
		   }
	   }
	   
	   @Override
	public void onDestroy() {
		mp.stop();
		is_running = false;
		isForegroundStarted = false;
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
