package net.playblack.cuboids.commands;

import net.playblack.cuboids.MessageSystem;
import net.playblack.cuboids.blockoperators.SphereGenerator;
import net.playblack.cuboids.blocks.CBlock;
import net.playblack.cuboids.gameinterface.CPlayer;
import net.playblack.cuboids.selections.CuboidSelection;
import net.playblack.cuboids.selections.SelectionManager;
import net.playblack.mcutils.ToolBox;

/**
 * Create a sphere or ball around a center point
 * @author Chris
 *
 */
public class Csphere extends CBaseCommand {

    
    public Csphere() {
        super("Create a sphere: /csphere <radius> <block>:[data] <hollow>", 3, 4);
    }

    @Override
    public void execute(CPlayer player, String[] command) {
        if(!parseCommand(player, command)) {
            return;
        }
        //Check for the proper permissions
        MessageSystem ms = MessageSystem.getInstance();
        if(!player.hasPermission("cIgnoreRestrictions")) {
            if(!player.hasPermission("cWorldMod")) {
                ms.failMessage(player, "permissionDenied");
                return;
            }
        }
        boolean fill=true;
        if(command.length == 4) {
            fill = false;
        }
        
        //create a new template block
        CBlock material = CBlock.parseBlock(command[2]);
        if(material == null) {
            ms.failMessage(player, "invalidBlock");
            return;
        }
        int radius = ToolBox.parseInt(command[1]);
        if(radius == -1) {
            ms.failMessage(player, "invalidRadius");
            return;
        }
        //prepare the selection
        CuboidSelection template = SelectionManager.getInstance().getPlayerSelection(player.getName());
        if(!template.getBlockList().isEmpty()) {
            template.clearBlocks();
        }
        if(template.getOrigin() == null) {
            ms.failMessage(player, "originNotSet");
            return;
        }
        
        //Create the block generator
        SphereGenerator gen = new SphereGenerator(template, player.getWorld());
        gen.setHollow(fill);
        gen.setMaterial(material);
        gen.setRadius(radius);
        
        if(gen.execute(player, true)) {
            ms.successMessage(player, "sphereCreated");
        }
        else {
            ms.failMessage(player, "sphereNotCreated");
            ms.failMessage(player, "selectionIncomplete");
        }
        return;
    }
}
