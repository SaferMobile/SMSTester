////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// MainTabActivity: UI class for display various tabs of the app
//
////////////////////////////////////////////////////////////////////

package org.safermobile.sms;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, SMSSenderActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("home").setIndicator("Home",
				res.getDrawable(android.R.drawable.ic_dialog_info)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, EditKeywordActivity.class);
		spec = tabHost.newTabSpec("keywords").setIndicator("Keywords",
				res.getDrawable(android.R.drawable.ic_menu_edit)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, LogViewActivity.class);
		intent.putExtra("mode", SMSLogger.MODE_SEND);

		spec = tabHost.newTabSpec("sent").setIndicator("Sent",
				res.getDrawable(android.R.drawable.ic_menu_send)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, LogViewActivity.class);
		intent.putExtra("mode", SMSLogger.MODE_RECV);

		spec = tabHost.newTabSpec("recv").setIndicator("Recv",
				res.getDrawable(android.R.drawable.ic_menu_recent_history)).setContent(
				intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, LogViewActivity.class);
		intent.putExtra("mode", SMSLogger.MODE_RECV_DATA);

		spec = tabHost.newTabSpec("recvdata").setIndicator("Recv-Data",
				res.getDrawable(android.R.drawable.ic_menu_agenda)).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}

}
