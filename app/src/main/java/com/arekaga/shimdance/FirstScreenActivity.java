package com.arekaga.shimdance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * First activity presented to the user.
 * Contains devices list, start button and if bluetooth is turned off also button to turn it on.
 */
public class FirstScreenActivity extends Activity {

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
    private boolean mIsConnected;
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
     * List of selected SHIMMER device names
     */
    private ArrayList<String> mSelectedDeviceNames;
    /**
     * List of paired SHIMMER devices
     */
    private ArrayList<BluetoothDevice> mPairedShimmerDevices;
    /**
     * List of paired SHIMMER device names
     */
    private ArrayList<String> mPairedShimmerDeviceNames;
    //endregion

    /**
     * Sensor device manager
     */
    private SensorDeviceManager mSensorDeviceManager;

    /**
     * View for displaying signals (TextMode/PlotMode)
     */
    private DisplayView mDisplayView;

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
        // try to activate bluetooth adapter
        activateBluetooth();
        // fill devices list
        fillDevicesList();
    }

    /**
     * Handler for updating GUI
     */
    private final Handler mGUIUpdateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //read message
                case ExampleDataProcessor.MESSAGE_READ:
                    onShimmerProcessorEvent();
                    break;
                //unknown message
                default:
                    Log.e("ExampleActivity", "Unknown message received.");
                    break;
            }
        }
    };

    /**
     * Creates GUI components
     */
    private void createGUIComponents() {
        mIsConnected = false;
        mDevicesListView = (DevicesListView) findViewById(R.id.DevicesListView);
        mInfoTextView = (TextView) findViewById(R.id.InfoTextView);
        mStartButton = (Button) findViewById(R.id.StartButton);
        mEnableBluetoothButton = (Button) findViewById(R.id.EnableBluetoothButton);

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
     * Creates components for chosen display view
     */
    public void setDisplayMode() {
        /*
        //if display view is not present create it
        if (mDisplayView == null) {
            //initialize object for displaying
            mDisplayView = new DisplayView(this, (LinearLayout) findViewById(R.id.linearLayout));
        }
        //set text mode
        mDisplayView.setTextMode(true);

        //set signal definition (YOU HAVE TO SET AXES THAT SHOULD BE DISPLAYED)
        ArrayList<String> str_ax = new ArrayList<String>();

        //iterator for devices
        Iterator<String> iter_names = mSelectedDeviceNames.iterator();

        //index in internal sensors
        int idx_internal_sensors = 0;

        //loop over all selected devices
        while (iter_names.hasNext()) {
            //get next device name
            String name = iter_names.next();

            //accel
            str_ax.add(name + " -> " + "A1 ");
            str_ax.add(name + " -> " + "A2 ");
            str_ax.add(name + " -> " + "A3 ");

            //gyro
            str_ax.add(name + " -> " + "G1 ");
            str_ax.add(name + " -> " + "G2 ");
            str_ax.add(name + " -> " + "G3 ");

            idx_internal_sensors++;
        }

        mDisplayView.setStringSignals(str_ax);

        //initialize display view
        mDisplayView.init();
        */
    }

    /**
     * On Connect Button action
     *
     * @param v Calling view - in this case Connect button
     */
    public void onConnectShimmerClick(View v) {
        //NOT CONNECTED
        if (!mIsConnected) {
            //check if any shimmer sensor was chosen
            if (sensorsChosen()) {
                //set device managers
                mSensorDeviceManager = new SensorDeviceManager(this, mSelectedDevices, mBluetoothAdapter, mInternalSensors, mAccelRanges, mSamplingRates, mGUIUpdateHandler);
                //start SHIMMER sensor
                mSensorDeviceManager.startShimmer();
                //set display mode
                setDisplayMode();
                //set connection state
                mIsConnected = true;
            } else {
                //set connection state
                mIsConnected = false;
                //exit
                return;
            }
            //CONNECTED
        } else {
            //stop processing and disconnect
            mSensorDeviceManager.stopShimmer();
            mIsConnected = false;
            Toast.makeText(this, "Processing stopped and disconnected.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if any device is selected
     *
     * @return True if any device is selected
     */
    private boolean sensorsChosen() {
        //checks if sensors are chosen from list
        if (mSelectedDevices == null || mSelectedDevices.isEmpty()) {
            Toast.makeText(this, "No sensors checked. Choose from list.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Updates display data after getting reading from Shimmer
     */
    public void onShimmerProcessorEvent() {
        //get processing buffer size        
        int proc_size = mSensorDeviceManager.getDataProcessor().getProcessingBufferSize();
        //get processing buffer
        CalibratedData[] data = mSensorDeviceManager.getDataProcessor().getProcessingBuffer();

        //UPDATE DISPLAY VIEW

        //iterator for insertion
        int iter_insert = 0;

        //get number of selected devices
        //int num_devices = mSelectedDeviceNames.size();
        int num_devices = mInternalSensors.length;

        //loop over all selected devices
        for (int i = 0; i < num_devices; i++) {
            int num_samples = 0;
            //get puffer size of current sensor
            for (int j = 0; j < proc_size; j++) {
                //if data packet belongs to current sensor
                if (data[j].id == i) {
                    num_samples++;
                }
            }

            //arrays for signal data - accel
            double[] ax = new double[num_samples];
            double[] ay = new double[num_samples];
            double[] az = new double[num_samples];
            //arrays for signal data - gyro
            double[] gx = new double[num_samples];
            double[] gy = new double[num_samples];
            double[] gz = new double[num_samples];
            //array for timestamp
            float[] timestamp = new float[num_samples];

            //iterator in data
            int iter_data = 0;
            for (int j = 0; j < proc_size; j++) {
                //if data packet belongs to current sensor
                if (data[j].id == i) {
                    //set arrays
                    ax[iter_data] = data[j].accelX;
                    ay[iter_data] = data[j].accelY;
                    az[iter_data] = data[j].accelZ;
                    gx[iter_data] = data[j].gyroX;
                    gy[iter_data] = data[j].gyroY;
                    gz[iter_data] = data[j].gyroZ;
                    timestamp[iter_data] = data[j].timeStamp;

                    iter_data++;
                }
            }

            mDisplayView.update(iter_insert++, timestamp, ax, num_samples);
            mDisplayView.update(iter_insert++, timestamp, ay, num_samples);
            mDisplayView.update(iter_insert++, timestamp, az, num_samples);

            mDisplayView.update(iter_insert++, timestamp, gx, num_samples);
            mDisplayView.update(iter_insert++, timestamp, gy, num_samples);
            mDisplayView.update(iter_insert++, timestamp, gz, num_samples);
        }
    }

    /**
     * Checks Bluetooth status
     *
     * @return 0: no BT adapter present, e.g. simulator;
     * 1: adapter present but cannot be activated
     * 2: adapter present
     */
    private int activateBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //no adapter present
        if (mBluetoothAdapter == null) {
            return 0;
        }
        //BLUETOOTH adapter active
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return 2;
        } else {
            //trying to activate
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return 1;
        }
    }


    /**
     * handles all results coming from other finished avtivities
     *
     * @param requestCode Request code passed by finished activity
     * @param resultCode  Finished activity result code
     * @param data        Additional data passed by finished activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check the right intent
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                fillDevicesList();//create corresponding GUI
            }
        }

        if (requestCode == REQUEST_SENSOR_CONFIG_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                //get SHIMMER sensor configurations
                mSamplingRates = data.getDoubleArrayExtra("samplingRates");
                mAccelRanges = data.getIntArrayExtra("accelRanges");
                mInternalSensors = data.getIntArrayExtra("internalSensors");
            }
        }
    }


}
