package de.tum.nst.pushbotcontrol;

import android.content.res.Resources;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import de.tum.nst.connect.RobotSocketManager;
import de.tum.nst.model.DVSevent;
import de.tum.nst.model.Pushbot;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.content.Context;
import android.graphics.Color;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;


import de.tum.nst.pushbotcontrol.R;

import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/*this is actually the activity that is used the more often, it can schow the dvs events of the actual bot and rc control it.
 it also shows the two tracking images

 to understand the control part, you should look at the touchactivity, i explained it there

 for showing the events, i explain it at each thread.

*/


public class ActivityVideo extends Activity implements OnClickListener {
    ImageView miamageview;
    private Bitmap bmp;
    int values, val, c;
    int width, heigth;
    int factor = 5;
    Bitmap bitmap, bitmap2, bitmap3;

    int widthT = 128;

    private boolean resent = false;
    int comptResent = 0;
    int maxcomptResent = 15;
    int lastMotorCommandRight = 0, lastMotorCommandLeft = 0;

    private final static int BIG_CIRCLE_SIZE = 120;
    private final static int FINGER_CIRCLE_SIZE = 20;

    private int motorLeft = 0;
    private int motorRight = 0;

    float radiuscircle = 158;   //200; //old value

    double factorLessBehin = 0.5;
    double factorLessSide = 0.5;

    private boolean show_Debug;
    private boolean needtheshow = true;
    private int xRperc;
    private int pwmMax = MainActivity.maxPowerMotor;
    private String commandLeft;
    private String commandRight;
    int[] trackValues = new int[(128) * (128)];

    double decayFactor = MainActivity.decayFactor;

    ShowTheEvents theShow;
    ShowTheTracking tracking1 ,tracking2;

    Button btn_return;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        btn_return = (Button) findViewById(R.id.LightButton);
        btn_return.setOnClickListener(this);

        MyView v1 = new MyView(this);
        LinearLayout layout1 = (LinearLayout) findViewById(R.id.circle);
        layout1.addView(v1);


        Resources res = getResources();
        String name = "no robot tracked";
        try {
            name = MainActivity.list.get(MainActivity.trackingone[0]).nameofthebot;//res.getString(R.string.nameofthebot, (MainActivity.trackingone[0] + 1));
            name += "'s view:";
        } catch (Exception e){}
        TextView textv = (TextView) findViewById(R.id.nameofthebot1);
        textv.setText(name);

        name = "no robot tracked";
        //name = res.getString(R.string.nameofthebot, (MainActivity.trackingone[1] + 1));
        try {
            name = MainActivity.list.get(MainActivity.trackingone[1]).nameofthebot;//res.getString(R.string.nameofthebot, (MainActivity.trackingone[0] + 1));
            name += "'s view:";
        } catch (Exception e){}
        textv = (TextView) findViewById(R.id.nameofthebot2);
        textv.setText(name);

        MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
        MainActivity.list.get(MainActivity.chosenone).enableMotorDriver();

        xRperc = Integer.parseInt((String) getResources().getText(R.string.default_xRperc));

        commandLeft = (String) getResources().getText(R.string.default_commandLeft);
        commandRight = (String) getResources().getText(R.string.default_commandRight);

        MainActivity.list.get(MainActivity.chosenone).updateTimestampMode(0);
        MainActivity.list.get(MainActivity.chosenone).DVSstreamactive = true;
        MainActivity.list.get(MainActivity.chosenone).enableEvents();
        MainActivity.list.get(MainActivity.chosenone).showImage = true;

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.shield_slat);


        width = (128*factor);
        heigth = (128*factor);

        //define the array size
        MainActivity.list.get(MainActivity.chosenone).rgbValues = new int[width * heigth];


        for (int i = 0; i < width; i++) {
            for (int j = 0; j < heigth; j++) {
                MainActivity.list.get(MainActivity.chosenone).rgbValues[(j * width) + i] = Color.BLACK;
            }
        }




        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                miamageview = (ImageView) findViewById(R.id.matrix);

                bitmap = Bitmap.createBitmap(MainActivity.list.get(MainActivity.chosenone).rgbValues, width, heigth, Bitmap.Config.ARGB_8888);
                miamageview.setImageBitmap(bitmap);
            }
        });



        theShow = new ShowTheEvents(bitmap);
        theShow.start();        //start the show!

        //int[] trackValues = new int[(128 * 2) * (128 * 2)];

        for(int i = 0; i < (128) * (128); i++){
            trackValues[i] = 0xFFF0F0F0;
        }
        bitmap = Bitmap.createBitmap(widthT, widthT, Bitmap.Config.ARGB_8888);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                miamageview = (ImageView) findViewById(R.id.matrix2);
                bitmap.setPixels(trackValues, 0, widthT, 0 ,0, widthT, widthT);
                miamageview.setImageBitmap(bitmap);
            }
        });


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                miamageview = (ImageView) findViewById(R.id.matrix3);
                bitmap.setPixels(trackValues, 0, widthT, 0 ,0, widthT, widthT);
                miamageview.setImageBitmap(bitmap);
            }
        });


        if(MainActivity.trackingone[0] != -1) {
            tracking1 = new ShowTheTracking(bitmap2, 0);
            tracking1.start();
        }

        if(MainActivity.trackingone[1] != -1) {
            tracking2 = new ShowTheTracking(bitmap3, 1);
            tracking2.start();
        }

    }




    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LightButton:
                finish();
                onBackPressed();
                break;
        }


    }




//+++++++++++++++++++++++++++++++++++              Thread to show the DVS events           +++++++++++++++++++++++++++
/*
    This thread is basically a while loop that looks at the values of the event recieved by the robotsocketmanager.
    It shows this values and reset them to black after showing them.

    I made here again a tampon array to change the value there, it is better for performance.

 */
    private class ShowTheEvents extends Thread {
        Bitmap bitmap;
        ImageView miamageview = (ImageView) findViewById(R.id.matrix);
        int[] rgbValuesShow = new int[(128 * 5) * (128 * 5)];       // the tampon array of colors

        public ShowTheEvents(Bitmap bitmap) {
            this.bitmap = bitmap;
        }


        @Override
        public void run() {
            int xint, yint, tmp;
            miamageview = (ImageView) findViewById(R.id.matrix);
            bitmap = Bitmap.createBitmap(width, heigth, Bitmap.Config.ARGB_8888);
            int a = MainActivity.list.get(MainActivity.chosenone).negEventFromBot.size();
            int b = MainActivity.list.get(MainActivity.chosenone).posEventFromBot.size();
            int redc, greenc, bluec;

            if(a>width){a = width;}



            for (int i = 0; i < width; i++) {
                for (int j = 0; j < heigth; j++) {
                    rgbValuesShow[(j * width) + i] = Color.BLACK;
                }
            }



            while(needtheshow){
                try{
                    rgbValuesShow = MainActivity.list.get(MainActivity.chosenone).rgbValues;
                } catch(Exception e){}
                for(int i = 0; i < width - 1; i++){
                    for(int j = 0; j < width - 1; j++){

                        tmp = rgbValuesShow[(j * width) + i];

  //    this is used to slowly reduce the color of each event if the
                        if(tmp != Color.BLACK) {
                            redc = red(tmp);
                            redc = (int)( redc / decayFactor);
                            greenc = green(tmp);
                            greenc = (int)( greenc / decayFactor);
                            bluec = blue(tmp);
                            bluec = (int)( bluec / decayFactor);

                            tmp = argb(0xFF, redc, greenc, bluec);
                            rgbValuesShow[(j * width) + i] = tmp;
                        }
                       // if(tmp != Color.BLACK){
                         //   rgbValuesShow[(j * width) + i] = Color.BLACK;
                       /* switch (tmp){
                            case(Color.GREEN):{
                                //MainActivity.list.get(MainActivity.chosenone).rgbValues[(j * width) + i] = Color.BLACK;
                                rgbValuesShow[(j * width) + i] = Color.BLACK;
                                break;
                            }
                            case(Color.RED):{
                                //MainActivity.list.get(MainActivity.chosenone).rgbValues[(j * width) + i] = Color.BLACK;
                                rgbValuesShow[(j * width) + i] = Color.BLACK;
                                break;
                            }
                        }*/
                    }
                }


                try {
                    MainActivity.list.get(MainActivity.chosenone).rgbValues = rgbValuesShow;
                    Thread.sleep(1);
                } catch (Exception e) {}

                // showing the values we got
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // ? miamageview = (ImageView) findViewById(R.id.matrix);
                        try {
                        bitmap.setPixels(MainActivity.list.get(MainActivity.chosenone).rgbValues, 0, width, 0 ,0, width, heigth);
                        miamageview.setImageBitmap(bitmap);
                        } catch (Exception e) {}
                    }
                });



                //some motor commands:
                if(resent) {
                    MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(lastMotorCommandLeft, lastMotorCommandRight);
                    comptResent++;
                    if(comptResent == maxcomptResent){
                        resent = false;
                        comptResent = 0;
                    }
                }


            }

        }

    }




    //++++++++++++++++++++++++++++++            Thread to schow the tracking            +++++++++++++++++++++++++
    /*
    this is a class to show the tracking coordiante of the two robots that are sending use there tracking coordinate.
    i made just one class for this tow image matrix, that is why there are if statements.
     */

    private class ShowTheTracking extends Thread {
        Bitmap bitmap;
        ImageView miamageview = (ImageView) findViewById(R.id.matrix2);
        int[] rgbValuesShow = new int[(128 * 2) * (128 * 2)];
        int tracked, widthT = 128 * 2, tracking;

        public ShowTheTracking(Bitmap bitmap, int tracked) {
            this.tracked = tracked;
            this.bitmap = bitmap;
            this.tracking = MainActivity.trackingone[tracked];
            System.out.println("tracking : " + tracking);
        }


        @Override
        public void run() {
            int xint, yint, tmp;


            bitmap = Bitmap.createBitmap(widthT, widthT, Bitmap.Config.ARGB_8888);

            if(tracked == 0) {
                miamageview = (ImageView) findViewById(R.id.matrix2);
            } else {
                miamageview = (ImageView) findViewById(R.id.matrix3);
            }




            while(needtheshow){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bitmap.setPixels(MainActivity.list.get(tracking).trackValues, 0, widthT, 0 ,0, widthT, widthT);
                        miamageview.setImageBitmap(bitmap);
                    }
                });


                try {
                    Thread.sleep(100);
                } catch (Exception e) {}
            }

        }

    }







    //++++++++++++++++++++++            to handle the touche control            ++++++++++++++++++++++++++

    class MyView extends View {

        Paint fingerPaint, borderPaint, textPaint;

        int dispWidth;
        int dispHeight;

        float x;
        float y;

        float xcirc;
        float ycirc;

        String directionL = "";
        String directionR = "";
        String cmdSend;
        String temptxtMotor;


        boolean drag = false;
        float dragX = 0;
        float dragY = 0;


        Button button = new Button(getContext());



        public MyView(Context context) {
            super(context);
            fingerPaint = new Paint();
            fingerPaint.setAntiAlias(true);
            fingerPaint.setColor(Color.RED);

            borderPaint = new Paint();
            borderPaint.setColor(Color.BLUE);
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(3);

            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(14);

        }


        protected void onDraw(Canvas canvas) {
            dispWidth = (int) Math.round(6*(this.getRight()-this.getLeft())/10);
            dispHeight = (int) Math.round((this.getBottom()-this.getTop())/2);
            if(!drag){
                x = dispWidth;
                y = dispHeight;
                fingerPaint.setColor(Color.RED);
            }

            canvas.drawCircle(x, y, FINGER_CIRCLE_SIZE, fingerPaint);
            canvas.drawCircle(dispWidth, dispHeight, radiuscircle, borderPaint);

        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {


            float evX = event.getX();
            float evY = event.getY();

            xcirc = event.getX() - dispWidth;
            ycirc = event.getY() - dispHeight;

            float radius = (float) Math.sqrt(Math.pow(Math.abs(xcirc),2)+Math.pow(Math.abs(ycirc),2));

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    if(radius >= 0 && radius <= radiuscircle){
                        x = evX;
                        y = evY;
                        fingerPaint.setColor(Color.GREEN);
                        CalcMotor(xcirc,ycirc);
                        invalidate();
                        drag = true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (drag && radius >= 0 && radius <= radiuscircle) {
                        x = evX;
                        y = evY;
                        fingerPaint.setColor(Color.GREEN);
                        CalcMotor(xcirc,ycirc);
                        invalidate();
                    } else if(drag && radius > radiuscircle){

                    double angle = Math.acos(xcirc/Math.sqrt((xcirc)*(xcirc) + (ycirc)*(ycirc)));

                    if(evY > dispHeight){
                        x = (float) Math.cos(angle) * radiuscircle + dispWidth;
                        y = (float) Math.sin(angle) * radiuscircle + dispHeight;
                    } else {
                        x = (float) Math.cos(angle) * radiuscircle + dispWidth;
                        y = -(float) Math.sin(angle) * radiuscircle + dispHeight;
                    }

                    xcirc = x - dispWidth - 5;
                    ycirc = y - dispHeight - 5;


                    fingerPaint.setColor(Color.GREEN);
                    CalcMotor(xcirc,ycirc);
                    invalidate();
                }
            break;


            case MotionEvent.ACTION_UP:
                resent = true;
                xcirc = 0;
                ycirc = 0;
                drag = false;
                CalcMotor(xcirc,ycirc);
                invalidate();
                break;
            }
            return true;
        }
    }

    private void CalcMotor(float calc_x, float calc_y) {

        calc_x = -calc_x;


        int xAxis = -Math.round(calc_x * pwmMax / radiuscircle);
        int yAxis = -Math.round(calc_y * pwmMax / radiuscircle);

        //the dead zone could be done better
        if (Math.abs(xAxis) < 0.2 * pwmMax) {
            xAxis = 0;
        }
        if (Math.abs(yAxis) < 0.2 * pwmMax) {
            yAxis = 0;
        }

        int radius = (int) Math.sqrt(Math.pow(Math.abs(xAxis), 2) + Math.pow(Math.abs(yAxis), 2));


        System.out.println("x  :" + xAxis);
        System.out.println("y  :" + yAxis);

        //System.out.println("radius  :" +radius);

        double angle = Math.acos(xAxis / Math.sqrt((xAxis) * (xAxis) + (yAxis) * (yAxis)));


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


        if (yAxis < 0) {
            motorLeft *= factorLessBehin;
            motorRight *= factorLessBehin;
        }

        motorLeft = (int) (motorLeft * (Math.abs(Math.sin(angle)) * 2 + 1)) / 3;
        motorRight = (int) (motorRight * (Math.abs(Math.sin(angle)) * 2 + 1)) / 3;
        //int parametre = ;

        if (xAxis < 0 && radius > 0.9 * pwmMax) {
            motorLeft = (int) (motorLeft * (Math.abs(Math.sin(angle)) + 2)) / 3;
        } else if (radius > 0.9 * pwmMax) {
            motorRight = (int) (motorRight * (Math.abs(Math.sin(angle)) + 2)) / 3;
        }


        // be sure the the speed is lower than the miximum speed, is not really usefull
		/*
		System.out.println("left (a)  :" + motorLeft);
		System.out.println("right (a) :" + motorRight);

        if(motorLeft > pwmMax) motorLeft = pwmMax;
        if(motorLeft < -pwmMax * factorLessBehin) motorLeft = (int)(-pwmMax * factorLessBehin);
        if(motorRight > pwmMax) motorRight = pwmMax;
        if(motorRight < -pwmMax * factorLessBehin) motorRight = (int)(-pwmMax * factorLessBehin);
		*/


        System.out.println("left  :" + motorLeft);
        System.out.println("right :" + motorRight);

        MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(motorLeft, motorRight);

    }











    @Override
    protected void onResume() {
        super.onResume();

        try{
            MainActivity.list.get(MainActivity.chosenone).enableEvents();
            MainActivity.list.get(MainActivity.chosenone).showImage = true;
        } catch (Exception e){

        }
    }

    @Override
    protected void onPause() {
            super.onPause();

        needtheshow = false;
        try{
            theShow.join();
        } catch(Exception e) {
            theShow.interrupt();
        }

        if(MainActivity.trackingone[0] != -1) {
            try{
                tracking1.join();
            } catch(Exception e) {
                tracking1.interrupt();
            }

        }

        if(MainActivity.trackingone[1] != -1) {
            try{
                tracking2.join();
            } catch(Exception e) {
                tracking2.interrupt();
            }
        }

        try{
            if(MainActivity.list.get(MainActivity.chosenone).getCommandThread().connected) {
                MainActivity.list.get(MainActivity.chosenone).disableEvents();
                MainActivity.list.get(MainActivity.chosenone).DVSstreamactive = false;
                MainActivity.list.get(MainActivity.chosenone).showImage = false;
            }
        } catch (Exception e){}

    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }
}


