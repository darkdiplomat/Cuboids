package net.playblack.cuboids.commands;

import net.playblack.cuboids.Config;
import net.playblack.cuboids.MessageSystem;
import net.playblack.cuboids.gameinterface.CPlayer;
import net.playblack.cuboids.regions.CuboidE;
import net.playblack.cuboids.regions.CuboidInterface;
import net.playblack.cuboids.selections.CuboidSelection;
import net.playblack.cuboids.selections.SelectionManager;

/**
 * Add a new Cuboid
 * @author Chris
 *
 */
public class CmodAdd extends BaseCommand {

    public CmodAdd() {
        super("Add a new Cuboid: /cmod <area> add/create", 3);
    }

    @Override
    public void execute(CPlayer player, String[] command) {
        if(!parseCommand(player, command)) {
            return;
        }
        MessageSystem ms = MessageSystem.getInstance();
        if(!player.hasPermission("cIgnoreRestrictions")) {
            if(!player.hasPermission("ccreate")) {
                ms.failMessage(player, "permissionDenied");
                return;
            }
        }
        CuboidE defaultC = Config.getInstance().getDefaultCuboidSetting(player);
        CuboidSelection selection = SelectionManager.getInstance().getPlayerSelection(player.getName());
        if(!selection.isComplete()) {
            ms.failMessage(player, "selectionIncomplete");
            return;
        }
        CuboidE cube = new CuboidE(selection.getOrigin(), selection.getOffset());
        cube.overwriteProperties(defaultC);
        cube.addPlayer("o:"+player.getName());
        if(CuboidInterface.getInstance().addCuboid(cube)) {
            ms.successMessage(player, "cuboidCreated");
        }
        else {
            ms.failMessage(player, "cuboidNotCreated");
        }
    }
}