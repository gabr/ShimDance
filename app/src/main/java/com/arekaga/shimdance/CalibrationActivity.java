package com.arekaga.shimdance;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.lme.plotview.Plot;
import de.lme.plotview.PlotView;
import de.lme.plotview.SamplingPlot;


public class CalibrationActivity extends Activity {

    private EditText mEtY;
    private EditText mEtPeak;
    private EditText mEtXtl;
    private EditText mEtXtr;
    private EditText mEtZtf;
    private EditText mEtZtb;
    private TextView mOutputTextView1;
    private TextView mOutputTextView2;

    private PlotView mPlotView1;
    private PlotView mPlotView2;
    private SamplingPlot[] mPlot1;
    private SamplingPlot[] mPlot2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        mEtY = (EditText) findViewById(R.id.etY);
        mEtPeak = (EditText) findViewById(R.id.etPeak);
        mEtXtl = (EditText) findViewById(R.id.etXtl);
        mEtXtr = (EditText) findViewById(R.id.etXtr);
        mEtZtf = (EditText) findViewById(R.id.etZtf);
        mEtZtb = (EditText) findViewById(R.id.etZtb);
        mPlotView1 = (PlotView) findViewById(R.id.plot1);
        mPlotView2 = (PlotView) findViewById(R.id.plot2);

        mOutputTextView1 = (TextView) findViewById(R.id.outptTextView1);
        mOutputTextView1.setMovementMethod(new ScrollingMovementMethod());

        mOutputTextView2 = (TextView) findViewById(R.id.outptTextView2);
        mOutputTextView2.setMovementMethod(new ScrollingMovementMethod());

        mEtY.setText(Double.toString(DataProcessor.getmYt()));
        mEtPeak.setText(Double.toString(DataProcessor.getPeak()));
        mEtXtl.setText(Double.toString(DataProcessor.getmXtl()));
        mEtXtr.setText(Double.toString(DataProcessor.getmXtr()));
        mEtZtf.setText(Double.toString(DataProcessor.getmZtf()));
        mEtZtb.setText(Double.toString(DataProcessor.getmZtb()));

        mPlot1 = new SamplingPlot[3];
        for (int i = 0; i < 3; i++) {
            //mPlot1[i] = new SamplingPlot("", null, Plot.PlotStyle.LINE, 1000);
            mPlot1[i] = new SamplingPlot("", Plot.generatePlotPaint(2f, 255, i == 0 ? 255 : 0, i == 1 ? 255 : 0, i == 2 ? 255 : 0), Plot.PlotStyle.CROSS, 1000);
            //set axes definition
            mPlot1[i].setAxis( "time", " ", 1f, "amplitude", " ", 1f );
            //attach plot to plot view
            mPlotView1.attachPlot(mPlot1[i]);
        }

        mPlot2 = new SamplingPlot[3];
        for (int i = 0; i < 3; i++) {
            //mPlot2[i] = new SamplingPlot("", null, Plot.PlotStyle.LINE, 1000);
            mPlot2[i] = new SamplingPlot("", Plot.generatePlotPaint(2f, 255, i == 0 ? 255 : 0, i == 1 ? 255 : 0, i == 2 ? 255 : 0), Plot.PlotStyle.CROSS, 1000);
            //set axes definition
            mPlot2[i].setAxis( "time", " ", 1f, "amplitude", " ", 1f );
            //attach plot to plot view
            mPlotView2.attachPlot(mPlot2[i]);
        }

        DataProcessor.addHandler(mOnData);
    }

    @Override
    public void onStop() {
        DataProcessor.removeHandler(mOnData);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private Handler mOnData = new Handler() {

        public void handleMessage(Message msg) {
            // obtain data from msg
            CalibratedData[] data = (CalibratedData[]) msg.obj;

            if (data[0] != null) {
                mPlot1[0].addValue((float) data[0].accelX, (long) data[0].timeStamp);
                mPlot1[1].addValue((float) data[0].accelY, (long) data[0].timeStamp);
                mPlot1[2].addValue((float) data[0].accelZ, (long) data[0].timeStamp);
            }

            if (data[1] != null) {
                mPlot2[0].addValue((float) data[1].accelX, (long) data[1].timeStamp);
                mPlot2[1].addValue((float) data[1].accelY, (long) data[1].timeStamp);
                mPlot2[2].addValue((float) data[1].accelZ, (long) data[1].timeStamp);
            }

            if (mOutputTextView1.getLineCount() > 2)
                mOutputTextView1.setText("");

            if (mOutputTextView2.getLineCount() > 2)
                mOutputTextView2.setText("");

            if (data[0] != null && data[0].direction != null) {
                mOutputTextView1.append(" " + data[0].direction);
            }

            if (data[1] != null && data[1].direction != null) {
                mOutputTextView2.append(" " + data[1].direction);
            }
        }
    };

    public void saveConfiguration(View view) {
        try {
            double Y = Double.parseDouble(mEtY.getText().toString());
            double Peak = Double.parseDouble(mEtPeak.getText().toString());
            double Xtl = Double.parseDouble(mEtXtl.getText().toString());
            double Xtr = Double.parseDouble(mEtXtr.getText().toString());
            double Ztf = Double.parseDouble(mEtZtf.getText().toString());
            double Ztb = Double.parseDouble(mEtZtb.getText().toString());

            DataProcessor.setmYt(Y);
            DataProcessor.setPeak(Peak);
            DataProcessor.setmXtl(Xtl);
            DataProcessor.setmXtr(Xtr);
            DataProcessor.setmZtf(Ztf);
            DataProcessor.setmZtb(Ztb);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Wrong data", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
}
