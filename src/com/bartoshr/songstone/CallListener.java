package com.bartoshr.songstone;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by bartosh on 11.01.15.
 */
public class CallListener extends PhoneStateListener {

    Context context;

    CallListener(Context context){
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        if (state == TelephonyManager.CALL_STATE_RINGING) {

            Main.pause(context);
            Log.i("Songstone","CALL_STATE_RINGING");

        }  else if(state == TelephonyManager.CALL_STATE_IDLE) {

            Main.switchSong(context);
            Log.i("Songstone","CALL_STATE_RINGING");

        }

       // super.onCallStateChanged(state, incomingNumber);
    }
}
