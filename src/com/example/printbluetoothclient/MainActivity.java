package com.example.printbluetoothclient;

import my.util.MyBluetooth;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";
	private View button;
	private MyBluetooth bluetooth;
	private TextView label;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final MainActivity myActivity = this;
		
		setContentView(R.layout.activity_main);
		bluetooth = new MyBluetooth(myActivity);
		
		
		label = (TextView)findViewById(R.id.textView1);
		
		button = findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v (TAG, "Button click");
				int val = bluetooth.connectToServer("1200.0", "xxxx-xxxx-xxxx-xxxx");
				label.setText("result: " + val);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
