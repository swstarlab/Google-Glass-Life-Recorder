package collector;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorMonitor implements SensorEventListener{

	private Sensor accSensor;
	private Sensor linearAccSensor;
	private Sensor gyroSensor;
	private Sensor magSensor;
	private SensorManager sensorManager;
	
	private Object accMonitor;
	private Object gyroMonitor;
	private Object oriMonitor;
	
	private boolean running;
	
	private float accX; // m/s^2
	private float accY;
	private float accZ;
	
	private float azimuth; // degree
	private float pitch;
	private float roll;
	
	private float rotX; // rad/s
	private float rotY;
	private float rotZ;
	
	private float[] R; 
	private float[] I; 
	private float[] gravity; 
	private float[] geomagnetic; 
	private float[] values; 
	
	
	public SensorMonitor(Context context) {
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		
		accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		magSensor= sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		accMonitor = new Object();
		gyroMonitor = new Object();
		oriMonitor = new Object();
		
		R = new float[9];
		I = new float[3];
		gravity = new float[3];
		geomagnetic = new float[3];
		values = new float[3];
	}
	
	public void start(){
		if (running)
			return;
		
		sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, linearAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST);
		
		running = true;
	}
	
	public void stop(){
		if (!running)
			return;
		
		sensorManager.unregisterListener(this);
		
		running = false;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (event.sensor == gyroSensor){
			
			synchronized (gyroMonitor) {
				rotX = event.values[0];
				rotY = event.values[1];
				rotZ = event.values[2];
			}
		}
		else if (event.sensor == magSensor){
			geomagnetic = event.values;
		}
		else if (event.sensor == accSensor){
			gravity = event.values;
			
			if (geomagnetic != null && gravity != null){
				boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
				
				if (success) {
					SensorManager.getOrientation(R, values);
					
					synchronized (oriMonitor) {
						azimuth = values[0];
						pitch = values[1];
						roll = values[2];
					}
				}
			}
		}
		else if (event.sensor == linearAccSensor){
			synchronized (accMonitor) {
				accX = event.values[0];
				accY = event.values[1];
				accZ = event.values[2];
			}
		}
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	public SensorData createSensorData(){
		
		SensorData data = new SensorData();
		
		synchronized (gyroMonitor) {
			data.rotX = rotX;
			data.rotY = rotY;
			data.rotZ = rotZ;
		}
		
		synchronized (oriMonitor) {
			data.azimuth = azimuth;
			data.pitch = pitch;
			data.roll = roll;
		}
		
		synchronized (accMonitor) {
			data.accX = accX;
			data.accY = accY;
			data.accZ = accZ;
		}
		
		data.rotX = data.rotX*180/Math.PI;
		data.rotY = data.rotY*180/Math.PI;
		data.rotZ = data.rotZ*180/Math.PI;
		
		data.azimuth = data.azimuth*180/Math.PI;
		data.pitch = data.pitch*180/Math.PI;
		data.roll = data.roll*180/Math.PI;
		
		data.roundData();
		

    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS").format(new Date());
    	data.timeStamp = timeStamp;
		data.time = System.currentTimeMillis();
		//Log.d("test", "sensoring:"+data.getSerialization());
		return data;
	}
	
}
