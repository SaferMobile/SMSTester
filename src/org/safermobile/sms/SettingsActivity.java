/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package org.safermobile.sms;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.KeyEvent;


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
