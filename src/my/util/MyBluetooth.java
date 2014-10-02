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
import android.widget.Toast;

public class MyBluetooth {
	
	private static final String TAG = "MyBluetooth";
	// Un tentativo di send ogni mezzo secondo
	private static final int MAX_TENTATIVI = 10;
	
	// DELL
	 private String serverDeviceAddress = "7C:7A:91:7B:9C:EB";
	
	// Raspberry
//	private String serverDeviceAddress = "00:02:72:A7:DC:E6";
	private int serverDeviceRFCOMMPort = 3;

	private Context context;
	

	public MyBluetooth(Context context)
	{
		this.context = context;
	}
	
	public int sendToServer (String data) throws InterruptedException
	{
		BluetoothDevice serverDevice = null;
		BluetoothSocket socket = null;
		boolean success = false;
		int tentativi = 0;		
		int ris = -1;
		
		while ((!success) && (tentativi < MAX_TENTATIVI))
		{
			Log.v (TAG, "connection...");	
			try {
				if (serverDevice == null)
					serverDevice = createBluetoothServerDevice();
				Method m = serverDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
				socket = (BluetoothSocket) m.invoke(serverDevice, Integer.valueOf(serverDeviceRFCOMMPort));  
				
				socket.connect();
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();
				out.write ((new String(data +"&")).getBytes());
				Log.v (TAG, "sent string");
				
				ris = in.read();
				out.write(0);
				Log.v (TAG, "Receive code: " + ris);
				success = true;
				out.close();
				Log.v (TAG, "close");
			} catch (Exception e) {
				Log.e (TAG, "TENTATIVO " + tentativi + " FALLITO");
				tentativi++;
				e.printStackTrace();
				Thread.sleep(500);
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		if (!success)
			Toast.makeText(context, "Mi dispiace non riesco a stampare :-( Impossibile contattare la stampante. Ci rinuncio", 
					Toast.LENGTH_LONG).show();
		else
			Toast.makeText(context, "Scontrino spedito in stampa con success", 
					Toast.LENGTH_LONG).show();
		
		return ris;
	}
	
	
	private BluetoothDevice createBluetoothServerDevice (){
		
		BluetoothDevice serverDevice = null;
		
		BluetoothAdapter mBluetoothAdapter;
		Set<BluetoothDevice> pairedDevices;
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null)  
		{
			if (!mBluetoothAdapter.isEnabled())
			{
				Log.v (TAG, "Richiesta attivazione bluetooth");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.context.startActivity(enableBtIntent);
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
				
				return serverDevice;
			}
		}
		Log.e (TAG, "Creazione MyBluetooth FAILED");
		Toast.makeText(context, "Errore senza speranza. Inizializzazione bluetooth fallita, contattare Giuseppe.", 
				   Toast.LENGTH_LONG).show();
		return null;
	}
}
