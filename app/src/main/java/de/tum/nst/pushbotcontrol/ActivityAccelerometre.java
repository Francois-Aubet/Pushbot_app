package de.tum.nst.pushbotcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;


public class ActivityAccelerometre extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private ToggleButton LightButton;

    private Button btn_return;

    private int xAxis = 0;
    private int yAxis = 0;
    private int motorLeft = 0;
    private int motorRight = 0;
    private String address;
    private boolean show_Debug;
    private int xMax;
    private int yMax;
    private int yThreshold;
    private int pwmMax;
    private int pwmyMax;
    private int statex, statey;
    boolean stated, isclicked = false;

    float[] values = new float[3];
    float[] RR = new float[9];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);


        xMax = Integer.parseInt((String) getResources().getText(R.string.default_xMax));

        yMax = Integer.parseInt((String) getResources().getText(R.string.default_yMax));
        yThreshold = Integer.parseInt((String) getResources().getText(R.string.default_yThreshold));
        pwmMax = (int)(2 * MainActivity.maxPowerMotor);
        pwmyMax = pwmMax + 70;

        //loadPref();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        btn_return = (Button)  findViewById(R.id.backacc);

        LightButton = (ToggleButton) findViewById(R.id.LightButton);

        btn_return.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                onBackPressed();

                return false;
            }
        });

        LightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(LightButton.isChecked()){
                   // bl.sendData(String.valueOf(commandHorn+"1\r"));
                    MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
                    MainActivity.list.get(MainActivity.chosenone).enableMotorDriver();

                    /*SensorManager.getRotationMatrix(R, null, accelerometreValues, magnetometreValues);
                    SensorManager.getOrientation(RR, values);
                    statex = (int) (values[0]*pwmMax/20);
                    statey = (int) (values[1]*pwmMax/20);
                    System.out.print("statex =" + statex);
                    System.out.print("statey =" + statey);*/
                    stated = false;
                    isclicked = true;
                }else{
                   // bl.sendData(String.valueOf(commandHorn+"0\r"));
                    MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
                    MainActivity.list.get(MainActivity.chosenone).disableMotorDriver();
                    isclicked = false;
                }
            }
        });
    }

  /*  private final Handler mHandler =  new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case cBluetooth.BL_NOT_AVAILABLE:
                    Log.d(cBluetooth.TAG, "Bluetooth is not available. Exit");
                    Toast.makeText(getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case cBluetooth.BL_INCORRECT_ADDRESS:
                    Log.d(cBluetooth.TAG, "Incorrect MAC address");
                    Toast.makeText(getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
                    break;
                case cBluetooth.BL_REQUEST_ENABLE:
                    Log.d(cBluetooth.TAG, "Request Bluetooth Enable");
                    BluetoothAdapter.getDefaultAdapter();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                    break;
                case cBluetooth.BL_SOCKET_FAILED:
                    Toast.makeText(getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        };
    };*/

    public void onSensorChanged(SensorEvent e) {
        String directionL = "";
        String directionR = "";
        String cmdSend;

        if(!stated){
            statex = Math.round((e.values[0])*pwmMax/20);
            statey = Math.round((e.values[1])*pwmMax/20);
            stated = true;
            try {
                Thread.sleep(50);
            } catch (Exception exept) {}
        }

        if(isclicked) {
            xAxis = Math.round((e.values[0]) * pwmMax / 20);
            yAxis = Math.round((e.values[1]) * pwmMax / 20);

            //System.out.println("x  :" +xAxis);
            //System.out.println("y  :" +yAxis);

            xAxis = xAxis - statex;
            yAxis = yAxis - statey;

            if (xAxis > pwmMax) xAxis = pwmMax;
            else if (xAxis < -pwmMax) xAxis = -pwmMax;

            if (yAxis > pwmyMax) yAxis = pwmyMax;
            else if (yAxis < -pwmyMax) yAxis = -pwmyMax;

            //else if(yAxis >= 0 && yAxis < yThreshold) yAxis = 0;
            //else if(yAxis < 0 && yAxis > -yThreshold) yAxis = 0;

            int radius = (int) Math.sqrt(Math.pow(Math.abs(xAxis), 2) + Math.pow(Math.abs(yAxis), 2));

                //try to adjust the behaviour :
            /*int adjust = 100;
            radius = (int) Math.pow(2, radius);
            radius = radius / adjust;  //100 is a gess, tbc

            int xR = Math.round(200*60/100);

            xAxis = (int) Math.pow(2, xAxis);
            xAxis = xAxis / adjust;

            yAxis = (int) Math.pow(2, yAxis);
            yAxis = yAxis / adjust;*/

            if (yAxis < 2 && yAxis > -2) {
                yAxis = 0;
            }
            if (xAxis < 2 && xAxis > -2) {
                xAxis = 0;
            }

            System.out.println("x  :" + xAxis);
            System.out.println("y  :" + yAxis);

            xAxis = -xAxis;
            //yAxis = -yAxis;

            if (xAxis > 0) {
                if (yAxis > 0) {
                    motorLeft = radius;
                } else {
                    motorLeft = -radius;
                }

                motorRight = yAxis;
            } else if (xAxis < 0) {
                motorLeft = yAxis;

                if (yAxis > 0) {
                    motorRight = radius;
                } else {
                    motorRight = -radius;
                }

            } else if (xAxis == 0) {
                motorLeft = yAxis;
                motorRight = yAxis;
            }

            /*
            if(xAxis > 0) {
                motorRight = yAxis;
                if(Math.abs(Math.round(e.values[0])) > xR){
                    motorLeft = Math.round((e.values[0]-xR)*pwmMax/(xMax-xR));
                    motorLeft = Math.round(-motorLeft * yAxis/pwmMax);
                    //if(motorLeft < -pwmMax) motorLeft = -pwmMax;
                }
                else motorLeft = yAxis - yAxis*xAxis/pwmMax;
            }
            else if(xAxis < 0) {
                motorLeft = yAxis;
                if(Math.abs(Math.round(e.values[0])) > xR){
                    motorRight = Math.round((Math.abs(e.values[0])-xR)*pwmMax/(xMax-xR));
                    motorRight = Math.round(-motorRight * yAxis/pwmMax);
                    //if(motorRight > -pwmMax) motorRight = -pwmMax;
                }
                else motorRight = yAxis - yAxis*Math.abs(xAxis)/pwmMax;
            }
            else if(xAxis == 0) {
                motorLeft = yAxis;
                motorRight = yAxis;
            }

            if(motorLeft > 0) {
                directionL = "-";
            }
            if(motorRight > 0) {
                directionR = "-";
            }
    */

            if (motorLeft > pwmyMax) motorLeft = pwmyMax;
            if (motorLeft < -pwmyMax) motorLeft = -pwmyMax;
            if (motorRight > pwmyMax) motorRight = pwmyMax;
            if (motorRight < -pwmyMax) motorRight = -pwmyMax;

            //cmdSend = String.valueOf(commandLeft+directionL+motorLeft+"\r"+commandRight+directionR+motorRight+"\r");

            motorRight = -motorRight;
            motorLeft = -motorLeft;

            MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(motorLeft, motorRight);

            try {
                Thread.sleep(10);
            } catch (Exception exp) {
            }

           /* TextView textX = (TextView) findViewById(R.id.textViewX);
            TextView textY = (TextView) findViewById(R.id.textViewY);
            TextView mLeft = (TextView) findViewById(R.id.mLeft);
            TextView mRight = (TextView) findViewById(R.id.mRight);
            TextView textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);

            if(show_Debug){
                textX.setText(String.valueOf("X:" + String.format("%.1f",e.values[0]) + "; xPWM:"+xAxis));
                textY.setText(String.valueOf("Y:" + String.format("%.1f",e.values[1]) + "; yPWM:"+yAxis));
                mLeft.setText(String.valueOf("MotorL:" + directionL + "." + motorLeft));
                mRight.setText(String.valueOf("MotorR:" + directionR + "." + motorRight));
                textCmdSend.setText(String.valueOf("Send:" + cmdSend.toUpperCase()));
            }
            else{
                textX.setText("");
                textY.setText("");
                mLeft.setText("");
                mRight.setText("");
                textCmdSend.setText("");
            }*/

        }
    }


   /* private void loadPref(){
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        address = mySharedPreferences.getString("pref_MAC_address", address);			// Ïåðâûé ðàç çàãðóæàåì äåôîëòíîå çíà÷åíèå
        xMax = Integer.parseInt(mySharedPreferences.getString("pref_xMax", String.valueOf(xMax)));
        xR = Integer.parseInt(mySharedPreferences.getString("pref_xR", String.valueOf(xR)));
        yMax = Integer.parseInt(mySharedPreferences.getString("pref_yMax", String.valueOf(yMax)));
        yThreshold = Integer.parseInt(mySharedPreferences.getString("pref_yThreshold", String.valueOf(yThreshold)));
        pwmMax = Integer.parseInt(mySharedPreferences.getString("pref_pwmMax", String.valueOf(pwmMax)));
        show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
        commandLeft = mySharedPreferences.getString("pref_commandLeft", commandLeft);
        commandRight = mySharedPreferences.getString("pref_commandRight", commandRight);
        commandHorn = mySharedPreferences.getString("pref_commandHorn", commandHorn);
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
        MainActivity.list.get(MainActivity.chosenone).disableMotorDriver();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // loadPref();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }
}
