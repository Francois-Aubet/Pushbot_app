package de.tum.nst.pushbotcontrol;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.tum.nst.model.MotorCommand;



public class ActivityButtons extends Activity {


	private Button btn_forward, btn_backward, btn_left, btn_right, btn_stop, btn_return;
	private CheckBox btn_laser, btn_buzzer;

	private int motorLeft = 0;
	private int motorRight = 0;
	private String address;
	private int pwmBtnMotor;
	private int pwmBtnMotorSide;
	private String commandLeft;
	private String commandRight;
	private String commandHorn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_buttons);


		//just for test purpos here
		MainActivity.list.get(MainActivity.chosenone).disableEvents();

		pwmBtnMotor = (int) (MainActivity.maxPowerMotor);
		pwmBtnMotorSide = (int) (MainActivity.maxPowerMotor * 0.6);
		commandLeft = (String) getResources().getText(R.string.default_commandLeft);
		commandRight = (String) getResources().getText(R.string.default_commandRight);
		commandHorn = (String) getResources().getText(R.string.default_commandHorn);

		MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
		MainActivity.list.get(MainActivity.chosenone).enableMotorDriver();


		btn_forward = (Button) findViewById(R.id.forward);
		btn_backward = (Button) findViewById(R.id.backward);
		btn_left = (Button) findViewById(R.id.left);
		btn_right = (Button) findViewById(R.id.right);
		btn_stop = (Button) findViewById(R.id.stop);
		btn_return = (Button) findViewById(R.id.backbut);

		btn_laser = (CheckBox) findViewById(R.id.laser);
		//btn_buzzer = (CheckBox) findViewById(R.id.buzzer);

		btn_laser.setOnClickListener(btn_laser_listener);
		//btn_buzzer.setOnClickListener(btn_buzzer_listener);

		btn_forward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(pwmBtnMotor, pwmBtnMotor);
					return true;
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
					return true;
				}


				return false;
			}
		});

		btn_return.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				onBackPressed();
				MainActivity.list.get(MainActivity.chosenone).enableLaserRobot();

				return false;
			}
		});


		btn_stop.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);

				return false;
			}
		});

		btn_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(pwmBtnMotorSide, pwmBtnMotor);
					return true;
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
					return true;
				}

				return false;
			}
		});

		btn_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(pwmBtnMotor, pwmBtnMotorSide);
					return true;
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
					return true;
				}

				return false;
			}
		});

		btn_backward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(-pwmBtnMotorSide, -pwmBtnMotorSide);
					System.out.println("patate");
					return true;
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
					System.out.println("patate cuite");
					return true;
				}

				return false;
			}
		});


	}



	private OnClickListener btn_laser_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!((CheckBox)v).isChecked()){
				MainActivity.list.get(MainActivity.chosenone).disableLaserRobot();
			}
			else{
				MainActivity.list.get(MainActivity.chosenone).enableLaserRobot();
			}

		}
	};

	private OnClickListener btn_buzzer_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!((CheckBox)v).isChecked()){
				MainActivity.list.get(MainActivity.chosenone).disableBuzzerRobot();
			}
			else{
				MainActivity.list.get(MainActivity.chosenone).enableBuzzerRobot();
			}

		}
	};

//    private final Handler mHandler =  new Handler() {
//        public void handleMessage(android.os.Message msg) {
//        	switch (msg.what) {
//            case cBluetooth.BL_NOT_AVAILABLE:
//               	Log.d(cBluetooth.TAG, "Bluetooth is not available. Exit");
//            	Toast.makeText(getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
//                finish();
//                break;
//            case cBluetooth.BL_INCORRECT_ADDRESS:
//            	Log.d(cBluetooth.TAG, "Incorrect MAC address");
//            	Toast.makeText(getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
//                break;
//            case cBluetooth.BL_REQUEST_ENABLE:   
//            	Log.d(cBluetooth.TAG, "Request Bluetooth Enable");
//            	BluetoothAdapter.getDefaultAdapter();
//            	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, 1);
//                break;
//            case cBluetooth.BL_SOCKET_FAILED:
//            	Toast.makeText(getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
//                finish();
//                break;
//            }
//        };
//    };


	@Override
	protected void onResume() {
		super.onResume();


	}

	@Override
	protected void onPause() {
		super.onPause();
		MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
		MainActivity.list.get(MainActivity.chosenone).disableMotorDriver();
	}

}