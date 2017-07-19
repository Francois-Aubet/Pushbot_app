package de.tum.nst.model;

/**
 * Created by Francois on 28/03/2017.
 */

public class LaserCommand implements RobotCommand {

    private final boolean on;
    public LaserCommand(boolean on) {
        this.on = !on;
    }


    @Override
    public String toCommandString() {
        return "!PA0=" + (on?"0\n":"1000000\n");
    }


}
