package de.tum.nst.pushbotcontrol;

import android.content.res.Resources;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.tum.nst.model.Pushbot;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.app.Instrumentation;

/*
	This class is here to chose what we want to do with the bot.

 */


public class BotActivity extends Activity implements OnClickListener {
	ListView vue;
	View vuepourboutton;
	Pushbot.OnStatusChangedListerner listener;
	static Button btnActAccelerometer, btnActButtons, btnActTouch, btnBotOptions, btnLedOn, btn_return;
	static ToggleButton btnConnect;
	boolean firsttimehere = false;
	boolean tosee = false;
	boolean manual = false;
	boolean needed = true;
	private long startTime;
	static ConnectedFeedback theFeedback;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bot);
		vuepourboutton = findViewById(R.id.button_connect);

		int number = MainActivity.chosenone;
//	    see if the pushbot is new. If so adds it to the list of pushbots
		if (MainActivity.chosenone == -1) {
			firsttimehere = true;
			number = MainActivity.list.size();
			number++;
			Pushbot newbot = new Pushbot(listener);
			newbot.setNumberofthebot(number);
			MainActivity.list.add(newbot);
			number--;
			MainActivity.chosenone = number;

			Intent intent = new Intent(this, IPActivity.class);
			startActivity(intent);
		}



		btnActAccelerometer = (Button) findViewById(R.id.button_accel);
		btnActAccelerometer.setOnClickListener(this);

		btnActButtons = (Button) findViewById(R.id.button_buttons);
		btnActButtons.setOnClickListener(this);

		btnActTouch = (Button) findViewById(R.id.button_touch);
		btnActTouch.setOnClickListener(this);

		btnLedOn = (Button) findViewById(R.id.button_led_on);
		btnLedOn.setOnClickListener(this);

		btnBotOptions = (Button) findViewById(R.id.button_botoptions);
		btnBotOptions.setOnClickListener(this);

		btn_return = (Button) findViewById(R.id.backbot);

		btn_return.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				onBackPressed();

				return false;
			}
		});


		/*
		Solution ideas:
			-use time stamp so that it is not possible to click again 1 second after disactivating

			-change accessibility to private?		!

			-use an other listener (onclick?) for the purpose of reconnecting
			-check without manual, might be the reason
		 */

		btnConnect = (ToggleButton) findViewById(R.id.button_connect);

	/*	btnConnect.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				//btnConnect.setChecked(false);

				if (manual) {	//!btnConnect.isChecked()
					//System.out.print("clc\nc"+"\n \n \n\n\n\n\n\n"+"\nc\nfclse\nc\nc+\"\n \n \n \n \n\n\n\n"+"\nclc\n");
					// The toggle is enabled
					//if((System.currentTimeMillis() - startTime) > 200.0) {
						MainActivity.list.get(MainActivity.chosenone).connect();



					//if (firsttimehere) {
						MainActivity.list.get(MainActivity.chosenone).resetPushbot();
						firsttimehere = false;
					//}
					//}
					manual = false;



				} else {
					manual = true;
					//System.out.print("dld\nd\nd\nfdlse\nd\nd\ndld\n");
					// The toggle is disabled
					startTime = System.currentTimeMillis();
					MainActivity.list.get(MainActivity.chosenone).disconnect();
					//needed = false;
					//theFeedback.interrupt();
					try {
						//Thread.sleep(900);
					} catch (Exception e) {}


				}


				//onResume();

				return false;
			}});*/



		btnConnect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (manual) {
					// The toggle is enabled
						manual = false;
						MainActivity.list.get(MainActivity.chosenone).connect();

						MainActivity.list.get(MainActivity.chosenone).resetPushbot();


				} else {
					// The toggle is disabled
					manual = true;
					MainActivity.list.get(MainActivity.chosenone).disconnect();

					//startTime = System.currentTimeMillis();
				}
			}});


		// the connect button and its feedback, to see if the bot is connected or not
	/*	btnConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!manual){
					manual = true;
					System.out.print("blb\nb\nb\nfblse\nb\nb\nblb\n");
					return;
				}

				/*if (tosee) {
					btnConnect.setChecked(true);
					tosee = false;
					return;
				}*/
		/*

				if (isChecked) {
					System.out.print("clc\nc\nc\nfclse\nc\nc\nclc\n");
					// The toggle is enabled
					MainActivity.list.get(MainActivity.chosenone).connect();

					if (firsttimehere) {
						MainActivity.list.get(MainActivity.chosenone).resetPushbot();
						firsttimehere = false;
					}

					boolean a = false; //= false;
					try {
						Thread.sleep(500);
						a = MainActivity.list.get(MainActivity.chosenone).getCommandThread().connected;
					} catch (Exception e) {
					}

					if (a) {
						btnConnect.setChecked(true);


					} else {
						btnConnect.setChecked(false);
					}
				} else {
					System.out.print("dld\nd\nd\nfdlse\nd\nd\ndld\n");
					// The toggle is disabled
					//btnConnect.setChecked(false);
					//theFeedback.interrupt();
					MainActivity.list.get(MainActivity.chosenone).disconnect();


				}
			}
		});*/



		theFeedback = new ConnectedFeedback();
		theFeedback.start();
	}



	// when a button is clicked it starts the corresponding activity.
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_led_on:
				Intent intent_video = new Intent(this, ActivityVideo.class);
				startActivity(intent_video);

				break;
			case R.id.button_accel:
				Intent intent_accel = new Intent(this, ActivityAccelerometre.class);
				startActivity(intent_accel);
				break;
			case R.id.button_buttons:
				Intent intent_buttons = new Intent(this, ActivityButtons.class);
				startActivity(intent_buttons);
				break;
			case R.id.button_touch:
				Intent intent_touch = new Intent(this, ActivityTouch.class);
				startActivity(intent_touch);
				break;
			case R.id.button_botoptions:
				Intent intent_botoptions = new Intent(this, ActivityBotOptions.class);
				startActivity(intent_botoptions);

				break;
			default:
				break;
		}
	}





	// on resume has to show if the bot is connected or not
	@Override
	protected void onResume() {
		super.onResume();
		needed = true;

		String name;
		name = MainActivity.list.get(MainActivity.chosenone).nameofthebot;
		TextView textv = (TextView) findViewById(R.id.nameofthebot);
		textv.setText(name);
		textv.setShadowLayer(1, 3, 3, Color.GRAY);

		boolean a = false;


			try {
				a = MainActivity.list.get(MainActivity.chosenone).connectedb;
				//System.out.println("\n6\n6\n6\n");

				System.out.print(a);

				//System.out.println("\n6\n6\n6\n");

				if (a) {
					manual = false;

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//manual = false;
							btnConnect.setChecked(true);
							//btnConnect.setSelected(true);

							//System.out.print("bla\na\na\ntrue\na\na\nbla\n");
						}
					});
					//MainActivity.list.get(MainActivity.chosenone).connectionRecovered();
				} else {
					manual = true;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//manual = true;
							btnConnect.setChecked(false);
							//btnConnect.setSelected(false);

							//System.out.print("bla\na\na\nfalse\na\na\nbla\n");
						}
					});

					//btnConnect.setChecked(false);
					//btnConnect.setSelected(false);
				}
			} catch (Exception e) {
				//System.out.println("\n6\n6\n6\n");
			}


	}



	@Override
	protected void onPause() {
		//needed = false;
		//theFeedback.interrupt();
		super.onPause();
	}


	@Override
	protected void onStop(){
		super.onStop();
	}

	@Override
	protected void onDestroy(){
		needed = false;
		//theFeedback.interrupt();
		try {
			theFeedback.join();
		}catch (Exception ex){theFeedback.interrupt();}
		super.onDestroy();
	}


	//+++++++++++++++++++++			to show the connection feedback			+++++++++++++++++++++++
	private class ConnectedFeedback extends Thread {
		int i = 0;
		Instrumentation inst = new Instrumentation();


		public ConnectedFeedback() {
		}


		@Override
		public void run() {
			boolean a = false;

			while (needed) {
				//onResume();

				try {
					a = MainActivity.list.get(MainActivity.chosenone).connectedb;
					//System.out.print(a + "\n");
					if (a) {
						manual = false;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								btnConnect.setChecked(true);
								//btnConnect.setSelected(true);

							}
						});
					} else {
						//tosee = false;
						manual = true;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								btnConnect.setChecked(false);
								//btnConnect.setSelected(false);
							}
						});
					}
				} catch (Exception e) {}



				try {
					Thread.sleep(390);
				} catch (Exception e) {
				}

			}

		}

	}

}