package org.safermobile.sms;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

public class SMSDataReceiver extends BroadcastReceiver {


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
				_smsLogger = new SMSLogger("recvdata");
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
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                
		        String from = msgs[i].getOriginatingAddress();
		        String to = msgs[i].getServiceCenterAddress();
		        
		        String msg = "";
		        
		        if (msgs[i].getMessageBody() != null)
		        	msg = msgs[i].getMessageBody().toString();
		        else if (msgs[i].getUserData() != null)
		        	msg = new String(msgs[i].getUserData());
		        	
		        Date rec = new Date(msgs[i].getTimestampMillis());
		        
		        _smsLogger.logReceive("recv-data",from, to, msg, rec, operator, cid+"", lac+"");
		        
		        Toast.makeText(context, "recvd DATA msg from " + from + ": \"" + msg + "\"" , Toast.LENGTH_SHORT).show();
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