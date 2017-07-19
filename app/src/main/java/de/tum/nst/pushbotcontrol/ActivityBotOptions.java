package de.tum.nst.pushbotcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/*
    This is an activity to chose features for a bot, the 3 actual ones are:
        - If the bot activates the led to be tracked by the others.
        - If the bot is in chain mode.
        - If the bot sends use the tacking points.

    If you have other of such options to chose I would suggest you to put them here.
 */

public class ActivityBotOptions extends Activity {

    CheckBox btnrobotchain, btnledrobotchain, btnrobottracking;
    Button btnsetdefault;
    EditText sensibility = null;
    EditText dvsdecay = null;

    private Button btn_return;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_botoptions);

        btnrobotchain = (CheckBox)findViewById(R.id.robotchain);
        if(MainActivity.list.get(MainActivity.chosenone).isFollower()) {
            btnrobotchain.setChecked(true);
        }

        btnledrobotchain = (CheckBox)findViewById(R.id.robotchainled);
        if(MainActivity.list.get(MainActivity.chosenone).isLeader()) {
            btnledrobotchain.setChecked(true);
        }

        btnrobottracking = (CheckBox)findViewById(R.id.robottracking);
        if(MainActivity.list.get(MainActivity.chosenone).istracking) {
            btnrobottracking.setChecked(true);
        }



        btnrobotchain.setOnClickListener(checkedListener1);
        btnledrobotchain.setOnClickListener(checkedListener2);
        btnrobottracking.setOnClickListener(checkedListener3);


        int tmp =  MainActivity.maxPowerMotor;
        tmp = (int)(tmp/1.1);
        sensibility = (EditText)findViewById(R.id.editSensibility);
        sensibility.setText("" + tmp);


        tmp = (int)(MainActivity.decayFactor * 9);
        dvsdecay = (EditText)findViewById(R.id.editDecayDVS);
        dvsdecay.setText("" + tmp);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        btnsetdefault = (Button) findViewById(R.id.btnsetdefault);
        btnsetdefault.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int tmp = MainActivity.maxPowerMotorDefault;
                tmp = (int)(tmp/1.1);
                sensibility.setText("" + tmp);

                tmp = (int)(MainActivity.decayFactorDefault * 9);
                dvsdecay.setText("" + tmp);
                return false;
            }
        });

        btn_return = (Button)  findViewById(R.id.backopt);
        btn_return.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int tmp;
                tmp = Integer.parseInt(sensibility.getText().toString());
                if(tmp < 101 && tmp > -1){
                    MainActivity.maxPowerMotor = (int)(tmp * 1.1);
                }

                tmp = Integer.parseInt(dvsdecay.getText().toString());
                if(tmp < 12){ tmp = 12;}
                if(tmp < 101 && tmp > -1){
                    MainActivity.decayFactor = (tmp / 9.0);
                }


                /*try{
                    MainActivity.decayFactor = Double.parseDouble(dvsdecay.getText().toString());
                } catch (Exception e){
                    // could implement a warning here because of the wrong input
                }*/



                onBackPressed();

                return false;
            }
        });
    }


// to set if the robot is in chain mode or not
    private OnClickListener checkedListener1 = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!((CheckBox)v).isChecked()){
                MainActivity.list.get(MainActivity.chosenone).desableRobotChain();
                MainActivity.list.get(MainActivity.chosenone).setFollower(false);
            }
            else{
                MainActivity.list.get(MainActivity.chosenone).enableRobotChain();
                MainActivity.list.get(MainActivity.chosenone).setFollower(true);
                //MainActivity.list.get(MainActivity.chosenone).askoptions();
            }

        }
    };

    //set if the robot activates it led for beeing tracked.
    private OnClickListener checkedListener2 = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!((CheckBox)v).isChecked()){
                MainActivity.list.get(MainActivity.chosenone).desableLEDRobotChain();
                MainActivity.list.get(MainActivity.chosenone).setLeader(false);
            }
            else{
                MainActivity.list.get(MainActivity.chosenone).enableLEDRobotChain();
                MainActivity.list.get(MainActivity.chosenone).setLeader(true);
                //onBackPressed();
            }

        }
    };

    private OnClickListener checkedListener3 = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!((CheckBox)v).isChecked()){
                if(MainActivity.trackingone[0] == MainActivity.chosenone){
                    MainActivity.trackingone[0] = -1;
                } else if(MainActivity.trackingone[1] == MainActivity.chosenone){
                    MainActivity.trackingone[1] = -1;
                }
                MainActivity.list.get(MainActivity.chosenone).disableTracking();
                MainActivity.list.get(MainActivity.chosenone).istracking = false;
            }
            else{
                MainActivity.list.get(MainActivity.chosenone).enableTracking();
                MainActivity.list.get(MainActivity.chosenone).istracking = true;
                if(MainActivity.lastracked == 0){MainActivity.lastracked = 1;}
                else {MainActivity.lastracked = 0;}
                MainActivity.trackingone[MainActivity.lastracked] = MainActivity.chosenone;
                //onBackPressed();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
