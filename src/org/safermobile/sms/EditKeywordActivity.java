package org.safermobile.sms;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditKeywordActivity extends Activity {

	private EditText txtEditor;
	
	public final static String BASE_PATH = "/sdcard/jbsms";

	public final static String KEYWORD_FILE = BASE_PATH + "/keywords.txt";
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.editor);
	        
	        txtEditor = (EditText)findViewById(R.id.editor);
	
	        String data = Utils.loadTextFile(KEYWORD_FILE);
	        
	        if (data == null || data.length() == 0)
	        {
	        	try {
					data = Utils.loadAssetText(this, "keywords.txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        
	        txtEditor.setText(data);
	        
	        
	        
	        txtEditor.addTextChangedListener(new TextWatcher() { 
	        	
                public void  afterTextChanged (Editable s){ 
                
                	
                } 
	            
                public void  beforeTextChanged  (CharSequence s, int start, int count, int after){ 
	            	
	            }
            
	            public void  onTextChanged  (CharSequence s, int start, int before, int count) { 
	            	
	            } 

	        });
	        
	        txtEditor.setOnFocusChangeListener(new View.OnFocusChangeListener()
	        { 
	          
	           public void onFocusChange(View v, boolean gotFocus)
	           {
	               if (!gotFocus)
	               { 
	            	   
	            	   String data = txtEditor.getText().toString();
	            	   Utils.saveTextFile(KEYWORD_FILE, data, false);
	            	   	            	   
	               }
	           }
	        });
	      
	      
	     
	        
	    }
	
	/*
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         
         MenuItem mItem = null;
         
         mItem = menu.add(0, 1, Menu.NONE, "Add");
         mItem = menu.add(0, 2, Menu.NONE, "Delete");
         mItem = menu.add(0, 3, Menu.NONE, "Import");
         
        
         return true;
     }
     
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		
 		super.onMenuItemSelected(featureId, item);
 		
 		if (item.getItemId() == 1)
 		{
 		
 		}
 		else if (item.getItemId() == 2)
 		{
 		
 		}
 		else if (item.getItemId() == 3)
 		{
 		
 		}
         return true;
 	}
 	*/
     
}
