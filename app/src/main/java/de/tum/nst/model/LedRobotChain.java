package de.tum.nst.model;

/**
 * Created by Francois on 18/10/2016.
 */

public class LedRobotChain implements RobotCommand {

    private String ledstate = "+";

//    @Override
/*    public String toCommandString() {
        return "!CL" + ledstate + '\n';
    }   */

    String command = "!PC=1000\n!PC0=500\n!PC1=500\n";

    @Override
    public String toCommandString() {
        return command;
    }



    public LedRobotChain (boolean a) {
        super();

        if(!a) {
            command = "!CL-\n";
        }
        else{
            command = "!CL+\n";
        }
    }




}
