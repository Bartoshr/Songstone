package com.bartoshr.songstone;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class SongService extends Service {

	static enum action {play,pause}
	
	// real player
    MediaPlayer mp = new MediaPlayer();
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { 
    	
    	int action_id = intent.getIntExtra("action", 0);
    	action act = action.values()[action_id];
    	
    	switch(act)
    	{
    	case play:
        	int song_id = intent.getIntExtra("id", 0);
        	playSong(song_id);
    		break;
    	case pause:
    			pause_resume();
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
	   
	   
	   public void pause_resume() 
	   {
		   if(mp.isPlaying())
		   {
			   mp.pause();
		   	}else
		   {
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
