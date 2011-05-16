////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// SettingsActivity: loads and displays XML-based settings from preferences file
//
////////////////////////////////////////////////////////////////////
package org.safermobile.sms;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;


public class SettingsActivity 
		extends PreferenceActivity implements OnPreferenceClickListener {

	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		
		
	}
	
	
	@Override
	protected void onResume() {
	
		super.onResume();
	
		
	};
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		
		
		
		
		return true;
	}


}
