package com.bartoshr.songstone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {
    
    public RemoteControlReceiver() {
    	super();
    	Log.i("Songstone", "Hello");
    }
    
    public void onReceive(Context context, Intent intent) {
    	if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
    		KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.i("Songstone", "Key = "+event.getKeyCode());
        }
    }
} 