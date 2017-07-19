package de.tum.nst.pushbotcontrol;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.res.Resources;
import 	android.widget.LinearLayout;

public class ActivityTouch extends Activity implements OnClickListener {
	
//    private cBluetooth bl = null;
	
	private final static int BIG_CIRCLE_SIZE = 120;
	private final static int FINGER_CIRCLE_SIZE = 20;
	
    private int motorLeft = 0;
    private int motorRight = 0;
    
    float radiuscircle = 160;   //200; (old value)

	double factorLessBehin = 0.7;
	double factorLessSide = 0.5;
    
    private boolean show_Debug;
    private int xRperc;
    private int pwmMax = MainActivity.maxPowerMotor;
    private String commandLeft;
    private String commandRight;
    private String commandHorn;

	Button btn_return;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_touch);
		btn_return = (Button) findViewById(R.id.LightButton);
		btn_return.setOnClickListener(this);		//dont work because of canva


        MyView v1 = new MyView(this);
        //setContentView(new MyView(this));
        //setContentView(v1);

		LinearLayout layout1 = (LinearLayout) findViewById(R.id.circle);
		layout1.addView(v1);

		MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(0, 0);
		MainActivity.list.get(MainActivity.chosenone).enableMotorDriver();

        xRperc = Integer.parseInt((String) getResources().getText(R.string.default_xRperc));

        commandLeft = (String) getResources().getText(R.string.default_commandLeft);
        commandRight = (String) getResources().getText(R.string.default_commandRight);
        commandHorn = (String) getResources().getText(R.string.default_commandHorn);




		btn_return.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				onBackPressed();

				return false;
			}
		});


	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.LightButton:
				onBackPressed();
				break;
		}


	}

	class MyView extends View {

		Paint fingerPaint, borderPaint, textPaint;

        int dispWidth;
        int dispHeight;
        
        float x;
        float y;
        
        float xcirc;
        float ycirc;

        boolean drag = false;

		public MyView(Context context) {
        	super(context);
        	fingerPaint = new Paint();
        	fingerPaint.setAntiAlias(true);
        	fingerPaint.setColor(Color.RED);
                
        	borderPaint = new Paint();
        	borderPaint.setColor(Color.BLUE);
        	borderPaint.setAntiAlias(true);
        	borderPaint.setStyle(Style.STROKE);
        	borderPaint.setStrokeWidth(3);
        	
	        textPaint = new Paint(); 
	        textPaint.setColor(Color.WHITE); 
	        textPaint.setStyle(Style.FILL); 
	        textPaint.setColor(Color.BLACK); 
	        textPaint.setTextSize(14);
        }


        protected void onDraw(Canvas canvas) {
        	dispWidth = (int) Math.round(6*(this.getRight()-this.getLeft())/7);
        	dispHeight = (int) Math.round((this.getBottom()-this.getTop())/2.5);
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


	private void CalcMotor(float calc_x, float calc_y){
    	
    	calc_x = -calc_x;


		int xAxis = -Math.round(calc_x*pwmMax/radiuscircle);
		int yAxis = -Math.round(calc_y*pwmMax/radiuscircle);

		//the dead zone could be done better
		if(Math.abs(xAxis) < 0.2 * pwmMax){ xAxis = 0;}
		//else if(xAxis > 0){ xAxis = (int)(xAxis*(1.25) - 0.25);}
		//else {xAxis = (int)(xAxis*(1.25) + 0.25);}

		if(Math.abs(yAxis) < 0.2 * pwmMax){ yAxis = 0;}
		//else if(yAxis > 0){ yAxis = (int)(yAxis*(1.25) - 0.25);}
		//else {yAxis = (int)(yAxis*(1.25) + 0.25);}


    	int radius = (int) Math.sqrt(Math.pow(Math.abs(xAxis),2)+Math.pow(Math.abs(yAxis),2));

		//int xR = Math.round(radiuscircle*xRperc/100);

		
		System.out.println("x  :" +xAxis);
		System.out.println("y  :" +yAxis);

		//System.out.println("radius  :" +radius);

		double angle = Math.acos(xAxis/Math.sqrt((xAxis)*(xAxis) + (yAxis)*(yAxis)));

		/*xAxis = (int) (((xAxis/pwmMax * 0.8 * pwmMax) + pwmMax * 0.2));
		yAxis = (int) (((yAxis/pwmMax * 0.8 * pwmMax) + pwmMax * 0.2));
		radius = (int) (((radius/pwmMax * 0.8 * pwmMax) + pwmMax * 0.2));

		/*if(xAxis > 0){
			if(yAxis > 0){	motorLeft = (int)(radius * (1 - ((xAxis * factorLessSide) /pwmMax)));}
			else{	motorLeft = -(int)(radius * (1 - ((xAxis * factorLessSide) /pwmMax)));}
			
			motorRight = (int)(yAxis * (1 - ((xAxis * factorLessSide) /pwmMax)));
		}
		else if(xAxis < 0){
			motorLeft = (int)(yAxis * (1 - ((-xAxis * factorLessSide) /pwmMax)));;

			if(yAxis > 0){	motorRight = (int)(radius * (1 - ((-xAxis * factorLessSide) /pwmMax)));}
			else{	motorRight = -(int)(radius * (1 - ((-xAxis * factorLessSide) /pwmMax)));}

		}
		  else if(xAxis == 0) {
			motorLeft = yAxis;
			motorRight = yAxis;
		}*/


		if(xAxis > 0){
			if(yAxis > 0){	motorLeft = radius;}
			else{	motorLeft = -radius;}

			motorRight = yAxis;
		}
		else if(xAxis < 0){
			motorLeft = yAxis;

			if(yAxis > 0){	motorRight = radius;}
			else{	motorRight = -radius;}

		}
		else if(xAxis == 0) {
			motorLeft = yAxis;
			motorRight = yAxis;
		}



		// try of exponential groth of the speed (does not really work)
		/*if(xAxis != 0 ){	//&& !(radius > 0.9 * pwmMax)
			if(motorRight > 0){
				motorRight = (int)Math.pow(1.8,motorRight/(0.1 * pwmMax));
			} else {
				motorRight = -(int)Math.pow(1.8,Math.abs(motorRight/(0.1 * pwmMax)));
			}
			if(motorLeft > 0){
				motorLeft = (int)Math.pow(1.8,motorLeft/(0.1 * pwmMax));
			} else {
				motorLeft = -(int)Math.pow(1.8,Math.abs(motorLeft/(0.1 * pwmMax)));
			}
		}*/

		if(yAxis < 0){
			motorLeft *= factorLessBehin;
			motorRight *= factorLessBehin;
		}

		motorLeft = (int)(motorLeft * (Math.abs(Math.sin(angle))*2 + 1))/3;
		motorRight = (int)(motorRight * (Math.abs(Math.sin(angle))*2 + 1))/3;
		//int parametre = ;

		if(xAxis < 0 && radius > 0.9 * pwmMax){
			motorLeft = (int)(motorLeft * (Math.abs(Math.sin(angle)) + 2))/3;
		} else if(radius > 0.9 * pwmMax){
			motorRight = (int)(motorRight * (Math.abs(Math.sin(angle)) + 2))/3;
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



		System.out.println("left  :" +motorLeft);
		System.out.println("right :" +motorRight);
        
        //cmdSend = String.valueOf(commandLeft+directionL+motorLeft+"\r"+commandRight+directionR+motorRight+"\r");

        MainActivity.list.get(MainActivity.chosenone).updateMotorsSpeed(motorLeft, motorRight);
		
	}
	

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






	  /* if(xAxis > 0) {
			motorRight = yAxis;
			if(Math.abs(Math.round(calc_x)) > xR){
				motorLeft = Math.round((calc_x-xR)*pwmMax/(radiuscircle-xR));
				motorLeft = Math.round(-motorLeft * yAxis/pwmMax);
			}
			else motorLeft = yAxis - yAxis*xAxis/pwmMax;
	   }
	   else if(xAxis < 0) {
			motorLeft = yAxis;
			if(Math.abs(Math.round(calc_x)) > xR){
				motorRight = Math.round((Math.abs(calc_x)-xR)*pwmMax/(radiuscircle-xR));
				motorRight = Math.round(-motorRight * yAxis/pwmMax);
			}
				else motorRight = yAxis - yAxis*Math.abs(xAxis)/pwmMax;
	   }
	   else if(xAxis == 0) {
			motorLeft = yAxis;
			motorRight = yAxis;
	  }*/



//        motorLeft = Math.abs(motorLeft);
//        motorRight = Math.abs(motorRight);
