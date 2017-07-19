package de.tum.nst.model;

public class TimestampCommand implements RobotCommand {

	private final int timestampMode;

	public TimestampCommand(int timestampMode) {
		this.timestampMode = timestampMode;
	}

	@Override
	public String toCommandString() {
		return "!E" + timestampMode + '\n';
	}

	public int getBiasSetting() {
		return timestampMode;
	}

}
