package varabe.manipulatorcontroller.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.util.Log;

import java.lang.ref.WeakReference;

import varabe.manipulatorcontroller.R;
import varabe.manipulatorcontroller.activity.MainActivity;

import static varabe.manipulatorcontroller.activity.MainActivity.MESSAGE_DEVICE_NAME;
import static varabe.manipulatorcontroller.activity.MainActivity.MESSAGE_STATE_CHANGE;
import static varabe.manipulatorcontroller.activity.MainActivity.MESSAGE_TOAST;
import static varabe.manipulatorcontroller.activity.MainActivity.MESSAGE_WRITE;

public class BluetoothResponseHandler extends Handler {
    private static final String TAG = "BtResponseHandler";
    private WeakReference<MainActivity> mActivity;

    // Messages that inform user of current connection state
    public static String MESSAGE_NOT_CONNECTED;
    public static String MESSAGE_CONNECTING;
    public static String MESSAGE_CONNECTED;

    public BluetoothResponseHandler(MainActivity activity) {
        mActivity = new WeakReference<>(activity);
        MESSAGE_NOT_CONNECTED = activity.getString(R.string.message_not_connected);
        MESSAGE_CONNECTING = activity.getString(R.string.message_connecting);
        MESSAGE_CONNECTED = activity.getString(R.string.message_connected);
    }

    public void setTarget(MainActivity target) {
        mActivity.clear();
        mActivity = new WeakReference<MainActivity>(target);
    }

    @Override
    public void handleMessage(Message msg) {
        // Меняет текст на ActionBar, чтобы оповестить пользователя о смене состояния подключения
        MainActivity activity = mActivity.get();
        if (activity != null) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    final ActionBar bar = activity.getSupportActionBar();
                    switch (msg.arg1) {
                        case DeviceConnector.STATE_CONNECTED:
                            bar.setSubtitle(MESSAGE_CONNECTED);
                            break;
                        case DeviceConnector.STATE_CONNECTING:
                            bar.setSubtitle(MESSAGE_CONNECTING);
                            break;
                        case DeviceConnector.STATE_NONE:
                            bar.setSubtitle(MESSAGE_NOT_CONNECTED);
                            break;
                    }
                    activity.invalidateOptionsMenu();
                    break;

                case MESSAGE_DEVICE_NAME:
                    activity.setDeviceName((String) msg.obj);
                    break;

                case MESSAGE_WRITE:
                    // stub
                    break;

                case MESSAGE_TOAST:
                    // stub
                    break;
            }
        }
        else {
            Log.d(TAG, "handleMessange: Activity not found");
        }
    }

}