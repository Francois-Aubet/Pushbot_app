package de.tum.nst.model;

/**
 * Created by Francois on 28/03/2017.
 */

public class BuzzerCommand implements RobotCommand {

    private final boolean on;
    public BuzzerCommand(boolean on) {
        this.on = !on;
    }


    @Override
    public String toCommandString() {
        return "!PB0=" + (on?"0\n":"25000\n");
    }

}
