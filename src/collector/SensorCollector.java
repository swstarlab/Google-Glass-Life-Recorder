package collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

public class SensorCollector{

	String timeStamp;
	String externalPath;
    BufferedWriter bfw;
    
	private static final int CAPACITY = 120000;
	private static final int STOP_COLLECT_SIZE = CAPACITY*9/10;
	
	private static final int COLLECT_DELAY = 4000; // 처음 최초 시작 딜레이
	private static final int COLLECT_PERIOD = 33; // = 1000/33 hz
	private static final int COLLECT_PERIOD_MIN = 20;
	private static final int SAVE_DELAY = COLLECT_DELAY + 5000;
	private static final int SAVE_TIMER_PERIOD = 500;
	private static final int SAVE_PERIOD = 5000;
	
	private static final int SAVE_MIN = 50;
	private static final int SAVE_MAX = 1000;
	
	
	
	private BlockingQueue<SensorData> queue;
	private Timer timer;
	private SensorMonitor sensorMonitor;
	private boolean running;
	
	
	public SensorCollector(SensorMonitor sensorMonitor) {
		queue = new LinkedBlockingQueue<SensorData>(CAPACITY);
		this.sensorMonitor = sensorMonitor;

	}

    public synchronized void setFileOpen(){
    	timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS").format(new Date());
		externalPath = Environment.getExternalStorageDirectory()+ File.separator + "DCIM/Camera/data_"+timeStamp+".csv";


		SensorData data = sensorMonitor.createSensorData();
	    File mfile = new File(externalPath);
		{
			//Log.d("test",""+mfile.getAbsolutePath());
			try {
				FileWriter mfw = new FileWriter(mfile, true);
			    bfw = new BufferedWriter(mfw);//이게 진리라는군
				bfw.append(""+data.getHeader());
			    bfw.newLine();
			    Log.d("test","open csv file");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
	public void start(){
		if (running)
			return;

		setFileOpen();
		timer = new Timer();
		timer.scheduleAtFixedRate(new Collector(), COLLECT_DELAY, COLLECT_PERIOD);
		//timer.scheduleAtFixedRate(new WebSaver(), SAVE_DELAY, SAVE_TIMER_PERIOD);
		
		running = true;
	}
	
	public void stop(){
		if (!running)
			return;
		
		timer.cancel();
		timer = null;
		
		running = false;

		try {
			bfw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	// -------------------------------------------------------------------------------------------
	
	private class Collector extends TimerTask {

		
		private long previous;
		
		@Override
		public void run() {
			
			if (queue.size() >= STOP_COLLECT_SIZE){
				return;
			}
			
			long cur = System.currentTimeMillis();
			if (cur - previous < COLLECT_PERIOD_MIN){
				return;
			}

			previous = cur;
			//queue.add(sensorMonitor.createSensorData());

			SensorData data = sensorMonitor.createSensorData();
			//Log.d("test",""+data.getSerialization());
			try {
				bfw.append(""+data.getSerialization());
				bfw.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class WebSaver extends TimerTask {
		
		private long previousSaveTime;
		private List<SensorData> dataList;
		
		public WebSaver() {
			dataList = new ArrayList<SensorData>(SAVE_MAX);
			
		}



		@Override
		public void run() {
			
			long curTime = System.currentTimeMillis();
			if (curTime - previousSaveTime < SAVE_PERIOD)
				return;
			
			
			int pollSize = Math.min(SAVE_MAX - dataList.size(), queue.size());
			if (pollSize < 0){
				pollSize = 0;
			}
			
			if (pollSize + dataList.size() < SAVE_MIN){
				return;
			}
			
			for (int i = 0; i < pollSize; i++){
				dataList.add(queue.poll());
			}
			
			try {
//				JSONArray arr = new JSONArray();
				for (int i = 0; i < dataList.size(); i++){
					SensorData data = dataList.get(i);
					//Log.d("test",""+data.getSerialization());
					bfw.append(""+data.getSerialization());
//					JSONObject obj = new JSONObject();
//					
//					obj.put("time", data.time);
//					obj.put("gpsLat", data.gpsLatitude);
//					obj.put("gpsLong", data.gpsLongitude);
//					obj.put("netLat", data.netLatitude);
//					obj.put("netLong", data.netLongitude);
//					obj.put("cellId", data.cellId);
//					
//					arr.put(obj);
					dataList.remove(i);
				}
				dataList.removeAll(dataList);
//				int result = ServerManager.getInstance().saveSensorDataList(dataList);
//				if (result == ServerResult.SUCCESS){
//					previousSaveTime = curTime;
//					Log.i("test", "sensor data save success num=" + dataList.size());
//					dataList.clear();
//				}
//				else{
//					throw new Exception("saving sensor data list failed, result=" + result);
//				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
