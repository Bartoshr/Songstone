package com.bartoshr.songstone;

import java.io.IOException;
import java.util.ArrayList;

import android.R.anim;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;

public class SongService extends Service {

	static enum action {play,pause}
	static boolean is_running = false; // indicate if service running
	
	//Actions 
    public static final String ACTION_PLAY="com.bartoshr.songstone.ACTION_PLAY";
    public static final String ACTION_PAUSE="com.bartoshr.songstone.ACTION_PAUSE";
    public static final String ACTION_NEXT = "com.bartoshr.songstone.ACTION_NEXT";
	public static final String ACTION_PREV="com.bartoshr.songstone.ACTION_PREV";
	public static final String ACTION_FLOW="com.bartoshr.songstone.ACTION_FLOW";
	
	//Notifications stuff
	static NotificationCompat.Builder noteBuilder;
	static NotificationManager noteManager;
	static RemoteViews noteView;
	static int noteID = 1337;
    Notification note;
	   
    // Service own current indicator
    public static int currentSong = -1;
    
	// real player
    MediaPlayer mp = new MediaPlayer();
    
    // Foreground
    boolean isForegroundStarted = false;
    

    
    public SongService() {
		Main.Log("SONGSEVICE");
		is_running = true;
	}
    
    
    
    @Override
	public void onCreate() {
		setForeground();
		setAnimation();
		super.onCreate();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) { 
   
    	String action = intent.getAction();
    	Context context = getApplicationContext();
    	
    	 if (action.equals(ACTION_PLAY)) {
    		 Main.Log("ACTION_PLAY");
             playSong(currentSong);
         }else if(action.equals(ACTION_PAUSE)) {
        	 Main.Log("ACTION_PAUSE");
             switchState();
         }else if (action.equals(ACTION_NEXT)) {
        	 Main.Log("ACTION_NEXT");
        	 nextSong();
         }else if (action.equals(ACTION_PREV)) {
        	 Main.Log("ACTION_PREV");
        	 prevSong();
         }else if (action.equals(ACTION_FLOW)) {
        	 Main.Log("ACTION_FLOW");
        	 flow();
         }
     	
    	return Service.START_NOT_STICKY;
    }

	public void setAnimation()
	{
		Main.Log("SET_ANIMATION");
		ImageView aniView = new ImageView(this);
		
	     aniView.setBackgroundResource(R.drawable.left_arrow_animation);
	     
	     aniView.setBackgroundResource(R.drawable.right_arrow_animation);
	}
	

	

	
	
	   public void playSong(int id)
	   {
		   // just in case
		   id = (id != -1) ? id : 0;
		   currentSong = id;
		   
		   try {
			mp.reset();
			mp.setDataSource(Main.getSongPath(id));
			mp.prepare();
	  	  	mp.start();
	  	  	
		    if(isForegroundStarted)startForeground(1337, note);
		    updateNote(id);
		    updatePanel(id);
	  	  	
			Main.openPanel(getApplicationContext());
		    
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
	   
	   public void nextSong() {
			// Check if last song or not   
			if (++currentSong >= Main.songsList.size()) {
				currentSong = -1;
			} else {
				playSong(currentSong);
			}
		}
	   
		public void prevSong() {
			if (--currentSong >= 0) {
				playSong(currentSong);
			} else {
				playSong(Main.songsList.size()-1);
			}
		}
	   
	   private void flow()
	   {
		   if (currentSong != currentSong 
				   || currentSong == -1)
		   {
			   playSong(currentSong);
		   }
		   else
		   {
			   switchState();
		   }
	   }
	   
	   
		private void setForeground()
		{
			isForegroundStarted = true;
			
			noteView = new RemoteViews(getPackageName(),
	                R.layout.notification);
			noteView.setImageViewResource(R.id.imagenotileft,R.drawable.arrow_left);
		    noteView.setImageViewResource(R.id.imagenotiright,R.drawable.arrow_right);
		    noteView.setTextViewText(R.id.title,"Mr. Sandman - The Chordettes");
			
		    setNoteButtons();
   		
			noteManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			
			noteBuilder = new NotificationCompat.Builder(
	                this);
	        note = noteBuilder
	                .setSmallIcon(R.drawable.ic_launcher)
	                .setWhen(System.currentTimeMillis())
	                .setAutoCancel(false)
	                .setContentTitle("Songstone")
	                .setContentText("This text")
	                .setContent(noteView)
	                .build();
		}
	
		
		private void setNoteButtons()
		{
			Intent intent = new Intent(getApplicationContext(), SongService.class);

	        intent.setAction(ACTION_NEXT);
			PendingIntent pendingIntent = 
					PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			noteView.setOnClickPendingIntent(R.id.imagenotiright, pendingIntent);
			
			
			intent.setAction(ACTION_PREV);
			pendingIntent = 
					PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			noteView.setOnClickPendingIntent(R.id.imagenotileft, pendingIntent);
			
			
			intent = new Intent(getApplicationContext(), Main.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			pendingIntent = 
					PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	        noteView.setOnClickPendingIntent(R.id.title, pendingIntent);
		}
	   
		private void updateNote(int id)
		{
			String title = Main.getSongTitle(id);
				
			note = noteBuilder
		    .setContentTitle(title)
		    .setContentText("is played")
		    .setAutoCancel(false)
		    .setWhen(System.currentTimeMillis()).build();
			
			noteView.setTextViewText(R.id.title, title);
			
			
			 noteManager.notify(noteID, noteBuilder.build());
		}
		
		private void updatePanel(int id)
		{
			String title = Main.getSongTitle(id);
			Main.songLabel.setText(title);
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
