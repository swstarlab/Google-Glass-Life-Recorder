package com.google.android.glass.sample.stopwatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.glass.timeline.DirectRenderingCallback;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;


public class ChronometerDrawer implements Callback {//DirectRenderingCallback
	Camera mCamera;
	MediaRecorder recorder;
	CaptureThread ct;
	VideoStopAndRecordingThread vst;

	SurfaceView view;
	Parameters parameters;
	SurfaceHolder sHolder;
	
	View cardView;
	Toast startRecording;
	Toast stopRecording;
	Toast tictack;
	boolean isRecording=false;
	
	boolean videoMode=true;
	boolean AutoRecording=true;

	String timeStamp;
	String externalPath;
	String externalAudioPath;
	
    public void StopWholeProcess(){
    	if(!videoMode){
        	StopPictureThreading();
    	}else {
    		StopVideoThreading();
		}
    	StopCamera();
    }

    public void StopCamera(){
    	if(mCamera!=null){
	    	mCamera.stopPreview();
	        mCamera.release();
	        mCamera=null;
    	}
    }
    public void StopRecorder(){
    	if(recorder!=null){
		   	recorder.stop();
		   	recorder.release();
		   	recorder=null;
    	}
    }
    public void StopPictureThreading(){
    	StopRecorder();
    	ct.stopRunning();
    	if(isRecording){
			isRecording = false;
		}
    }
    
    public void StopVideoThreading(){
    	if(AutoRecording)
    		vst.stopRunning();
    	
    	StopVideoRecording();
    }

    public void StopVideoRecording(){
    	if(isRecording){
			isRecording = false;
	    	StopRecorder();
    	}
    }
    public void setFileName(){
    	//timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS").format(new Date());
    	timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
    	if(!videoMode){
			externalPath = Environment.getExternalStorageDirectory()+ File.separator + "DCIM/Camera/Pic_"+timeStamp+".jpg";
			externalAudioPath = Environment.getExternalStorageDirectory()+ File.separator + "DCIM/Camera/Aud_"+timeStamp+".mp4";
			
		}else{
			externalPath = Environment.getExternalStorageDirectory()+ File.separator + "DCIM/Camera/Vid_"+timeStamp+".mp4";
		}
    }
    
    Camera.PictureCallback jpegCallBack=new Camera.PictureCallback() {
	    public void onPictureTaken(byte[] data, Camera camera) {
            tictack.show();
	    	
	    	setFileName();
			File destination = new File(externalPath);
	        try {
	            Bitmap userImage = BitmapFactory.decodeByteArray(data, 0, data.length);
	            FileOutputStream out = new FileOutputStream(destination);
	            userImage.compress(Bitmap.CompressFormat.JPEG, 100, out);        
	
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
	    }
	};

    public ChronometerDrawer(Context context) {
 	   startRecording = Toast.makeText(context, "Start Recording!", Toast.LENGTH_SHORT);
 	   stopRecording = Toast.makeText(context, "Stop Recording!", Toast.LENGTH_SHORT);
 	   tictack = Toast.makeText(context, "tick tock!", Toast.LENGTH_SHORT);
        
        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		view = new SurfaceView(context);
		view.setLayoutParams(params);
	   	sHolder = view.getHolder();
	   	sHolder.addCallback(this);  

	     
	   	if(!videoMode){
    	   ct = new CaptureThread();
    	   ct.start();
	   	}else{
	   		if(AutoRecording){
		   		vst = new VideoStopAndRecordingThread();
	    	    vst.start();
	   		}
	   	}
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		 parameters = mCamera.getParameters();
//	     mCamera.setParameters(parameters);
    }

	@Override
    public void surfaceCreated(SurfaceHolder holder) {
		Log.d("test", "created");
		sHolder=holder;
			startRecording(holder);
    }
    
	public void startRecording(SurfaceHolder holder){
		if(!isRecording){
	       if(!videoMode){
	    	   if(mCamera==null)
	   	      		mCamera = Camera.open();
			     if(recorder==null)
			   		    recorder = new MediaRecorder();
			     
//			   Camera.Parameters params = mCamera.getParameters();
//		        params.setRecordingHint(true);
//		        mCamera.setParameters(params);
		        
//			   Camera.Parameters params = mCamera.getParameters();
//			   params.setAntibanding(params.ANTIBANDING_AUTO);//지원안됨
//			   params.setAutoExposureLock(true);//더구린듯
//			   params.setAutoWhiteBalanceLock(true);//더구린듯2
			   //params.setColorEffect(params.)
//			   params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
//			   params.setJpegQuality(100);
//		        mCamera.setParameters(params);
		         try {
		      	   mCamera.setPreviewDisplay(holder);
			         mCamera.startPreview();
		  		} catch (IOException e) {
		  			e.printStackTrace();
		  		}
//		         
			     	recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//					
//	                //recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));//표준
					recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			       setFileName();
			       recorder.setOutputFile(externalAudioPath);
					recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			 	   try {
			 		   recorder.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					recorder.start();
					
					isRecording = true;
					
		   	}else{
			   if(mCamera==null)
		      		mCamera = Camera.open();
		     if(recorder==null)
		   		    recorder = new MediaRecorder();
	  	     
			   Camera.Parameters params = mCamera.getParameters();
		        params.setRecordingHint(true);
		        mCamera.setParameters(params);
		        
		       try {
		    	   mCamera.setPreviewDisplay(holder);
			         mCamera.startPreview();
			         mCamera.unlock();
				       recorder.setCamera(mCamera);

				       try {
						recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
						recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));//표준
//						recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));//테스트중
//		                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_CIF));//테스트중
						
//		                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QCIF));//너무화질이..구리다
//		                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));//1분에3매가
//		                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));//1시간에 4기가
//		                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA));//에러남
						
		                
//						recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//						recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//						recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//						recorder.setVideoSize(720, 480);
//						recorder.setVideoFrameRate(15);
//						recorder.setMaxDuration(1*10*1000);
						
						} catch (Exception e) {
//							recorder.stop();
//							
//						   	recorder.release();
//						   	mCamera.lock();
//					    	mCamera.stopPreview();
//					        mCamera.release();
//					        mCamera=null;
						}
			        
				       setFileName();
				       recorder.setOutputFile(externalPath);
				       recorder.setPreviewDisplay(holder.getSurface());
				     
				 	   try {
				 		   recorder.prepare();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						recorder.start();
						isRecording = true;
			        
		       	} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		   	}
		}
	}
	
	
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
	       Log.d("test", "destroyed");
//	   	recorder.stop();
//	   	recorder.release();
        sHolder = null;
    }

    
    class CaptureThread extends Thread{
	    int counter = 0;
	    boolean running = true;
	    void stopRunning(){
	    	running = false;
	    }
		
		@Override
		public void run() {
			super.run();
			while(running){
			     Log.d("test", "count: "+counter++);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(isRecording){
				     mCamera.takePicture(null, null, jpegCallBack);
				}
			}
		}
	}

    class VideoStopAndRecordingThread extends Thread{
	    int counter = 0;
	    boolean running = true;
	    void stopRunning(){
	    	running = false;
	    }
		@Override
		public void run() {
			super.run();
			while(running){
			     Log.d("test", "count: "+counter++);
				try {
					Thread.sleep(5*60*1000);//
					//Thread.sleep(1*5*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	 StopVideoRecording();
			     startRecording(sHolder);
			}
		}
	}
}

