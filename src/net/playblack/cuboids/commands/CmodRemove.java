package net.playblack.cuboids.commands;

import net.playblack.cuboids.gameinterface.CPlayer;
import net.playblack.cuboids.regions.CuboidInterface;

/**
 * Remove a new Cuboid
 * @author Chris
 *
 */
public class CmodRemove extends BaseCommand {

    public CmodRemove() {
        super("Remove a new Cuboid: /cmod <area> remove/delete", 3);
    }

    @Override
    public void execute(CPlayer player, String[] command) {
        if(!parseCommand(player, command)) {
            return;
        }
        CuboidInterface.getInstance().removeCuboid(player, command[1]);
    }
}