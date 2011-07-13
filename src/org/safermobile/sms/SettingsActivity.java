////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// SettingsActivity: loads and displays XML-based settings from preferences file
//
////////////////////////////////////////////////////////////////////
package org.safermobile.sms;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, SMSTesterConstants {

	EditTextPreference mDefaultRecipientPreference;
	EditTextPreference mLogBasePathPreference;
	EditTextPreference mTimeDelayPreference;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		mDefaultRecipientPreference = (EditTextPreference) findPreference("pref_default_recipient");
		mLogBasePathPreference = (EditTextPreference) findPreference("pref_log_base_path");
		mTimeDelayPreference = (EditTextPreference) findPreference("pref_time_delay");
	}

	@Override
	protected void onResume() {
		super.onResume();
		setSummaries();
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		setSummaries();
	}

	private void setSummaries() {
		String text;
		text = mDefaultRecipientPreference.getText();
		if (text == null || text.equals(Utils.defaultRecipient)) {
			mDefaultRecipientPreference.setSummary(R.string.pref_default_recipient_summary);
		} else {
			mDefaultRecipientPreference.setSummary(text);
		}
		text = mLogBasePathPreference.getText();
		if (text == null || text.equals(Utils.defaultLogFolder)) {
			mLogBasePathPreference.setSummary(R.string.pref_log_base_path_summary);
		} else {
			mLogBasePathPreference.setSummary(text);
		}
		text = mTimeDelayPreference.getText();
		if (text == null || text.equals(Integer.toString(Utils.defaultTimeDelay))) {
			mTimeDelayPreference.setSummary(R.string.pref_time_delay_summary);
		} else {
			mTimeDelayPreference.setSummary(text);
		}
	}
}
