package varabe.brc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    // Intent request codes
    static final int REQUEST_CONNECT_DEVICE = 1;
    static final int REQUEST_ENABLE_BT = 2;
    static final int REQUEST_FINE_LOCATION_PERMISSION = 3;

    // Message types sent from the DeviceConnector Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    BluetoothAdapter btAdapter;

    private static final String SAVED_PENDING_REQUEST_ENABLE_BT = "PENDING_REQUEST_ENABLE_BT";
    // do not resend request to enable Bluetooth
    // if there is a request already in progress
    // See: https://code.google.com/p/android/issues/detail?id=24931#c1
    boolean pendingRequestEnableBt = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        // getActionBar().setHomeButtonEnabled(false);

        if (state != null) {
            pendingRequestEnableBt = state.getBoolean(SAVED_PENDING_REQUEST_ENABLE_BT);
        }
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            final String no_bluetooth = getString(R.string.no_bt_support);
            showAlertDialog(no_bluetooth);
            Log.d(TAG, "No bluetooth found");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (btAdapter != null && !btAdapter.isEnabled() && !pendingRequestEnableBt) {
            pendingRequestEnableBt = true;
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }


//    @Override
//    public synchronized void onResume() {
//        super.onResume();
//    }
//
//
//    @Override
//    public synchronized void onPause() {
//        super.onPause();
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_PENDING_REQUEST_ENABLE_BT, pendingRequestEnableBt);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_bluetooth:
                if (isAdapterReady()) {
                    if (isConnected()) stopConnection();
                    else startDeviceListActivity();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                return true;
            default:
                Log.d(TAG, "User clicked item in menu that we don't support yet");
                return true;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    Log.d(TAG, "REQUEST_CONNECT_DEVICE result: " + device.toString());
                    //if (isAdapterReady() && (connector == null)) setupConnector(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                pendingRequestEnableBt = false;
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "BT not enabled");
                }
                break;
        }
    }

    boolean isAdapterReady() {
        return (btAdapter != null) && (btAdapter.isEnabled());
    }
    boolean isConnected() {
        // TODO
        return false;
    }
    void stopConnection() {
        // TODO
    }
    private void startDeviceListActivity() {
        stopConnection();
        Intent discoverBtDevicesIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(discoverBtDevicesIntent, REQUEST_CONNECT_DEVICE);
    }

    // TODO: При переконфигурациях будет теряться
    void showAlertDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setMessage(message);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    // ==========================================================================
}
