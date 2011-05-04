package org.safermobile.sms;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SMSSenderActivity extends Activity implements Runnable {

	private SMSLogger _smsLogger;
	private SmsManager sms = SmsManager.getDefault();
	private TelephonyManager _telMgr;

	private String _fromPhoneNumber;
	private String _toPhoneNumber;
	private int cid;
	private int lac;
	private String operator;
	
	public final static short SMS_DATA_PORT = 7027;
	boolean _useDataPort = true;
	int _timeDelay = 1000; //1 second
	boolean _doLoop = false;
	boolean keepRunning = false;
	
	Thread runThread;
	
	ProgressDialog statusDialog;
	
	private TextView _textView = null;
	private ArrayList<String> listMsgs;
	
	private final static String SENT = "SMS_SENT";
	private final static String DELIVERED = "SMS_DELIVERED";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);
        
        _fromPhoneNumber = getMyPhoneNumber(); //get the local device number
        
        _textView = (TextView)findViewById(R.id.messageLog);
        
        _textView.setText(getString(R.string.welcome));
        
        loadPrefs();
        
    	try
		{	
    		_smsLogger = new SMSLogger(SMSLogger.MODE_SEND);
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Error setting up SMS Log: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		
		_telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

	
        
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
    
    private void loadPrefs ()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());

        _toPhoneNumber = prefs.getString("pref_default_recipient", "");
        _useDataPort = prefs.getBoolean("pref_use_data", false);
        _timeDelay = Integer.parseInt(prefs.getString("pref_time_delay", "1000"));
        _doLoop = prefs.getBoolean("pref_loop", false);
    }
    
    private String getMyPhoneNumber(){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE); 
        return mTelephonyMgr.getLine1Number();
        }
    
    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message, boolean useDataPort)
    {        
    
    	 PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
    	            new Intent(SENT), 0);
    	        
    	 getLocationInfo();
 		_smsLogger.logStart(operator, cid+"", lac+"", new Date());
 		
    	 //---when the SMS has been sent---
         SMSSentStatusReceiver statusRev = new SMSSentStatusReceiver(_fromPhoneNumber, phoneNumber, message, operator, cid+"", lac+"",_smsLogger);
         registerReceiver(statusRev, new IntentFilter(SENT));
        
        if (!useDataPort)
        {
        	sms.sendTextMessage(phoneNumber, null, message, sentPI, null);      
        }
        else
        {
        	sms.sendDataMessage(phoneNumber, null, SMS_DATA_PORT, message.getBytes(), sentPI, null);
        }
        
       
    } 
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

    private void stopSMSTest ()
    {
    	_doLoop = false;
    	keepRunning = false;
    	
    }
    
	private void startSMSTest ()
    {
		loadPrefs();
		getLocationInfo();
		_smsLogger.logStart(operator, cid+"", lac+"", new Date());
		
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Start SMS");
    	alert.setMessage("Enter target phone number");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	
    	if (_toPhoneNumber != null)
    		input.setText(_toPhoneNumber);
    	
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    	  _toPhoneNumber = input.getText().toString();
    	  sendTestMessages (_toPhoneNumber, _useDataPort);
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    	    // Canceled.
    	  }
    	});

    	alert.show();
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
	
    private void sendTestMessages (String toPhoneNumber, boolean useDataPort)
    {
	
    	_toPhoneNumber = toPhoneNumber;
    	
    	statusDialog = ProgressDialog.show(this, "",
    			"Starting send...", true);
    	statusDialog.setCancelable(true);
    	statusDialog.show();
    	listMsgs = loadTestMessageList();

    	statusDialog.setMax(listMsgs.size());
    			
    	runThread = new Thread (this);
    	runThread.start ();
    	
    }
    
    public void run ()
    {
    	keepRunning = true;
    	
    	while (_doLoop)
    	{
    	
	    	Iterator<String> itMsgs = listMsgs.iterator();
	    	
	    	int count = 0;
	    	while (keepRunning && itMsgs.hasNext())
	    	{
	    		String nextMsg = itMsgs.next();
	    		sendSMS(_toPhoneNumber,nextMsg, _useDataPort);
	    		
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    	}
    	}
    	
    	Message msg = new Message();
		Bundle data = new Bundle();		
		data.putInt("count", -1);
		msg.setData(data);
		handler.sendMessage(msg);
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	
        	
        	if (msg.getData()!=null)
        	{
        		Bundle data = msg.getData();

        		String status = data.getString("status");
        		int count = data.getInt("count");
        		
        		if (status != null && count != -1)
        		{
        			statusDialog.setMessage(status);
        			statusDialog.setProgress(count);
        		}
        		else
        		{
        			statusDialog.dismiss();
        		}
        	}
        	else
        	{
        		statusDialog.dismiss();
        	}

        }
        
        
    };
    
    private ArrayList<String> loadTestMessageList ()
    {
    	ArrayList<String> listMsg = new ArrayList<String>();
    	
    	String kwlist = Utils.loadTextFile(EditKeywordActivity.KEYWORD_FILE);
    	StringTokenizer st = new StringTokenizer(kwlist,"\n");
    	while (st.hasMoreTokens())
    		listMsg.add(st.nextToken());
    	
    	return listMsg;
    }
    //---sends an SMS message to another device---
    private void sendSMSMonitor(String phoneNumber, String message)
    {        
    	
        PendingIntent sentPI = null;
        PendingIntent deliveredPI = null;
       
        sentPI = PendingIntent.getBroadcast(this, 0,
            new Intent(SENT), 0);
 
        deliveredPI = PendingIntent.getBroadcast(this, 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        SMSSentStatusReceiver statusRev = new SMSSentStatusReceiver(_fromPhoneNumber, phoneNumber, message, operator, cid+"", lac+"", _smsLogger);
        registerReceiver(statusRev, new IntentFilter(SENT));
 
        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:

                    	//  SMSLogger.logDelivery(thisPhoneNumber, phoneNumber, "delivered", ts)

                        break;
                    case Activity.RESULT_CANCELED:
                        
                        //  SMSLogger.logDelivery(thisPhoneNumber, phoneNumber, "delivered", ts)

                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        
        
        
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);   
        _smsLogger.logSend(_fromPhoneNumber, phoneNumber, message, new Date(), operator, cid+"", lac+"" );

    }    
    
    /*
     * Create the UI Options Menu (non-Javadoc)
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
     
     /* When a menu item is selected launch the appropriate view or activity
      * (non-Javadoc)
 	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
 	 */
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		
 		super.onMenuItemSelected(featureId, item);
 		
 		if (item.getItemId() == 1)
 		{
 			startSMSTest();
 		}
 		else if (item.getItemId() == 2)
 		{
 			stopSMSTest();
 		}
 		else if (item.getItemId() == 3)
 		{
        	startActivityForResult(new Intent(getBaseContext(), SettingsActivity.class), 1);

 		}
 		else if (item.getItemId() == 4)
 		{
 			String version = getVersionName( this, SMSSenderActivity.class);
 			String aboutMsg = "SMSTester: " + version + "\ncontact: nathan@guardianproject.info";
 			
 			Toast.makeText(this, aboutMsg, Toast.LENGTH_LONG).show();
 		}
 		
         return true;
 	}
 	
 	public static String getVersionName(Context context, Class cls) 
 	{
 	  try {
 	    ComponentName comp = new ComponentName(context, cls);
 	    PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
 	    return pinfo.versionName;
 	  } catch (android.content.pm.PackageManager.NameNotFoundException e) {
 	    return null;
 	  }
 	}
}