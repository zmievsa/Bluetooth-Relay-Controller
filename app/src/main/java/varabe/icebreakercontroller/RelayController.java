package varabe.icebreakercontroller;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

import varabe.icebreakercontroller.activity.MainActivity;
import varabe.icebreakercontroller.bluetooth.DeviceConnector;

// The class handles command sending and connecting/disconnecting
public class RelayController {
    private static final String TAG = "RelayController";

    public static final String[] SUPPORTED_CHANNELS = new String[] {"A", "B", "C", "D", "E", "F", "H", "I"};

    // Relay commands
    public static final int COMMAND_ONE_SECOND_BLINK = 0;
    public static final int COMMAND_SWITCH = 1;
    public static final int COMMAND_INTERLOCK = 2;
    public static final int COMMAND_OPEN = 3;
    public static final int COMMAND_CLOSE = 4;

    private WeakReference<MainActivity> activity;
    private static DeviceConnector connector;

    public RelayController(MainActivity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public void sendCommand(View view, int command) {
        String relayChannelAssociatedWithView = view.getTag().toString();
        sendCommand(relayChannelAssociatedWithView, command);
    }

    public void sendCommand(String relayChannel, int command) {
        sendCommand(relayChannel + command);
    }

    public void sendCommand(String commandString) {
        if (!commandString.isEmpty() && isConnected()) {
            final String COMMAND_ENDING = "\r\n";
            byte[] command = (commandString + COMMAND_ENDING).getBytes();
            connector.write(command);
        }
    }
    public void deactivateAllAvailibleRelayChannels() {
        for (String channel : SUPPORTED_CHANNELS) {
            sendCommand(channel, COMMAND_OPEN);
        }
    }

    // Connector-related methods
    public void connect(BluetoothDevice connectedDevice) {
        stopConnection();
        MainActivity activity = this.activity.get();
        if (activity != null) {
            try {
                String name = activity.getString(R.string.unknown_device_name);
                DeviceData data = new DeviceData(connectedDevice, name);
                connector = new DeviceConnector(data, activity.handler);
                connector.connect();
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "setupConnector failed: " + e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
    }

    public void stopConnection() {
        MainActivity activity = this.activity.get();
        if (connector != null && activity != null) {
            connector.stop();
            connector = null;
            activity.setDeviceName(null);
        }
    }
}