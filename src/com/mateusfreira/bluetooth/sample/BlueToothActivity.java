package com.mateusfreira.bluetooth.sample;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BlueToothActivity extends ListActivity implements
		BlueToothSeachListener, BluetoothConnectionListener {
	BlueToothMananger blueToothMananger;
	List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	private List<String> names = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		blueToothMananger = new BlueToothMananger(this);
		if (!blueToothMananger.isBlueToothEnabled()) {
			blueToothMananger.forceEnableBlueTooth();
		} else {
			searchDevices();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		searchDevices();
	}

	private void searchDevices() {
		blueToothMananger.startSearchDevices(this);
	}

	@Override
	public void deviceFound(BluetoothDevice bluetoothDevice) {
		devices.add(bluetoothDevice);
		updateUiDevices();
	}

	private void updateUiDevices() {
		for (BluetoothDevice device : devices) {
			names.add(device.getName());
		}
		runOnUiThread(new Runnable() {

			public void run() {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						BlueToothActivity.this,
						android.R.layout.simple_list_item_1,
						BlueToothActivity.this.names);
				setListAdapter(adapter);
			}
		});
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		blueToothMananger.conectTo(devices.get(position), this, 1);
	}

	@Override
	public void erro(Throwable e) {
		Toast.makeText(this, "Erro no bluetooth", Toast.LENGTH_LONG).show();

	}

	@Override
	public void messageReceived(final byte[] message, int bytes) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(BlueToothActivity.this, new String(message),
						Toast.LENGTH_LONG).show();

			}
		});

	}

	@Override
	public void connectionLost() {
		Toast.makeText(this, "ConnectionLost", Toast.LENGTH_LONG).show();

	}

}
/**
 * private static final int REQUEST_ENABLE_BT = 1230; private BluetoothAdapter
 * mBluetoothAdapter; private List<BluetoothDevice> mArrayAdapter = new
 * ArrayList<BluetoothDevice>(); private ArrayList<String> names; private final
 * BroadcastReceiver mReceiver = new BroadcastReceiver() { public void
 * onReceive(Context context, Intent intent) { String action =
 * intent.getAction(); if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 * BluetoothDevice device = intent
 * .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); mArrayAdapter.add(device);
 * names.add(device.getName()); updateViewDevices(); } } };
 * 
 * @Override public void onCreate(Bundle savedInstanceState) {
 *           super.onCreate(savedInstanceState); mBluetoothAdapter =
 *           BluetoothAdapter.getDefaultAdapter(); if (mBluetoothAdapter !=
 *           null) {
 * 
 *           if (!mBluetoothAdapter.isEnabled()) { Intent enableBtIntent = new
 *           Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
 *           startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); } else {
 *           searchDevices(); } } else { Toast.makeText(this,
 *           " Esse dispositivo nao da suporte a bluetooth!!",
 *           Toast.LENGTH_LONG).show(); } }
 * @Override protected void onActivityResult(int requestCode, int resultCode,
 *           Intent data) { super.onActivityResult(requestCode, resultCode,
 *           data); searchDevices(); }
 * 
 *           private void searchDevices() { Set<BluetoothDevice> pairedDevices =
 *           mBluetoothAdapter .getBondedDevices(); names = new
 *           ArrayList<String>(); if (pairedDevices.size() > 0) { for
 *           (BluetoothDevice device : pairedDevices) {
 *           mArrayAdapter.add(device); names.add(device.getName()); } }
 *           updateViewDevices(); }
 * 
 *           private void updateViewDevices() { runOnUiThread(new Runnable() {
 * 
 *           public void run() { ArrayAdapter<String> adapter = new
 *           ArrayAdapter<String>( BlueToothActivity.this,
 *           android.R.layout.simple_list_item_1, BlueToothActivity.this.names);
 *           setListAdapter(adapter); } }); }
 * 
 *           protected void onListItemClick(ListView l, View v, int position,
 *           long id) { String item = (String)
 *           getListAdapter().getItem(position); Toast.makeText(this, item +
 *           " selected", Toast.LENGTH_LONG).show(); new
 *           ConnectThread(mArrayAdapter.get(position)).start(); }
 * 
 *           }
 * 
 *           class ConnectThread extends Thread { private BluetoothSocket
 *           mmSocket = null; private BluetoothDevice mmDevice = null;
 * 
 *           public ConnectThread(BluetoothDevice device) { mmDevice = device;
 *           tryConnection(); }
 * 
 *           private void tryConnection() { try { BluetoothSocket tmp = null;
 *           Method m = mmDevice.getClass().getMethod("createRfcommSocket", new
 *           Class[] { int.class }); tmp = (BluetoothSocket) m.invoke(mmDevice,
 *           1); mmSocket = tmp; } catch (Throwable e) { Log.e("ErroBlue",
 *           "SIm", e); } }
 * 
 *           public void run() { try { mmSocket.connect();
 *           manageConnectedSocket(mmSocket); } catch (IOException
 *           connectException) { Log.e("Erro", "Erro ao connectar"); try {
 *           mmSocket.close(); } catch (IOException closeException) { } return;
 *           }
 * 
 *           }
 * 
 *           private void manageConnectedSocket(BluetoothSocket mmSocket2) { new
 *           ConnectedThread(mmSocket2).start(); }
 * 
 *           public void cancel() { try { mmSocket.close(); } catch (IOException
 *           e) { } }
 * 
 *           }
 * 
 *           class ConnectedThread extends Thread { private final
 *           BluetoothSocket mmSocket; private final InputStream mmInStream;
 *           private final OutputStream mmOutStream;
 * 
 *           public ConnectedThread(BluetoothSocket socket) { mmSocket = socket;
 *           InputStream tmpIn = null; OutputStream tmpOut = null;
 * 
 *           // Get the input and output streams, using temp objects because //
 *           member streams are final try { tmpIn = socket.getInputStream();
 *           tmpOut = socket.getOutputStream(); } catch (IOException e) { }
 * 
 *           mmInStream = tmpIn; mmOutStream = tmpOut; }
 * 
 *           public void run() { byte[] buffer = new byte[1024]; int bytes; //
 *           bytes returned from read()
 * 
 *           while (true) { try { bytes = mmInStream.read(buffer); } catch
 *           (IOException e) { break; } } }
 * 
 *           public void cancel() { try { mmSocket.close(); } catch (IOException
 *           e) { } } }
 */
