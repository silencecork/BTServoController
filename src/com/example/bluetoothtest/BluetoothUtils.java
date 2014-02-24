package com.example.bluetoothtest;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

public class BluetoothUtils {

	public static final int REQUEST_ENABLE_BT = 0;
	
	public static void enableBluetooth(Activity activity) {
		Intent enableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	}
	
	public static BluetoothAdapter getDefaultBluetoothAdapter() {
		return BluetoothAdapter.getDefaultAdapter();
	}
	
	public static ArrayList<BluetoothDevice> showPairedDevice(BluetoothAdapter adapter) {
		if (!adapter.isEnabled()) {
			return null;
		}
		Set<BluetoothDevice> devices = adapter.getBondedDevices();
		ArrayList<BluetoothDevice> retList = new ArrayList<BluetoothDevice>();
		retList.addAll(devices);
		
		return retList;
	}
	
	public static void doDiscovery(BluetoothAdapter adapter) {
		if (!adapter.isEnabled()) {
			return;
		}
		if (adapter.isDiscovering()) {
			adapter.cancelDiscovery();
		}
		
		adapter.startDiscovery();
	}
	
	public static void disableAll(BluetoothAdapter adapter) {
		adapter.cancelDiscovery();
		adapter.disable();
	}
	
	public static void doConnect(Activity activity, BluetoothDevice device) {
		Intent intent = new Intent(activity, Control.class);
		intent.putExtra("device", device.getAddress());
		activity.startActivity(intent);
	}
	
	public static void pairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass()
					.getMethod("createBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e("BluetoothUtils", e.getMessage());
		}
	}
	
	public static void unpairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass()
					.getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e("BluetoothUtils", e.getMessage());
		}
	}
}
