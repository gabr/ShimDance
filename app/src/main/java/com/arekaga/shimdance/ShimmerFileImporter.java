/**
 * 
 * ShimmerFileImporter
 * Copyright (C) 2014 Pattern Recognition Lab, University Erlangen-Nuremberg
 * 
 * Import of Shimmer sensor data acquired by ShimmerConnect/SDLog
 * 
 * @author Dominik Schuldhaus
 * 
 */

package com.arekaga.shimdance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShimmerFileImporter {
	
	//filename of file
	private String 		m_filename 			= null;
	
	//BELOW INFORMATION IS TAKEN FROM FILENAME
	
	//name of shimmer, 4 letters/numbers, e.g. 9A9D
	String 				m_col_shimmername 	= null;
	
	//sampling rate
	private double 		m_samplingRate 		= -1;
	
	//accel range
	private double 		m_accrange 			= -1;
	
	//gyro range
	private double 		m_gyrrange 			= -1;
	
	//title of file content
	private String 		m_title 			= null;
		
	//BELOW INFORMATION IS TAKEN FROM HEADER OF SENSOR FILE
	
	//description of columns
	private String [] 	m_col_description  	= null;
	//binary vector indicating if column contains raw or calibrated data
	private boolean [] 	m_col_raw 			= null;
	//units of raw data
	private String [] 	m_col_units_raw 	= null;
	//units of calibrated data
	private String [] 	m_col_units_cal 	= null;
	
	//ADDITIONAL MEMBER VARIABLE
	
	//buffered reader for reading
	private BufferedReader m_reader 		= null;
	
	//vector contains current sensor data (either raw or calibrated)
	private double [] 	m_cur_data 			= null;
	
	//mode of acquisition/content
	//sc: shimmer connect
	//sdr: sdlog, raw
	//sdc: sdlog, calibrated
	private String 		m_mode 				= null;
	
	//included internal sensors
	private boolean m_is_acc				= false;
	private boolean m_is_gyr				= false;
	private boolean m_is_ecg				= false;
	private boolean m_is_emg				= false;
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//   METHODS   ////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//GETTER
	
	public String GetFilename(){
		return m_filename;
	}
	
	public String GetShimmername(){
		return m_col_shimmername;
	}
	
	public double GetSamplingRate(){
		return m_samplingRate;
	}
	
	public double GetAccRange(){
		return m_accrange;
	}
	
	public double GetGyrRange(){
		return m_gyrrange;
	}
	
	public String GetTitle(){
		return m_title;
	}
	
	public String [] GetColumnDescription(){
		return mapping_col_description(m_col_description);
	}
	
	public String [] GetColumnUnitsRaw(){
		return m_col_units_raw;
	}
	
	public String [] GetColumnUnitsCal(){
		return m_col_units_cal;
	}
	
	public String GetMode(){
		return m_mode;
	}
	
	public boolean IsAcc(){
		return m_is_acc;
	}

	public boolean IsGyr(){
		return m_is_gyr;
	}
	
	public boolean IsEmg(){
		return m_is_emg;
	}
	
	public boolean IsEcg(){
		return m_is_ecg;
	}
	
	//SETTER
	
	public void SetFilename(String filename){
		m_filename = filename;
	}
	
	
	//method for setting information of filename
	private boolean parse_filename(){
		
		//split filename
		String [] splitter = m_filename.split("_");
		
		//if filename is not valid
		if(splitter[0].compareTo("shimmer") != 0){
			return false;
		}
		else{
			//set name of shimmer
			m_col_shimmername = splitter[1];
		}
			
		//if filename is not valid
		if(splitter[2].compareTo("mode") != 0){
			return false;
		}
		else{
			//set mode of acquisition/file content
			m_mode = splitter[3];
		}
		
		//if filename is not valid
		if(splitter[4].compareTo("sampling") != 0){
			return false;
		}
		else{
			//set sampling rate
			m_samplingRate = mapping_sampling_rate(Integer.parseInt(splitter[5]));	
		}
		
		//if sensors is not valid
		if(splitter[6].compareTo("sensors") != 0){
			return false;
		}
		else{
			//set used sensors according to filename
			String sensors = splitter[7];	
			
			if(sensors.contains("acc")){
				m_is_acc = true;
			}
			
			if(sensors.contains("gyr")){
				m_is_gyr = true;
			}
			
			if(sensors.contains("emg")){
				m_is_emg = true;
			}
			
			if(sensors.contains("ecg")){
				m_is_ecg = true;
			}
		}
		
		//iterator of sensor parameters
		int iter_split = 8;
		//while title key is not present
		while(iter_split < splitter.length - 1){
			
			//if accelerometer range is given
			if(splitter[iter_split].compareTo("arange") == 0){
				//set accel range
				m_accrange = mapping_acc_range(Integer.parseInt(splitter[iter_split+1]));
			}
			else if(splitter[iter_split].compareTo("grange") == 0){
				//set gyro range
				m_gyrrange = mapping_gyr_range(Integer.parseInt(splitter[iter_split+1]));
			}
			else if (splitter[iter_split].compareTo("title") == 0){
				m_title = splitter[iter_split+1];
			}
			
			iter_split += 2; 
		}
		
		return true;
		
		
	}
	
	public boolean is_available(String type){
		
		boolean is_raw = false;
		if(type.compareTo("RAW") == 0){
			is_raw = true;
		}
		
		//loop over all columns
		for(int iter_col = 0; iter_col < m_col_raw.length; iter_col++){
			if(m_col_raw[iter_col] == is_raw){
				return true;
			}
		}
		
		return false;
		
	}

	//return value
	//0: ok
	//1: error in parsing filename
	//2: error in init mode
	public int Init(){
		
		//indicator if filename was successfully parsed
		boolean is_successfull = false;
		
		//set information of encoded in filename
		try{
			is_successfull = parse_filename();
		}
		catch(ArrayIndexOutOfBoundsException  e){
			is_successfull = false;
		}
		
		//if parsing was not successfull
		if(!is_successfull){
			return 1;
		}
		
		
		is_successfull = false;
		
		//read first line (default shimmer 1)
		try {
			m_reader.readLine().split("\t");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if ShimmerConnect was used
		if(m_mode.compareTo("sc") == 0){
			init_ShimmerConnect();
		}
		//if ShimmerLog was used
		else if(m_mode.compareTo("sdr") == 0 || m_mode.compareTo("sdc") == 0){
			init_SDLog();
		}
		else{
			return 2;
		}
		
		//initialize array for sensor data
		m_cur_data = new double[m_col_description.length];
		
		return 0;
		
	}
	
	//constructor
	public ShimmerFileImporter(InputStream in){
		
		//init buffered reader
		m_reader = new BufferedReader(new InputStreamReader(in));
		
	}
	
	public ShimmerFileImporter(String filename){
		
		//init buffered reader
		try {
			m_reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//set filename
		m_filename = filename;
		
	}
	
	public void Reset(){
		//reset reader
		try {
			m_reader.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double [] GetLine(String type){
		
		//line of file
		String str_line = null;
		//splitting of current line
		String [] str_linesplit = null;
		try {
			//read current line
			str_line = m_reader.readLine();
			
			//if end of file
			if(str_line == null){
				return null;
			}
			
			//split current line
			str_linesplit = str_line.split("\t");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//iterator over m_cur_data
		int iter_data = 0;
		//loop over all splitted elements
		for(int i = 0; i < str_linesplit.length; i++){
			//if column contains raw sensor data and chosen type is raw
			if(m_col_raw[i] && type.compareTo("RAW") == 0){
				//set sensor array
				m_cur_data[iter_data] = Double.parseDouble(str_linesplit[i]);
				//increment iterator
				iter_data++;
			}
			//if column contains calibrated data and chose type is calibrated
			else if(!m_col_raw[i] && type.compareTo("CAL") == 0){
				//set sensor data
				//-> preprocessing: replace , by . 
				m_cur_data[iter_data] = Double.parseDouble(str_linesplit[i].replaceAll(",","\\."));
				//increment iterator
				iter_data++;
			}
		}
		
		//return sensor data according to specified type (raw/calibrated)
		return m_cur_data;	
				
	}
	
	public void Close(){
		
		try {
			//close reader
			m_reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void init_ShimmerConnect(){
		
		try {
			
			//read second line (description of columns)
			String [] tmp = m_reader.readLine().split("\t");
			//since shimmer connect stores raw and calibrated data description of columns was repeated in the file -> tmp.length/2
			m_col_description = new String[tmp.length/2];
			for(int i = 0; i < tmp.length/2; i++){
				m_col_description[i] = tmp[i];
			}
			
			//// read third line (raw/calibrated column)
			String [] col_raw = m_reader.readLine().split("\t");
			m_col_raw = new boolean[col_raw.length];
			for(int i = 0;i < col_raw.length; i++){
				if(col_raw[i].compareTo("RAW") == 0){
					m_col_raw[i] = true;
				}
				else{
					m_col_raw[i] = false;
				}
			}
			
			//read fourth line (units of sensor data in columns)
			tmp = m_reader.readLine().split("\t");
			
			//separate arrays for raw and calibrated units
			m_col_units_raw = new String[tmp.length/2];
			m_col_units_cal = new String[tmp.length/2];
			
			//set raw units
			for(int i = 0; i < tmp.length/2; i++){
				m_col_units_raw[i] = tmp[i];
			}
			
			//set calibrated units
			for(int i = 0; i < tmp.length/2; i++){
				m_col_units_cal[i] = tmp[tmp.length/2+i];
			}
			
			
		} catch (IOException e){
			
		}
		
		
		
	}
	
	private void init_SDLog(){
		
		try {
						
			//read second line (description of columns)
			m_col_description = m_reader.readLine().split("\t");
						
			//read third line (raw or calibrated, not both like in ShimmerConnect)
			String [] col_raw = m_reader.readLine().split("\t");
			m_col_raw = new boolean[col_raw.length];
			
			//set binary vector
			boolean is_raw = false;
			if(col_raw[0].compareTo("uncalibrated") == 0){
				is_raw = true;
			}
			for(int i = 0; i < col_raw.length; i++){
				m_col_raw[i] = is_raw;
			}
					
			//set units according to determination raw/calibrated
			if(is_raw){
				m_col_units_raw = m_reader.readLine().split("\t");
			}
			else{
				m_col_units_cal = m_reader.readLine().split("\t");
			}
			
		} catch (IOException e){
			
		}
		
	}
	
	//set mapping of column description for output
	private String [] mapping_col_description(String [] in){
		
		String [] out = new String[in.length];
		
		for(int i = 0; i < in.length; i++){
			if(in[i].compareTo("Accelerometer X") == 0 || in[i].compareTo("Accelerometer X*") == 0) {
				out[i] = "Ax";
			}
			else if(in[i].compareTo("Accelerometer Y") == 0 || in[i].compareTo("Accelerometer Y*") == 0) {
				out[i] = "Ay";
			}
			else if(in[i].compareTo("Accelerometer Z") == 0 || in[i].compareTo("Accelerometer Z*") == 0) {
				out[i] = "Az";
			}
			else if(in[i].compareTo("Gyroscope X") == 0 || in[i].compareTo("Gyroscope X*") == 0) {
				out[i] = "Gx";
			}
			else if(in[i].compareTo("Gyroscope Y") == 0 || in[i].compareTo("Gyroscope Y*") == 0) {
				out[i] = "Gy";
			}
			else if(in[i].compareTo("Gyroscope Z") == 0 || in[i].compareTo("Gyroscope Z*") == 0) {
				out[i] = "Gz";
			}
			else if(in[i].compareTo("ECG RA-LL") == 0 || in[i].compareTo("ECG RA-LL*") == 0) {
				out[i] = "ECG_R";
			}
			else if(in[i].compareTo("ECG LA-LL") == 0 || in[i].compareTo("ECG LA-LL*") == 0) {
				out[i] = "ECG_L";
			}
			else if(in[i].compareTo("EMG") == 0 || in[i].compareTo("EMG*") == 0) {
				out[i] = "EMG";
			}
			else if(in[i].compareTo("Timestamp") == 0){
				out[i] = "T";
			}
		}
		
		return out;
		
	}
	
	//set mapping of sampling rate
	private double mapping_sampling_rate(int in){
		
		double out = -1;
		
		switch(in){
		case 0:
			out = 10.2;
			break;
		case 1:
			out = 51.2;
			break;	
		case 2:
			out = 102.4;
			break;
		case 3:
			out = 128;
			break;
		case 4:
			out = 170.7;
			break;
		case 5:
			out = 204.8;
			break;
		case 6:
			out = 256;
			break;
		case 7:
			out = 512;
			break;
		case 8:
			out = 1024;
			break;
		}
		
		return out;
		
	}
	
	//set mapping of accel range
	private double mapping_acc_range(int in){
		
		double out = -1;
		
		switch(in){
		case 0: 
			out = 1.5;
			break;
		case 1: 
			out = 2;
			break;
		case 2: 
			out = 4;
			break;
		case 3: 
			out = 6;
			break;
		}
		
		return out;
	}
	
	
	//set mapping of gyro range
	private double mapping_gyr_range(int in){
		
		double out = -1;
		
		switch(in){
		case 0:
			out = 500;
			break;
		case 1:
			out = 2000;
			break;
		
		}
		
		return out;
	}
	
}
