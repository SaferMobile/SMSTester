////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// SMSDataReceiver: handles the callbacks that occur when SMS messages
// sent to a specific port as "data" messages are received
//
////////////////////////////////////////////////////////////////////

package org.safermobile.sms;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

public class SMSDataReceiver extends BroadcastReceiver implements SMSTesterConstants {

	SMSLogger _smsLogger;

	private TelephonyManager _telMgr;

	private int cid;
	private int lac;
	private String operator;

	private void init(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context
				.getApplicationContext());
		String logBasePath = prefs.getString("pref_log_base_path", Utils.defaultLogFolder);

		try {
			_smsLogger = new SMSLogger("recvdata", logBasePath);
		} catch (Exception e) {
			Toast.makeText(context, "Error setting up SMS Log: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (_telMgr == null)
			_telMgr = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

		if (_smsLogger == null)
			init(context);

		// ---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage sms = null;
			for (int i = 0; i < pdus.length; i++) {
				sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
				String msg = "";
				if (sms.getMessageBody() != null)
					msg = sms.getMessageBody().toString();
				else if (sms.getUserData() != null)
					msg = new String(sms.getUserData());
				// skip messages that don't have the SMSTester header
				if (!msg.startsWith(Utils.defaultMessageTag)) continue;

				String from = sms.getOriginatingAddress();
				String to = sms.getServiceCenterAddress();
				Date rec = new Date(sms.getTimestampMillis());
				getLocationInfo();

				_smsLogger.logReceive("recv-data", from, to, msg, rec, operator,
						cid + "", lac + "");

				Toast.makeText(context,
						"recvd DATA msg from " + from + ": \"" + msg + "\"",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void getLocationInfo() {

		CellLocation location = (CellLocation) _telMgr.getCellLocation();

		if (location instanceof GsmCellLocation) {
			cid = ((GsmCellLocation) location).getCid();
			lac = ((GsmCellLocation) location).getLac();

		}

		operator = _telMgr.getNetworkOperator();

	}
}