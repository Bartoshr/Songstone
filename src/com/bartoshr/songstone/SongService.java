package com.bartoshr.songstone;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Notification;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SongService extends Service {

	static enum action {play,pause}
	
	// real player
    MediaPlayer mp = new MediaPlayer();
    
    public SongService() {
		Log.i("Songstone", "Constructor");
		
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
			mp.setDataSource(Main.songsList.get(id).get("songPath"));
			mp.prepare();
	  	  	mp.start();
	  	  	
	  	  	setForeground();
	  	  	
	  	  	mp.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					nextSong();
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
			Intent notificationIntent = new Intent(this, Main.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
	                this);
	        Notification note = builder.setContentIntent(pendingIntent)
	                .setSmallIcon(R.drawable.ic_launcher).setTicker("Music Playing").setWhen(System.currentTimeMillis())
	                .setAutoCancel(true).setContentTitle("Songstone")
	                .setContentText("This text").build();
	        
		     startForeground(1337, note);
		}
	   
		private void nextSong() {
			// Check if last song or not
			if (++Main.currentSong >= Main.songsList.size()) {
				Main.currentSong = 0;
			} else {
				playSong(Main.currentSong);
			}
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
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
