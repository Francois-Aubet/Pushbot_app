package de.tum.nst.model;

public class MotorDriverCommand implements RobotCommand {

	private final boolean record;

	public MotorDriverCommand(boolean record) {
		this.record = record;
	}

	@Override
	public String toCommandString() {
		return "!M" + (record ? "+\n" : "-\n");
	}

}
