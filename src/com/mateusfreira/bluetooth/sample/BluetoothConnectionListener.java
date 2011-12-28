package com.mateusfreira.bluetooth.sample;

public interface BluetoothConnectionListener {
	public void erro(Throwable e);

	public void messageReceived(byte[] message, int bytes);

	public void connectionLost();

}
