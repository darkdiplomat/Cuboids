package net.playblack.cuboids.commands;

import net.playblack.cuboids.gameinterface.CPlayer;
import net.playblack.cuboids.regions.CuboidInterface;

/**
 * Set welcome and / or farewell messages
 * @author Chris
 *
 */
public class CmodMessages extends BaseCommand {

    private boolean setFarewell = false;
    public CmodMessages(String action) {
        super("Set area message: /cmod <area> "+action+" <Your message (leave blank to remove message)>", 3);
        if(action.equalsIgnoreCase("farewell") || action.equalsIgnoreCase("goodbye")) {
            setFarewell = true;
        }
    }

    @Override
    public void execute(CPlayer player, String[] command) {
        if(!parseCommand(player, command)) {
            return;
        }
        String message = null;
        StringBuilder toMessage = new StringBuilder();
        if(command.length > 3) {
            for(int i = 3; i < command.length; i++) {
                if(i > 3) {
                    toMessage.append(" ");
                }
                toMessage.append(command[i]);
            }
            message = toMessage.toString();
        }
        
        if(setFarewell) {
            CuboidInterface.getInstance().setFarewell(player, command[1], message);
        }
        else {
            CuboidInterface.getInstance().setWelcome(player, command[1], message);
        }
    }
}
