package de.tum.nst.model;

public class ResetCommand implements RobotCommand {
	
	@Override
	public String toCommandString() {
		return "R\n";
	}
	
	
}
