package de.tum.nst.model;

public class SensorCommand implements RobotCommand {

	
	public static final int TRACKED_POINT = 29;
	
	private final boolean enabled;
	private final long id;
	private final int period;

	public SensorCommand(boolean enabled, long id, int period) {
		this.enabled = enabled;
		this.id = id;
		this.period = period;
	}

	public SensorCommand(boolean enabled, long id) {
		this(enabled, id, 0);
	}

	public SensorCommand(boolean enabled) {
		this(enabled, 0, 0);
	}

	@Override
	public String toCommandString() {
		if (enabled) {
			return "!S+" + id + "," + period + "\n";
		} else {
			if (id > 0) {
				return "!S-" + id + "\n";
			} else {
				return "!S-\n";
			}
		}
	}

}
