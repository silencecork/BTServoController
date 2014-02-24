package com.example.bluetoothtest;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.example.bluetoothtest.CircularSeekBar.OnCircularSeekBarChangeListener;

public class Control extends Activity {
	
	private BluetoothDevice mDevice;
	
	private static final int COMMAND_SEND_FINISH = 0;
	
	private static final int COMMAND_CONNECT_DEVICE_FINISH = 1;
	
	private static final int COMMAND_SEND_COMMAD = 2;
	
	private TextView mDegreeText;
	
	private BluetoothAdapter mBluetoothAdapter;
	
	private CircularSeekBar mSeekBar;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == COMMAND_SEND_FINISH) {
				
			} else if (msg.what == COMMAND_CONNECT_DEVICE_FINISH) {
				if (mSeekBar != null) {
					mSeekBar.setEnabled(true);
					mSeekBar.setProgress(0);
				}
				mBTThread.writeCommand(new Command("0"));
			} else if (msg.what == COMMAND_SEND_COMMAD) {
				int progress = msg.arg1;
				if (mDegreeText != null) {
					mDegreeText.setText(String.valueOf(progress));
				}
				Command command = new Command(String.valueOf(progress));
				if (mBTThread != null) {
					mBTThread.writeCommand(command);
				}
			}
		}
		
	};
	
	private BTThread mBTThread; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String addr = getIntent().getStringExtra("device");
		if (addr == null) {
			finish();
			return;
		}
		
		setContentView(R.layout.activity_control);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		mDevice = mBluetoothAdapter.getRemoteDevice(addr);
		
		mBTThread = new BTThread(mDevice, mHandler);
		mBTThread.start();
		
		mDegreeText = (TextView) findViewById(R.id.degree);
		
		mSeekBar = (CircularSeekBar) findViewById(R.id.seekBar1);
		mSeekBar.setEnabled(false);
		mSeekBar.setOnSeekBarChangeListener(new OnCircularSeekBarChangeListener() {


			@Override
			public void onProgressChanged(CircularSeekBar circularSeekBar,
					int progress, boolean fromUser) {
				if (fromUser) {
//					mBTThread.writeCommand(new Command(String.valueOf(progress)));
					if (!mHandler.hasMessages(COMMAND_SEND_COMMAD)) {
						Message message = Message.obtain(mHandler, COMMAND_SEND_COMMAD, progress, 0);
						mHandler.sendMessageDelayed(message, 30);
					}
				}
				
			}

			@Override
			public void onStopTrackingTouch(CircularSeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStartTrackingTouch(CircularSeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (mBTThread != null) {
			mBTThread.stopBTThread();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	class Command {
		
		private String strCommand;
		
		public Command(String str) {
			strCommand = str;
		}
		
		@Override
		public String toString() {
			return strCommand;
		}
	}

	class BTThread extends Thread {
		private static final String TAG = "BTThread";
		private static final String BASE_UUID = "00001101-0000-1000-8000-00805F9B34FB";
		private BluetoothDevice mDevice;
		private boolean mIsDone;
		
		private Handler mUIHandler;
		private InputStream mIn;
		private OutputStream mOut;
		
		BTThread(BluetoothDevice device, Handler handler) {
			mDevice = device;
			mUIHandler = handler;
		}
		
		public void stopBTThread() {
			mIsDone = true;
		}
		
		@Override 
		public void run() {
			mBluetoothAdapter.cancelDiscovery();
			
			BluetoothSocket socket = null;
			try {
				socket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(BASE_UUID));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (socket == null) {
				return;
			}
			
			Log.i(TAG, "create server socket success");
			
			try {
				socket.connect();
			} catch (IOException e1) {
				e1.printStackTrace();
				
				return;
			}
			
			
			mUIHandler.sendEmptyMessage(COMMAND_CONNECT_DEVICE_FINISH);
			
			try {
				mOut = socket.getOutputStream();
				mIn = socket.getInputStream();
				byte[] buffer = new byte[1024];
				while (!mIsDone) {
					if (mIn.available() > 0) {
						int bytes = mIn.read(buffer);
						Log.i(TAG, "listening.."+bytes);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close(mOut);
				close(mIn);
				disconnect(socket);
			}
			
		}
		
		public void writeCommand(Command command) {
			if (mOut == null) {
				return;
			}
			String strCommand = command.toString();
			try {
				mOut.write(strCommand.getBytes());
				mOut.flush();
				mUIHandler.sendEmptyMessage(COMMAND_SEND_FINISH);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		private void close(Closeable c) {
			if (c != null) {
				try {
					c.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		private void disconnect(BluetoothSocket socket){
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
