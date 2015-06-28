package com.arekaga.shimdance;

import java.util.ArrayList;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import de.lme.plotview.Plot;
import de.lme.plotview.Plot.PlotStyle;
import de.lme.plotview.PlotView;
import de.lme.plotview.SamplingPlot;


public class DisplayView {
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//   ATTRIBUTES   /////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//corresponding context of display view
	private Context 			mContext;
	
	//number of displayed signals
	private int 				mNumberSignals = 0;
	
	//signal names
	private ArrayList<String>	mStringSignals = null;
	
	//current mode (true = text, false = plot)
	private boolean 			mTextMode = true;
	
	//Layout of display
	private LinearLayout 		mLayout = null;
	
	
	//TEXT MODE
	//View for TextMode
	private TextView [] 		mTextView = null;
	//Layout for sensor values
	private TableLayout 		mSensorTableLayout = null;
	
	//PLOT MODE
	//View for PlotMode
	private PlotView			mPlotView = null; 
	//Plots for PlotView
	private SamplingPlot []		mPlot = null;
	//array of time stamps
	private float []			mTimestamp = null;
	//array for signal values 
	private double []			mSignalData = null;
	//current signal index
	private int 				mCurSignalIdx = -1;
	//length of signal data
	private int 				mSignalDataLength = -1;
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//   METHODS   ////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public DisplayView(Context context, LinearLayout layout) {
		//set layout (defined in calling activity)
		mLayout = layout;
		//set context (defined in calling activity)
		mContext = context;
	}
	
	
	public void setStringSignals(ArrayList<String> str) {
		//set string definition of signals
		mStringSignals = str;
		//set number of signals
		mNumberSignals = mStringSignals.size();
	}
	
	
	public void setTextMode(boolean val) {
		mTextMode = val;
		
	}
	
	
	public void init() {
		//remove views from layout
		mLayout.removeView(mPlotView);
		mLayout.removeView(mSensorTableLayout);
		//initialize selected mode
		if(mTextMode) {
			init_textMode();
		} else {
			init_plotMode();
		}
	}
	
	private void init_plotMode() {
		//initialize plot view
		mPlotView = new PlotView(mContext);
		//array for all signals (one sampling plot for each signal)		
		mPlot = new SamplingPlot[mNumberSignals];
		//loop over all signals
		for(int i = 0; i < mNumberSignals; i++) {
			//initialize sampling plot
			//mPlot[i] = new SamplingPlot("", null, PlotStyle.LINE, 1000);
			mPlot[i] = new SamplingPlot("", Plot.generatePlotPaint( 2f, 255, 38, 126, 202 ), PlotStyle.CROSS, 1000);
			//set axes definition
			mPlot[i].setAxis( "time", " ", 1f, "amplitude", " ", 1f );		
			//attach plot to plot view
			mPlotView.attachPlot(mPlot[i]);
		}
		//add plot view to layout
		mLayout.addView(mPlotView);
	}
	
	
	private void init_textMode() {
		//initialize table layout
		mSensorTableLayout = new TableLayout(mContext);					
		//set parameters (width, height)
		mSensorTableLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		//set table rows (one signal / row)
		TableRow [] tr_row_signaldata = new TableRow[mNumberSignals];
		//array for all text views
		mTextView = new TextView[mNumberSignals];
		//loop over all signals
		for(int i = 0; i < mNumberSignals; i++) {
			//initialize table row
			tr_row_signaldata[i] = new TableRow(mContext);
			//set text view for signal description
			TextView row_desc = new TextView(mContext);
			row_desc.setText(mStringSignals.get(i));
		   	tr_row_signaldata[i].addView(row_desc);
		   	//set text view for signal data
		   	mTextView[i] = new TextView(mContext);
		   	mTextView[i].setText("0.0");
		   	tr_row_signaldata[i].addView(mTextView[i]);
		   	//add row to layout
		   	mSensorTableLayout.addView(tr_row_signaldata[i]);
		}
		//add to layout   
		mLayout.addView(mSensorTableLayout);
	}
	
	
	public void update(int signal_idx, float [] timestamp, double [] array, int length) {
		//set current signal
		mCurSignalIdx 		= signal_idx;
		//set time stamp
		mTimestamp 			= timestamp;
		//set signal data
		mSignalData 		= array;
		//set length of signal data		
		mSignalDataLength 	= length;
		//update desired mode
		if(mTextMode) {
			update_textMode();
		} else {
			update_plotMode();
		}		
	}
	
	
	public void update_textMode() {
		//loop over signal data length
		for(int i = 0; i < mSignalDataLength; i++){
			//update text
		    mTextView[mCurSignalIdx].setText(String.valueOf((int)(mSignalData[i]*100)/100.0) + "  ");
		}
	}
	
	
	public void update_plotMode() {
		//loop over signal data length
		for(int i = 0; i < mSignalDataLength; i++) {
			//update plot
			mPlot[mCurSignalIdx].addValue((float)mSignalData[i], (long)mTimestamp[i]);
		}	
	}

}
