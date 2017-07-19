package de.tum.nst.model;

public class LedCommand implements RobotCommand {
		
	private final int ledState;
	
	@Override
	public String toCommandString() {
		return "!L" + ledState + '\n';
	}

	public LedCommand(int ledState) {
		super();
		this.ledState = ledState;
	}
	
	
	
}
