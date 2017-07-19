package de.tum.nst.model;

public class EventsCommand implements RobotCommand {
	
	private final boolean record;
	public EventsCommand(boolean record) {
		this.record = record;
	}


	@Override
	public String toCommandString() {
		return "E" + (record?"+\n":"-\n");
	}

}
