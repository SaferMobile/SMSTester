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

public class SMSReceiver extends BroadcastReceiver implements SMSTesterConstants {


	SMSLogger _smsLogger;
	 
	private TelephonyManager _telMgr;

	private int cid;
	private int lac;
	private String operator;
	
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	if (_telMgr==null)
			_telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    	if (_smsLogger == null)
    	{
	    	try
			{	
	    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	        	String logBasePath = prefs.getString("pref_log_base_path", LOG_DEFAULT_PATH);
				_smsLogger = new SMSLogger("recv", logBasePath);
			}
			catch (Exception e)
			{
				Toast.makeText(context, "Error setting up SMS Log: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			
    	}
    	
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String str = "";            
        if (bundle != null)
        {

	        getLocationInfo();
	        
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                
		        String from = msgs[i].getOriginatingAddress();
		        String to = _telMgr.getLine1Number();
		        String msg = msgs[i].getMessageBody().toString();
		        Date rec = new Date(msgs[i].getTimestampMillis());
		       
		        String opAndSMSC = operator + '/' +  msgs[i].getServiceCenterAddress();
		        
		        _smsLogger.logReceive("recv-text",from, to, msg, rec, opAndSMSC, cid+"", lac+"");
		        
		        Toast.makeText(context, "recvd text msg from " + from + ": \"" + msg + "\"" , Toast.LENGTH_SHORT).show();
        	}
        }                         
    }
    
	private void getLocationInfo ()
	{
		
		
		
		CellLocation location = (CellLocation) _telMgr.getCellLocation();
		
		if (location instanceof GsmCellLocation)
		{
			cid = ((GsmCellLocation)location).getCid();
			lac = ((GsmCellLocation)location).getLac();
			
		}
		
		operator = _telMgr.getNetworkOperator();
	
	}
}