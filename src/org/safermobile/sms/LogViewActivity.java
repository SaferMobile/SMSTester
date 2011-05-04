package org.safermobile.sms;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LogViewActivity extends Activity {
	
	private SMSLogger _smsLogger;

	private TextView _textView = null;
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);
        
        _textView = (TextView)findViewById(R.id.messageLog);
        
       
        String mode = "";
        
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
        	mode = extras.getString("mode");
        }
        
		_smsLogger = new SMSLogger(mode);
        
        
    }
    

	@Override
	protected void onResume() {
		super.onResume();
		
		String logFile = _smsLogger.getLogFilePath();
    	_textView.setText(Utils.loadTextFile(logFile));
	
	}

	
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

   
    
   
    /*
     * Create the UI Options Menu (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         
         MenuItem mItem = null;
         
         mItem = menu.add(0, 1, Menu.NONE, "Refresh");
         mItem.setIcon(android.R.drawable.ic_menu_agenda);
         
         mItem = menu.add(0, 2, Menu.NONE, "Rotate Log");
         mItem.setIcon(android.R.drawable.ic_menu_rotate);
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
 			String logFile = _smsLogger.getLogFilePath();
 	    	_textView.setText(Utils.loadTextFile(logFile));
 		}
 		else if (item.getItemId() == 2)
 		{

 	    	_smsLogger.rotateLogFile();
 	    	
 	   	String logFile = _smsLogger.getLogFilePath();
	    	_textView.setText(Utils.loadTextFile(logFile));
 		}
         return true;
 	}
}