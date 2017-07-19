package de.tum.nst.model;

/**
 * Created by Francois on 18/10/2016.
 */

public class RobotChainCommand implements RobotCommand {

        private String ledstate = "+";

        @Override
        public String toCommandString() {
            return "!C" + ledstate + '\n';
        }

        public RobotChainCommand (boolean a) {
            super();

            if(!a) {
                ledstate = "-";
            }
        }




    }
