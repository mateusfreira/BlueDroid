package com.mateusfreira.bluetooth.sample;

import java.util.ArrayList;
import java.util.List;

import org.bluedroid.bluetooth.BlueToothMananger;
import org.bluedroid.bluetooth.listener.BlueToothSeachListener;
import org.bluedroid.bluetooth.listener.BluetoothConnectionListener;

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
		names = new ArrayList<String>();
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
