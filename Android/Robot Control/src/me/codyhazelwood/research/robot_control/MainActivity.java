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

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	public Button   connectButton;
	public Button   settingsButton;
	public Intent   controlIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        connectButton  = (Button)findViewById(R.id.btn_connect);
        settingsButton = (Button)findViewById(R.id.btn_settings);
        
        controlIntent = new Intent();
        controlIntent.setClass(this, Control.class);
        
        //On Click Listener for Connect Button
        connectButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		startActivity(controlIntent);
        	}
        });
        
        //On Click Listener for Settings Button
        settingsButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent();
        		intent.setClass(MainActivity.this, Settings.class);
        		startActivityForResult(intent, 0);
        	}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	//No need to check the item, since there is only one
    	Intent intent = new Intent();
		intent.setClass(MainActivity.this, Settings.class);
		startActivityForResult(intent, 0);
		return true;
    }
}
