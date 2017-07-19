package de.tum.nst.model;

public class BiasCommand implements RobotCommand {

	private final int biasSetting;

	public BiasCommand(int biasSetting) {
		this.biasSetting = biasSetting;
	}

	@Override
	public String toCommandString() {
		return "!BD" + biasSetting + '\n';
	}

	public int getBiasSetting() {
		return biasSetting;
	}

}
