package collector;

import com.google.android.glass.sample.stopwatch.Application;

import android.content.Context;



public class CollectorManager {
	
	private static CollectorManager instance;


	public synchronized static CollectorManager getInstance() {
		if (instance == null) {
			instance = new CollectorManager();
		}
		return instance;
	}
	
	// -----------------------------------------------------------------
	

	private boolean running;
	private SensorCollector sensorCollector;
	private SensorMonitor sensorMonitor;
	
	public CollectorManager() {
		Context context = Application.getInstance().getContext();
		
		sensorMonitor = new SensorMonitor(context);
		sensorCollector = new SensorCollector(sensorMonitor);
	}
	
	public synchronized void start(){
		if (running)
			return;
		
		sensorMonitor.start();
		sensorCollector.start();
		
		running = true;
	}
	
	public synchronized void stop(){
		if (!running)
			return;
		
		sensorMonitor.stop();
		sensorCollector.stop();
		
		running = false;
	}
	
	public SensorMonitor getSensorMonitor() {
		return sensorMonitor;
	}
	
	public boolean isRunning() {
		return running;
	}
	
}
