package com.bartoshr.songstone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

// class for Headset buttons handling

public class RemoteControlReceiver extends BroadcastReceiver {
    
    public RemoteControlReceiver() {
    	super();
    }
    
    public void onReceive(Context context, Intent intent) {
    	if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
    		KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
         
            if(event.getAction() == 0)
            {
            	switch(event.getKeyCode())
            	{
            		case KeyEvent.KEYCODE_MEDIA_PLAY:
            			 
    	        		 Log.i("Songstone", "onReceive, CurrentSong ="+Main.currentSong);
    	        		 Main.powerButton(context, Main.currentSong);
            			break;
            		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            			Main.switchSong(context);
            			break;
            		case KeyEvent.KEYCODE_MEDIA_NEXT: // keyevent value 87
            			Log.i("Songstone", "NEXT_BUTTON");
            			Main.nextSong(context);
            			break;
            		case KeyEvent.KEYCODE_MEDIA_PREVIOUS: // keyevent value 88
            			Log.i("Songstone", "PREV_BUTTON");
            			Main.prevSong(context);
            			break;
            		default:
            			 Log.i("Songstone", "Key = "+event.getKeyCode()+ " ,"+event.getAction());
            			 break;
            	}
            }
            
        }
    }
} 