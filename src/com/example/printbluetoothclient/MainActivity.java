package com.example.printbluetoothclient;

import my.util.MailManager;
import my.util.MyGcm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {
	private BroadcastReceiver updateListViewReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			Log.i (TAG, "Ricevuto messaggio di update");
			myactivity.updateListView();
	    }
	};
	
	
	private static final String TAG = "MainActivity";
	private static MainActivity myactivity = null;
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myactivity = this;
		
		MyGcm mygcm = new MyGcm(this);
		mygcm.checkPlayServices();
		
		IntentFilter intentFilter = new IntentFilter("update");
		registerReceiver(updateListViewReceiver, intentFilter);
		
		updateListView();
		//bluetooth = new MyBluetooth(myActivity);
		
		
		//Intent intent = new Intent(this, PrintBluetoothService.class);
		//startService(intent);
		/*
		button = findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v (TAG, "Button click");
				int val = bluetooth.connectToServer("1200.0", "xxxx-xxxx-xxxx-xxxx");
				label.setText("result: " + val);
			}
		});
		*/
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		updateListView();
	}
	
	public void updateListView() {
		ListAdapter adapter = new MySimpleArrayAdapter(getApplicationContext(), MailManager.getIstance().getMails());
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(adapter);
		listView.invalidateViews();
	}
}


