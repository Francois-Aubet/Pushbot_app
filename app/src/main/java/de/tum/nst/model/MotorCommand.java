package de.tum.nst.model;

import java.util.Stack;

public class MotorCommand implements RobotCommand {
	
	public static final int MOTOR1 = 0;
	public static final int MOTOR2 = 1;
	public static final int MAX_SPEED = 80;
	
	private int speed;
	private int motor;

	private MotorCommand(int motor, int speed) {
		this.speed = speed;
		this.motor = motor;
	}

	public int getSpeed() {
		return speed;
	}

	public int getMotor() {
		return motor;
	}

	private final static Stack<MotorCommand> pool = new Stack<MotorCommand>();

	public synchronized static void free(MotorCommand m) {
		if (m != null)
			pool.push(m);
	}

	public synchronized static MotorCommand obtain(int motor, int speed) {
		if (pool.empty()) {
			return new MotorCommand(motor,speed);
		}
		final MotorCommand m = pool.pop();
		m.speed = speed;
		m.motor = motor;
		return m;
	}

	@Override
	public String toCommandString() {
		return "!MV" + motor + "=" + speed + "\n";
	}

	@Override
	public String toString() {
		return toCommandString();
	}
}
