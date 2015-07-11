package com.arekaga.shimdance;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Shimmer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


@SuppressLint("HandlerLeak")
public class SensorDeviceManager {
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//   ATTRIBUTES   /////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//corresponding activity to display data and stuff
	private Activity						mActivity = null;
	//data processor
	private DataProcessor                   mDataProcessor = null;
    //on connected handler
    private Handler                         mOnConnected = null;
	
	//BLUETOOTH attributes
	private ArrayList<BluetoothDevice>		mSelectedDevices;
	BluetoothAdapter 						mBluetoothAdapter;

	//time stamp management
	private double 							mDeltaTimeStamp;
	private double							mNewTimeStamp;
	private double							mOldTimeStamp;
	private float							mAccTimeStamp;
	private boolean							mFirstTimeStamp;
	private double 							mMaxSamplingRate;
	
	//SHIMEMR devices
	private ArrayList<Shimmer> 				mShimmerDevices;
	private boolean 						mShimmerConnectedFlag = false;
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//   METHODS   ////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//constructor for live mode
	public SensorDeviceManager(Activity activity, 
							   ArrayList<BluetoothDevice> selected_devices, 
							   BluetoothAdapter bluetooth_adapter, 
							   int[] internalSensors,
							   int[] accelRanges, 
							   double[] samplingRates,
                               double frequency,
                               Handler onConnected) {
        //set on connected handler
        mOnConnected = onConnected;
		//the corresponding activity to this processor
		mActivity = activity;
		//set selected BLUETOOTH devices
		mSelectedDevices = selected_devices;
		mBluetoothAdapter = bluetooth_adapter;
		//set SHIMMER devices
		mShimmerDevices = new ArrayList<Shimmer>();
		//iterate over all BLUETOOTH devices and create new SHIMMER devices
		Iterator<BluetoothDevice> iter = mSelectedDevices.iterator();
		for(int i = 0; i < mSelectedDevices.size(); i++) {
    		//get next
    		BluetoothDevice device = iter.next();  		
    		//initialize new shimmer
    		Log.i("SamplingRate", String.valueOf(samplingRates[i]));
    		Log.i("AccelRange", String.valueOf(accelRanges[i]));
    		Log.i("InternalSensors", String.valueOf(internalSensors[i]));
    		Shimmer shimmer = new Shimmer(mActivity, mLiveHandler, 
    									  device.getAddress(), 
    									  samplingRates[i], 
    									  accelRanges[i], 
    									  4, 
    									  internalSensors[i],
    									  false);
    		//add new shimmer to list
    		mShimmerDevices.add(shimmer);	
    	}
		//time stamp management
		mMaxSamplingRate = 0;
		for(int i = 0; i < samplingRates.length; i++) {
			if(samplingRates[i] > mMaxSamplingRate) {
				mMaxSamplingRate = samplingRates[i];
			}
		}
		mDeltaTimeStamp = (32768 / mMaxSamplingRate);
		mOldTimeStamp 	= 0.0;
		mAccTimeStamp 	= 0;
		//set data processor
		this.mDataProcessor = new DataProcessor(frequency);
	}
	
	//handler for incoming live BLUETOOTH data
	private final Handler mLiveHandler = new Handler() {	
		/////////////////////////////////////////////////
		// Do not perform expensive computations here! //
		// Do not modify the GUI from here!			   //
		/////////////////////////////////////////////////		
		public void handleMessage(Message msg) {
			try {  		
				//local data structure
				CalibratedData tempData = new CalibratedData();
	        	//handlers have a what identifier which is used to identify the type of msg
	            switch (msg.what) {
	            	//within each message an object can be include, object clusters are used to represent the data structure of the SHIMMER device
	            	case Shimmer.MESSAGE_READ:
	            		if (msg.obj instanceof ObjectCluster) {
	            			ObjectCluster objectCluster = (ObjectCluster) msg.obj; 
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> timestamp = objectCluster.mPropertyCluster.get("Timestamp"); 
	            			if (timestamp.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(timestamp, "RAW"));
	            				mNewTimeStamp = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> gyroXFormats = objectCluster.mPropertyCluster.get("Gyroscope X");
	            			if (gyroXFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(gyroXFormats, "CAL"));
	            				tempData.gyroX = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> gyroYFormats = objectCluster.mPropertyCluster.get("Gyroscope Y"); 
	            			if (gyroYFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(gyroYFormats, "CAL")); 
	            				tempData.gyroY = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> gyroZFormats = objectCluster.mPropertyCluster.get("Gyroscope Z"); 
	            			if (gyroZFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(gyroZFormats, "CAL")); 
	            				tempData.gyroZ = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> accelXFormats = objectCluster.mPropertyCluster.get("Accelerometer X"); 
	            			if (accelXFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats, "CAL")); 
	            				tempData.accelX = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> accelYFormats = objectCluster.mPropertyCluster.get("Accelerometer Y");  
	            			if (accelYFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats, "CAL")); 
	            				tempData.accelY = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> accelZFormats = objectCluster.mPropertyCluster.get("Accelerometer Z"); 
	            			if (accelZFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats, "CAL")); 
	            				tempData.accelZ = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> ecgFormats = objectCluster.mPropertyCluster.get("ECG");
	            			if (ecgFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ecgFormats, "CAL")); 
	            				tempData.ecg = formatCluster.mData;
	            			}
	            			//first retrieve all the possible formats for the current sensor device
	            			Collection<FormatCluster> emgFormats = objectCluster.mPropertyCluster.get("EMG");
	            			if (emgFormats.size() != 0) {
	            				//retrieve the calibrated data
	            				FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(emgFormats, "CAL")); 
	            				tempData.emg = formatCluster.mData;
	            			}
	            			//save name of the SHIMMER which sends data
	            			tempData.name = objectCluster.mMyName;
	            			//compute right ID and calibrate the time stamp
	            			Iterator<BluetoothDevice> iter = mSelectedDevices.iterator();
	            			int count = 0;
	            			//save correct data to correct IDs and do time stamp calibration
	            			while (iter.hasNext()) {
	            				BluetoothDevice device = iter.next(); 
	            				if (device.getAddress() == objectCluster.mMyName) {
	            					//set ID
	            					tempData.id = count;
            						//set time stamp
            						tempData.timeStamp = calibrateTimeStamp(mNewTimeStamp);
	            					//log status
	    	            			Log.i("SensorDeviceManager", "New live data available!");
	            					//handle new incoming data
	    		            		mDataProcessor.handleNewData(tempData);	
	            				} else {
	            					//address do not match, increment ID
	            					count++;
	            				}
	            			}
	            		} else {
	            			//object has not the right instance
	            			Log.e("SensorDeviceManager", "Object of message is not an ObjectCluster instance.");
	            		}
	            		break;
	                case Shimmer.MESSAGE_TOAST:
	                	Log.i("toast", msg.getData().getString(Shimmer.TOAST));	
	                	//Toast.makeText(mActivity.getApplicationContext(), msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
	                	break;
	                case Shimmer.MESSAGE_ACK_RECEIVED:
	                	Log.i("SensorDeviceManager", "SHIMMER ack message received.");	
	                	break;
	                case Shimmer.MESSAGE_DEVICE_NAME:
	                	Log.i("SensorDeviceManager", "SHIMMER device name received.");	
	                	break;	
	                case Shimmer.MESSAGE_INQUIRY_RESPONSE:
	                	Log.i("SensorDeviceManager", "SHIMMER response inquiried.");	
	                	break;
	                case Shimmer.MESSAGE_SAMPLING_RATE_RECEIVED:
	                	Log.i("SensorDeviceManager", "SHIMMER sampling rate received.");	
	                	break;
	                case Shimmer.MESSAGE_STATE_CHANGE:
	                	Log.i("SensorDeviceManager", "SHIMMER state changed.");	
	                	//differentiate between state changes
						switch (msg.arg1) {
						 	case Shimmer.MSG_STATE_FULLY_INITIALIZED:
								//////////////////////////////////////////////////////////////////////
								// Do not perform expensive computations here! 						//
								// If one these states get lost, the SHIMMER can not work properly! //
								//////////////////////////////////////////////////////////////////////
						 		Log.i("SensorDeviceManager", "SHIMMER state fully initialized.");					 		
						 		//connect shimmer consecutively 
								Iterator<BluetoothDevice> iter_bluetooth = mSelectedDevices.iterator();
								Iterator<Shimmer> iter_shimmer = mShimmerDevices.iterator();
								while(iter_shimmer.hasNext()) {
									Shimmer shimmer = iter_shimmer.next();
									BluetoothDevice device = iter_bluetooth.next();
									Log.i("SensorDeviceManager", "SHIMMER connected.");
									//check if SHIMMER are connected and not streaming
									if ((shimmer.getShimmerState() == Shimmer.STATE_CONNECTED) && (shimmer.getStreamingStatus() == false)) {
										mShimmerConnectedFlag = true;
									} else {
										mShimmerConnectedFlag = false;
									}
									if (shimmer.getShimmerState() != Shimmer.STATE_CONNECTED) {
                                        Toast.makeText(mActivity.getApplicationContext(), "Connected to first device", Toast.LENGTH_SHORT).show();
										//connect the unconnected SHIMMER
										shimmer.connect(device.getAddress(), "default");
										Log.i("SensorDeviceManager", "Connect SHIMMER " + device.getAddress() + " .");
										break;
									}
								}
								//all shimmer 
								if(mShimmerConnectedFlag == true) {
									iter_shimmer = mShimmerDevices.iterator();
									while(iter_shimmer.hasNext()) {
										Shimmer shimmer = iter_shimmer.next();
										shimmer.startStreaming();
										Log.i("SensorDeviceManager", "Start SHIMMER " + shimmer.getDeviceName() + " .");
									}
                                    mOnConnected.obtainMessage(FirstScreenActivity.CONNECTED).sendToTarget();
								}
							    break;
						    case Shimmer.STATE_CONNECTING:
						    	Log.i("SensorDeviceManager", "SHIMMER state is connecting.");
						    	break;
							case Shimmer.STATE_NONE:
								Log.i("SensorDeviceManager", "SHIMMER state is none.");
								break;
							case Shimmer.STATE_CONNECTED:
								Log.i("SensorDeviceManager", "SHIMMER state is connected.");
								break;
							case Shimmer.MSG_STATE_STREAMING:
								Log.i("SensorDeviceManager", "SHIMMER state is started streaming.");
								break;
							case Shimmer.MSG_STATE_STOP_STREAMING:
								Log.i("SensorDeviceManager", "SHIMMER state is stopped streaming.");
							    break;
						 }
						//from "case Shimmer.MESSAGE_STATE_CHANGE"
	                	break;	
	                case Shimmer.MESSAGE_WRITE:
	                	Log.i("SensorDeviceManager", "SHIMMER write message received.");	
	                	break;	
	                case Shimmer.MESSAGE_STOP_STREAMING_COMPLETE:
	                	Log.i("SensorDeviceManager", "SHIMMER stop streaming complete.");	
	                	break;	
	                case Shimmer.MESSAGE_PACKET_LOSS_DETECTED:
	                	Log.i("SensorDeviceManager", "SHIMMER packet lost.");	
	                	break;	
	                default:
	                	Log.e("SensorDeviceManager", "Unknown SHIMMER message received.");
	                	break;
	            }		
			} catch (Exception e) {
				//some unknown error occurred
				Log.e("SensorDeviceManager", "An error occured on SHIMMER sensor data processing!");
				Log.e("SensorDeviceManager", e.toString());
				e.printStackTrace();
			}	
		}
	};
	
	//function to start SHIMMER
	public void startShimmer() {
		//live mode
        //connect only first SHIMMER device, all other devices have to be connected consecutively in the handler
        String deviceAddress = mSelectedDevices.get(0).getAddress().toString();
        mShimmerDevices.get(0).connect(deviceAddress, "default");
        Log.i("SensorDeviceManager", "Connect SHIMMER " + deviceAddress + " .");
	}
		
	
	//function to stop SHIMMER
	public void stopShimmer() {
		//live mode
        //stop streaming and disconnect SHIMMER
        Iterator<Shimmer> iter_shimmer = mShimmerDevices.iterator();
        while (iter_shimmer.hasNext()) {
            try {
                Shimmer shimmer = iter_shimmer.next();
                shimmer.stopStreaming();
                Log.i("SensorDeviceManager", "Streaming stopped.");
                shimmer.stop();
                Log.i("SensorDeviceManager", "Disconnected.");
            } catch (Exception e) { }
        }
		//set SHIMEMR state
		mShimmerConnectedFlag = false;
	}

	
	//function to calibrated time stamp
	private float calibrateTimeStamp(double timeStamp) {
		//compute time stamp
		if (mFirstTimeStamp) {
			//first time stamp can be every known positive timer counter value 
			mAccTimeStamp += (float)(1000.0 / mMaxSamplingRate);
			mOldTimeStamp = timeStamp;
			mFirstTimeStamp = false;
		} else {
			if (timeStamp > mOldTimeStamp) {
				//normally timer counter value increases
				mAccTimeStamp += (float)((((timeStamp - mOldTimeStamp) / mDeltaTimeStamp) * 1000.0) / mMaxSamplingRate);
				mOldTimeStamp = timeStamp;
			} else {
				//handle timer counter overflow 
				mAccTimeStamp += (float)(1000 / mMaxSamplingRate);
				mOldTimeStamp = timeStamp;
			}
		}
		return mAccTimeStamp;
	}
		
	
	//function to get data processor
	public DataProcessor getDataProcessor() {
		return this.mDataProcessor;
	}

}
