package varabe.brc.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import varabe.brc.PagerAdapter;
import varabe.brc.R;
import varabe.brc.RelayController;
import varabe.brc.bluetooth.BluetoothResponseHandler;
import varabe.brc.relaybutton.RelayButton;

import static varabe.brc.bluetooth.BluetoothResponseHandler.MESSAGE_NOT_CONNECTED;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final String DEVICE_NAME = "DEVICE_NAME";

    // Intent request codes
    static final int REQUEST_CONNECT_DEVICE = 1;
    static final int REQUEST_ENABLE_BT = 2;
    static final int REQUEST_FINE_LOCATION_PERMISSION = 3;

    // Colors
    public static int RELEASED_BUTTON_COLOR;
    public static int PRESSED_BUTTON_COLOR;
    public static int RELEASED_BUTTON_TEXT_COLOR;
    public static int PRESSED_BUTTON_TEXT_COLOR;
    public static int RELEASED_DISABLED_BUTTON_COLOR;
    public static int PRESSED_DISABLED_BUTTON_COLOR;

    private BluetoothAdapter btAdapter;
    private static RelayController relayController;
    public BluetoothResponseHandler handler;
    private String deviceName;

    public static RelayController getRelayController() {
        return relayController;
    }

    public String getDeviceName() {
        return deviceName;
    }

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
        setupInterface();

        RELEASED_BUTTON_COLOR = getResources().getColor(R.color.releasedButtonColor);
        PRESSED_BUTTON_COLOR = getResources().getColor(R.color.pressedButtonColor);
        RELEASED_BUTTON_TEXT_COLOR = getResources().getColor(R.color.releasedButtonTextColor);
        PRESSED_BUTTON_TEXT_COLOR = getResources().getColor(R.color.pressedButtonTextColor);
        RELEASED_DISABLED_BUTTON_COLOR = getResources().getColor(R.color.releasedDisabledButtonColor);
        PRESSED_DISABLED_BUTTON_COLOR = getResources().getColor(R.color.pressedDisabledButtonColor);
        if (relayController.isConnected() && (state != null))
            setDeviceName(state.getString(DEVICE_NAME));
    }

    private void setupInterface() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        RelayButton.clearButtons();
        setupTabLayout();
    }

    private void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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
        // TODO: Come up with a more descriptive name and refactor to divide into more coherent
        // TODO: chunks of code
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
