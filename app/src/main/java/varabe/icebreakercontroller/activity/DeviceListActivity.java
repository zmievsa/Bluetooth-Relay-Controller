package varabe.icebreakercontroller.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import varabe.icebreakercontroller.R;


public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";
    private static final boolean D = false;

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter btAdapter;
    private ArrayAdapter<String> foundDevicesArrayAdapter;
    private final Set<String> foundDevicesSet = new HashSet<>();

    private ListView foundDevicesListView;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        // Set default result to CANCELED, in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btDiscoveryReceiver, filter);

        foundDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        foundDevicesListView = findViewById(R.id.found_devices);
        foundDevicesListView.setAdapter(foundDevicesArrayAdapter);
        foundDevicesListView.setOnItemClickListener(deviceClickListener);

        scanButton = findViewById(R.id.button_scan);
        onScanButtonClicked(scanButton); // automatically start discovery
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(btDiscoveryReceiver);
    }
    public void onScanButtonClicked(View view) {
        Log.d(TAG, "scanButton clicked");
        doDiscovery();
        view.setEnabled(false);
    }
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");
        foundDevicesArrayAdapter.clear();
        foundDevicesSet.clear();

        setTitle(R.string.search_in_progress_message);
        if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }
    private final OnItemClickListener deviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            btAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            CharSequence info = ((TextView) v).getText();
            if (info != null) {
                CharSequence address = info.toString().substring(info.length() - 17);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };
    private final BroadcastReceiver btDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Recieved information during discovery: " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG, "Found device during discovery " + device.toString());
                    String address = device.getAddress();
                    if (!foundDevicesSet.contains(address)) {
                        foundDevicesListView.setEnabled(true);
                        foundDevicesSet.add(address);
                        String name = device.getName();
                        if ((name == null) || name.isEmpty()) name = getString(R.string.unknown_device_name);
                        foundDevicesArrayAdapter.add(name + '\n' + device.getAddress());
                    }
                } else {
                    Log.e(TAG, "Could not get parcelable extra from device: " + BluetoothDevice.EXTRA_DEVICE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setTitle(R.string.select_device);
                if (foundDevicesSet.isEmpty()) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    foundDevicesArrayAdapter.add(noDevices);
                    foundDevicesListView.setEnabled(false);
                }
                scanButton.setEnabled(true);
            }
        }
    };
}
