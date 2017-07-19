package de.tum.nst.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RTCCommand implements RobotCommand {
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	@Override
	public String toCommandString() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return "!T" + sdf.format(cal.getTime()) + "\n";
	}

}
