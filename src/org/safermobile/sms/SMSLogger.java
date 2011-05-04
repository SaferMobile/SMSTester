package org.safermobile.sms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class SMSLogger {

	
	private final String TAG = "JBSMS";

	private String _logMode = null;
	
	private String basePath = "/sdcard/jbsms";
	private String logFilePath = null;
	
	public final static String MODE_SEND = "send";
	public final static String MODE_RECV = "recv";
	public final static String MODE_RECV_DATA = "recvdata";
	
	public SMSLogger (String logMode)
	{
		_logMode = logMode;
		
		if (logFilePath == null)
		{
			init();
		}
	}
	
	public void init ()
	{
		
		File fileDir = new File(basePath);
		if (!fileDir.exists())
			fileDir.mkdir();
		
		//load existing log data
		logFilePath = basePath + "/jbsmstest" + "-" + _logMode + ".csv";
		
	}
	
	public void rotateLogFile () 
	{
		
		String logData = Utils.loadTextFile(logFilePath);
		
		//copy it to new file
		Date logDate = new Date();
		String newLogFilePath = basePath + "/jbsmstest" + "-" + _logMode + "-" + logDate.getYear() + logDate.getMonth() + logDate.getDate() + "-" + logDate.getHours() + logDate.getMinutes() + logDate.getSeconds() + ".csv";
		Utils.saveTextFile(newLogFilePath, newLogFilePath, false);
		
		Utils.saveTextFile(logFilePath, "", false);
		
		
	}
	
	public String getLogFilePath ()
	{
		return logFilePath;
	}
	

	public void logStart (String operator, String cid, String lac, Date sent)
	{
		String[] vals = {"start",operator, cid, lac,sent.toGMTString()};
		String log = generateCSV(vals) + "\n";
		Log.i(TAG, log);
		
		
		Utils.saveTextFile(logFilePath, log, true);
	
	}
	
	
	public void logSend (String from, String to, String smsMsg, Date sent, String operator, String cid, String lac)
	{
		String[] vals = {"sent",from,to,smsMsg,sent.toGMTString(),operator,cid,lac};
		String log = generateCSV(vals) + "\n";
		Log.i(TAG, log);
		
		Utils.saveTextFile(logFilePath, log, true);
	
	}
	
	public void logReceive (String mode, String from, String to, String smsMsg, Date rec, String operator, String cid, String lac)
	{
		String[] vals = {mode,from,to,smsMsg,rec.toGMTString(),operator,cid,lac};
		
		String log = generateCSV(vals) + "\n";
		
		Log.i(TAG, log);
		

		Utils.saveTextFile(logFilePath, log, true);

	
	}
	
	public void logError (String from, String to, String error, Date ts, String operator, String cid, String lac)
	{
		String[] vals = {"err",from,to,error,ts.toGMTString(),operator,cid,lac};
		String log = generateCSV(vals) + "\n";
		Log.i(TAG, log);
		
		
		Utils.saveTextFile(logFilePath, log, true);

	}
	
	public void logDelivery (String from, String to, String deliveryStatus, Date ts)
	{
		String[] vals = {"del",from,to,deliveryStatus,ts.toGMTString()};
		String log = generateCSV(vals) + "\n";
		Log.i(TAG, log);
		
		
		Utils.saveTextFile(logFilePath, log, true);

	}
	
	private String generateCSV(String[] params)
	{
		StringBuffer csv = new StringBuffer();
		
		for (int i = 0; i < params.length; i++)
		{
			csv.append('"');
			csv.append(params[i]);
			csv.append('"');

			if ((i+1)<params.length)
				csv.append(',');
			
		}
		
		
		return csv.toString();
	}
	
	
}
