package me.codyhazelwood.research.robot_control;

public abstract class Component {
	
	/**
	 * The current state of a servo system.
	 */
	public enum  State     {
		LEFT,
		RIGHT,
		UP,
		DOWN,
		STOP,
		UP_RIGHT,
		UP_LEFT
	}
	
	protected class Message {
	}

	protected boolean setDown;  //In down direction calibration mode?
    protected float   down;     //Calibrate Down
    
    protected static final float  center = (float) 0.5;   //Calibrate Center
	protected static final String TAG    = "ControlService";
	
	public State   currentState;
	public boolean enabled;
	
	public abstract String left(float val);
	public abstract String right(float val);
	public abstract String up(float val);
	public abstract String down(float val);
	public abstract String stop();
	
	public abstract String calculateAction(float x, float y, float z);
	
	public abstract void enable();
	public abstract void disable();
}
