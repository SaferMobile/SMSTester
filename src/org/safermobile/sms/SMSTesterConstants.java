////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// SMSTesterConstants: globals!
//
////////////////////////////////////////////////////////////////////

package org.safermobile.sms;

public interface SMSTesterConstants {

	public final static String TAG = "SMSTester";

	public final static String KEYWORD_FILE = "keywords.txt";

	// messages to confirm that the recipient does indeed want to receive a deluge of messages
	public final static String REQUEST_START_MSG = "REQUEST_START_MSG";
	public final static String ALLOW_START_MSG = "ALLOW_START_MSG";
	public final static String DENY_START_MSG = "DENY_START_MSG";

	public final static String EXTRAS_BASE_PATH = "basePath";
	public final static short SMS_DATA_PORT = 7027;

	public final static String SENT = "SMS_SENT";
	public final static String DELIVERED = "SMS_DELIVERED";

}
