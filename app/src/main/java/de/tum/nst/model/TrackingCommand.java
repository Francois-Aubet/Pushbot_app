package de.tum.nst.model;

public class TrackingCommand implements RobotCommand {

	private final boolean enabled;

	public TrackingCommand(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toCommandString() {
		return "!KR+125" + '\n';
	}

}
