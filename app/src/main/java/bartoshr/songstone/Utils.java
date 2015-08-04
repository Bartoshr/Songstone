package bartoshr.songstone;

import android.bluetooth.BluetoothAdapter;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by bartosh on 04.08.15.
 */
public class Utils {

    public static void toggleBluetooth()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled()) bluetoothAdapter.disable();
        else bluetoothAdapter.enable();
    }

}
