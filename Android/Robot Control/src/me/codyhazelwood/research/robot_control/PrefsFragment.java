/* -----------------------------------------------------------------------
 * Programmer:   Cody Hazelwood
 * Date:         December 31, 2012
 * Platform:     Android API 11 - 14
 * Description:  Used to load the Settings Menu
 * Dependencies: R.xml.preferences
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

import me.codyhazelwood.research.robot_control.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}
}
