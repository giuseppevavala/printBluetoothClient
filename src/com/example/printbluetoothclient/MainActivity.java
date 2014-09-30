package com.example.printbluetoothclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import my.util.MailManager;
import my.util.MyBluetooth;
import my.util.MyGcm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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


	protected static final String TEST_DATA = "totale=1200.00;data=26/08/1986;ora=09:00;carta=4023-6004-2698-8578";
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
		
		Button btnTest = (Button) findViewById(R.id.btnTest);
		btnTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				(new MyBluetooth(getApplicationContext())).sendToServer(TEST_DATA);
			}
		});
		
		final EditText editTextValue = (EditText) findViewById(R.id.editTextValue);		
		Button btnManualPrint = (Button) findViewById(R.id.btnManualPrint);
		btnManualPrint.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Date date = new Date();
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
				DateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.ITALY);
				
				String data = "carta=xxxx-xxxx-xxxx-xxxx;";
				data += "totale=" + editTextValue.getText() + ";";
				data += "data=" + dateFormat.format(date) + ";";
				data += "ora=" + hourFormat.format(date) + ";";

				(new MyBluetooth(getApplicationContext())).sendToServer(data);
			}
		});
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


