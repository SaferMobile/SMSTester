////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// SMSSenderActvity: the UI and workflow code for configurating and starting
// the SMS sending process
//
////////////////////////////////////////////////////////////////////

package org.safermobile.sms;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SMSSenderActivity extends Activity implements Runnable, SMSTesterConstants {

	/* handles logging of events to disk */
	private SMSLogger _smsLogger;

	/* handles parsing and logging error codes */
	private SMSErrorStatusReceiver _statusRev;

	/* the OS manager for sending/recv SMS messages */
	private SmsManager sms = SmsManager.getDefault();

	/* for reading metadata related to phone and radio status */
	private TelephonyManager _telMgr;

	/* phone number of sending device */
	private String _fromPhoneNumber;

	/* phone number(s) of receiving device(s), comma delimited */
	private String _toPhoneNumber;

	/*
	 * whether or not to send message on default port as 'text' or on custom
	 * port as 'data'
	 */
	boolean _useDataPort = false;

	/* whether or not to append tracking & analysis metadata to body of message */
	boolean _addTrackingMetadata = true;

	/* default delay between sending each test message */
	int _timeDelay = 5000; // ms

	/* whether or not to continuously loop test */
	boolean _doLoop = false;

	/* for managing thread and looping */
	boolean keepRunning = false;

	/* for managing run process and loop */
	Thread runThread;

	/* for displaying progress status */
	ProgressDialog statusDialog;

	/* main window for display info */
	private TextView _textView = null;

	/* list of messages to send */
	private ArrayList<String> listMsgs;

	/* for handling callbacks on SMS events */
	PendingIntent _sentPI;
	PendingIntent _deliveredPI;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);

		_fromPhoneNumber = getMyPhoneNumber(); // get the local device number

		_textView = (TextView) findViewById(R.id.messageLog);

		_textView.setText(getString(R.string.welcome));

		loadPrefs();

		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			String logBasePath = prefs.getString("pref_log_base_path", Utils.defaultLogFolder);
			_smsLogger = new SMSLogger(SMSLogger.MODE_SEND, logBasePath);
		} catch (Exception e) {
			Toast.makeText(this, "Error setting up SMS Log: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		_telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		_sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

		_deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

	}

	@Override
	protected void onResume() {
		super.onResume();

		loadPrefs();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		loadPrefs();
	}

	/*
	 * Loading the preferences from the system prefs into our class variables
	 */
	private void loadPrefs() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplication());

		_toPhoneNumber = prefs.getString("pref_default_recipient", "");
		_useDataPort = prefs.getBoolean("pref_use_data", false);
		_addTrackingMetadata = prefs.getBoolean("pref_use_tracking", true);
		_timeDelay = Integer.parseInt(prefs.getString("pref_time_delay", "5000"));
		_doLoop = prefs.getBoolean("pref_run_continuously", false);
	}

	/* get the device phone number */
	private String getMyPhoneNumber() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}

	// ---sends an SMS message to another device---
	private void sendSMS(String phoneNumber, String testMessage, boolean useDataPort,
			boolean addTrackingMetadata) {

		String operator;
		int cid = -1, lac = -1;

		CellLocation location = (CellLocation) _telMgr.getCellLocation();

		if (location instanceof GsmCellLocation) {
			cid = ((GsmCellLocation) location).getCid();
			lac = ((GsmCellLocation) location).getLac();

		}

		operator = _telMgr.getNetworkOperator();

		StringBuffer message = new StringBuffer();
		message.append(testMessage);

		if (addTrackingMetadata) {
			String shortUUID = java.util.UUID.randomUUID().toString();
			shortUUID = shortUUID.substring(0, 8);

			message.append(',');
			message.append("id:");
			message.append(shortUUID);
			message.append(',');
			message.append("ts:");
			message.append(new Date().getTime());
			message.append(',');
			message.append("cid:");
			message.append(cid);
			message.append(',');
			message.append("lac:");
			message.append(lac);
			message.append(',');
			message.append("op:");
			message.append(operator);

		}

		if (!useDataPort) {
			sms.sendTextMessage(phoneNumber, null, message.toString(), _sentPI,
					_deliveredPI);
		} else {
			sms.sendDataMessage(phoneNumber, null, SMS_DATA_PORT, message.toString()
					.getBytes(), _sentPI, _deliveredPI);
		}

		_smsLogger.logSend(_fromPhoneNumber, _toPhoneNumber, message.toString(),
				new Date(), operator, cid + "", lac + "");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	private void stopSMSTest() {
		_doLoop = false;
		keepRunning = false;
		if (runThread.isAlive())
			runThread.interrupt();
	}

	private void startSMSTest() {
		loadPrefs();

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Start SMS");
		alert.setMessage("Enter target phone number");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_PHONE);

		if (_toPhoneNumber != null)
			input.setText(_toPhoneNumber);

		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				_toPhoneNumber = input.getText().toString();
				sendTestMessages(_toPhoneNumber, _useDataPort);
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
	}

	private void sendTestMessages(String toPhoneNumber, boolean useDataPort) {

		_toPhoneNumber = toPhoneNumber;

		if (_statusRev != null)
			unregisterReceiver(_statusRev);

		// ---when the SMS has been sent---
		_statusRev = new SMSErrorStatusReceiver(_fromPhoneNumber, _toPhoneNumber,
				_smsLogger);
		registerReceiver(_statusRev, new IntentFilter(SENT));

		statusDialog = ProgressDialog.show(this, "", "Starting send...", true);
		statusDialog.setCancelable(true);
		statusDialog.show();
		listMsgs = loadTestMessageList();

		statusDialog.setMax(listMsgs.size());

		runThread = new Thread(this);
		runThread.start();

	}

	public void run() {
		keepRunning = true;

		// _smsLogger.logStart(operator, cid+"", lac+"", new Date());

		do {

			Iterator<String> itMsgs = listMsgs.iterator();

			int count = 0;
			while (keepRunning && itMsgs.hasNext()) {
				String nextMsg = itMsgs.next();

				sendSMS(_toPhoneNumber, nextMsg, _useDataPort, _addTrackingMetadata);

				Message msg = new Message();
				Bundle data = new Bundle();
				data.putString("status", "sending message: \"" + nextMsg + "\"");
				count++;
				data.putInt("count", count);
				msg.setData(data);
				handler.sendMessage(msg);

				try {
					Thread.sleep(_timeDelay);
				} catch (InterruptedException e) {
					Log.i(TAG, "couldn't sleep!", e);
				}

			}
		} while (_doLoop);

		Message msg = new Message();
		Bundle data = new Bundle();
		data.putInt("count", -1);
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (msg.getData() != null) {
				Bundle data = msg.getData();

				String status = data.getString("status");
				int count = data.getInt("count");

				if (status != null && count != -1) {
					statusDialog.setMessage(status);
					statusDialog.setProgress(count);
				} else {
					statusDialog.dismiss();
				}
			} else {
				statusDialog.dismiss();
			}

		}

	};

	private ArrayList<String> loadTestMessageList() {
		ArrayList<String> listMsg = new ArrayList<String>();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String logBasePath = prefs.getString("pref_log_base_path", Utils.defaultLogFolder);

		File _keywordFile = new File(logBasePath, KEYWORD_FILE);

		if (_keywordFile.exists()) {
			String kwlist = Utils.loadTextFile(_keywordFile);
			StringTokenizer st = new StringTokenizer(kwlist, "\n");
			while (st.hasMoreTokens())
				listMsg.add(st.nextToken());

		}

		return listMsg;
	}

	/*
	 * //---sends an SMS message to another device--- private void
	 * sendSMSMessage(String phoneNumber, String message) {
	 * 
	 * PendingIntent sentPI = null; PendingIntent deliveredPI = null;
	 * 
	 * sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
	 * 
	 * deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED),
	 * 0);
	 * 
	 * //---when the SMS has been sent--- SMSErrorStatusReceiver statusRev = new
	 * SMSErrorStatusReceiver(_fromPhoneNumber, phoneNumber, message, operator,
	 * cid+"", lac+"", _smsLogger); registerReceiver(statusRev, new
	 * IntentFilter(SENT));
	 * 
	 * //---when the SMS has been delivered--- registerReceiver(new
	 * BroadcastReceiver(){
	 * 
	 * @Override public void onReceive(Context arg0, Intent arg1) { switch
	 * (getResultCode()) { case Activity.RESULT_OK:
	 * 
	 * // SMSLogger.logDelivery(thisPhoneNumber, phoneNumber, "delivered", ts)
	 * 
	 * break; case Activity.RESULT_CANCELED:
	 * 
	 * // SMSLogger.logDelivery(thisPhoneNumber, phoneNumber, "delivered", ts)
	 * 
	 * break; } } }, new IntentFilter(DELIVERED));
	 * 
	 * 
	 * sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	 * _smsLogger.logSend(_fromPhoneNumber, phoneNumber, message, new Date(),
	 * operator, cid+"", lac+"" );
	 * 
	 * }
	 */

	/*
	 * Create the UI Options Menu (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem mItem = null;

		mItem = menu.add(0, 1, Menu.NONE, "Start");
		mItem.setIcon(android.R.drawable.ic_menu_send);

		mItem = menu.add(0, 2, Menu.NONE, "Stop");
		mItem.setIcon(android.R.drawable.ic_media_pause);

		mItem = menu.add(0, 3, Menu.NONE, "Settings");
		mItem.setIcon(android.R.drawable.ic_menu_preferences);

		mItem = menu.add(0, 4, Menu.NONE, "About");
		mItem.setIcon(android.R.drawable.ic_menu_help);

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
			startSMSTest();
		} else if (item.getItemId() == 2) {
			stopSMSTest();
		} else if (item.getItemId() == 3) {
			startActivityForResult(new Intent(getBaseContext(), SettingsActivity.class),
					1);

		} else if (item.getItemId() == 4) {
			String version = getVersionName(this, SMSSenderActivity.class);
			String aboutMsg = "SMSTester: " + version
					+ "\nLearn more at: http://safermobile.org";

			Toast.makeText(this, aboutMsg, Toast.LENGTH_LONG).show();
		}

		return true;
	}

	public static String getVersionName(Context context, Class cls) {
		try {
			ComponentName comp = new ComponentName(context, cls);
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(
					comp.getPackageName(), 0);
			return pinfo.versionName;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return null;
		}
	}
}