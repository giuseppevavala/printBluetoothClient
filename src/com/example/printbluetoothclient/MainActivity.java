package com.example.printbluetoothclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import my.util.MailManager;
import my.util.MyBluetooth;
import my.util.MyGcm;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

@SuppressLint("HandlerLeak")
public class MainActivity extends ActionBarActivity {
	private BroadcastReceiver updateListViewReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			Log.i (TAG, "Ricevuto messaggio di update");
			myactivity.updateMessages();
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
				try {
					(new MyBluetooth(getApplicationContext())).sendToServer(TEST_DATA);
				} catch (Exception e) {
					e.printStackTrace();
				}
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

				try {
					(new MyBluetooth(getApplicationContext())).sendToServer(data);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateMessages();		
	}
	
	public void updateMessages() {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				getHistoryMessage();
				getMessageFinishHandler.sendEmptyMessage(0);
			}
		})).start();
	}
	
	private void updateListView() {
		ListAdapter adapter = new MySimpleArrayAdapter(getApplicationContext(), MailManager.getIstance().getMails());
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(adapter);
		listView.invalidateViews();
	}
	
	
	private void getHistoryMessage() {
		Log.i (TAG, "Ottengo la lista dei messaggi");
		
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpGet httpget = new HttpGet("http://pacific-dusk-5592.herokuapp.com/message");

	    try {
	        String authorizationString = "Basic " + Base64.encodeToString(("mobile" + ":" + "lam1apassw0rds1cur1ss1ma").getBytes(), Base64.DEFAULT); 
	        httpget.setHeader("Authorization", authorizationString);

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httpget);
	        
	        JSONArray array = new JSONArray(EntityUtils.toString(response.getEntity()));
	        
	        MailManager.getIstance().clear();
	        for (int i = 0; i < array.length(); i++)
	        	MailManager.getIstance().addMail(-1, array.getString(i));
	        
	        Log.i (TAG, array.toString());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 
	}
	
	
	private Handler getMessageFinishHandler = new Handler() {
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	updateListView();
        }
    };
}


