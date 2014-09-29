package my.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MyGcm {

	
	
	
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "847001789997";
	
	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "MyGCM";
	
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	
	String regid;
	private Activity activity;
	
	public MyGcm(Activity activity) {
	    this.activity = activity;
		
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
	    if (checkPlayServices()) {
	        gcm = GoogleCloudMessaging.getInstance(activity);
	        regid = getRegistrationId(activity.getApplicationContext());
	        Log.i (TAG, "TROVATO IL MIO REGID: ===> " + regid);
	        if (regid.isEmpty()) {
	            registerInBackground();
	        }
	    } else {
	        Log.i(TAG, "No valid Google Play Services APK found.");
	    }
	}
	
	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	public boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            activity.finish();
	        }
	        return false;
	    }
	    return true;
	}
	
	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGcmPreferences(context);
	    int appVersion = getAppVersion();
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
	
	/**
	 * Gets the current registration ID for application on GCM service, if there is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGcmPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion();
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		final Context context = activity.getApplicationContext();
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
					if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;
	
	                // You should send the registration ID to your server over HTTP, so it
	                // can use GCM/HTTP or CCS to send messages to your app.
	                sendRegistrationIdToBackend();
	
	                // For this demo: we don't need to send it because the device will send
	                // upstream messages to a server that echo back the message using the
	                // 'from' address in the message.
	
	                // Persist the regID - no need to register again.
	                storeRegistrationId(context, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }
	
	        @Override
	        protected void onPostExecute(String msg) {
	        	// TODO
	        }
	    }.execute(null, null, null);
	}
	
	// Send an upstream message.
	public void sendMessage(String msg) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", msg);
                    data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	// TODO
            }
        }.execute(null, null, null);
	}
	
		
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private int getAppVersion() {
		try {
	        Context context = activity.getApplicationContext() ;
			PackageInfo packageInfo = context .getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return activity.getSharedPreferences(activity.getClass().getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
	 * messages to your app. Not needed for this demo since the device sends upstream messages
	 * to a server that echoes back the message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		Log.i (TAG, "Mi registro al sito internet");
		
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://pacific-dusk-5592.herokuapp.com/register");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("userid", regid));
	        
	        String authorizationString = "Basic " + Base64.encodeToString(("mobile" + ":" + "lam1apassw0rds1cur1ss1ma").getBytes(), Base64.DEFAULT); 
	        httppost.setHeader("Authorization", authorizationString);
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	        Log.i (TAG, response.toString());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 
	}

}