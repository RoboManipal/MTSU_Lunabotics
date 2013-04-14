/* -----------------------------------------------------------------------
 * Programmer:   Cody Hazelwood
 * Date:         December 31, 2012
 * Platform:     Android API 11 - 14
 * Description:  Connects to the robot's WebSocket server, listens to
 *               accelerometer values, and handles communication to and
 *               from the robot.
 * Dependencies: Autobahn Android WebSocket Library
 * -----------------------------------------------------------------------
 * Copyright © 2012 Cody Hazelwood.
 * 
 * Autobahn|Android is Copyright © 2012 Alejandro Hernandez and is
 * licensed under the Apache License, Version 2.0.
 *              
 * Research project funded by:
 *
 *        Undergraduate Research Experience and Creative Activity
 *                 Middle Tennessee State University
 *                    Mentor: Dr. Saleh Sbenaty
 * -----------------------------------------------------------------------
 */

package me.codyhazelwood.research.robot_control;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class ControlService {
	
	private static final String TAG = "ControlService";
    
    private Camera   camera;          //Camera Component
    private Drive    drive;           //Drive  Component
    private Handler  handler  ;       //UI Update Handler
    private TextView txtTemperature;  //Text View for Temperature on UI
    
    private final WebSocketConnection wsc = new WebSocketConnection();
    
    /**
     * Constructor.  Creates a new Camera and Drive Component.
     */
    ControlService() {
    	camera    = new Camera();
    	drive     = new Drive();
    }
    
    /**
     * Set the temperature handler for the ControlService
     * @param handler        The handler to be used for updating the Control UI
     * @param txtTemperature The text view that will contain the 
     */
    public void setTemperatureHandler(Handler handler, TextView txtTemperature) {
        this.handler        = handler;
    	this.txtTemperature = txtTemperature;
    }
    
    /**
     * Connect to WebSocket server.
     * @param wsURL URL of the WebSocket server.
     */
    public void connect(String wsURL) {
    	startWebSocketConnection(wsURL);
    }
    
    /**
     * Stops everything and disconnects from the server.
     */
    public void disconnect() {
    	turnLedsOff();
    	JSONObject json = new JSONObject();
		try {
			json.put("comp", "stop");
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
    	wsc.sendTextMessage(json.toString());
    	wsc.disconnect();
    }
    
    /**
     * Enables camera pan/tilt.
     */
    public void enableCameraControl() {
    	camera.enable();
    }
    
    /**
     * Disables camera pan/tilt.
     */
    public void disableCameraControl() {
    	wsc.sendTextMessage(camera.stop());
    	camera.disable();
    }
    
    /**
     * Enables drive train control.
     */
    public void enableDriveControl() {
    	drive.enable();
    	wsc.sendTextMessage(drive.up(10)); 
    	Log.d(TAG, "Drive Control Enabled");
    }
    
    /**
     * Disables drive train control.
     */
    public void disableDriveControl() {
    	wsc.sendTextMessage(drive.stop());
    	drive.disable();
    	Log.d(TAG, "Drive Control Disabled");
    }
    
    /**
     * Turns LEDs on.
     */
    public void turnLedsOn() {
    	JSONObject json = new JSONObject();
		try {
			json.put("comp", "leds");
			json.put("s", true);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
		
    	wsc.sendTextMessage(json.toString());
    }
    
    /**
     * Turns LEDSs off.
     */
    public void turnLedsOff() {
    	JSONObject json = new JSONObject();
		try {
			json.put("comp", "leds");
			json.put("s", false);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
		
    	wsc.sendTextMessage(json.toString());
    }
    
    /**
     * Enable Autonomy
     */
	public void enableAutonomy() {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "autonomy");
			json.put("s", true);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
		
		wsc.sendTextMessage(json.toString());
	}

	/**
	 * Disable Autonomy
	 */
	public void disableAutonomy() {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "autonomy");
			json.put("s", false);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
		
		wsc.sendTextMessage(json.toString());
	}
	
	public void moveCameraTo(int x, int y) {
	    JSONObject json = new JSONObject();
	    try {
	    	json.put("comp", "camera");
	    	json.put("dir", "direct");
	    	json.put("x", x);
	    	json.put("y", y);
	    } catch (JSONException e) {
	    	Log.d(TAG, e.toString());
	    }
	    
	    Log.d(TAG, "Moving Camera To A Specific Position");
	    wsc.sendTextMessage(json.toString());
	}
	
    /**
     * Accepts accelerometer values and processes them.
     * @param x X Direction
     * @param y Y Direction
     * @param z Z Direction
     */
    public void accelerometerValues(float x, float y, float z) {
    	String cameraMessage = camera.calculateAction(x, y, z);
    	String driveMessage  = drive.calculateAction(x, y, z);
    	
    	if (cameraMessage != null) {
    		wsc.sendTextMessage(cameraMessage);
    	}
    	
    	if (driveMessage != null) {
    		wsc.sendTextMessage(driveMessage);
    	}
    }
    
    /**
     * Start a connection to a WebSocket server
     * @param server The address of the WebSocket server to connect to.
     */
    private void startWebSocketConnection(String server) {
		try {
	        wsc.connect(server, new WebSocketHandler() {
	            @Override
	            public void onOpen() {
	            	Log.d(TAG, "Connected to WebSocket Server");
	            }

	            @Override
	            public void onTextMessage(final String payload) {
	            	Log.d(TAG, "Recieved msg: " + payload);
	            	handler.post(new Runnable() {
	            		@Override
	            		public void run() {
	            			txtTemperature.setText("Temp: " + payload + "¼F");
	            		}
	            	});
	            }

	            @Override
	            public void onClose(int code, String reason) {
	                
	            }
	         });
	      } catch (WebSocketException e) {
	          Log.d(TAG, e.toString());
	      }
	}
}
