package de.tum.nst.connect;

import android.graphics.Color;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import de.tum.nst.model.DVSevent;
import de.tum.nst.pushbotcontrol.MainActivity;


public class RobotSocketManager extends Thread {
	private final BlockingQueue<String> queue;
	private final String hostIP;
	private Socket socket = null;
	private final int port;
	private final OnStatusChangedListener listener;
	private RobotOutput robotOutput;	//the class is here, in the RobotSocketManager class, look there to understand
	private RobotInput robotInput;		//the class is here, in the RobotSocketManager class, look there to understand
	private int timestampMode = 0;
	public boolean connected = false;	// leting know if the connection is active
	public boolean enabled = true;
	private final int numberofthebot;
	private int[] buffRead = new int[3];
	private ConnectionStatus connectionStatus;
	private BufferedReader in;
	private BufferedInputStream instream;
	private PrintWriter output;

	static long sendtime;
	static long [] times = new long[10000];
	static int indexTimes = 0;
	static boolean justsend = false;

	public interface OnStatusChangedListener {
		public void onStatusChanged(boolean connected);

		public void onAwaked();

		public boolean isSleeping();
	}

	public RobotSocketManager(BlockingQueue<String> queue, String hostIP, int port, OnStatusChangedListener listener, int numberofthebot) {
		this.queue = queue;
		this.hostIP = hostIP;
		this.port = port;
		this.listener = listener;
		this.numberofthebot = numberofthebot;
	}

	public RobotInput getRobotInput() {
		return this.robotInput;
	}




/*	public void updateTimestampMode(int selection) {
		timestampMode = selection;
		if (robotInput != null) {
			robotInput.currentProcessingStep = 0;
		}
	}		*/


	// This is connecting the android device to the pushbot.
	//  It opens output and input streams to communicate with it.
	@Override
	public void run() {

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(hostIP, port), 5000);
			output = new PrintWriter(socket.getOutputStream(), true);
			//final PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
//			listener.onStatusChanged(true);
			robotOutput = new RobotOutput(queue, output);
			robotOutput.start();
			in = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream(),5000), "ISO-8859-1"));
			instream = new BufferedInputStream(socket.getInputStream(),5000);
			robotInput = new RobotInput(instream, listener);



			robotInput.start();

			connectionStatus = new ConnectionStatus();
			connectionStatus.start();

			connectionStatus.join();
			robotInput.join();
/*			if (!robotInput.stopRequested) {
				// Error with the socket
				robotOutput.stopRequested = true;
				robotOutput.interrupt();
				listener.onStatusChanged(false);
				return;
			}*/
			robotOutput.join();
		} catch (IOException | InterruptedException e ) { //
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
					socket.close();
					connected = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
			listener.onStatusChanged(false);
		}

		//System.out.println("\n 5\n 5\n 5\n 5\n 5\n 5\n 5\n  wh4ole"+"5\n 5\n 5\n 5\n");
	}



/*
	This method is used to stop the connection.
 */
	public void dispose() {
		//System.out.print("bladi"+numberofthebot+"\naddi\nadi\nfalse\nadi\nadi"+numberofthebot+"\nbladi\n");
		enabled = false;
		MainActivity.list.get(MainActivity.chosenone).connectedb = false;
		try {
			this.socket.close();
		} catch(Exception e){}

		if (MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).getCommandThread().connectionStatus != null) {
			this.connectionStatus.stopRequested = true;

			/*try {
				this.connectionStatus.join();
				//connectionStatus.stop();
			} catch(Exception e){this.connectionStatus.interrupt();}*/
		}

		if (this.robotInput != null) {
			this.robotInput.stopRequested = true;
			/*try {
				this.robotInput.join();
				//this.robotInput.stop();
			} catch(Exception e){this.robotInput.interrupt();}*/
		}

		if (this.robotOutput != null) {
			this.robotOutput.stopRequested = true;

			/*try {
				this.robotOutput.join();
				//this.robotOutput.stop();
			} catch(Exception e){this.robotOutput.interrupt();}*/
		}

		MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).askoptions();

		/*try {
			join();
		} catch (InterruptedException e) {
			interrupt();
			e.printStackTrace();
		}*/

		connected = false;
		MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).connectedb = false;
	}




/*
	This method is used to restart the connection after the connection was lost.
 */
	public void restart(){

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (robotInput != null) {
			robotInput.stopRequested = true;
			/*try {
				this.robotInput.join();
				//this.robotInput.stop();
			} catch(Exception e){this.robotInput.interrupt();}*/
		}
		if (robotOutput != null) {
			robotOutput.stopRequested = true;
			/*try {
				this.robotOutput.join();
				//this.robotInput.stop();
			} catch(Exception ex){
				this.robotOutput.interrupt();
			}*/
		}

		try {
			Thread.sleep(50);
		} catch (Exception e) {}

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(hostIP, port), 5000);

			if(socket.isConnected()) {
				//enabled = false;
				//connectionStatus.interrupt();
				connected = true;
				MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).connectedb = true;


				output = new PrintWriter(socket.getOutputStream(), true);
				robotOutput = new RobotOutput(queue, output);

				in = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream(),5000), "ISO-8859-1"));
				instream = new BufferedInputStream(socket.getInputStream(),5000);
				robotInput = new RobotInput(instream, listener);


				//connectionStatus = new ConnectionStatus();

				try {
					Thread.sleep(35);
				} catch (Exception e) {}

				robotOutput.start();
				robotInput.start();

				try {
					Thread.sleep(35);
				} catch (Exception e) {}

				//robotOutput.join();
				//connectionStatus.join();

				MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).connectionRecovered();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}



/*
	Even if the name is a bit stupid or misleading, this is a class to send things to the robot.

	The commands that are send by the methods of the pushbot class are used here and they are send.

*/
	private static class RobotOutput extends Thread {
		private final BlockingQueue<String> queue;
		private final PrintWriter output;

		public RobotOutput(BlockingQueue<String> queue, PrintWriter output) {
			this.queue = queue;
			this.output = output;
		}

		boolean stopRequested = false;

		@Override
		public void run() {
			while (!stopRequested) {
				try {
					final String command = queue.take();
					//tool to mesure the rtt
					/*if(justsend){
						times[indexTimes] = (System.nanoTime() - sendtime) / 1000;
						System.out.print(times[indexTimes] + "\n \n \n" + indexTimes + "\n \n \n" + sendtime);
						if(times[indexTimes] != 0){
							indexTimes++;
						}
						justsend = false;
						if(indexTimes == 1000){
							long sum = 0;
							long max = 0;
							long min = 1000;
							for(int i = 0; i < indexTimes; i++){
								sum += times[i];
								if(times[i] < min) min = times[i];
								if(times[i] > max) max = times[i];
							}
							sum = sum / 1000;
							System.out.print(" Moyenne!!! \n \n !!" + sum + "\n");
							System.out.print(" MAx!!! \n \n !!" + max + "\n");
							System.out.print(" Min!!! \n \n !!" + min + "\n");
						}
					}*/
					output.print(command);
					output.flush();
					System.out.println(command);
				} catch (InterruptedException e) {
				}
			}
			output.close();
			queue.clear();

			//System.out.println("\n 5\n 5\n 5\n 5\n 5\n 5\n 5\n  whole"+"5\n 5\n 5\n 5\n");
		}

	}




	public Socket getSocket() {
		return socket;
	}





/*
	Even if the name is a bit stupid or misleading, this is a class to read what the robot sends.


 */

	private class RobotInput extends Thread {
		private int cont = 0;
		private BufferedInputStream inputstream;
		int factor = 5;
		int factortrack = 2;
		int width = (128*factor);
		int trackwidth = (128*factortrack);

		private final OnStatusChangedListener listener;
		private int currentProcessingStep = 0;
		private StringBuilder builder = new StringBuilder();
		private int eventX;
		private int eventY;
		private boolean eventPolarity;
		private long eventTimestamp = 0;
		int statetrack = 0, compteurint = 0;
		private int trakeventX;
		private int trakeventY;

		private final static int NO_TIMESTAMP = 0;
		private final static int VARIABLE_TIMESTAMP = 1;
		private final static int TWO_BYTE_TIMESTAMP = 2;
		private final static int THREE_BYTE_TIMESTAMP = 3;
		private final static int FOUR_BYTE_TIMESTAMP = 4;
		int[] rgbValuesRead = new int[(128 * 5) * (128 * 5)];
		public int[] trackValuesRead = new int[(128 * 2) * (128 * 2)];

		private InputStreamReader reader;
		private boolean running = true;

		public RobotInput(BufferedInputStream inputstream, OnStatusChangedListener listener) {
			this.inputstream = inputstream;
			this.listener = listener;
		}

		boolean stopRequested = false;


		public RobotInput(InputStreamReader reader, OnStatusChangedListener listener) {
			this.reader = reader;
			this.listener = listener;
		}


		BufferedReader inputreader;

		public RobotInput(BufferedReader inputreader, OnStatusChangedListener listener) {
			this.inputreader = inputreader;
			this.listener = listener;
		}


		private void addEvent() {
			//System.out.print(eventTimestamp + "\n");

			if(cont < 21) {
				/*
				for 21 events in a row i add the values in the tampon matrix that is created in this thread
				this is to optimize performance, this makes a huge difference if it always directly writes the values in the
				array of MainActivity
				 */

				for(int i = eventY*factor; i < (eventY*factor + factor - 1); i++){
					for(int j = eventX*factor; j < (eventX*factor + factor - 1); j++){
						if(eventPolarity) {
							//MainActivity.list.get(MainActivity.chosenone).rgbValues[(j * width) + i] = Color.GREEN;
							rgbValuesRead[(j * width) + i] = Color.GREEN;
						} else{
							//MainActivity.list.get(MainActivity.chosenone).rgbValues[(j * width) + i] = Color.RED;
							rgbValuesRead[(j * width) + i] = Color.RED;
						}
					}
				}



				cont++;
			} else{ //if(cont == 51){
				MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).rgbValues = rgbValuesRead;
				cont = 0;
			}
		//	 else {cont++;}

		}


		private void addTrackEvent() {
				//DVSevent evt = new DVSevent(trakeventY, trakeventX);

				for (int i = 0; i < trackwidth; i++) {
					for (int j = 0; j < trackwidth; j++) {
						trackValuesRead[(j * trackwidth) + i] = Color.BLACK;
					}
				}

				for(int i = trakeventX*factortrack; i < (trakeventX*factortrack + factortrack); i++){
					for(int j = 0; j < trackwidth - 1; j++){
						try {
							trackValuesRead[(j * trackwidth) + i] = Color.GREEN;
						} catch (Exception exp){}
					}
				}
				for(int i = 0; i < trackwidth - 1; i++){
					for(int j = trakeventY*factortrack; j < (trakeventY*factortrack + factortrack); j++){
						try {
							trackValuesRead[(j * trackwidth) + i] = Color.GREEN;
						} catch (Exception exp){}
					}
				}

				MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).trackValues = trackValuesRead;

		}


//the while loop has to be realy simple because else the reading of events is going to be too slow
//	this is an important isue, if the reading is to slow we cant recognise what the image is
		@Override
		public void run() {

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < width; j++) {
					rgbValuesRead[(j * width) + i] = Color.BLACK;
				}
			}

			for (int i = 0; i < trackwidth; i++) {
				for (int j = 0; j < trackwidth; j++) {
					trackValuesRead[(j * trackwidth) + i] = Color.BLACK;
				}
			}
			MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).trackValues = trackValuesRead;

			MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).updateTimestampMode(0);
			timestampMode = 0; //VARIABLE_TIMESTAMP;

			try {
				int buf = inputstream.read();
				char buff2[] = new char[1];
				byte data;

				//long startTime = System.currentTimeMillis();
				//int testCont = 0;

				while (!stopRequested) { //this.running &&			buf != -1

					// this was a smal thing to see how long the programm needed to read 20000 events, i let it here because
					//		it could be usefull again if changing this loop
					/*if(testCont == 20000) {
						System.out.print("Le temps : " + (System.currentTimeMillis() - startTime));
						startTime = System.currentTimeMillis();
						testCont = 0;
					} else{
						testCont++;
					}*/

					data = (byte) buf;

					connected = true;
					MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).connectedb = true;

					switch (currentProcessingStep) {
						case 0: {
							if ((data & 0x80) == 0) {
								if(data == '-'){
									currentProcessingStep = 6;
									statetrack = 0;

								} else {
									//System.out.print((char) data);
								}
							} else {
								eventX = (int) (data & 0x7f);
								currentProcessingStep++;
							}
							break;
						}
						case 1: {
							eventPolarity = ((data & 0x80) != 0);
							eventY = (int) (data & 0x7f);
							if (timestampMode == NO_TIMESTAMP) {
								currentProcessingStep = 0;
								//eventTimestamp = System.currentTimeMillis();
								addEvent();

							} else {
								eventTimestamp = 0;
								currentProcessingStep++;
							}


							break;
						}
						/*
							in this version of the app is only the mode without time stamp used
							the following steps are here in case you want to use the mode with time stamp
							you might have to change a bit the whole switch case

							the timestamp are not used because the events are directly shown as they arrive
							which means that the whole event showing is made without the timestamps
							you will have to use timestamps only in case you want to change the event showing
							*/

						case 2: {
							currentProcessingStep++;
							if (timestampMode == VARIABLE_TIMESTAMP) {
								eventTimestamp = data & 0x7f;
								//if ((data & 0x80) != 0) {
									addEvent();
									currentProcessingStep = 0;
								//}
							} else {
								eventTimestamp = data;
							}
							break;
						}
						case 3: {
							if (timestampMode == VARIABLE_TIMESTAMP) {
								eventTimestamp = (eventTimestamp << 7)
										| (data & 0x7f);
								if ((data & 0x80) != 0) {
									addEvent();
									currentProcessingStep = 0;
								}
							} else {
								eventTimestamp = (eventTimestamp << 8)
										| (data);
								if (timestampMode == TWO_BYTE_TIMESTAMP) {
									addEvent();
									currentProcessingStep = 0;
								} else {
									currentProcessingStep++;
								}
							}
							break;
						}
						case 4: {
							if (timestampMode == VARIABLE_TIMESTAMP) {
								eventTimestamp = (eventTimestamp << 7)
										| (data & 0x7f);
								if ((data & 0x80) != 0) {// If not set
									// discard event
									addEvent();
								}
								currentProcessingStep = 0;
							} else {
								eventTimestamp = (eventTimestamp << 8)
										| (data);
								if (timestampMode == THREE_BYTE_TIMESTAMP) {
									addEvent();
									currentProcessingStep = 0;
								} else {
									currentProcessingStep++;
								}
							}
							break;
						}
						case 5: { //
							if (timestampMode == FOUR_BYTE_TIMESTAMP) {
								eventTimestamp = (eventTimestamp << 8)
										| (data);
								addEvent();
								currentProcessingStep = 0;
							} else {
								throw new RuntimeException("Event parser bug");
							}
						}
						// case 6 is here to analyse the tracking points, there are send in form of strings
						// for example : -Kx=23,y=73
						// i used a switch case here to decode them
						case 6: {
							switch (statetrack) {
								case(0):{
									if(data != 'K'){currentProcessingStep = 0; statetrack = 0;}
									statetrack++;
									break;
								}
								case(1):{
									if((char)data != 'x'){currentProcessingStep = 0; statetrack = 0;}

									//statetrack++;
									statetrack++;
									break;
								}
								case(2):{
									if((char)data != '='){currentProcessingStep = 0; statetrack = 0;}
									compteurint = 0;
									statetrack++;
									break;
								}
								case(3):{
									if((char)data < 58 && (char)data >47){
										buffRead[compteurint] = (int)data - 48;
										System.out.print(buffRead[compteurint]);
										compteurint++;
										break;
									} else {
										if(buffRead[1] == -1){
											trakeventX = buffRead[0];
										} else if(buffRead[2] == -1){
											trakeventX = buffRead[0] * 10 + buffRead[1];
										} else{
											trakeventX = buffRead[0] * 100 + buffRead[1] * 10 + buffRead[2] ;
										}
										buffRead[0] = -1;
										buffRead[1] = -1;
										buffRead[2] = -1;
										statetrack++;
									}
								}
								case(4):{
									if((char)data != ','){currentProcessingStep = 0; statetrack = 0;}
									statetrack++;
									break;
								}
								case(5):{
									if((char)data != 'y'){currentProcessingStep = 0; statetrack = 0;}
									compteurint = 0;
									statetrack++;
									break;
								}
								case(6):{
									if((char)data != '='){currentProcessingStep = 0; statetrack = 0;}
									statetrack++;

									break;
								}
								case(7):{
									if((char)data < 58 && (char)data >47){
										buffRead[compteurint] = (int)data - 48;
										compteurint++;
										break;
									} else {
										if(buffRead[1] == -1){
											trakeventY = buffRead[0];
										} else if(buffRead[2] == -1){
											trakeventY = buffRead[0] * 10 + buffRead[1];
										} else{
											trakeventY = buffRead[0] * 100 + buffRead[1] * 10 + buffRead[2] ;
										}
										buffRead[0] = -1;
										buffRead[1] = -1;
										buffRead[2] = -1;
										addTrackEvent();
										trakeventX = 0;
										trakeventY = 0;
										statetrack=0;
										currentProcessingStep = 0;
									}

								}
							}




							break;
						}

					}



					buf = inputstream.read();


				}
			}
			catch (IOException e) {
				System.out.println("IOReader::IOException");
				System.out.println(e.getMessage());
				//e.printStackTrace();
				connected = false;
				MainActivity.list.get(numberofthebot  - MainActivity.numberofDeleted).connectedb = false;
			}

			//System.out.println("\n 3\n 3\n 3\n 3\n 3\n 3\n 3\n  whole"+"3\n 3\n 3\n 3\n 3\n");

		}





		/*@Override
		https://coderanch.com/t/386178/java/bit-char
		public void run() {
			try {
				// inputstream.read();
				while (!stopRequested) {
					int charReceived = inputstream.read();
					if (charReceived == -1)
						break;
					//if (listener.isSleeping())
					//	listener.onAwaked();
					switch (currentProcessingStep) {
						case 0: {
							if ((charReceived & 0x80) == 0) {
								builder.append((char) charReceived);
							} else {
								if (builder.length() > 0) {
									//RobotTextParser.parseTextReply(builder.toString());
									System.out.println(builder.toString());
									builder.setLength(0);
								}
								eventX = charReceived & 0x7f;
								currentProcessingStep++;
							}
							break;
						}
						case 1: {
							eventPolarity = ((charReceived & 0x80) != 0);
							eventY = charReceived & 0x7f;
							if (timestampMode == NO_TIMESTAMP) {
								currentProcessingStep = 0;
								eventTimestamp = System.currentTimeMillis();
								addEvent();
							} else {
								eventTimestamp = 0;
								currentProcessingStep++;
							}
							break;
						}
						case 2: {
							currentProcessingStep++;
							if (timestampMode == VARIABLE_TIMESTAMP) {
								eventTimestamp = charReceived & 0x7f;
								if ((charReceived & 0x80) != 0) {
									addEvent();
									currentProcessingStep = 0;
								}
							} else {
								eventTimestamp = charReceived;
							}
							break;
						}
						case 3: {
							if (timestampMode == VARIABLE_TIMESTAMP) {
								eventTimestamp = (eventTimestamp << 7)
										| (charReceived & 0x7f);
								if ((charReceived & 0x80) != 0) {
									addEvent();
									currentProcessingStep = 0;
								}
							} else {
								eventTimestamp = (eventTimestamp << 8)
										| (charReceived);
								if (timestampMode == TWO_BYTE_TIMESTAMP) {
									addEvent();
									currentProcessingStep = 0;
								} else {
									currentProcessingStep++;
								}
							}
							break;
						}
						case 4: {
							if (timestampMode == VARIABLE_TIMESTAMP) {
								eventTimestamp = (eventTimestamp << 7)
										| (charReceived & 0x7f);
								if ((charReceived & 0x80) != 0) {// If not set
									// discard event
									addEvent();
								}
								currentProcessingStep = 0;
							} else {
								eventTimestamp = (eventTimestamp << 8)
										| (charReceived);
								if (timestampMode == THREE_BYTE_TIMESTAMP) {
									addEvent();
									currentProcessingStep = 0;
								} else {
									currentProcessingStep++;
								}
							}
							break;
						}
						case 5: { //
							if (timestampMode == FOUR_BYTE_TIMESTAMP) {
								eventTimestamp = (eventTimestamp << 8)
										| (charReceived);
								addEvent();
								currentProcessingStep = 0;
							} else {
								throw new RuntimeException("Event parser bug");
							}
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					inputstream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}*/


	}


/*
	This class is meant to always have an actual value for the variable "connected"
	which is true if the robot is connected and false else.
	To do this we set the value of connected to flase, and then send some infos to the robot, if it is
	connected an answer will be recieved. In the lope that gets the ansewr the value of connected will
	be set back to true. I no answer comes, this means the robot is not connected and the the value
	stays false.
 */

	private class ConnectionStatus extends Thread {

		public ConnectionStatus() {
		}

		private boolean stopRequested = false;
		private boolean lastco = true;

		// the idea is to try every second if the robot is connected

		@Override
		public void run() {
			while (!stopRequested) {
				//System.out.println("\n \n \n\n\n\n\n\n"+numberofthebot + connected+"\n \n \n"+ (!connected && !lastco && enabled) +" whole"+"\n\n\n\n\n");
				if(!connected && !lastco && enabled){   // && enabled
					try {
						restart();
						lastco = true;
					} catch (Exception ex){}
				} //else {
					lastco = connected;
				//}


				connected = false;
				try {
					MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).connectedb = false;


					MainActivity.list.get(numberofthebot - MainActivity.numberofDeleted).askoptions();
					//justsend = true;
					//sendtime = System.nanoTime();
				} catch (Exception exp){}


				try {
					Thread.sleep(899);
				} catch (Exception e) {}

			}

			//System.out.println("\n 4\n 4\n 4\n 4\n 4\n 4\n 4\n  whole"+"4\n 4\n 4\n 4\n");
		}

	}


}




