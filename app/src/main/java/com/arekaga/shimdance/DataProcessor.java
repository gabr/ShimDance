package com.arekaga.shimdance;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class DataProcessor {

    //region Algorithm
    private static double mYt = 8.2;
    private static double mXtl = 2.2;
    private static double mXtr = 2.2;
    private static double mZtf = 2.2;
    private static double mZtb = 2.1;
    private static double peak = 20;

    private ArrayList<Double> mAccelX;
    private ArrayList<Double> mAccelY;
    private ArrayList<Double> mAccelZ;
    private ArrayList<Float> mTimeStamp;
    //endregion

    //region Filter
    private int mWindowWidth;
    private int mWindowIndex;
    private int mDirectionWidth;
    private double mFrequency;

    private double mFc = 0.001;
    private double mB = 0.09;
    private double mN;
    private double mSumOfW;
    private ArrayList<Double> mArrangedN = new ArrayList<Double>();
    private ArrayList<Double> mH = new ArrayList<Double>();
    private ArrayList<Double> mW = new ArrayList<Double>();
    private ArrayList<Double> mFilter = new ArrayList<Double>();
    //endregion

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   ATTRIBUTES   /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //handlers
    private static ArrayList<Handler> mHandlers = new ArrayList<Handler>();

    //ring buffer and processing buffer
    private CalibratedData[] m_ringBuffer;
    private CalibratedData[] m_processingBuffer;
    private int m_counter;
    private Lock m_dataLock;
    private int m_bufferPosition;

    //buffer constants
    private final int RINGBUFFERSIZE = 400;
    private final int COPYSIZE = 20;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   METHODS   ////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void addHandler(Handler handler) {
        if (handler != null)
            mHandlers.add(handler);
    }

    public static void removeHandler(Handler handler) {
        if (handler != null)
            mHandlers.remove(handler);
    }


    public DataProcessor(double frequency) {
        //initialize ring buffer and processing buffer
        m_ringBuffer = new CalibratedData[RINGBUFFERSIZE];
        m_processingBuffer = new CalibratedData[COPYSIZE];
        m_counter = 0;
        m_bufferPosition = 0;
        m_dataLock = new ReentrantLock();

        //create filter
        createFilter(frequency);
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
        Log.i("Processor", "Data received from SHIMMER sensor");
        //buffer management
        m_bufferPosition = (m_bufferPosition + 1) % RINGBUFFERSIZE;
        m_counter++;
        //unlock ring buffer
        m_dataLock.unlock();
        //log status
        Log.i("ExampleProcessor", String.valueOf(m_counter));
        //if enough data is available, copy it to processing buffer and execute method "onProcess" in a new thread
        if (m_counter == COPYSIZE) {
            //reset counter
            m_counter = 0;
            //lock to prevent parallel access to processing buffer
            m_dataLock.lock();
            //copy the whole array to be processed
            if ((m_bufferPosition - COPYSIZE) >= 0) {
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

        //lock to prevent parallel access to processing buffer and mean values
        m_dataLock.lock();

        CalibratedData[] output = new CalibratedData[2];

        for (int i = 0; i < COPYSIZE; i++) {

            if (m_processingBuffer[i].id != 0 && m_processingBuffer[i].id != 1)
                continue;;1

            if (mTimeStamp.size() == mWindowWidth) {
                mTimeStamp.remove(0);
                mAccelX.remove(0);
                mAccelY.remove(0);
                mAccelZ.remove(0);
            }

            mTimeStamp.add(m_processingBuffer[i].timeStamp);
            mAccelX.add(m_processingBuffer[i].accelX);
            mAccelY.add(m_processingBuffer[i].accelY);
            mAccelZ.add(m_processingBuffer[i].accelZ);

            // calculate direction
            m_processingBuffer[i].direction = extractDirection(m_processingBuffer[i]);

            output[m_processingBuffer[i].id] = m_processingBuffer[i];
        }

        //unlock processing buffer
        m_dataLock.unlock();
        Log.i("ExampleDataProcessor", "extensive computations stopped");

        // call all handlers
        for (Handler h: mHandlers) {
            Message msg = h.obtainMessage();
            // pass them current data from two shimmers
            msg.obj = output;
            msg.sendToTarget();
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

    private double sinc(double x) {
        return Math.sin(x)/x;
    }

    private void createFilter(double frequency) {
        mFrequency = frequency;
        mWindowWidth = (int) (mFrequency / 2.0);
        mWindowIndex = (int) (mWindowWidth / 2.0);
        mDirectionWidth = (int) (mWindowWidth / 4.0);
        mN = (int) (Math.ceil(4 / mB));

        if (((int) mN) % 2 == 0) {
            mN += 1;
        }

        for (int i = 0; i < mN; i++) {
            mArrangedN.add(new Double(i));
        }

        for (double item : mArrangedN) {
            mH.add(sinc(2 * mFc * (item - (mN - 1) / 2.0)));
        }
        for (double item : mArrangedN) {
            mW.add(0.42 - 0.5 * Math.cos(2 * Math.PI * item / (mN - 1)) + 0.08 * Math.cos(4 * Math.PI * item / (mN - 1)));
        }
        for (int i = 0; i < mH.size(); i++) {
            mH.set(i, mH.get(i) * mW.get(i));
        }

        for (int i = 0; i < mH.size(); i++) {
            mSumOfW += mH.get(i);
        }

        for (int i = 0; i < mH.size(); i++) {
            mFilter.add(mH.get(i) / mSumOfW);
        }

        mAccelX = new ArrayList<Double>();
        mAccelY = new ArrayList<Double>();
        mAccelZ = new ArrayList<Double>();
        mTimeStamp = new ArrayList<Float>();
    }

    private String extractDirection(CalibratedData data) {
        float x = (float)data.accelX;
        float y = (float)data.accelY;
        float z = (float)data.accelZ;

        if (Math.abs(x) > peak || Math.abs(y) > peak || Math.abs(z) > peak) {
            return "PIK";
        }
        y = -y;
        String direction = null;
        if (y < mYt) {
            if (x > mXtr) {
                direction = "r";
            }
            if (x < -mXtl) {
                direction = "l";
            }
            if (z > mZtf) {
                direction = "f";
            }
            if (z < -mZtb) {
                direction = "b";
            }
        }

        return direction;
    }

    public static double getmYt() {
        return mYt;
    }

    public static void setmYt(double mYt) {
        DataProcessor.mYt = mYt;
    }

    public static double getmXtl() {
        return mXtl;
    }

    public static void setmXtl(double mXtl) {
        DataProcessor.mXtl = mXtl;
    }

    public static double getmXtr() {
        return mXtr;
    }

    public static void setmXtr(double mXtr) {
        DataProcessor.mXtr = mXtr;
    }

    public static double getmZtf() {
        return mZtf;
    }

    public static void setmZtf(double mZtf) {
        DataProcessor.mZtf = mZtf;
    }

    public static double getmZtb() {
        return mZtb;
    }

    public static void setmZtb(double mZtb) {
        DataProcessor.mZtb = mZtb;
    }

    public static double getPeak() {
        return peak;
    }

    public static void setPeak(double peak) {
        DataProcessor.peak = peak;
    }
}
