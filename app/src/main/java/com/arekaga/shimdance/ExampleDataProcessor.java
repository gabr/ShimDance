package com.arekaga.shimdance;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.util.Log;


public class ExampleDataProcessor {
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//   ATTRIBUTES   /////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//handler for communication with the GUI
	public static final int 				MESSAGE_READ = 1;
	private final Handler					m_handler;
	
	//ring buffer and processing buffer
	private CalibratedData[] 				m_ringBuffer;
	private CalibratedData[] 				m_processingBuffer;
	private int 							m_counter;
	private Lock 							m_dataLock;
	private int 							m_bufferPosition;
	
	//buffer constants
	private final int 						RINGBUFFERSIZE 	= 400;
	private final int 						COPYSIZE 		= 20;

	//example processing values
	private double[] 						m_meanValues1 	= {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};	
	private double[] 						m_meanValues2 	= {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//   METHODS   ////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ExampleDataProcessor(Handler handler) {
		//set handler for notifying the GUI 
		this.m_handler = handler;
		//initialize ring buffer and processing buffer
		m_ringBuffer 		= new CalibratedData[RINGBUFFERSIZE];
		m_processingBuffer	= new CalibratedData[COPYSIZE];
		m_counter 			= 0;
		m_bufferPosition 	= 0;
		m_dataLock 			= new ReentrantLock();
		
	}
	
	
	public CalibratedData[] getProcessingBuffer() {
		return m_processingBuffer;
	}
	
	
	public int getProcessingBufferSize() {
		return COPYSIZE;
	}
	
	
	public void handleNewData(CalibratedData data) {
		//lock to prevent parallel access to ring buffer
		m_dataLock.lock();
		//collect data from first sensor in list, calibrate and save in local data structure
		m_ringBuffer[m_bufferPosition] = data;
		//log status
		Log.i("Processor", "Data received from SHIMMER sensor or from simulation mode.");    
		//buffer management
		m_bufferPosition = (m_bufferPosition + 1) % RINGBUFFERSIZE;
		m_counter++;
		//unlock ring buffer
		m_dataLock.unlock();
		//log status
		Log.i("ExampleProcessor", String.valueOf(m_counter));
		//if enough data is available, copy it to processing buffer and execute method "onProcess" in a new thread
		if(m_counter == COPYSIZE) {
			//reset counter
			m_counter = 0;
			//lock to prevent parallel access to processing buffer
			m_dataLock.lock();
			//copy the whole array to be processed
			if((m_bufferPosition - COPYSIZE) >= 0) {
				System.arraycopy(m_ringBuffer, (m_bufferPosition - COPYSIZE), m_processingBuffer, 0, COPYSIZE);
			} else {
				System.arraycopy(m_ringBuffer, (RINGBUFFERSIZE - (COPYSIZE - m_bufferPosition)), m_processingBuffer, 0, (COPYSIZE - m_bufferPosition));
				System.arraycopy(m_ringBuffer, 0, m_processingBuffer, (COPYSIZE - m_bufferPosition), m_bufferPosition);
			}			
			//log status
			Log.i("ExampleProcessor", "arraycopy");
			//unlock ring buffer
			m_dataLock.unlock();
			/////////////////////////////
			// Schedule the processing //
			/////////////////////////////
			scheduleProcessing();
		}
	}
	

	protected void onProcess() {
		///////////////////////////////////////////////////////
		// This methods performs more extensive computations //
		///////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Called in a separate thread after scheduleProcessing() was called.              //
		// You can do expensive computations on the processing buffer here.				   //
		// Keep in mind that the ring buffer may change during execution of this function! //
		// Do not modify the GUI from here!												   //
		/////////////////////////////////////////////////////////////////////////////////////
		Log.i("ExampleDataProcessor", "extensive computations started");
		int values1 = 0;
		double sum_accel_x_1 = 0.0;
		double sum_accel_y_1 = 0.0;
		double sum_accel_z_1 = 0.0;
		double sum_gyro_x_1 = 0.0;
		double sum_gyro_y_1 = 0.0;
		double sum_gyro_z_1 = 0.0;
		double sum_ecg_1 = 0.0;
		double sum_emg_1 = 0.0;		
		int values2 = 0;
		double sum_accel_x_2 = 0.0;
		double sum_accel_y_2 = 0.0;
		double sum_accel_z_2 = 0.0;
		double sum_gyro_x_2 = 0.0;
		double sum_gyro_y_2 = 0.0;
		double sum_gyro_z_2 = 0.0;
		double sum_ecg_2 = 0.0;
		double sum_emg_2 = 0.0;	
		//lock to prevent parallel access to processing buffer and mean values
		m_dataLock.lock();		
		//sum up the values on processing buffer for every sensor
		for(int i = 0; i < COPYSIZE; i++) {
			if(m_processingBuffer[i].id == 0) {
				values1++;
				sum_accel_x_1 = sum_accel_x_1 + m_processingBuffer[i].accelX;
				sum_accel_y_1 = sum_accel_y_1 + m_processingBuffer[i].accelY;
				sum_accel_z_1 = sum_accel_z_1 + m_processingBuffer[i].accelZ;
				sum_gyro_x_1 = sum_gyro_x_1 + m_processingBuffer[i].gyroX;
				sum_gyro_y_1 = sum_gyro_y_1 + m_processingBuffer[i].gyroY;
				sum_gyro_z_1 = sum_gyro_z_1 + m_processingBuffer[i].gyroZ;
				sum_ecg_1 = sum_ecg_1 + m_processingBuffer[i].ecg;
				sum_emg_1 = sum_emg_1 + m_processingBuffer[i].emg;
			}
			if(m_processingBuffer[i].id == 1) {
				values2++;
				sum_accel_x_2 = sum_accel_x_2 + m_processingBuffer[i].accelX;
				sum_accel_y_2 = sum_accel_y_2 + m_processingBuffer[i].accelY;
				sum_accel_z_2 = sum_accel_z_2 + m_processingBuffer[i].accelZ;
				sum_gyro_x_2 = sum_gyro_x_2 + m_processingBuffer[i].gyroX;
				sum_gyro_y_2 = sum_gyro_y_2 + m_processingBuffer[i].gyroY;
				sum_gyro_z_2 = sum_gyro_z_2 + m_processingBuffer[i].gyroZ;
				sum_ecg_2 = sum_ecg_2 + m_processingBuffer[i].ecg;
				sum_emg_2 = sum_emg_2 + m_processingBuffer[i].emg;
			}
		}	
		//compute mean for each sensor and save in class variable
		if(values1 != 0) {
			m_meanValues1[0] = (double) sum_accel_x_1 / values1;
			m_meanValues1[1] = (double) sum_accel_y_1 / values1;
			m_meanValues1[2] = (double) sum_accel_z_1 / values1;
			m_meanValues1[3] = (double) sum_gyro_x_1 / values1;
			m_meanValues1[4] = (double) sum_gyro_y_1 / values1;
			m_meanValues1[5] = (double) sum_gyro_z_1 / values1;
			m_meanValues1[6] = (double) sum_ecg_1 / values1;
			m_meanValues1[7] = (double) sum_emg_1 / values1;
		}
		if(values2 != 0) {
			m_meanValues2[0] = (double) sum_accel_x_2 / values2;
			m_meanValues2[1] = (double) sum_accel_y_2 / values2;
			m_meanValues2[2] = (double) sum_accel_z_2 / values2;
			m_meanValues2[3] = (double) sum_gyro_x_2 / values2;
			m_meanValues2[4] = (double) sum_gyro_y_2 / values2;
			m_meanValues2[5] = (double) sum_gyro_z_2 / values2;
			m_meanValues2[6] = (double) sum_ecg_2 / values2;
			m_meanValues2[7] = (double) sum_emg_2 / values2;
		}	
		//unlock processing buffer
		m_dataLock.unlock();	
		Log.i("ExampleDataProcessor", "extensive computations stopped");
		/////////////////////////////////////////////////////////////
		// Notify the GUI that the complex computation is finished //
		/////////////////////////////////////////////////////////////			
		m_handler.obtainMessage(MESSAGE_READ).sendToTarget();
	}


  	public double[] getMeanValues(int flag) {
  		//get method for meanValues
  		if (flag == 0) {
  			return m_meanValues1;
  		} else {
  			return m_meanValues2;
  		}
  	}	
  	
  	
	public void scheduleProcessing() {
		//schedule the comprehensive processing task in a separate thread
		new Thread(new Runnable() {
			public void run() {
				try {
					onProcess();
				} catch (Exception e) {
					return;
				}
			}
		}).start();
	}
	
}
