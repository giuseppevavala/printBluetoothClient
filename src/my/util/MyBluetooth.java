package my.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBluetooth {
	
	private static final String TAG = "MyBluetooth";
	private BluetoothAdapter mBluetoothAdapter;
	private Set<BluetoothDevice> pairedDevices;
	
	private String serverDeviceAddress = "7C:7A:91:7B:9C:EB";
	private int serverDeviceRFCOMMPort = 3;
	private BluetoothDevice serverDevice = null;
	

	public MyBluetooth(Context context)
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null)  
		{
			if (!mBluetoothAdapter.isEnabled())
			{
				Log.v (TAG, "Richiesta attivazione bluetooth");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				context.startActivity(enableBtIntent);
			}
			
			Log.v (TAG, "Loading paired devices");
			pairedDevices = mBluetoothAdapter.getBondedDevices();
			// If there are paired devices
			if (pairedDevices.size() > 0) {
				// Loop through paired devices
				for (BluetoothDevice device : pairedDevices) {
					// Add the name and address to an array adapter to show in a ListView
					Log.v (TAG, "Device: " + device.getName() + "\n" + device.getAddress());
					if (device.getAddress().compareToIgnoreCase (serverDeviceAddress) == 0)
						serverDevice = device;
				}
			}
			if (serverDevice != null )
			{
				Log.v (TAG, "Creazione MyBluetooth: SUCCESS");
				return;
			}
		}
		Log.e (TAG, "Creazione MyBluetooth FAILED");
	}
	
	public int sendToServer (String data)
	{
		Log.v (TAG, "connection...");	
		BluetoothSocket socket = null;
		try {
			
			Method m = serverDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
			socket = (BluetoothSocket) m.invoke(serverDevice, Integer.valueOf(serverDeviceRFCOMMPort));  
			
			socket.connect();
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			out.write ((new String(data +"&")).getBytes());
			Log.v (TAG, "sent string");
			
			int ris = in.read();
			out.write(0);
			Log.v (TAG, "Receive code: " + ris);
			
			out.close();
			Log.v (TAG, "close");
			return ris;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return -1;
		
		
	}
}
