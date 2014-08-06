package com.bartoshr.songstone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

// class for Headset buttons handling

public class RemoteControlReceiver extends BroadcastReceiver {
    
	Intent mIntent;
	
    public RemoteControlReceiver() {
    	super();
    }
    
    public void onReceive(Context context, Intent intent) {
    	if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
    		KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);



                if (mIntent == null) mIntent = new Intent(context, SongService.class);

                if (event.getAction() == 0) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            mIntent.setAction(SongService.ACTION_FLOW);
                            context.startService(mIntent);
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            Main.switchSong(context);
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT: // keyevent value 87
                            Log.i("Songstone", "NEXT_BUTTON");
                            mIntent.setAction(SongService.ACTION_NEXT);
                            context.startService(mIntent);
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS: // keyevent value 88
                            Log.i("Songstone", "PREV_BUTTON");
                            mIntent.setAction(SongService.ACTION_PREV);
                            context.startService(mIntent);
                            break;
                        default:
                            Log.i("Songstone", "Key = " + event.getKeyCode() + " ," + event.getAction());
                            break;
                    }
                }

        }
    }
} 