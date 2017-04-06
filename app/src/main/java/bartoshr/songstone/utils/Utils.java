package bartoshr.songstone.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by bartosh on 04.08.15.
 */
public class Utils {

    public static void toggleBluetooth(Context context)
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        if(bluetoothAdapter.isEnabled()) bluetoothAdapter.disable();
        else bluetoothAdapter.enable();
    }

}
