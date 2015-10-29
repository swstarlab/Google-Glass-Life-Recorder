package com.google.android.glass.sample.stopwatch;

import android.content.Context;
import android.provider.Settings.Secure;

public class Application {

	private static Application instance;


	public synchronized static Application getInstance() {
		if (instance == null) {
			instance = new Application();
		}
		return instance;
	}
	
	
	// -----------------------------------------------------------------
	
	private String androidId;
	private boolean initialized;
	private Context context;
	
	private Application() {
	}
	
	public void initialize(Context context) {
		
		if (initialized)
			return;
		
		this.context = context.getApplicationContext();
		androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		
		initialized = true;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public String getAndroidId() {
		return androidId;
	}
	
	public Context getContext() {
		return context;
	}
}




