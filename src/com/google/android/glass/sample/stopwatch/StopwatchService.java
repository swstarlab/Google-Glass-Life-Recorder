package com.google.android.glass.sample.stopwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import collector.CollectorManager;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class StopwatchService extends Service {

    private static final String LIVE_CARD_TAG = "aaaa";

    private ChronometerDrawer mCallback;

    private LiveCard mLiveCard;

	private Application application;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    
    CollectorManager cm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
    	refreshSD();
        application = Application.getInstance();
        if (!application.isInitialized()){
        	 application.initialize(this);
        }
        
        cm = CollectorManager.getInstance();
        cm.start();
        
    	super.onCreate();
    }

	public void refreshSD() {
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+Environment.getExternalStorageDirectory()+ File.separator + "DCIM/Camera/")));

	   MediaScannerConnection.scanFile(this, new String[] { Environment.getExternalStorageDirectory().toString() }, null, new MediaScannerConnection.OnScanCompletedListener() {
	       /*
	        *   (non-Javadoc)
	        * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
	        */
	       public void onScanCompleted(String path, Uri uri) 
	         {
	             Log.d("test", "Scanned " + path + ":");
	             Log.d("test", "-> uri=" + uri);
	         }
	       });
	}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {

            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            // Keep track of the callback to remove it before unpublishing.
            mCallback = new ChronometerDrawer(this);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback);

            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
        }

        // Return START_NOT_STICKY to prevent the system from restarting the service if it is killed
        // (e.g., due to an error). It doesn't make sense to restart automatically because the
        // stopwatch state will have been lost.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
    	//refreshSD();
    	mCallback.StopWholeProcess();
        
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        
        cm.stop();
        
        super.onDestroy();
    }
}
