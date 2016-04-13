package fretx.version3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class LearnFragmentTuner extends Fragment{
	public static final String TAG = "AndroidTuner";
	private PdUiDispatcher dispatcher;
	
/*	private final boolean LAUNCHANALYZER = true;

	private int nFlag = 0;*/

	Timer timer;
	MyTimerTask myTimerTask;
	int nCounter = 0;

	/*private NewUiController uiController = null;*/

	private ArrayList<Button> buttons = new ArrayList<Button>();


	private TextView tvTimeLapse;
	//int oldString = 1;

	int newPitch = 0;
	int notes[] = new int[]{40, 45, 50, 55, 59, 64};
	//int notes[] = new int[]{48, 50, 52, 53, 55, 57, 59};
	String labels[] = new String[]{"E-String Low","A-String","D-String","G-String","B-String","E-String High"};
	//String labels[] = new String[]{"C","D","E","F","G","A", "B"};

	/*private static final String notes[] =
			{"C", "C", "D", "E", "E", "F",
					"F", "G", "A", "A", "B", "B"};

	private static final String sharps[] =
			{"", "\u266F", "", "\u266D", "", "",
					"\u266F", "", "\u266D", "", "\u266D", ""};*/

	private PdService pdService = null;

	private final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pdService = ((PdService.PdBinder)service).getService();
			try {
				initPd();
				loadPatch();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				mActivity.finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};

	private MainActivity mActivity;
	private View rootView;

	public LearnFragmentTuner(){

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.learn_fragment_tuner, container, false);

		buttons.add((Button)rootView.findViewById(R.id.button1));
		buttons.add((Button)rootView.findViewById(R.id.button2));
		buttons.add((Button)rootView.findViewById(R.id.button3));
		buttons.add((Button)rootView.findViewById(R.id.button4));
		buttons.add((Button)rootView.findViewById(R.id.button5));
		buttons.add((Button)rootView.findViewById(R.id.button6));

		//button2 = (Button)rootView.findViewById(R.id.button2);
		//button2.setBackgroundColor(Color.BLUE);
		//button3 = (Button)rootView.findViewById(R.id.button3);
		//button3.setBackgroundColor(Color.BLUE);
		//button4 = (Button)rootView.findViewById(R.id.button4);
		//button4.setBackgroundColor(Color.BLUE);
		//button5 = (Button)rootView.findViewById(R.id.button5);
		//button5.setBackgroundColor(Color.BLUE);
		//button6 = (Button)rootView.findViewById(R.id.button6);
		//button6.setBackgroundColor(Color.BLUE);

		for(int i = 0; i < buttons.size(); i++){
			if(i==0){
				buttons.get(0).setBackgroundColor(Color.GREEN);
			}else{
				buttons.get(i).setBackgroundColor(Color.BLUE);
			}
		}

		//initSystemServices();
		//mActivity.bindService(new Intent(mActivity, PdService.class), pdConnection, mActivity.BIND_AUTO_CREATE);



		tvTimeLapse = (TextView)rootView.findViewById(R.id.tvTimeLapse);

		startTimer();

		/*uiController = new NewUiController(this);

		if(LAUNCHANALYZER) {
			try {
				if (Config.soundAnalyzer == null )
					Config.soundAnalyzer = new SoundAnalyzer();
			} catch(Exception e) {
				Toast.makeText(mActivity, "The are problems with your microphone :(", Toast.LENGTH_LONG ).show();
				Log.e(TAG, "Exception when instantiating SoundAnalyzer: " + e.getMessage());
			}
			Config.soundAnalyzer.addObserver(uiController);
		}*/
		return rootView;

    }

	public void startTimer(){
		if (timer != null){
			timer.cancel();
		}
		timer = new Timer();

		myTimerTask = new MyTimerTask();

		timer.schedule(myTimerTask, 1000, 1000);
	}

	private void  initPd() throws IOException {
		// Configure the audio glue
		AudioParameters.init(mActivity);
		int sampleRate = AudioParameters.suggestSampleRate();
		PdAudio.initAudio(sampleRate, 1, 2, 8, true);
		//pdService.initAudio(sampleRate, 1, 2, 5000f);

		ConnectThread connectThread = new ConnectThread(Util.str2array("{" + (6-newPitch) + ",0}"));
		connectThread.run();

		start();

		// Create and install the dispatcher
		dispatcher = new PdUiDispatcher();
		PdBase.setReceiver(dispatcher);
		dispatcher.addListener("pitch", new PdListener.Adapter() {
			@Override
			public void receiveFloat(String source, final float x) {
				float dx = (x - notes[newPitch]) / 2;
				if (-0.2 < dx && dx < 0.2) {
					//if(oldPitch < 5) {
					//	oldPitch = oldPitch + 1;
					//}else{
					//	oldPitch = 0;
					//}
					Random rand = new Random();
					buttons.get(newPitch).setBackgroundColor(Color.BLUE);
					if(newPitch < 5) {
						newPitch = newPitch + 1;
					}else{
						newPitch = 0;//rand.nextInt(notes.length);
					}
					buttons.get(newPitch).setBackgroundColor(Color.GREEN);
					//pitchLabel.setText(labels[newPitch]);
					successPlayer();
					ConnectThread connectThread = new ConnectThread(Util.str2array("{" + (6-newPitch) + ",0}"));
					connectThread.run();
				}
			}
		});
	}

	public void successPlayer(){
		MediaPlayer mediaPlayer= MediaPlayer.create(mActivity, R.raw.success_sound);
		mediaPlayer.start();
	}

	private void start() {
		if (!pdService.isRunning()) {
			Intent intent = new Intent(mActivity,
					MainActivity.class);
			pdService.startAudio(intent, R.drawable.icon,
					"GuitarTuner", "Return to GuitarTuner.");
		}
	}

	private void loadPatch() throws IOException {
		File dir = mActivity.getFilesDir();
		IoUtils.extractZipResource(
				getResources().openRawResource(R.raw.tuner), dir, true);
		File patchFile = new File(dir, "tuner.pd");
		PdBase.openPatch(patchFile.getAbsolutePath());
	}

	private void initSystemServices() {
		TelephonyManager telephonyManager =
				(TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (pdService == null) return;
				if (state == TelephonyManager.CALL_STATE_IDLE) {
					start();
				} else {
					pdService.stopAudio();
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {

			nCounter ++;

			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					tvTimeLapse.setText("" + nCounter / 60 + " : " + nCounter % 60);
				}
			});
		}

	}


	// 1-6 strings (ascending frequency), 0 - no string.
	/*public void changeString(int stringId) {
		if(oldString ==stringId) {
			Log.d("+++", "changString" + stringId + "oldString" + oldString);

			if (oldString == 1){

				successPlayer();

				ConnectThread connectThread = new ConnectThread(Util.str2array("{6,0}"));
				connectThread.run();

				button1.setBackgroundColor(Color.BLUE);
				button2.setBackgroundColor(Color.GREEN);
				oldString = 2;
				return;
			}
			if (oldString == 2){
				successPlayer();

				ConnectThread connectThread = new ConnectThread(Util.str2array("{5,0}"));
				connectThread.run();

				button2.setBackgroundColor(Color.BLUE);
				button3.setBackgroundColor(Color.GREEN);
				oldString = 3;
				return;
			}
			if (oldString == 3){
				successPlayer();
				ConnectThread connectThread = new ConnectThread(Util.str2array("{4,0}"));
				connectThread.run();
				oldString = 4;
				button3.setBackgroundColor(Color.BLUE);
				button4.setBackgroundColor(Color.GREEN);
				return;
			}
			if (oldString == 4){
				successPlayer();
				ConnectThread connectThread = new ConnectThread(Util.str2array("{3,0}"));
				connectThread.run();
				oldString = 5;
				button4.setBackgroundColor(Color.BLUE);
				button5.setBackgroundColor(Color.GREEN);
				return;
			}
			if (oldString == 5){
				successPlayer();
				ConnectThread connectThread = new ConnectThread(Util.str2array("{2,0}"));
				connectThread.run();
				oldString = 6;
				button5.setBackgroundColor(Color.BLUE);
				button6.setBackgroundColor(Color.GREEN);
				return;
			}
			if (oldString == 6){
				successPlayer();
				ConnectThread connectThread = new ConnectThread(Util.str2array("{1,0}"));
				connectThread.run();
				oldString = 1;
				button6.setBackgroundColor(Color.BLUE);
				button1.setBackgroundColor(Color.GREEN);
				if (nFlag == 1){
					nFlag = 0;
					timer.cancel();
					if (nCounter <= 40){
						Config.nPoints = 5;
					}
					if (nCounter>40 && nCounter <=60){
						Config.nPoints = 4;
					}
					if (nCounter>60 && nCounter<=80){
						Config.nPoints = 3;
					}
					if (nCounter>80 && nCounter <=100){
						Config.nPoints = 2;
					}
					if (nCounter >100){
						Config.nPoints = 1;
					}
					Util.stopViaData();

					android.support.v4.app.FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
					LearnFragmentTunerResult yesnoDialog = new LearnFragmentTunerResult();
					yesnoDialog.setCancelable(true);
					yesnoDialog.setDialogTitle("Result");
					yesnoDialog.show(fragmentManager, "Yes/No Dialog");
				}else{
					nFlag = 1;
				}
				return;
			}
		}
	}


	public void coloredGuitarMatch(double match) {
//		tune.setBackgroundColor(
//				Color.rgb((int)(match*targetColor[0]+ (1-match)*awayFromTargetColor[0]),
//						(int)(match*targetColor[1]+ (1-match)*awayFromTargetColor[1]),
//						(int)(match*targetColor[2]+ (1-match)*awayFromTargetColor[2])));


	}

	String oldmsg = "";
	public void displayMessage(String msg, String stringName, boolean positiveFeedback) {
		int textColor = positiveFeedback ? Color.rgb(34,139,34) : Color.rgb(255,36,0);
//		mainMessage.setText(msg);
//		stringMessage.setText(stringName);
//		mainMessage.setTextColor(textColor);
		Log.d("***", "displayMessage:"+msg+":"+stringName+":"+textColor);
		if (msg.equals(oldmsg)){
			return;
		}

//		tvMessage.setText(msg +"\n" + stringName);
//		Toast.makeText(this, msg +"\n"+stringName, Toast.LENGTH_LONG ).show();
		oldmsg = msg;
	}*/


	public void dumpArray(final double [] inputArray, final int elements) {
		Log.d(TAG, "Starting File writer thread...");
		final double [] array = new double[elements];
		for(int i=0; i<elements; ++i)
			array[i] = inputArray[i];
		new Thread(new Runnable() {
			@Override
			public void run() {
				try { // catches IOException below
					// Location: /data/data/your_project_package_structure/files/samplefile.txt
					String name = "Chart_" + (int)(Math.random()*1000) + ".data";
					FileOutputStream fOut = mActivity.openFileOutput(name,
							Context.MODE_WORLD_READABLE);
					OutputStreamWriter osw = new OutputStreamWriter(fOut);

					// Write the string to the file
					for(int i=0; i<elements; ++i)
						osw.write("" + array[i] + "\n");
					/* ensure that everything is
					 * really written out and close */
					osw.flush();
					osw.close();
					Log.d(TAG, "Successfully dumped array in file " + name);
				} catch(Exception e) {
					Log.e(TAG,e.getMessage());
				}
			}
		}).start();
	}

	/*public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(ConfigFlags.menuKeyCausesAudioDataDump) {
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				Log.d(TAG,"Menu button pressed");
				Log.d(TAG,"Requesting audio data dump");
				Config.soundAnalyzer.dumpAudioDataRequest();
				return true;
			}
		}
		return false;
	}
    @Override
	public void onDestroy() {
		super.onDestroy();
        Log.d(TAG, "onDestroy()");
		// Hint that it might be a good idea
		System.runFinalization();

	}

	@Override
	public  void onPause() {
		super.onPause();
        Log.d(TAG, "onPause()");
	}



	@Override
	public void onResume() {
		super.onResume();
        Log.d(TAG,"onResume()");
        if(Config.soundAnalyzer!=null)
			Config.soundAnalyzer.ensureStarted();
	}

	@Override
	public void onStart() {
		super.onStart();
        Log.d(TAG,"onStart()");
        if(Config.soundAnalyzer!=null)
			Config.soundAnalyzer.start();
	}

	@Override
	public  void onStop() {
		super.onStop();
        Log.d(TAG,"onStop()");
        if(Config.soundAnalyzer!=null)
			Config.soundAnalyzer.stop();
	}*/
	/////////////////////////////////BlueToothConnection/////////////////////////
	static private class ConnectThread extends Thread {
		byte[] array;
		public ConnectThread(byte[] tmp) {
			array = tmp;
		}

		public void run() {
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				Util.startViaData(array);
			} catch (Exception connectException) {
				Log.i(BluetoothClass.tag, "connect failed");
				// Unable to connect; close the socket and get out
				try {
					BluetoothClass.mmSocket.close();
				} catch (IOException closeException) {
					Log.e(BluetoothClass.tag, "mmSocket.close");
				}
				return;
			}
			// Do work to manage the connection (in a separate thread)
			if (BluetoothClass.mHandler == null)
				Log.v("debug", "mHandler is null @ obtain message");
			else
				Log.v("debug", "mHandler is not null @ obtain message");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity)getActivity();
		ConnectThread connectThread = new ConnectThread(Util.str2array("{0}"));
		connectThread.run();
		initSystemServices();
		mActivity.bindService(new Intent(mActivity, PdService.class), pdConnection, mActivity.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ConnectThread connectThread = new ConnectThread(Util.str2array("{0}"));
		connectThread.run();
		mActivity.unbindService(pdConnection);
	}
}