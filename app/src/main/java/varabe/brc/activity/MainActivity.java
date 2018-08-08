package varabe.brc.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import varabe.brc.R;
import varabe.brc.RelayController;
import varabe.brc.bluetooth.BluetoothResponseHandler;
import varabe.brc.relaybuttons.BlinkingButton;
import varabe.brc.relaybuttons.HoldButton;
import varabe.brc.relaybuttons.MutuallyExclusiveButtonContainer;
import varabe.brc.relaybuttons.MutuallyExclusiveButtonManager;
import varabe.brc.relaybuttons.RelayButton;
import varabe.brc.relaybuttons.SwitchButton;

import static varabe.brc.bluetooth.BluetoothResponseHandler.MESSAGE_NOT_CONNECTED;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final String DEVICE_NAME = "DEVICE_NAME";

    // Message types sent from the DeviceConnector Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Intent request codes
    static final int REQUEST_CONNECT_DEVICE = 1;
    static final int REQUEST_ENABLE_BT = 2;
    static final int REQUEST_FINE_LOCATION_PERMISSION = 3;

    // Colors
    public static int COLOR_GRAY;
    public static int COLOR_RED;

    private BluetoothAdapter btAdapter;
    private static RelayController relayController;
    public BluetoothResponseHandler handler;
    private String deviceName;

    private static final String SAVED_PENDING_REQUEST_ENABLE_BT = "PENDING_REQUEST_ENABLE_BT";
    // do not resend request to enable Bluetooth
    // if there is a request already in progress
    // See: https://code.google.com/p/android/issues/detail?id=24931#c1
    boolean pendingRequestEnableBt = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        if (state != null) {
            pendingRequestEnableBt = state.getBoolean(SAVED_PENDING_REQUEST_ENABLE_BT);
        }
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            showAlertDialog(getString(R.string.no_bt_support));
            Log.d(TAG, "No bluetooth found");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
        }
        if (handler == null) handler = new BluetoothResponseHandler(this);
        else handler.setTarget(this);
        relayController = new RelayController(this);
        setupButtons();

        COLOR_GRAY = getResources().getColor(R.color.colorGray);
        COLOR_RED = getResources().getColor(R.color.colorRed);
        if (relayController.isConnected() && (state != null))
            setDeviceName(state.getString(DEVICE_NAME));
        else {
            setDeviceName(null);
        }
    }

    private void setupButtons() {
        RelayButton.clearButtons();
        RelayButton arrowUp = new BlinkingButton(findViewById(R.id.imageViewArrowUp), relayController);
        RelayButton arrowDown = new BlinkingButton(findViewById(R.id.imageViewArrowDown), relayController);
        RelayButton arrowLeft = new BlinkingButton(findViewById(R.id.imageViewArrowLeft), relayController);
        RelayButton arrowRight = new BlinkingButton(findViewById(R.id.imageViewArrowRight), relayController);
        RelayButton arrowRotateLeft = new BlinkingButton(findViewById(R.id.imageViewArrowRotateLeft), relayController);
        RelayButton arrowRotateRight = new BlinkingButton(findViewById(R.id.imageViewArrowRotateRight), relayController);
        RelayButton audioSignal = new HoldButton(findViewById(R.id.imageViewAudioSignal), relayController);
        RelayButton gasSupply = new SwitchButton(findViewById(R.id.imageViewGasSupply), relayController);
        // When board is still evaluating the last 1 second command (which is very rare),
        // it will result in a bug that will leave one of the relays active. If we wait for
        // the board to finish, the bug has no chance of occurring. Timeout of 1000 millis is the
        // worst case scenario
        int timeout = 1000;
        MutuallyExclusiveButtonContainer container = new MutuallyExclusiveButtonContainer(
                new RelayButton[] {
                        arrowUp, arrowDown, arrowLeft, arrowRight,
                        arrowRotateLeft, arrowRotateRight, audioSignal, gasSupply}, timeout
        );
        container.setPassiveButton(gasSupply);
        container.setPassiveButton(audioSignal);
        MutuallyExclusiveButtonManager MEBManager = new MutuallyExclusiveButtonManager();
        MEBManager.connectMutuallyExclusiveButtons(container);
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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_PENDING_REQUEST_ENABLE_BT, pendingRequestEnableBt);
        outState.putString(DEVICE_NAME, deviceName);
    }
    //    }
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
                    if (relayController.isConnected()) relayController.stopConnection();
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
                    if (isAdapterReady() && (!relayController.isConnected()))
                        relayController.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                pendingRequestEnableBt = false;
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "Enable BT request denied by the user");
                }
                break;
        }
    }
    boolean isAdapterReady() {
        return (btAdapter != null) && (btAdapter.isEnabled());
    }
    private void startDeviceListActivity() {
        if (relayController != null) relayController.stopConnection();
        Intent discoverBtDevicesIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(discoverBtDevicesIntent, REQUEST_CONNECT_DEVICE);
    }
    void showAlertDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setMessage(message);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        ActionBar bar = getSupportActionBar();
        if (deviceName != null) {
            bar.setSubtitle(deviceName);
            RelayButton.setEnabledAllButtons(true);
        } else {
            bar.setSubtitle(MESSAGE_NOT_CONNECTED);
            RelayButton.setEnabledAllButtons(false);
        }
    }
}
