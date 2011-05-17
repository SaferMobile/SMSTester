////////////////////////////////////////////////////////////////////
// SMSTester - https://lab.safermobile.org
// Copyright (c) 2011, SaferMobile / MobileActive
// See LICENSE for licensing information 
//
// EditKeyActivity: handles editing of list of keywords for sending
// as SMS messages, and persisting this to keywords.txt file 
////////////////////////////////////////////////////////////////////

package org.safermobile.sms;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class EditKeywordActivity extends Activity implements SMSTesterConstants
{

	private EditText _txtEditor;
	private File _keywordFile;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.editor);
	        
	        _txtEditor = (EditText)findViewById(R.id.editor);
	        
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        	String logBasePath = prefs.getString("pref_log_base_path", LOG_DEFAULT_PATH);
	        
	        _keywordFile = new File (logBasePath, KEYWORD_FILE);
	        
	        String data = Utils.loadTextFile(_keywordFile);
	        
	        if (data == null || data.length() == 0)
	        {
	        	try {
					data = Utils.loadAssetText(this, KEYWORD_FILE);
				} catch (IOException e) {
					Log.e(TAG, "error loading keyword file",e);
				}
	        }
	        
	        _txtEditor.setText(data);
	        
	        _txtEditor.addTextChangedListener(new TextWatcher() { 
	        	
                public void  afterTextChanged (Editable s){ 
                
                	
                } 
	            
                public void  beforeTextChanged  (CharSequence s, int start, int count, int after){ 
	            	
	            }
            
	            public void  onTextChanged  (CharSequence s, int start, int before, int count) { 
	            	
	            } 

	        });
	        
	        _txtEditor.setOnFocusChangeListener(new View.OnFocusChangeListener()
	        { 
	          
	           public void onFocusChange(View v, boolean gotFocus)
	           {
	               if (!gotFocus)
	               { 
	            	   
	            	   String data = _txtEditor.getText().toString();
	            	   Utils.saveTextFile(_keywordFile, data, false);
	            	   	            	   
	               }
	           }
	        });
	      
	      
	     
	        
	    }
	
     
}
