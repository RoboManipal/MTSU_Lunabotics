package me.codyhazelwood.research.robot_control;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Camera extends Component {
	
	public Camera() {
		currentState = State.STOP;
		enabled      = false;
	}
	
	public void enable() {
	    setDown = true;
	    enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public String left(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "camera");
			json.put("dir", "left");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		return json.toString();
	}
	
	public String right(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "camera");
			json.put("dir", "right");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		return json.toString();
	}
	
	public String up(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "camera");
			json.put("dir", "up");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		return json.toString();
	}
	
	public String down(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "camera");
			json.put("dir", "down");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		return json.toString();
	}
	
	public String stop() {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "camera");
			json.put("dir", "stop");
			json.put("val", 0);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
	    return json.toString();
	}
	
	public String calculateAction(float x, float y, float z) {
		String json      = null;
		State  prevState = currentState;
		
		if (setDown) {
    		down    = x;
    		setDown = false;
    	}
    	
		//If the camera is active, calculate the new state and JSON message
		if (enabled) {
			if (y < (center + 1.5) &&
    		    	y > (center - 1.5) &&
    		    	x < (down + 2) &&
    		    	x > (down - 2) ) {
    			currentState = State.STOP;
    			Log.d(TAG, "Stopping");
    			json = stop();
    		} else {
	    		if (y > center + 1.5) {
	    			currentState = State.RIGHT;
	    			Log.d(TAG, "Going Right");
	    			json = right(y);
	    		}
	    		else if (y < center - 1.5) {
	    			currentState = State.LEFT;
	    			Log.d(TAG, "Going Left");
	    			json = left(y);
	    		}
	    	
	    		if (x > down + 1.5) {
	    			currentState = State.DOWN;
	    			Log.d(TAG, "Going Down");
	    			json = down(x);
	    		}
	    		else if (x < down - 1.5) {
	    			currentState = State.UP;
	    			Log.d(TAG, "Going Up");
	    			json = up(x);
	    		}
    		}
		}
		
		//If the state hasn't changed, then don't send the state again
		if (prevState == currentState) {
			json = null;
		}
		
		return json;
	}
}
