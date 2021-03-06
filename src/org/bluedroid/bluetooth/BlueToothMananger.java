package org.bluedroid.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import org.bluedroid.bluetooth.exception.BlueToothAleryEnabledException;
import org.bluedroid.bluetooth.exception.BlueToothNotConnectedException;
import org.bluedroid.bluetooth.exception.BlueToothNotEnableException;
import org.bluedroid.bluetooth.exception.BlueToothSuportedException;
import org.bluedroid.bluetooth.listener.BlueToothSeachListener;
import org.bluedroid.bluetooth.listener.BluetoothConnectionListener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BlueToothMananger {
	private BluetoothAdapter mBluetoothAdapter;
	private BlueToothManangerConnectedThread connectionThead = null;
	private Activity activity;
	private BluetoothConnectionListener bluetoothConnectionListener;
	public static final int REQUEST_ENABLE_BT = 1230;
	public static final int TIME_SEARCH = 300;
	private BlueToothSeachListener blueToothSeachListener;
	private BluetoothSocket mmSocket = null;
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				blueToothSeachListener.deviceFound(device);

			}
		}
	};

	public BlueToothMananger(Activity activity) {
		this.activity = activity;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	/**
	 * 
	 * @throws BlueToothSuportedException
	 *             if not suported BlueTooth
	 */
	public boolean isBlueToothEnabled() {
		verifySuported();
		return mBluetoothAdapter.isEnabled();
	}

	private void verifySuported() {
		if (!isBlueToothSuported()) {
			throw new BlueToothSuportedException();
		}

	}

	public boolean isBlueToothSuported() {
		return mBluetoothAdapter != null;
	}

	/**
	 * 
	 * @throws BlueToothSuportedException
	 *             , BlueToothAleryEnabledException
	 * 
	 */
	public BlueToothMananger forceEnableBlueTooth() {
		verifySuported();
		if (isBlueToothEnabled()) {
			throw new BlueToothAleryEnabledException();
		}
		Intent enableBtIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		return this;
	}

	/**
	 * 
	 * @throws BlueToothSuportedException
	 *             , BlueToothNotEnableException
	 * 
	 */
	public BlueToothMananger startSearchDevices(
			BlueToothSeachListener blueToothSeachListener) {
		verifySuported();
		verifyEnabled();
		this.blueToothSeachListener = blueToothSeachListener;
		setParedDevices();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		activity.registerReceiver(mReceiver, filter);
		mBluetoothAdapter.startDiscovery();

		return this;
	}

	private void setParedDevices() {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				blueToothSeachListener.deviceFound(device);
			}
		}
	}

	private void verifyEnabled() {
		if (!isBlueToothEnabled()) {
			throw new BlueToothNotEnableException();
		}
	}

	public BlueToothMananger conectTo(BluetoothDevice device,
			BluetoothConnectionListener bluetoothConnectionListener, int idApp) {
		this.bluetoothConnectionListener = bluetoothConnectionListener;
		try {
			mBluetoothAdapter.cancelDiscovery();
			BluetoothSocket tmp = null;
			Method m = device.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			tmp = (BluetoothSocket) m.invoke(device, idApp);
			mmSocket = tmp;
			mmSocket.connect();

			connectionThead = new BlueToothManangerConnectedThread(mmSocket,
					bluetoothConnectionListener);
			connectionThead.start();
		} catch (Throwable e) {
			try {
				mmSocket.close();
			} catch (Exception e1) {
				bluetoothConnectionListener.erro(e);
			}
			bluetoothConnectionListener.erro(e);
		}
		return this;

	}

	/**
	 * 
	 * @throws BlueToothNotConnectedException
	 */
	public BlueToothMananger disconect() {
		verifyConnected();
		connectionThead.cancel();
		bluetoothConnectionListener = null;
		return this;
	}

	private void verifyConnected() {
		if (!isConnected()) {
			throw new BlueToothNotConnectedException();
		}
	}

	public BlueToothMananger conectTo(BluetoothDevice device,
			BluetoothConnectionListener bluetoothConnectionListener, UUID uuid) {
		try {
			mBluetoothAdapter.cancelDiscovery();
			mmSocket = device.createRfcommSocketToServiceRecord(uuid);
			new BlueToothManangerConnectedThread(mmSocket,
					bluetoothConnectionListener);
		} catch (Throwable e) {
			bluetoothConnectionListener.erro(e);
		}
		return this;
	}

	public boolean isConnected() {
		return bluetoothConnectionListener != null;
	}
}

class BlueToothManangerConnectedThread extends Thread {
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private BluetoothConnectionListener bluetoothConnectionListener;
	private boolean disconected = false;

	public BlueToothManangerConnectedThread(BluetoothSocket socket,
			BluetoothConnectionListener bluetoothConnectionListener) {
		mmSocket = socket;
		InputStream tmpIn = null;
		this.bluetoothConnectionListener = bluetoothConnectionListener;
		try {
			tmpIn = socket.getInputStream();
		} catch (IOException e) {
		}

		mmInStream = tmpIn;
	}

	public void run() {
		byte[] buffer = new byte[512];
		int bytes; // bytes returned from read()

		while (true) {
			try {
				bytes = mmInStream.read(buffer);
				bluetoothConnectionListener.messageReceived(buffer, bytes);
			} catch (IOException e) {
				if (!disconected) {
					bluetoothConnectionListener.erro(e);
					bluetoothConnectionListener.connectionLost();
				}
				break;
			}
		}
	}

	public void cancel() {
		try {
			disconected = true;
			mmSocket.close();
			bluetoothConnectionListener.connectionLost();
		} catch (IOException e) {
			bluetoothConnectionListener.erro(e);
			bluetoothConnectionListener.connectionLost();
		}
	}
}