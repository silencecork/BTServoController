package com.example.bluetoothtest;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private ListView mListView;
	private DeviceAdapter mAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private BTBroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new DeviceAdapter();
		mListView.setAdapter(mAdapter);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			finish();
			return;
		}
		
		mReceiver = new BTBroadcastReceiver();
		
		IntentFilter btReceiverIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		btReceiverIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		btReceiverIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		
		registerReceiver(mReceiver, btReceiverIntentFilter);
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		if (mBluetoothAdapter != null) {
			if (!mBluetoothAdapter.isEnabled()) {
				BluetoothUtils.enableBluetooth(this);
			}
		} 
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStop(){
		super.onStop();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BluetoothUtils.disableAll(mBluetoothAdapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.action_show_pair_device) {
			if (mListView != null) {
				mListView.setOnItemClickListener(mDeviceOnItemClickListener);
			}
			ArrayList<BluetoothDevice> pairList = BluetoothUtils.showPairedDevice(mBluetoothAdapter);
			if (mAdapter != null) {
				mAdapter.clearData();
				mAdapter.setData(pairList);
				mAdapter.notifyDataSetChanged();
			}
		} else if (item.getItemId() == R.id.action_discovery) {
			if (mListView != null) {
				mListView.setOnItemClickListener(mDeviceOnItemClickListener);
			}
			if (mAdapter != null) {
				mAdapter.clearData();
			}
			BluetoothUtils.doDiscovery(mBluetoothAdapter);
		}
		return true;
	}
	
	private OnItemClickListener mDeviceOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			BluetoothDevice device = (BluetoothDevice) v.getTag();
			if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
				BluetoothUtils.doConnect(MainActivity.this, device);
			} else {
				BluetoothUtils.pairDevice(device);
			}
		}
	};

	class DeviceAdapter extends BaseAdapter {
		
		private ArrayList<BluetoothDevice> mDatas;
		
		public void setData(ArrayList<BluetoothDevice> devices) {
			mDatas = devices;
		}
		
		public void updateData(BluetoothDevice device) {
			if (mDatas == null) {
				mDatas = new ArrayList<BluetoothDevice>();
			}
			if (!mDatas.contains(device)) {
				mDatas.add(device);
				notifyDataSetChanged();
			}
		}
		
		public void clearData(){
			if (mDatas == null) {
				return;
			}
			mDatas.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return (mDatas != null) ? mDatas.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return (mDatas != null) ? mDatas.get(arg0) : null;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (arg1 == null) {
				arg1 = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1, null);
			}
			
			TextView text = (TextView) arg1.findViewById(android.R.id.text1);
			
			BluetoothDevice device = (BluetoothDevice) getItem(arg0);
			String deviceName = device.getName();
			String deviceAddr = device.getAddress();
			
			text.setText(deviceName + " " + deviceAddr);
			
			arg1.setTag(device);
			
			return arg1;
		}
		
	}
	
	class BTBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (mAdapter != null) {
					mAdapter.updateData(device);
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
//				int prevBondState = intent.getIntExtra(
//						BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
//				int bondState = intent.getIntExtra(
//						BluetoothDevice.EXTRA_BOND_STATE, -1);
				
			}
		}
		
	}

}
