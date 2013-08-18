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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SetMACActivity extends Activity {
	  
	  private EditText macText =null;
	  private String currentMAC;
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.inputmac);
		    Intent intent = getIntent();

		    macText= (EditText)findViewById(R.id.inputmac);
		    currentMAC = intent.getStringExtra(this.getString(R.string.macAddress));
		    if (currentMAC==null) currentMAC=this.getString(R.string.mac);
		    macText.setText(currentMAC);
		  } 
		  
		public void onButtonClicked(View view) {
			switch (view.getId()) {
				case R.id.cancel:
					finish();
					return;
				case R.id.reset:
					macText.setText(R.string.mac);
					return;
				case R.id.ok:
			        Intent resultIntent = new Intent();
			        resultIntent.putExtra(this.getString(R.string.macAddress), macText.getText().toString()); 
			        this.setResult(RESULT_OK, resultIntent);
			        finish();
				    return;
			}
		}
		
}



