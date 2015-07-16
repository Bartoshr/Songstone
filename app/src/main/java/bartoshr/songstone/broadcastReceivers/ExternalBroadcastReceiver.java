package bartoshr.songstone.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;

import bartoshr.songstone.serivces.SongService;

/**
 * Created by bartosh on 16.07.15.
 */
public class ExternalBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        LocalBroadcastManager local = LocalBroadcastManager.getInstance(context);
        String action = intent.getAction();


        if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {

            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);

            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            String intentValue = null;

            switch (keyEvent.getKeyCode()) {

                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    intentValue = SongService.ACTION_TOGGLE;
                    break;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    intentValue = SongService.ACTION_TOGGLE;
                    break;

                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    intentValue = SongService.ACTION_NEXT;
                    break;

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    intentValue = SongService.ACTION_PREV;
                    break;

            }

            // Actually sending the Intent
            if (intentValue != null) {
                Intent broadcastIntent = new Intent(SongService.BROADCAST_ORDER);
                broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_ORDER, intentValue);
                local.sendBroadcast(broadcastIntent);
            }
        }
    }
}