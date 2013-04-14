package me.codyhazelwood.research.robot_control;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Drive extends Component {
	
	public Drive() {
		currentState = State.STOP;
		enabled      = false;
	}
	
	public String left(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "drive");
			json.put("dir", "left");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		Log.d(TAG, "Built Left JSON");
		return json.toString();
	}
	
	public String right(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "drive");
			json.put("dir", "right");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
		
		Log.d(TAG, "Built Right JSON");
		return json.toString();
	}
	
	public String forward_left(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "drive");
			json.put("dir", "forward_left");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		Log.d(TAG, "Built Forward/Left JSON");
		return json.toString();
	}
	
	public String forward_right(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "drive");
			json.put("dir", "forward_right");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		Log.d(TAG, "Built Forward/Right JSON");
		return json.toString();
	}
	
	public String up(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "drive");
			json.put("dir", "forward");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		Log.d(TAG, "Built Forward JSON");
		return json.toString();
	}
	
	public String down(float val) {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "drive");
			json.put("dir", "backward");
			json.put("val", val);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
 
		Log.d(TAG, "Built Down JSON");
		return json.toString();
	}
	
	public String stop() {
		JSONObject json = new JSONObject();
		try {
			json.put("comp", "drive");
			json.put("dir", "stop");
			json.put("val", 0);
			json.put("s", enabled);
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
		
		Log.d(TAG, "Built Stop JSON");
	    return json.toString();
	}
	
	public String calculateAction(float x, float y, float z) {
		String json      = null;
		State  prevState = currentState;
		
		//If the camera is active, calculate the new state and JSON message
		if (enabled) {
			if (y < 1.5 && y > -1.5 && x < 9) {
    			currentState = State.UP;
    			Log.d(TAG, "Going Forward");
    			json = up(0);
    		} 
			
			else if (y > -4 && y <= -1.5) {
	    		currentState = State.UP_LEFT;
	    		Log.d(TAG, "Going Forward/Left");
	    		json = forward_left(y);
	    	} 
			
			else if (y <= -4) {
	    		currentState = State.LEFT;
	    		Log.d(TAG, "Going Left");
	    		json = left(y);
	    	} 
			
			else if (y >= 1.5 && y < 4) {
	    		currentState = State.UP_RIGHT;
	    		Log.d(TAG, "Going Forward/Right");
	    		json = forward_right(x);
	    	}
			
			else if (y >= 4) {
	    		currentState = State.RIGHT;
	    		Log.d(TAG, "Going Right");
	    		json = right(x);
	    	}
	    	
			else if (x >= 8.7) {
	    		currentState = State.DOWN;
	    		Log.d(TAG, "Going Down");
	    		json = down(x);
	    	}
		}
		
		//If the state hasn't changed, then don't send the state again
		if (prevState == currentState) {
			json = null;
		}
			
		return json;
	}
	
	public void enable() {
		enabled      = true;
		currentState = State.UP;
	}
	
	public void disable() {
	    enabled      = false;
	    currentState = State.STOP;
	}
}