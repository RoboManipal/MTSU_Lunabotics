/* -----------------------------------------------------------------------
 * Programmer:   Cody Hazelwood
 * Date:         November 17, 2012
 * Platform:     Android 4.1
 * Description:  
 * Dependencies: 
 * -----------------------------------------------------------------------
 * Copyright © 2012.  Cody Hazelwood.
 *              
 * Research project funded by:
 *
 *        Undergraduate Research Experience and Creative Activity
 *                 Middle Tennessee State University
 *                    Mentor: Dr. Saleh Sbenaty
 * -----------------------------------------------------------------------
 */

package me.codyhazelwood.research.robot_control;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import me.codyhazelwood.research.robot_control.MjpegInputStream;
import me.codyhazelwood.research.robot_control.MjpegView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Control extends Activity implements SensorEventListener {
    private static final String TAG = "MjpegActivity";
    
    private SensorManager  mSensorManager;
	private Sensor         mAccelerometer;
	private SurfaceView    mCameraSurface;
	private Button         btnCameraControl;
	private Button         btnRobotControl;
	private ToggleButton   btnLedControl;
	private ToggleButton   btnAutonomy;
	private MjpegView      cameraView;
	private TextView       txtTemperature;
	private ControlService robot;
	
	private long lastCamMsgTime;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public void onStart() {
		super.onStart();
        setContentView(R.layout.activity_control);
        
        //Get access to UI elements
        mCameraSurface   = (SurfaceView)  findViewById(R.id.cameraSurface);
        btnCameraControl = (Button)       findViewById(R.id.btn_cameraControl);
    	btnRobotControl  = (Button)       findViewById(R.id.btn_robotControl);
    	btnLedControl    = (ToggleButton) findViewById(R.id.btn_lights);
    	btnAutonomy      = (ToggleButton) findViewById(R.id.btn_autonomy);
    	txtTemperature   = (TextView)     findViewById(R.id.txt_temp);
    	
    	//Load Preferences
    	SharedPreferences prefs     = PreferenceManager.getDefaultSharedPreferences(this);
    	String            cameraURL = "http://" + prefs.getString("prefIpAddress", "")
                                    + ":"       + prefs.getString("prefCameraPort", "")
                                                + prefs.getString("prefCameraPath", "");
    	String            wsURL     = "ws://"   + prefs.getString("prefWebSocketIpAddress", "")
    			                    + ":"       + prefs.getString("prefWebSocketPort", "");
    	
        //Start Camera View
        cameraView = new MjpegView(this, mCameraSurface);       
        
        new DoRead().execute(cameraURL);
        lastCamMsgTime = 0;
        
        //Initialize Accelerometer
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        
    	//Start Temperature Update Handler
    	final Handler handler = new Handler();
    	
    	//Create Control Services
    	robot = new ControlService();
    	robot.setTemperatureHandler(handler, txtTemperature);
    	robot.connect(wsURL);
    	
    	//Set Click Listeners
    	setClickListeners();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
    public void onPause() {
        super.onPause();
        
        mSensorManager.unregisterListener(this); //Stop Accelerometer Listener
        cameraView.stopPlayback();               //Stop Camera Playback
        robot.disconnect();                      //Disconnect from WebSocket Server
    }
	
	//Accelerometer Listener
	public void onSensorChanged(SensorEvent event) {
    	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {    		
    		robot.accelerometerValues(event.values[0], event.values[1], event.values[2]);
    	}
    }

	/**
	 * Dummy prototype.  Not used for this application, but required to be here.
	 */
	public void onAccuracyChanged(Sensor sensor, int accuracy) { /* Do Nothing */ }

    /**
     * Sets click listeners when used during onCreate()
     */
    private void setClickListeners() {
    	btnCameraControl.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					//robot.disableDriveControl();       //Disable Robot Control
        			robot.enableCameraControl();       //Enable Camera Control
        		} else if (event.getActionMasked() == MotionEvent.ACTION_UP){
        			robot.disableCameraControl();      //Disable Camera Control
        		}
				return false;
			}
        });
        
    	btnRobotControl.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					//robot.disableCameraControl();     //Disable Camera Control
        			robot.enableDriveControl();       //Enable Robot Control
        		} else if (event.getActionMasked() == MotionEvent.ACTION_UP){
        			robot.disableDriveControl();      //Disable Drive Control
        		}
				return false;
			}
        });
    	
    	mCameraSurface.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getActionMasked();
				
				Log.d(TAG, "Action: " + action);
				
				if (action == MotionEvent.ACTION_DOWN) {
					float x = event.getX();
					float y = event.getY();
					
					float x_view = v.getWidth();
					float y_view = v.getHeight();
					
					robot.moveCameraTo((int)(x/x_view * 180), (180 - (int)(y/y_view * 180)));
				}/*
				else {
					Log.d(TAG, "ActionMove");
					final int historySize = event.getHistorySize();
					final int ptrCount    = event.getPointerCount();
					
					for (int h = 0; h < historySize; h++) {
						for (int p = 0; p < ptrCount; p++) {
						    float x = event.getHistoricalX(p, h);
						    float y = event.getHistoricalY(p, h);
						    
						    float x_view = v.getWidth();
							float y_view = v.getHeight();
							
							if (!(lastCamMsgTime - System.currentTimeMillis() < 200)) {
								lastCamMsgTime = System.currentTimeMillis();
								robot.moveCameraTo((int)(x/x_view * 180), (180 - (int)(y/y_view * 180)));
							}
						}
					}
				}*/
				
				return false;
			}	
    	});
        
        btnAutonomy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton btn, boolean checked) {
        		if (checked) {
        			robot.disableDriveControl();         //Disable Robot Control
        			robot.enableAutonomy();              //Enable Autonomy
        			btnRobotControl.setClickable(false); //Disable Robot Control Button
        		} else {
        			robot.disableAutonomy();             //Disable Autonomy
        			btnRobotControl.setClickable(true);  //Enable Robot Control Button
        		}
        	}
        });
        
        btnLedControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton btn, boolean checked) {
        		if (checked) {
        			robot.turnLedsOn();
        		} else {
        			robot.turnLedsOff();
        		}
        	}
        });
    }
    
    //Class creates an async task to connect to the camera's http server
    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            HttpResponse      res        = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();     
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                return new MjpegInputStream(res.getEntity().getContent());  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
                //Error connecting to camera
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            cameraView.setSource(result);
            cameraView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        }
    }
    
}