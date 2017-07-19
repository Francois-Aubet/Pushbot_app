package de.tum.nst.pushbotcontrol;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import de.tum.nst.model.Pushbot;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/*
	Since this activity is called "main" i hope you come here at first to read it.
	I, francois aubet, wrote this code, if you have any questions feel free to ask : francois.aubet@gmail.com

	I made my best to comment the code in such way that it is understandable, I hope that this is enought.

	So, since you are now seeing this app for the first time, let me explain you the basic ideas of this app.

	There is one and only one MainActivity containing a list of bots. When you chose one you go in the activities for this bot.
	And you can only change the bot that you are using by going back to this activity.

	It looks a bit like that:

						+-----	BotActivity for pushbot 1 ...	 +---- ActivityVideo (to show dvs events)
						|										 |
						|										 |
		MainActivity ---+----- BotActivity for pushbot 2	-----+---- ActivityTouch
						|										 |
						|										 |
	 					+----- BotActivity for pushbot 3 ...	 +---- ActivityBotOption
																			.
																			.
																			.
																	(and so on, with all the other activities)


	One important object is the Pushbot class, it contains all the informations about one pushbot for the app side.
	It also has a RobotSocketManager object that is responsible for the whole communication with the bot.

	All the created puchbots are saved in the list contained in the MainActivity, many things go through this list.

	This is the plan of the app, i commented each activity so you should be able to anderstand while reading the code.

	Just a list of the activities here with there function:

	- MainActivity, as described, this is the activity from which you can chose which pushbot you want to use.

	- BotActivity is a list of all the possible interaction with a certain pushbot.

	- ActivityAccelerometre is a to command the bot using the accelerometer sensor
	- ActivityButtons is to command the bot with buttons
	- ActivityTouch is to command the bot using a circle on the screen

	- ActivityBotOptions is used to set up some options of the bot like activate the chain mode.

	- IPActivity is only called once, while creating a new puchbot object, to enter its ip address.
		(it could be an other option, to change the ip of a bot, but actualy they should be fixe)

	- TestActivity, I made tests in this activity. Right now it is empty but i let this here because you might also need it
		if you want to change things and you want to test them before.




	And a small look to the "model" part, there are actually mostly "commands class", this classes are classes that give
	a sting that can be send to the robot. Take a look at them it is quite clear once you see them.
	Then there is the Pushbot class, this  one contains all the infos about a pushbot.



	Last thing, i will not explain the java code it self, just how i organised it. There are good java courses on the internet
	you might need to look at input/output streams.


	PS: the number i give to the bots are not very well made, the first bot of the list hase "0" as number of the bot because
	 this is his place in the list of MainActivity. But this may lead to some confusions.
 */



// this is the first activity shown
// it allows to choose the pushbot with which we communicate

public class MainActivity extends Activity implements OnClickListener {
	Button btnNewPushbot, btnSelect, btndelete;
	public static List <Pushbot> list = new ArrayList<Pushbot>();		// this is the list of pushbots
	public static int chosenone = 0;			//this variable is used to see which robot is chosen in the list, the variable is used often
	public static int lastracked = 0;
	public static int[] trackingone = {-1,-1};	//this are the robots from which the tracking data are recorded

	public static int maxPowerMotor = 89;
	public static final int maxPowerMotorDefault = 89;
	public static double decayFactor = 7.8;
	public static final double decayFactorDefault = 7.8;

	public static int numberofDeleted = 0;

	private ListView botListshow = null;
	Pushbot.OnStatusChangedListerner listener;
	private String[] botnames;
	View myview;

	private boolean init = true;

	private int mSelectedItem;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);

	    TextView textv = (TextView) findViewById(R.id.textView1);
	    textv.setShadowLayer(1, 3, 3, Color.GRAY);

		botListshow = (ListView) findViewById(R.id.botlist);
	    
	    btnNewPushbot = (Button) findViewById(R.id.button_newpushbot);
	    btnNewPushbot.setOnClickListener(this);

		btnSelect = (Button) findViewById(R.id.button_selectpushbot);
		btnSelect.setOnClickListener(this);

		btndelete = (Button) findViewById(R.id.button_delpushbot);
		btndelete.setOnClickListener(this);


		//creation of 3 bots for show events: the first one is the leader, the tow other ones are tracking
		Pushbot newbot = new Pushbot(listener);
		newbot.setNumberofthebot(1);
		newbot.setHostIP("192.168.43.40");		//10.162.177.91
		newbot.nameofthebot = "Challenger";
		newbot.connect();
		try {
			Thread.sleep(60);
		} catch (Exception e) {}
		newbot.resetPushbot();
		newbot.setLeader(true);
		newbot.enableLEDRobotChain();
		MainActivity.list.add(newbot);
		//MainActivity.list.get(0).getCommandThread().enabled = true;

		Pushbot newbot2 = new Pushbot(listener);
		newbot2.setNumberofthebot(2);
		newbot2.setHostIP("192.168.43.41");		//10.162.177.99
		newbot2.nameofthebot = "Jessy";
		newbot2.connect();
		try {
			Thread.sleep(90);
		} catch (Exception e) {}
		newbot2.resetPushbot();
		newbot2.istracking = true;
		newbot2.enableRobotChain();
		newbot2.enableTracking();
		MainActivity.list.add(newbot2);
		MainActivity.trackingone[0] = 1;

		Pushbot newbot3 = new Pushbot(listener);
		newbot3.setNumberofthebot(3);
		newbot3.setHostIP("192.168.43.42");
		newbot3.nameofthebot = "David";
		newbot3.connect();
		try {
			Thread.sleep(80);
		} catch (Exception e) {}
		newbot3.resetPushbot();
		newbot3.istracking = true;
		newbot3.enableRobotChain();
		newbot3.enableTracking();
		MainActivity.list.add(newbot3);
		MainActivity.trackingone[1] = 2;

		/*Pushbot newbot4 = new Pushbot(listener);
		newbot4.setNumberofthebot(4);
		newbot4.setHostIP("10.162.177.99");
		MainActivity.list.add(newbot4);*/

		/*
			Most used american names:
				- james
				- Mary
				- John
				- Lisa
				- David
				- Laura
		*/

		//end of the adding of bots for show


		// showing the pushbot list
		/*botnames = new String[list.size()];

		for(int i = 0; i < list.size(); i++){
			int j = i + 1;
			botnames[i] = "Pushbot number " + j;
		}


		botListshow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, botnames));
		botListshow.setItemChecked(chosenone, true);*/


		list.get(0).enableLEDRobotChain();
		list.get(1).enableRobotChain();
		list.get(1).enableTracking();
		list.get(2).enableRobotChain();
		list.get(2).enableTracking();

		list.get(0).enableLEDRobotChain();
		list.get(1).enableRobotChain();
		list.get(1).enableTracking();
		list.get(2).enableRobotChain();
		list.get(2).enableTracking();


		botListshow.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adpterView, View view, int position,
									long id) {
				for (int i = 0; i < botListshow.getChildCount(); i++) {
					if(position == i ){
						botListshow.getChildAt(i).setBackgroundColor(0x7AC5CAE9);
					}else{
						botListshow.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
					}
				}
			}
		});
	}


	// allows to chose a pushbot and then opening the bot activity  knowing which one has been chosen (saved in the chosenone variable)
	// -1 means that this is a new bot
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_selectpushbot:
				if(list.size() > 0) {
					if(init) {
						init = false;
						/*list.get(0).enableLEDRobotChain();
						list.get(1).enableRobotChain();
						list.get(1).enableTracking();
						list.get(2).enableRobotChain();
						list.get(2).enableTracking();*/
					}
					chosenone = botListshow.getCheckedItemPosition();
					Intent intent1 = new Intent(this, BotActivity.class);
					startActivity(intent1);
				}
			break;

			case R.id.button_newpushbot:
	    	chosenone = -1;
	    	Intent intent2 = new Intent(this, BotActivity.class);
	    	startActivity(intent2);
	    	break;

			case R.id.button_delpushbot:
				if(list.size() > 0) {
					chosenone = botListshow.getCheckedItemPosition();
					try {
						list.get(chosenone).disconnect();
					} catch (Exception e){}
					list.remove(chosenone);
					if(trackingone[0] == chosenone){
						trackingone[0] = -1;
					} else if (trackingone[1] == chosenone){
						trackingone[1] = -1;
					} else if (trackingone[0] > chosenone){
						trackingone[0]--;
					}

					if(trackingone[1] > chosenone){
						trackingone[1]--;
					}
					numberofDeleted++;
					onResume();
				}
				break;

	    default:
	    	break;
	    }
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  
		Intent intent = new Intent();
		startActivityForResult(intent, 0); 
	  
		return true;
	}




// onResume must show the list again, furthermore it looks preselects a robot
// This time it also show if the robot is the leader or if it is tracking if its the case it shows it.
	@Override
	protected void onResume() {
		super.onResume();

		botListshow = (ListView) findViewById(R.id.botlist);

		botnames = new String[list.size()];

		// showing pushbot list
		int placeLeader = -1;
		for(int i = 0; i < list.size(); i++){
			int j = i + 1;

			botnames[i] = list.get(i).nameofthebot;

			if(list.get(i).isLeader()){
				botnames[i] += "    (leader)";
				placeLeader = i;
			}
			if(i == trackingone[0] || i == trackingone[1]){
				botnames[i] += "    (tracked)";
			}
		}

		botListshow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, botnames));
		if(placeLeader != -1){
			botListshow.setItemChecked(placeLeader, true);
			try {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						botListshow.getChildAt(0).setBackgroundColor(0x7AC5CAE9);
					}
				});
			} catch (Exception e) {}

			//botListshow.getChildAt(0).setBackgroundColor(0x7AC5CAE9);
			//botListshow.performItemClick(botListshow.findViewWithTag(botListshow.getAdapter().getItem(placeLeader)), placeLeader, botListshow.getAdapter().getItemId(placeLeader));
		} else{
			botListshow.setItemChecked(chosenone, true);
		}


	}

	@Override
	protected void onPause() {
		super.onPause();
	}



}
