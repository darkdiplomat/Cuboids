package net.playblack.cuboids.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.playblack.cuboids.Config;
import net.playblack.cuboids.CuboidSaveTask;
import net.playblack.cuboids.HMobTask;
import net.playblack.cuboids.MessageSystem;
import net.playblack.cuboids.blocks.CBlock;
import net.playblack.cuboids.blocks.CItem;
import net.playblack.cuboids.gameinterface.CPlayer;
import net.playblack.cuboids.gameinterface.CServer;
import net.playblack.cuboids.regions.Region.Status;
import net.playblack.cuboids.selections.CuboidSelection;
import net.playblack.cuboids.selections.SelectionManager;
import net.playblack.mcutils.ColorManager;
import net.playblack.mcutils.Location;

/**
 * Interfaces RegionManager and applies permissions.
 * 
 * @author Chris
 * 
 */
public class CuboidInterface {
    private RegionManager regions;
    private MessageSystem ms;
    private ScheduledExecutorService threadManager = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    public HashMap<String, CItem[]> playerInventories = new HashMap<String, CItem[]>();

    private static CuboidInterface instance = null;

    /**
     * Construct a new CuboidInterface
     */
    private CuboidInterface() {
        regions = RegionManager.get();
        ms = MessageSystem.getInstance();
        Config cfg = Config.getInstance();
        threadManager.scheduleAtFixedRate(new HMobTask(), 20, 20,
                TimeUnit.SECONDS);
        threadManager.scheduleAtFixedRate(new CuboidSaveTask(),
                cfg.getSaveDelay(), cfg.getSaveDelay(), TimeUnit.MINUTES);
    }

    public static CuboidInterface get() {
        if (instance == null) {
            instance = new CuboidInterface();
        }
        return instance;
    }
    
    public ScheduledExecutorService getThreadManager() {
        return threadManager;
    }
    
    public boolean setProperty(CPlayer player, String cubeName, String property, String value) {
        Region.Status state = Region.Status.fromString(value);
        if(state == Status.INVALID_PROPERTY) {
            ms.failMessage(player, "invalidPropertyState");
            return false;
        }
        
        Region rg = (Region)regions.getCuboidByName(cubeName, player.getWorld().getName(), player.getWorld().getDimension());
        if(rg == null) {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return false;
        }
        
        return false;
    }

    /*
     * **************************************************************************************
     * **************************************************************************************
     * AREA SETS FOR WELCOME AND GOODBYE
     * ****************************************
     */

    /**
     * Set the welcome message. Insert null for message to remove the welcome
     * message
     * 
     * @param player
     * @param cuboid
     * @param message
     */
    public void setWelcome(CPlayer player, String cuboid, String message) {
        Region cube = (Region)regions.getCuboidByName(cuboid, player.getWorld().getName(), player.getWorld().getDimension());
        cube.setWelcome(message);
        cube.hasChanged = true;
    }

    /**
     * Set the farewell message. Insert null for message to remove the farewell
     * message
     * 
     * @param player
     * @param cuboid
     * @param message
     */
    public void setFarewell(CPlayer player, String cuboid, String message) {
        Region cube = (Region)regions.getCuboidByName(cuboid, player.getWorld().getName(), player.getWorld().getDimension());
        cube.setFarewell(message);
        cube.hasChanged = true;
        
    }

    /*
     * **************************************************************************************
     * **************************************************************************************
     */
    /**
     * Load a cuboid from file
     * 
     * @param player
     * @param cube
     */
    public void loadCuboid(CPlayer player, String cube) {
        if ((player.hasPermission("cIgnoreRestrictions")) || (player.hasPermission("cAreaMod"))) {
            if (!regions.cuboidExists(cube, player.getWorld().getName(), player.getWorld().getDimension())) {
                ms.failMessage(player, "cuboidNotFoundOnCommand");
                return;
            }
            regions.loadSingle(cube, player.getWorld().getName(), player
                    .getWorld().getDimension());
            ms.successMessage(player, "cuboidLoaded");
        } else {
            ms.failMessage(player, "playerNotOwner");
        }
    }

    /**
     * Save a single cuboid to file
     * 
     * @param player
     * @param cube
     */
    public void saveCuboid(CPlayer player, String cube) {
        // getCuboidByName(cube, player.getWorld().getName(),
        // player.getWorld().getDimension())
        Region cubeNode = (Region)regions.getCuboidByName(cube, player.getWorld().getName(), player.getWorld().getDimension());
        if (cubeNode == null) {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return;
        }
        if ((cubeNode.playerIsOwner(player.getName())) || ((player.hasPermission("cIgnoreRestrictions")) || (player
                        .hasPermission("cAreaMod")))) {
            if (regions.saveSingle(cube, player.getWorld().getName(), player.getWorld().getDimension())) {
                ms.successMessage(player, "cuboidSaved");
            } else {
                ms.failMessage(player, "cuboidNotSaved");
            }
        } else {
            ms.failMessage(player, "playerNotOwner");
        }
    }

    /**
     * Save all cuboids in all worlds and dimensions
     * 
     * @param player
     */
    public void saveAll(CPlayer player) {
        if (player.hasPermission("cIgnoreRestrictions")) {
            regions.save(false, true);
            ms.successMessage(player, "cuboidSavedAll");
        } else {
            ms.failMessage(player, "permissionDenied");
        }
    }

    /**
     * Add a player to a region if he's not inside already
     * 
     * @param player
     * @param location
     * @deprecated Use handleRegionsForPlayer()
     */
    @Deprecated
    public void addPlayerWithin(CPlayer player, Location location) {
        throw new UnsupportedOperationException("addPlayerWithin is deprecated! use handleRegionsForPlayer intead!");
    }
    
    /**
     * Handle the leaving and entering of regions for a specified player.
     * This will update the currentRegion reference in a player, aswell
     * as send welcome/farewell messages and toggle game modes according to
     * the specifications of a region
     * @param player
     * @param from
     * @param to
     */
    public void handleRegionsForPlayer(CPlayer player, Location from, Location to) {
        if(from != null) {
            regions.removePlayerFromRegion(player, from);
        }
        if(to != null) {
            regions.addPlayerToRegions(player, to);
        }
    }

    /**
     * Remove a player from this region.
     * 
     * @param player
     * @param vFrom
     * @param vTo
     * @deprecated Use handleRegionsForPlayer()
     */
    @Deprecated
    public void removePlayerWithin(CPlayer player, Location vFrom, Location vTo) {
        throw new UnsupportedOperationException("removePlayerWithin is deprecated! use handleRegionsForPlayer intead!");
    }

    /*
     * **************************************************************************************
     * **************************************************************************************
     * INTERFACE FOR AREA LOOKUP + CHECKS FOR WHATEVER PERMISSION
     * ***************
     * ***********************************************************************
     * **
     * ************************************************************************
     * ************
     */

    /**
     * Check if item is restricted
     * 
     * @param position
     * @param itemId
     * @return true if restricted, false otherwise
     */
    public boolean itemIsRestricted(Location position, int itemId) {
        Region cube = regions.getActiveCuboidNode(position, true);
        if (cube == null) {
            return false;
        }
        return cube.isItemRestricted(itemId);
    }

    /**
     * Check if that command is on the restricted commands list
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean commandIsRestricted(CPlayer player, String command) {
        // !!!! NOTE: this MUST NOT check for permissions!!!
        // Check that in the implementation!
        // This is to avoid stack overflows!
        // if(player.hasPermission("cIgnoreRestrictions")) {
        // return false;
        // }
        Region cube = regions.getActiveCuboidNode(player.getLocation(), true);
        if (cube == null) {
            return false;
        }
        return cube.commandIsRestricted(command);
    }

    /**
     * Check if a cuboid exists
     * 
     * @param name
     * @param world
     * @return
     * @deprecated use Regionmanager.cuboidExists instead!
     */
    @Deprecated
    public boolean cuboidExist(String name, String world, int dimension) {
        return regions.cuboidExists(name, world, dimension);
    }

    /**
     * Add a cuboid to the tree
     * 
     * @param cube
     * @return
     */
    public boolean addCuboid(Region cube) {
        if (Config.getInstance().isAutoParent()) {
            // Override default with parent settings and make parent stuff
            Region parent = regions.getPossibleParent(cube);
            if (parent != null) {
                cube.setParent(parent);
                cube.putAll(parent.getAllProperties());
                return regions.addRegion(cube);
            }
        }
        return regions.addRegion(cube);
    }

    /**
     * Remove a cuboid from tree
     * 
     * @param player
     * @param cubeName
     * @return
     */
    public boolean removeCuboid(CPlayer player, String cubeName) {
        Region cube = regions.getCuboidByName(cubeName, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube == null) {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return false;
        }

        if (cube.playerIsOwner(player.getName())
                || player.hasPermission("cAreaMod")
                || player.hasPermission("cIgnoreRestrictions")
                || player.hasPermission("cdelete")) {
            regions.removeRegion(cube, false);
            ms.successMessage(player, "cuboidRemoved");
            return true;

        } else {
            ms.failMessage(player, "permissionDenied");
            return false;
        }
    }

    /**
     * Remove a player or group from the cuboid with given name (index 1)
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean disallowEntity(CPlayer player, String[] command) {
        Region cube = regions.getCuboidByName(command[1], player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube == null) {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return false;
        }
        if (cube.playerIsOwner(player.getName())
                || player.hasPermission("cAreaMod")
                || player.hasPermission("cIgnoreRestrictions")) {
            if (!player.hasPermission("callow")) {
                if (!player.hasPermission("cIgnoreRestrictions")) {
                    ms.failMessage(player, "permissionDenied");
                    return false;
                }
            }
            for (int i = 2; i < command.length; i++) {
                if (command[i].startsWith("g:")) {
                    cube.removeGroup(command[i]);
                } else {
                    cube.removePlayer(command[i]);
                }
            }

            regions.updateRegion(cube);
            ms.successMessage(player, "cuboidUpdated");
            return true;

        } else {
            ms.failMessage(player, "cuboidNotUpdated");
            return false;
        }
    }

    /**
     * Allow an entity into the area given with index 1 of command array
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean allowEntity(CPlayer player, String[] command) {
        Region cube = regions.getCuboidByName(command[1], player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName())
                    || player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {

                if (!player.hasPermission("callow")) {
                    if (!player.hasPermission("cIgnoreRestrictions")) {
                        ms.failMessage(player, "permissionDenied");
                        return false;
                    }
                }

                for (int i = 3; i < command.length; i++) {
                    if (command[i].startsWith("g:")) {
                        cube.addGroup(command[i]);
                    } else {
                        cube.addPlayer(command[i]);
                    }
                }
                regions.updateRegion(cube);
                ms.successMessage(player, "cuboidUpdated");
                return true;
            } else {
                ms.failMessage(player, "playerNotOwner");
                return false;
            }
        } else {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return false;
        }
    }

    /**
     * Remove items from the list of restricted items
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean allowItem(CPlayer player, String[] command) {
        Region cube = regions.getCuboidByName(command[1], player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName())
                    || player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {

                for (int i = 3; i < command.length; i++) {
                    cube.removeRestrictedItem(CServer.getServer().getItemId(
                            command[i]));
                }
                regions.updateRegion(cube);
                ms.successMessage(player, "cuboidUpdated");
                return true;
            } else {
                ms.failMessage(player, "playerNotOwner");
                return false;
            }
        } else {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return false;
        }
    }

    public boolean disallowItem(CPlayer player, String[] command) {
        Region cube = regions.getCuboidByName(command[1], player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName())
                    || player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {

                for (int i = 3; i < command.length; i++) {
                    cube.addRestrictedItem(CServer.getServer().getItemId(
                            command[i]));
                }
                regions.updateRegion(cube);
                ms.successMessage(player, "cuboidUpdated");
                return true;
            } else {
                ms.failMessage(player, "playerNotOwner");
                return false;
            }
        } else {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return false;
        }
    }

    /**
     * Put a command on the list of not allowed commands
     * 
     * @param player
     * @param command
     * @param cubeName
     * @return
     */
    public boolean restrictCommand(CPlayer player, String[] command,
            String cubeName) {
        Region cube = regions.getCuboidByName(cubeName, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName())
                    || player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {
                if (!player.hasPermission("ccommand")) {
                    ms.failMessage(player, "permissionDenied");
                    return false;
                }
                for (int i = 3; i < command.length; i++) {
                    cube.addRestrictedCommand(command[i]);
                }
                regions.updateRegion(cube);
                ms.successMessage(player, "cuboidUpdated");
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a command from te command blacklist
     * 
     * @param player
     * @param command
     * @param cubeName
     * @return
     */
    public boolean allowCommand(CPlayer player, String[] command,
            String cubeName) {
        Region cube = regions.getCuboidByName(cubeName, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName())
                    || player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {
                if (!player.hasPermission("ccommand")) {
                    ms.failMessage(player, "permissionDenied");
                    return false;
                }
                for (int i = 3; i < command.length; i++) {
                    cube.removeRestrictedCommand(command[i]);
                }
                regions.updateRegion(cube);
                ms.successMessage(player, "cuboidUpdated");
                return true;
            }
        }
        return false;
    }

    /**
     * Resize or move the cuboid bounding rectangle
     * 
     * @param player
     * @param cuboidName
     * @return
     */
    public boolean resize(CPlayer player, String cuboidName) {
        Region cube = regions.getCuboidByName(cuboidName, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName())
                    || player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {
                if (!player.hasPermission("cIgnoreRestrictions")) {
                    if (!player.hasPermission("cmove")) {
                        if (!player.hasPermission("cAreaMod")) {
                            ms.failMessage(player, "permissionDenied");
                            return false;
                        }
                    }
                }
                CuboidSelection selection = SelectionManager.getInstance()
                        .getPlayerSelection(player.getName());
                if (!selection.isComplete()) {
                    ms.failMessage(player, "selectionIncomplete");
                    return false;
                }
                cube.setBoundingBox(selection.getOrigin(), selection.getOffset());
                cube.hasChanged = true;
                regions.updateRegion(cube);
                regions.autoSortRegions();

                regions.saveSingle(regions.getCuboidByName(cube.getName(),
                        cube.getWorld(), cube.getDimension()));
                ms.successMessage(player, "cuboidMoved");
                return true;
            } else {
                ms.failMessage(player, "playerNotOwner");
                return false;
            }
        } else {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return false;
        }
    }

    public boolean canModifyBlock(CPlayer player, Location position) {
        return false;
        
    }
    /**
     * Set the priority of a cuboid
     * 
     * @param player
     * @param cubeName
     * @param prio
     */
    public void setPriority(CPlayer player, String cubeName, int prio) {
        Region cube = regions.getCuboidByName(cubeName, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName()) || player.hasPermission("cAreaMod") || player.hasPermission("cIgnoreRestrictions")) {
                cube.setPriority(prio);
                regions.updateRegion(cube);
                ms.successMessage(player, "prioritySet");
                return;
            } else {
                ms.failMessage(player, "priorityNotSet");
                ms.failMessage(player, "playerNotOwner");
                return;
            }
        }
        ms.failMessage(player, "cuboidNotFoundOnCommand");
        return;
    }

    public void setParent(CPlayer player, String subject, String parent) {
        Region cube = regions.getCuboidByName(subject, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            if (cube.playerIsOwner(player.getName())
                    || player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {
                Region parentCube = regions
                        .getCuboidByName(parent, player.getWorld().getName(),
                                player.getWorld().getDimension());
                if (parentCube == null) {
                    ms.failMessage(player, "parentNotSet");
                    ms.failMessage(player, "cuboidNotFoundOnCommand");
                    return;
                }

                // Check if the specified parent is a valid parent (parent
                // encloses child 100%)
                if (cube.cuboidIsWithin(parentCube, true)) {
                    cube.setParent(parentCube);
                    if (cube.getPriority() <= parentCube.getPriority()) {
                        cube.setPriority(parentCube.getPriority() + 1);
                    }
                    regions.updateRegion(cube);
                    ms.successMessage(player, "parentSet");
                    return;
                } else {
                    ms.failMessage(player, "notWithinSpecifiedParent");
                    ms.failMessage(player, "parentNotSet");
                    return;
                }
            } else {
                ms.failMessage(player, "playerNotOwner");
                return;
            }
        }
        ms.failMessage(player, "cuboidNotFoundOnCommand");
        return;
    }

    /*
     * **************************************************************************************
     * **************************************************************************************
     * INFORMATION RETRIEVAL
     */

    /**
     * Explain a cuboid. This is called when using the inspector tool
     * 
     * @param player
     * @param position
     */
    public void explainCuboid(CPlayer player, Location position) {
        Region node = regions.getActiveCuboidNode(position, true);

        if (node != null) {
            Region cube = node;
            player.sendMessage(ColorManager.LightGray + "---- "
                    + cube.getName() + " ----");
            if (cube.playerIsAllowed(player.getName(), player.getGroups())) {
                if (cube.playerIsOwner(player.getName())) {
                    player.sendMessage(ColorManager.LightGreen
                            + "You own this area (You can build!)");
                } else {
                    player.sendMessage(ColorManager.LightGray
                            + "You can build in this area");
                }
            } else if (player.hasPermission("cAreaMod")
                    || player.hasPermission("cIgnoreRestrictions")) {
                player.sendMessage(ColorManager.LightGreen
                        + "You can build in this area");
            } else {
                player.sendMessage(ColorManager.Rose
                        + "You can't build in this area");
            }
            String[] flags = cube.getFlagList().split(";");
            player.sendMessage(ColorManager.LightGray + "Flags: ");
                    
            for(String line : flags) {
                player.sendMessage(line);
            }
            player.sendMessage(ColorManager.LightGray + "Players: "
                    + ColorManager.Yellow + cube.getPlayerList());
            player.sendMessage(ColorManager.LightGray + "Groups: "
                    + ColorManager.Yellow + cube.getGroupList());

            String commands = cube.getRestrictedCommands().toString();
            String items = cube.getItemListAsNames();
            if (!commands.isEmpty()) {
                player.sendMessage(ColorManager.LightGray
                        + "Disallowed Commands:");
                player.sendMessage(ColorManager.Yellow + commands);
            }

            if (!items.isEmpty()) {
                player.sendMessage(ColorManager.LightGray + "Disallowed Items:");
                player.sendMessage(ColorManager.Yellow + items);
            }

            if (cube.getParent() == null) {
                player.sendMessage(ColorManager.LightGray + "Parent: "
                        + ColorManager.Yellow + "none");
            } else {
                player.sendMessage(ColorManager.LightGray + "Parent: "
                        + ColorManager.Yellow + cube.getParent());
            }
        } else {
            ms.notification(player, "No cuboid there!");
        }
        CBlock current = player.getWorld().getBlockAt(position);
        MessageSystem.customMessage(player, ColorManager.Blue, "Block: "
                + CServer.getServer().getItemName(current.getType()) + "("
                + current.getType() + ") : " + current.getData());
    }

    /**
     * Explain a cuboid to a player. this is called on command
     * 
     * @param player
     * @param name
     */
    public void explainCuboid(CPlayer player, String name) {
        Region cube = regions.getCuboidByName(name, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube != null) {
            player.sendMessage(ColorManager.LightGray + "---- "
                    + cube.getName() + " ----");
            String[] flags = cube.getFlagList().split(";");
            player.sendMessage(ColorManager.LightGray + "Flags: ");
                    
            for(String line : flags) {
                player.sendMessage(line);
            }
            player.sendMessage(ColorManager.LightGray + "Players: "
                    + ColorManager.Yellow + cube.getPlayerList());
            player.sendMessage(ColorManager.LightGray + "Groups: "
                    + ColorManager.Yellow + cube.getGroupList());
            
            
            String commands = cube.getRestrictedCommands().toString();
            String items = cube.getItemListAsNames();
            if (!commands.isEmpty()) {
                player.sendMessage(ColorManager.LightGray
                        + "Disallowed Commands:");
                player.sendMessage(ColorManager.Yellow + commands);
            }

            if (!items.isEmpty()) {
                player.sendMessage(ColorManager.LightGray + "Disallowed Items:");
                player.sendMessage(ColorManager.Yellow + items);
            }

            player.sendMessage(ColorManager.LightGray + "Priority: "
                    + ColorManager.Yellow + cube.getPriority());
            if (cube.getParent() == null) {
                player.sendMessage(ColorManager.LightGray + "Parent: "
                        + ColorManager.Yellow + "none");
            } else {
                player.sendMessage(ColorManager.LightGray + "Parent: "
                        + ColorManager.Yellow + cube.getParent());
            }
        } else {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
        }
    }

    /**
     * Display a list of cuboids in a given world and dimension
     * 
     * @param player
     * @param page
     */
    public void displayCuboidList(CPlayer player, int page) {
        String world = player.getWorld().getName();
        int dimension = player.getWorld().getDimension();
        String dimName = player.getWorld().dimensionFromId(dimension);

        int perPage = 10, maxPages = 0, amount = 0;
        ArrayList<Region> cuboids = regions.getAllInDimension(world,
                dimension);
        // Following is all taken from CuboidPlugin
        // Because I suck at making paging
        if (cuboids == null || cuboids.isEmpty()) {
            MessageSystem.customMessage(player, ColorManager.LightGray,
                    "No cuboids for world " + world + " in Dimension "
                            + dimName);
            return;
        }
        maxPages = (int) Math.ceil(cuboids.size() / perPage);
        if ((cuboids.size() % perPage) > 0) {
            maxPages++;
        }
        if (page > maxPages) {
            page = 1;
        }
        amount = (page - 1) * perPage;
        MessageSystem.customMessage(player, ColorManager.Yellow,
                "Cuboid Nodes (" + dimName + " in " + world + ") Page " + page
                        + " from " + maxPages);
        for (int i = amount; i < (amount + perPage); i++) {
            if (cuboids.size() <= i) {
                break;
            }
            Region cuboid = cuboids.get(i);
            MessageSystem.customMessage(
                    player,
                    ColorManager.LightGray,
                    cuboid.getName() + " : " + ColorManager.LightGreen
                            + cuboid.getFlagList());
        }
    }

    public void showCommandBlacklist(CPlayer player, String cubeName) {
        Region cube = regions.getCuboidByName(cubeName, player.getWorld()
                .getName(), player.getWorld().getDimension());
        if (cube == null) {
            ms.failMessage(player, "cuboidNotFoundOnCommand");
            return;
        }
        if (!player.hasPermission("cIgnoreRestrictions")) {
            if (!(cube.playerIsOwner(player.getName()) || player
                    .hasPermission("cAreaMod"))) {
                ms.failMessage(player, "permissionDenied");
                return;
            }
        }
        player.sendMessage(ColorManager.LightGreen + "Restricted Commands for "
                + cube.getName());
        player.sendMessage(ColorManager.LightGray
                + cube.getRestrictedCommands().toString());
    }

    public void killTasks() {
        threadManager.shutdown();

    }
}