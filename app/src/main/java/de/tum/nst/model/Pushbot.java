package de.tum.nst.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import de.tum.nst.model.BiasCommand;
import de.tum.nst.model.EventsCommand;
import de.tum.nst.model.MotorCommand;
import de.tum.nst.model.MotorDriverCommand;
import de.tum.nst.model.RTCCommand;
import de.tum.nst.model.RecordCommand;
import de.tum.nst.model.RobotCommand;
import de.tum.nst.model.SensorCommand;
import de.tum.nst.model.SleepCommand;
import de.tum.nst.model.TimestampCommand;
import de.tum.nst.model.TrackingCommand;
import de.tum.nst.model.DVSevent;
import de.tum.nst.connect.RobotSocketManager;


/*
	The class containing evrything about each pushbot.

 */


public class Pushbot implements RobotSocketManager.OnStatusChangedListener {

	public String hostIP = "10.162.177.90";
	public String nameofthebot = "not named";
	public int numberofthebot = -2;		// -2 means not initialised, -1 means new, positive values means the number of the bot
	private final int port = 56000;		// port on which we will connect them
	
	private final BlockingQueue<String> queue;
	public LinkedList<DVSevent> posEventFromBot = new LinkedList<DVSevent>();
	public LinkedList<DVSevent> negEventFromBot = new LinkedList<DVSevent>();
	public int[] rgbValues = new int[(128 * 5) * (128 * 5)];	// the pixels sean by the dvs camera of the pushbot
	public int[] trackValues = new int[(128 * 2) * (128 * 2)];	// the  pixels of the tracking point
	public boolean showImage = false;
	//private final DVSevent[] eventsList;
	private RobotSocketManager commandThread;
	private ConnectionState conectionState;
	private int lastBiasSettings;
	private final HashMap<Integer, Integer> lastMotorCommand;
	
	private final OnStatusChangedListerner listener;
	boolean motorInUse = false;
	public boolean DVSstreamactive = false;
	boolean isLeader = false;
	boolean isFollower = false;
	public boolean istracking = false;
	public boolean connectedb = false;

	public interface OnStatusChangedListerner {
		//public void onStatusChanged(ConnectionState state);
	}
	
	public enum ConnectionState {
		CONNECTED, CONNECTING, DISCONNECTED, SLEEPING
	};

	
	public Pushbot(OnStatusChangedListerner listener) {	
		queue = new ArrayBlockingQueue<String>(100);
		//eventsList = new DVSevent[10000];
		posEventFromBot.add(new DVSevent(0,0));
		negEventFromBot.add(new DVSevent(0,0));
		lastMotorCommand = new HashMap<Integer, Integer>(2);
		lastBiasSettings = -1;
		conectionState = ConnectionState.DISCONNECTED;
		this.listener = listener;
	}
	
	

	
	
//	+++++++++++++++++++++++++++++++++++++++++++++++++++++++++			getters, setters			++++++++++++++++++++++++++++++++++++++++++++++
	
	public int getNumberofthebot() {
		return numberofthebot;
	}
	public void setNumberofthebot(int numberofthebot) {
		this.numberofthebot = numberofthebot;
	}
	
	public ConnectionState getConectionState() {
		return conectionState;
	}
	
	public String getCurrentSpeed(int motor) {
		Integer lastSpeed = lastMotorCommand.get(motor);
		if (lastSpeed == null)
			return "?";
		return lastSpeed.toString();
	}


	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public RobotSocketManager getCommandThread() {
		return commandThread;
	}


	public void setLeader(boolean leader) {
		isLeader = leader;
	}


	public boolean isLeader() {
		return isLeader;
	}



	public boolean isFollower() {
		return isFollower;
	}

	public void setFollower(boolean follower) {
		isFollower = follower;
	}

//	+++++++++++++++++++++++++++++++++++++++++++++++++++++++++			methods from listener			++++++++++++++++++++++++++++++++++++++++++++++

/*
	This are the methods for the listener which is not used yet.

*/


	@Override
	public void onStatusChanged(boolean connected) {
		if (connected) {
			conectionState = ConnectionState.CONNECTED;
			updateListener();
		} else {
			conectionState = ConnectionState.DISCONNECTED;
			updateListener();
		}
	}
	
	@Override
	public void onAwaked() {
		if (conectionState == ConnectionState.SLEEPING) {
			conectionState = ConnectionState.CONNECTED;
			updateListener();
		}
	}

	@Override
	public boolean isSleeping() {
		return conectionState == ConnectionState.SLEEPING;
	}		
	
	
	
//	+++++++++++++++++++++++++++++++++++++++++++++++++++++++++			methods			++++++++++++++++++++++++++++++++++++++++++++++
	
/*
	This are all the different commands that can be send to a bot or interaction that we can have with it.
	They are then performed by the robotsocketmanager.
	The name of the methods actually explain what they are doing.
*/
	

	public void connect() {
		//if (conectionState == ConnectionState.DISCONNECTED) {
			this.commandThread = new RobotSocketManager(queue,hostIP, port, this, numberofthebot-1);
			this.conectionState = ConnectionState.CONNECTING;
			//System.out.println("co");
//			updateListener();
			commandThread.start();
		//}

	}

	public void disconnect() {
		//if (conectionState != ConnectionState.DISCONNECTED) {
		this.commandThread.dispose();
		try{
		//this.commandThread.join();
		} catch (Exception e) {}

		/*this.commandThread.interrupt();
		try{
			this.commandThread.stop();
		} catch (Exception e) {}

		/*try{
			Thread.sleep(40);
		} catch(Exception e) {}

		commandThread.dispose();
		commandThread.interrupt();
		try{
			commandThread.stop();
		} catch (Exception e) {}
		//	conectionState = ConnectionState.DISCONNECTED;*/
//			updateListener();
	//	}
	}

	//obsolete, this function is now build in the robotsocketmganager class
	public void ping() {
		if(commandThread.connected) {
			commandThread.connected = false;
			askoptions();
		} else{
			connect();
		}
	}


	public void connectionRecovered() {
		if(DVSstreamactive){
			enableEvents();
		}
		if(isLeader){
			enableLEDRobotChain();
		}
		if(isFollower){
			enableRobotChain();
		}
		if(istracking){
			enableTracking();
		}

	}


	private void updateListener() {
		//listener.onStatusChanged(conectionState);
	}

	public void dispose() {
		if (commandThread != null)
			commandThread.dispose();
	}

	
	private void sendMotorCommand(MotorCommand com) {
		Integer lastSpeed = lastMotorCommand.get(com.getMotor());
		if (lastSpeed != null) {
			if (lastSpeed == com.getSpeed())
				return;
		}
		while (!queue.offer(com.toCommandString())) {
			queue.poll();
		}
		lastMotorCommand.put(com.getMotor(), com.getSpeed());
	}

	public void updateBiasSettings(int selection) {
		if (lastBiasSettings == selection)
			return;
		sendCommand(new BiasCommand(selection));
		lastBiasSettings = selection;
	}

	public void updateTimestampMode(int selection) {
		sendCommand(new TimestampCommand(selection));
		//commandThread.updateTimestampMode(selection);
	}

	public void updateMotorsSpeed(int speedM1, int speedM2) {
		sendMotorCommand(MotorCommand.obtain(MotorCommand.MOTOR1, speedM1));
		sendMotorCommand(MotorCommand.obtain(MotorCommand.MOTOR2, speedM2));
	}

	public void stopMotor() {
		sendCommand(MotorCommand.obtain(MotorCommand.MOTOR1, 0));
		sendCommand(MotorCommand.obtain(MotorCommand.MOTOR2, 0));
	}

	public void sleep() {
		if (conectionState == ConnectionState.SLEEPING)
			return;
		final SleepCommand sleep = new SleepCommand();
		while (!queue.offer(sleep.toCommandString())) {
			queue.poll();
		}
		conectionState = ConnectionState.SLEEPING;
//		updateListener();
	}

	public void disableEvents() {
		sendCommand(new EventsCommand(false));
	}

	public void enableEvents() {
		sendCommand(new EventsCommand(true));
	}

	public void updateRTC() {
		sendCommand(new RTCCommand());
	}

	public void startRecord() {
		sendCommand(new RecordCommand(true));
	}

	public void stopRecord() {
		sendCommand(new RecordCommand(false));
	}

	public void enableMotorDriver() {
		sendCommand(new MotorDriverCommand(true));
	}

	public void disableMotorDriver() {
		sendCommand(new MotorDriverCommand(false));
	}

	public void enableTracking() {
		sendCommand(new TrackingCommand(true));
		//sendCommand(new SensorCommand(false, 1 << SensorCommand.TRACKED_POINT));
	}

	public void enableTrackingFollower() {
		sendCommand(new TrackingCommand(true));
		//sendCommand(new SensorCommand(true, 1 << SensorCommand.TRACKED_POINT,
			//	200));
	}

	public void disableTracking() {
		sendCommand(new TrackingCommand(false));
		//sendCommand(new SensorCommand(false, 1 << SensorCommand.TRACKED_POINT));
	}

	public void resetPushbot() {
		sendCommand(new ResetCommand());
	}
	
	public void setLedOn() {
		sendCommand(new LedCommand(1));
	}

	public void enableRobotChain() {
		sendCommand(new RobotChainCommand (true));
	}

	public void desableRobotChain() {
		sendCommand(new RobotChainCommand (false));
	}

	public void enableLEDRobotChain() {	sendCommand(new LedRobotChain (true)); }

	public void desableLEDRobotChain() { sendCommand(new LedRobotChain (false)); }

	public void enableLaserRobot() {	sendCommand(new LaserCommand (true)); }

	public void disableLaserRobot() { sendCommand(new LaserCommand (false)); }

	public void enableBuzzerRobot() {	sendCommand(new BuzzerCommand (true)); }

	public void disableBuzzerRobot() { sendCommand(new BuzzerCommand (false)); }

	private void sendCommand(RobotCommand com) {
		while (!queue.offer(com.toCommandString())) {
			queue.poll();
		}
	}

	public void askoptions() {
		sendCommand(new SleepCommand());
	}

	
}
