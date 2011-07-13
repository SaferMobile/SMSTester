////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// LogViewActivity: loads and parses text file into table display
////////////////////////////////////////////////////////////////////

package org.safermobile.sms;

import java.util.StringTokenizer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LogViewActivity extends Activity implements SMSTesterConstants {

	private SMSLogger _smsLogger;

	private TableLayout _table;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logtable);

		_table = (TableLayout) findViewById(R.id.logTable);

		String mode = "";

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mode = extras.getString("mode");
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String logBasePath = prefs.getString("pref_log_base_path", Utils.defaultLogFolder);
		_smsLogger = new SMSLogger(mode, logBasePath);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (_smsLogger.getLogFile().exists())
			displayLogData(Utils.loadTextFile(_smsLogger.getLogFile()));
	}

	private void displayLogData(String logData) {
		StringTokenizer st = new StringTokenizer(logData, "\n");

		_table.removeAllViews();

		while (st.hasMoreTokens()) {
			TableRow row = new TableRow(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");

			while (st2.hasMoreTokens()) {
				String value = st2.nextToken();

				TextView tvColumn = new TextView(this);
				tvColumn.setPadding(3, 3, 0, 0);
				tvColumn.setText(value);
				// tvColumn.setLayoutParams(new LayoutParams(100,10));
				row.addView(tvColumn);
			}

			_table.addView(row);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	/*
	 * Create the UI Options Menu (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem mItem = null;

		mItem = menu.add(0, 1, Menu.NONE, R.string.refresh);
		mItem.setIcon(android.R.drawable.ic_menu_agenda);

		mItem = menu.add(0, 2, Menu.NONE, R.string.rotate_log);
		mItem.setIcon(android.R.drawable.ic_menu_rotate);
		return true;
	}

	/*
	 * When a menu item is selected launch the appropriate view or activity
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		super.onMenuItemSelected(featureId, item);

		if (item.getItemId() == 1) {
			displayLogData(Utils.loadTextFile(_smsLogger.getLogFile()));

		} else if (item.getItemId() == 2) {

			_smsLogger.rotateLogFile();

			displayLogData(Utils.loadTextFile(_smsLogger.getLogFile()));

		}
		return true;
	}
}