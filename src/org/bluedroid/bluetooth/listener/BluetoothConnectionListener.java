package org.bluedroid.bluetooth.listener;

public interface BluetoothConnectionListener {
	public void erro(Throwable e);

	public void messageReceived(byte[] message, int bytes);

	public void connectionLost();

}
