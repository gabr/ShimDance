package com.arekaga.shimdance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * First activity presented to the user.
 * Contains devices list, start button and if bluetooth is turned off also button to turn it on.
 */
public class FirstScreenActivity extends Activity {

    //region Bluetooth status enum

    /**
     * Bluetooth status
     */
    private enum BluetoothStatus {
        off, on, missing, connecting
    }
    //endregion

    //region Sensor consts
    public static final int SENSOR_ACCEL = 0x80;
    public static final int SENSOR_GYRO = 0x40;
    public static final int SENSORS = SENSOR_ACCEL | SENSOR_GYRO;
    //endregion

    //region Handlers consts
    public static final int CONNECTED = 1;
    public static final int TIMEOUT = 2;
    public static final int BREAK = 3;
    //endregion

    //region Request constants
    /**
     * Request code for enabling BT
     */
    private static final int REQUEST_ENABLE_BT = 0;
    /**
     * Request code for Sensor Config Activity
     */
    private static final int REQUEST_SENSOR_CONFIG_ACTIVITY = 1;
    //endregion

    //region States
    /**
     * Is Shimmer connected
     */
    private boolean mIsConnecting = false;
    //endregion

    //region GUI elements
    /**
     * Devices list view
     */
    private DevicesListView mDevicesListView;
    /**
     * Information text view
     */
    private TextView mInfoTextView;
    /**
     * Start button
     */
    private Button mStartButton;
    /**
     * Enable bluetooth button
     */
    private Button mEnableBluetoothButton;
    /**
     * Information that there is no bluetooth adapter
     */
    private TextView mNoBluetoothTextView;
    /**
     * Frame around list view
     */
    private View mFrameLayout;
    /**
     * Progress bar for connection
     */
    private ProgressBar mProgress;
    //endregion

    //region Current selection
    /**
     * Array of sampling rates
     */
    private double[] mSamplingRates;
    /**
     * Array of accelerometer range
     */
    private int[] mAccelRanges;
    /**
     * Array of internal sensors
     */
    private int[] mInternalSensors;
    //endregion

    //region Bluetooth elements
    /**
     * BluetoothAdapter for BLUETOOTH access on mobile device
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * List of selected SHIMMER devices
     */
    private ArrayList<BluetoothDevice> mSelectedDevices;
    /**
     * List of paired SHIMMER devices
     */
    private ArrayList<BluetoothDevice> mPairedShimmerDevices;
    /**
     * List of paired SHIMMER device names
     */
    private ArrayList<String> mPairedShimmerDeviceNames;
    //endregion

    //region Connect Timeout Thread
    private static final int CONNECTION_TIMEOUT = 60;
    private ConnectTimeout mConnectTimeoutThread;

    private class ConnectTimeout extends Thread {
        private int mTimeoutSecconds = 1;
        private Handler mHandler = null;

        public ConnectTimeout(int timeoutSecconds, Handler handler) {
            mTimeoutSecconds = timeoutSecconds;
            mHandler = handler;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(mTimeoutSecconds * 1000);

                if (mHandler != null)
                    mHandler.obtainMessage(FirstScreenActivity.TIMEOUT).sendToTarget();
            } catch (Exception e) {}
        }
    }
    //endregion

    private DisplayView mDisplayView;

    /**
     * Sensor device manager
     */
    private SensorDeviceManager mSensorDeviceManager;

    /**
     * Invoke createGUIComponents()
     *
     * @param savedInstanceState Saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);

        //setup GUI
        createGUIComponents();
        // try to activate bluetooth adapter and set application according to state
        manageBluetoothState(activateBluetooth());
    }

    /**
     * On resume checks bluetooth status
     */
    @Override
    protected void onResume() {
        super.onResume();

        // check Bluetooth status
        manageBluetoothState(activateBluetooth());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings && !mIsConnecting) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        } else if (id == R.id.action_secreet) {
            Intent tracks = new Intent(getApplicationContext(), TracksActivity.class);
            startActivity(tracks);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for updating GUI
     */
    private final Handler mOnConnected = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what)
            {
                case FirstScreenActivity.CONNECTED:
                    mConnectTimeoutThread.interrupt();
                    Toast.makeText(getApplicationContext(), "Both devices connected", Toast.LENGTH_SHORT).show();

                    Intent tracks = new Intent(getApplicationContext(), TracksActivity.class);
                    startActivity(tracks);
                    break;

                case FirstScreenActivity.TIMEOUT:
                    mSensorDeviceManager.stopShimmer();
                    Toast.makeText(getApplicationContext(), "Connection timeout", Toast.LENGTH_SHORT).show();
                    break;

                case FirstScreenActivity.BREAK:
                    mConnectTimeoutThread.interrupt();
                    mSensorDeviceManager.stopShimmer();
                    break;
            }

            mIsConnecting = false;

            // enable disabled controls
            manageBluetoothState(BluetoothStatus.on);
        }
    };

    /**
     * Creates GUI components
     */
    private void createGUIComponents() {
        mDevicesListView = (DevicesListView) findViewById(R.id.DevicesListView);
        mInfoTextView = (TextView) findViewById(R.id.InfoTextView);
        mStartButton = (Button) findViewById(R.id.StartButton);
        mEnableBluetoothButton = (Button) findViewById(R.id.EnableBluetoothButton);
        mNoBluetoothTextView = (TextView) findViewById(R.id.NoBluetoothTextView);
        mFrameLayout = findViewById(R.id.FrameLayout);
        mProgress = (ProgressBar) findViewById(R.id.Progress);

        //log status
        Log.i("FirstScreenActivity", "Standard GUI components created.");
    }

    /**
     * Create paired Shimmer Bluetooth devices list
     */
    private void fillDevicesList() {
        //get paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //get paired devices iterator
        Iterator<BluetoothDevice> iter = pairedDevices.iterator();
        //initialize paired SHIMMER devices
        mPairedShimmerDevices = new ArrayList<BluetoothDevice>();

        //loop over all paired devices
        while (iter.hasNext()) {
            //get next device
            BluetoothDevice device = iter.next();
            //get device name
            String name = device.getName();
            //check if it is a SHIMMER sensor device
            if ((name.length() > 4) && (name.substring(0, 4).compareTo("RN42")) == 0) {
                //add SHIMMER sensor
                mPairedShimmerDevices.add(device);
            }
        }

        // add devices to list
        mDevicesListView.addBluetoothDevices(mPairedShimmerDevices);

        //log status
        Log.i("FirstScreenActivity", "Devices list created");
    }

    /**
     * Checks Bluetooth status
     *
     * @return Bluetooth status
     */
    private BluetoothStatus activateBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //no adapter present
        if (mBluetoothAdapter == null) {
            return BluetoothStatus.missing;
        }
        //BLUETOOTH adapter active
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return BluetoothStatus.on;
        } else {
            return BluetoothStatus.off;
        }
    }

    /**
     * Manage GUI elements
     *
     * @param status current Bluetooth status
     */
    private void manageBluetoothState(BluetoothStatus status) {
        switch (status) {
            case off:
                mInfoTextView.setVisibility(View.GONE);
                mDevicesListView.setVisibility(View.GONE);
                mStartButton.setVisibility(View.GONE);
                mEnableBluetoothButton.setVisibility(View.VISIBLE);
                mNoBluetoothTextView.setVisibility(View.GONE);
                mFrameLayout.setVisibility(View.GONE);
                mProgress.setVisibility(View.GONE);
                break;
            case on:
                mInfoTextView.setText(getResources().getString(R.string.InfoTextView));
                mInfoTextView.setVisibility(View.VISIBLE);
                mDevicesListView.setVisibility(View.VISIBLE);
                mStartButton.setVisibility(View.VISIBLE);
                mEnableBluetoothButton.setVisibility(View.GONE);
                mNoBluetoothTextView.setVisibility(View.GONE);
                mFrameLayout.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                fillDevicesList(); // create corresponding GUI
                break;
            case missing:
                mInfoTextView.setVisibility(View.GONE);
                mDevicesListView.setVisibility(View.GONE);
                mStartButton.setVisibility(View.GONE);
                mEnableBluetoothButton.setVisibility(View.GONE);
                mFrameLayout.setVisibility(View.GONE);
                mNoBluetoothTextView.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                break;
            case connecting:
                mInfoTextView.setText(getResources().getString(R.string.InfoTextViewConnectin));
                mInfoTextView.setVisibility(View.VISIBLE);
                mDevicesListView.setVisibility(View.INVISIBLE);
                mStartButton.setVisibility(View.INVISIBLE);
                mEnableBluetoothButton.setVisibility(View.GONE);
                mNoBluetoothTextView.setVisibility(View.GONE);
                mFrameLayout.setVisibility(View.INVISIBLE);
                mProgress.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Enable bluetooth button action
     *
     * @param view
     */
    public void onEnableBluetoothButton(View view) {
        //trying to activate
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    /**
     * Start button action.
     * Checks if two sensor are chosen and starts game activity
     *
     * @param view Calling view - start button
     */
    public void onStartButton(View view) {
        // disconnect if there are already connected devices
        if (mSelectedDevices != null && !mSelectedDevices.isEmpty()) {
            try {
                mSensorDeviceManager.stopShimmer();
            } catch (Exception e) {
                Toast.makeText(this, "Error while stopping previous connection!", Toast.LENGTH_LONG).show();
            }
        }

        // check number of selected devices
        mSelectedDevices = mDevicesListView.getSelectedDevices();
        if (mSelectedDevices.size() != 2) {
            Toast.makeText(this, "Select exactly two devices!", Toast.LENGTH_LONG).show();
            return;
        }

        // setup sensor settings
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        int accelRange = Integer.parseInt(sp.getString(SettingsActivity.ACCEL_RANGE, getResources().getString(R.string.AccelRangeDefaultValue)));
        double samplingRate = Double.parseDouble(sp.getString(SettingsActivity.SAMPLING_RATE, getResources().getString(R.string.SamplingRateDefaultValue)));

        mAccelRanges = new int[] { accelRange, accelRange };
        mSamplingRates = new double[] { samplingRate, samplingRate };
        mInternalSensors = new int[] { SENSORS, SENSORS };


        // two devices are selected so try to connect with them
        //set device managers
        mSensorDeviceManager = new SensorDeviceManager(this, mSelectedDevices, mBluetoothAdapter, mInternalSensors, mAccelRanges, mSamplingRates, samplingRate, mOnConnected);
        //start SHIMMER sensor
        mSensorDeviceManager.startShimmer();

        mIsConnecting = true;

        // show message
        Toast.makeText(this, "Initialize connection...", Toast.LENGTH_SHORT).show();

        // disable controls
        manageBluetoothState(BluetoothStatus.connecting);

        // start timeout
        mConnectTimeoutThread = new ConnectTimeout(CONNECTION_TIMEOUT, mOnConnected);
        mConnectTimeoutThread.start();
    }

    @Override
    public void onBackPressed() {
        if (mIsConnecting) {
            mOnConnected.obtainMessage(FirstScreenActivity.BREAK).sendToTarget();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * handles all results coming from other finished activities
     *
     * @param requestCode Request code passed by finished activity
     * @param resultCode  Finished activity result code
     * @param data        Additional data passed by finished activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check the right intent
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                manageBluetoothState(BluetoothStatus.on);
            } else {
                Toast.makeText(this, "Enabling Bluetooth failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
