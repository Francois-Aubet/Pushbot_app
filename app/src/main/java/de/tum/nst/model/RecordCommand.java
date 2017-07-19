package de.tum.nst.model;

public class RecordCommand implements RobotCommand {
	
	private final boolean record;
	public RecordCommand(boolean record) {
		this.record = record;
	}


	@Override
	public String toCommandString() {
		return "!ER" + (record?"+\n":"-\n");
	}

}
