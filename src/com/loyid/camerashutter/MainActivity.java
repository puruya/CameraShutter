package com.loyid.camerashutter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";
	
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mBluetoothEnabled = false;
	
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int MODE_CAMERA = 0;
	private static final int MODE_SHUTTER = 1;
	
	private static final String SERVICE_NAME = "camera_shutter";
	private static final UUID SERVICE_UUID = UUID.fromString("0001112f-0000-1000-8000-00805F9B34FB");
	
	private ToggleButton mToggleButton = null;
	private RadioGroup mRadioGroup = null;
	private int mMode = MODE_CAMERA;
	
	private static final int MSG_HANDLE_READ = 0;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_HANDLE_READ:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mRadioGroup = (RadioGroup)findViewById(R.id.radio_gruop);
		mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.radio_camera:
					mMode = MODE_CAMERA;
					break;
				case R.id.radio_shutter:
					mMode = MODE_SHUTTER;
					break;
				}
			}			
		});
		
		mToggleButton = (ToggleButton)findViewById(R.id.btn_toggle);
		mToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked == mBluetoothEnabled)
					return;
				mBluetoothEnabled = isChecked;
				if (!mBluetoothAdapter.isEnabled()) {
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				} else {
					mBluetoothAdapter.disable();
				}
			}			
		});
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
		createBluetoothAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void createBluetoothAdapter() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			mToggleButton.setEnabled(false);
			return;
		}
		
		mRadioGroup.check(R.id.radio_camera);
	}
	
	private void onButtonClick(View view) {
		cancelDiscovery();
			
		int id = view.getId();
		switch(id) {
		case R.id.btn_discovery:
			startDiscovery();
			break;
		case R.id.btn_focus:
			Log.d(TAG, "Focus button is clicked");
			break;
		case R.id.btn_shutter:
			Log.d(TAG, "Shutter button is clicked");
			break;
		case R.id.btn_start:
			startShutterServer();
			break;
		case R.id.btn_stop:
			stopShutterServer();
			break;
		}
	}

	private void stopShutterServer() {
		// TODO Auto-generated method stub
		
	}

	private void startShutterServer() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
			}
		}
	}
	
	private void startDiscovery() {
		mBluetoothAdapter.startDiscovery();
	}
	
	private void cancelDiscovery() {
		mBluetoothAdapter.cancelDiscovery();
	}
	
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
			} catch (IOException e) { }
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			while (true) {
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					manageConnectedSocket(socket);
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) { }
		}
	}
	
	private void manageConnectedSocket(BluetoothSocket socket) {
		
	}
	
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024];  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					String receivedString = new String(buffer);
					// Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MSG_HANDLE_READ, receivedString).sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) { }
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}
}
