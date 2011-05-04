package org.safermobile.sms;

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSSentStatusReceiver extends BroadcastReceiver {

	private String _toPhoneNumber;
	private String _fromPhoneNumber;
	private String _operator;
	private String _cid;
	private String _lac;
	
	private String _toMsg;
	private SMSLogger _smsLogger;
	
	public SMSSentStatusReceiver (String fromPhoneNumber, String toPhoneNumber, String msg, String operator, String cid, String lac, SMSLogger smsLogger)
	{
		_fromPhoneNumber = fromPhoneNumber;
		_toPhoneNumber = toPhoneNumber;
		_toMsg = msg;
		_operator = operator;
		_cid = cid;
		_lac = lac;
		
		_smsLogger = smsLogger;
	}
	
	@Override
	 public void onReceive(Context context, Intent arg1) {
		
		int resultCode = getResultCode();
		String resultTxt = "";
		
		Date ts = new Date();
		
        switch (getResultCode())
        {
            case Activity.RESULT_OK:
            	resultTxt = "sent";
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            	resultTxt = "generic failure";
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
            	resultTxt = "error no service";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
            	resultTxt = "error null pdu";
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            	resultTxt = "radio off";
                break;
        }
        
        if (resultTxt.equals("sent"))
        	_smsLogger.logSend(_fromPhoneNumber, _toPhoneNumber, _toMsg, ts, _operator, _cid, _lac);
        else
        	_smsLogger.logError(_fromPhoneNumber, _toPhoneNumber, resultTxt, ts, _operator, _cid, _lac);
        
    }

}
