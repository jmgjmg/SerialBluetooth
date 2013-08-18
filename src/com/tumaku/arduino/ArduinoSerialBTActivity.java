/*
Written by Javier Montaner montanerj@yahoo.com

This software is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

*/

package com.tumaku.arduino;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ArduinoSerialBTActivity extends Activity {

	  private static Button btnOn, btnOff, btnSend, btnClear, btnSetMAC;
	  private static EditText inputText;
	  private static TextView receivedText;
	  
	  private static BluetoothAdapter btAdapter = null;
	  private static BluetoothSocket btSocket = null;
	  private static OutputStream btOutputStream =null;
	  private static InputStream btInputStream =null;  
	  private static Context context=null;
	  static ReadInputTask  inputTask = null;
			  
	  private static final int MAC_CALL = 1;
	  private static final int ACTIVATE_BT_CALL = 2;
	  private static String currentMAC =null;
	  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context =this;
        btnOn = (Button) findViewById(R.id.btnOn);					
        btnOff = (Button) findViewById(R.id.btnOff);					
        btnSend = (Button) findViewById(R.id.btnSend);					
        btnClear = (Button) findViewById(R.id.btnClear);
        btnSetMAC= (Button) findViewById(R.id.btnSetMAC);
        inputText = (EditText) findViewById(R.id.inputText);
        receivedText = (TextView) findViewById(R.id.receivedText);
        connectionOffGUI();
        
        SharedPreferences settings = getSharedPreferences(this.getString(R.string.prefsFile), 0);        
        currentMAC = settings.getString(this.getString(R.string.macAddress), this.getString(R.string.mac));
        
        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	connectionOnGUI();
            	connectBT();
            }
          });
		   
		btnOff.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	cancelBT();
		    }
          });        

		btnSetMAC.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		      	 Intent intentMAC = new Intent(context, com.tumaku.arduino.SetMACActivity.class);
		       	 intentMAC.putExtra(context.getString(R.string.macAddress), currentMAC); 
		       	 startActivityForResult(intentMAC, MAC_CALL);				
		       	 return;			    
		    }
          }); 
    		
		
		btnSend.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	if (inputText.getText().toString().length()==0) {
	                Toast.makeText(context, "Empty message. Write some text before psuhing 'Send' button", Toast.LENGTH_SHORT).show();
		    		return;
		        }
			  	String content = inputText.getText().toString();
			  	inputText.setText("");
			  	try {
			  		btOutputStream.write(content.getBytes());	
			  	} catch (Exception ex) {
	                Toast.makeText(context, "Error sending data", Toast.LENGTH_SHORT).show();
			  		cancelBT();
			  	}
		    }
		  }); 	
		
		btnClear.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			  	receivedText.setText("");
		    }
          }); 	
		
    }
    
    public void onResume() {
        super.onResume();
        if (btnOff.isEnabled()) {
	        connectionOnGUI();
	        try {
	        		if (!btSocket.isConnected()) {
		                Toast.makeText(context, "Reconnect", Toast.LENGTH_SHORT).show();	        			
	        			connectBT();
	        		}
	        } catch (Exception exc){
	        	cancelBT();
	        }

        } else connectionOffGUI();
    }
     
    @Override
    public void onPause() {
      super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	outState.putBoolean("Bluetooth OFF",btnOff.isEnabled());
    	super.onSaveInstanceState(outState);
    }


    @Override
    public void onRestoreInstanceState(Bundle outState) {
        boolean offState = outState.getBoolean("Bluetooth OFF", false);
        if (offState) btnOff.setEnabled(true);
    	super.onSaveInstanceState(outState);
    }
    
    void connectBT(){
          btAdapter = BluetoothAdapter.getDefaultAdapter();
          checkBTState();
          if (btAdapter==null) {
        	  cancelBT();
        	  return;
          }          
          if (!btAdapter.isEnabled()) {
  	          Toast.makeText(context, "Activate Bluetooth on your device before trying to connect", Toast.LENGTH_SHORT).show();
  	          cancelBT();
  	          return;
          }
       
          BluetoothDevice device = btAdapter.getRemoteDevice(currentMAC);
          try { 
              // UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        	  // btSocket = device.createRfcommSocketToServiceRecord(uuid);   error& solution found in stackoverflow
        	  Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
        	  btSocket = (BluetoothSocket) m.invoke(device, 1);
         	  btAdapter.cancelDiscovery();
    	      btSocket.connect();
    	      btInputStream= btSocket.getInputStream();
    	      btOutputStream = btSocket.getOutputStream();
    	      inputTask = new ReadInputTask();
    	      inputTask.execute(btInputStream);	
         } catch (Exception e1){
 	        Toast.makeText(context, "Error trying to connect to device over BT", Toast.LENGTH_LONG).show();
 	        cancelBT();
 	        return;
         };
    }	
      
    

    
    
    private void closeBT() {
    	    if (btOutputStream!=null) {
    	    	try {
    	    	  btOutputStream.close();
    	    	} catch (Exception ex){};
    	    	btOutputStream=null;
    	    }
    	    if (btInputStream!=null) {
    	    	try {
    	    		btInputStream.close();
    	    	} catch (Exception ex){};
    	    	btInputStream=null;
    	    }
    	    if (btSocket!=null){
    	    	try {
    	    		btSocket.close();
    	    	} catch (Exception ex){}; 
    	    	btSocket=null;
    	    }
    	    
    	    if (inputTask != null) {
    	    	try {
    	    		inputTask.cancel(true);
    	    	} catch (Exception ex){}; 
    	    	inputTask=null;
    	    }  
    }
 
    private void cancelBT() {
    	closeBT();
    	connectionOffGUI();
    }

    
    private void connectionOnGUI() {
	  	btnOn.setEnabled(false);  
	  	btnClear.setEnabled(true);  
	  	btnSend.setEnabled(true);  
	  	btnOff.setEnabled(true);  
	  	inputText.setEnabled(true);
	  	btnSetMAC.setEnabled(false);
    }   
    
    private void connectionOffGUI() {
	  	btnOn.setEnabled(true);  
	  	btnClear.setEnabled(false);  
	  	btnSend.setEnabled(false);  
	  	btnOff.setEnabled(false);  
	  	inputText.setText("");
	  	inputText.setEnabled(false);
	  	receivedText.setText("");
	  	btnSetMAC.setEnabled(true);  

    }      
    
    public void appendText(String data){
    	receivedText.setText(receivedText.getText().toString() + data);
    }
    
    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) { 
	        Toast.makeText(context, "Bluetooth not supported by this phone", Toast.LENGTH_SHORT).show();
        } else {
          if (!btAdapter.isEnabled()) {
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ACTIVATE_BT_CALL);
          }
        }
    }   
  
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    super.onActivityResult(requestCode, resultCode, data); 
	    // See which child activity is calling us back.
	    if (requestCode==MAC_CALL) {
	            if (resultCode == RESULT_CANCELED) {}//DO NOTHING 
	            else {
	            	currentMAC = data.getStringExtra(this.getString(R.string.macAddress));  
	                SharedPreferences settings = getSharedPreferences(this.getString(R.string.prefsFile), 0);
	                SharedPreferences.Editor editor = settings.edit();
	                editor.putString(this.getString(R.string.macAddress),currentMAC);
	                editor.commit();	            	
	            }
	    }
	    if (requestCode==ACTIVATE_BT_CALL) {
            if (resultCode == RESULT_CANCELED) {}//DO NOTHING 
            else {
    	        Toast.makeText(context, "Bluetooth activated", Toast.LENGTH_SHORT).show();     	
            }
    }	}
      
	   private class ReadInputTask extends AsyncTask<InputStream, String, Void> {

           @Override
           protected Void doInBackground(InputStream... inps) {
                   InputStream inputStream = inps[0];
                   byte[] buffer = new byte[256];  // buffer store for the stream
                   int bytes; // bytes returned from read()
                   while (true) {
                       try { 
                           // Read from the InputStream
                           bytes = inputStream.read(buffer);     
                           if ( bytes>0) {// Get number of bytes and message in "buffer"                          
                        	   publishProgress(new String(buffer,0, bytes,"UTF-8"));
                           } 
                       } catch (Exception e) {
                    	   // To be done
                       }
                   }
           }
           
           protected void onProgressUpdate(String... values) {
               String input = values[0];
               appendText(input);
           }
           
}
	
	
	
}