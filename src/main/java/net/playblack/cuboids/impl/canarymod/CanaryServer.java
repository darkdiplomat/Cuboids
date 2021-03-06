package net.playblack.cuboids.impl.canarymod;

import java.util.ArrayList;
import java.util.HashMap;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.monster.EntityMob;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.world.DimensionType;
import net.playblack.cuboids.InvalidPlayerException;
import net.playblack.cuboids.gameinterface.CMob;
import net.playblack.cuboids.gameinterface.CPlayer;
import net.playblack.cuboids.gameinterface.CServer;
import net.playblack.cuboids.gameinterface.CWorld;

public class CanaryServer extends CServer {

    protected HashMap<String, CWorld> worlds = new HashMap<String, CWorld>(3);
    private HashMap<String, CPlayer> playerList;

    /**
     * DO <b>NOT</b> initialize the server at ANY OTHER place than in the
     * Bootstrapper Constructor!!!!! If you do, you will <b>DIE!</b>
     */
    public CanaryServer() {
        playerList = new HashMap<String, CPlayer>(Canary.getServer().getMaxPlayers());
    }

    @Override
    public CWorld getWorld(String name, int dimension) {
        if (worlds.containsKey(name + dimension)) {
            return worlds.get(name + dimension);
        }
        DimensionType dim = DimensionType.fromId(dimension);

        CanaryWorld world = new CanaryWorld(Canary.getServer().getWorldManager().getWorld(name, dim, false));
        worlds.put(name + dimension, world);
        return world;
    }

    @Override
    public CWorld getWorld(int id) {
        return getWorld(getDefaultWorld().getName(), id);
    }

    @Override
    public CWorld getDefaultWorld() {
        return getWorld(Canary.getServer().getDefaultWorldName(), 0);
    }

    @Override
    public int getDimensionId(String name) {
        return DimensionType.fromName(name).getId();
    }

    @Override
    public ArrayList<CPlayer> getPlayers() {
        ArrayList<CPlayer> players = new ArrayList<CPlayer>(Canary.getServer().getPlayerList().size());
        for(Player p : Canary.getServer().getPlayerList()) {
            //That is to make use of the player instance cache
            try {
                players.add(getPlayer(p.getName()));
            } catch (InvalidPlayerException e) {
                e.printStackTrace();
            }
        }
        return players;
    }

    @Override
    public CPlayer getPlayer(String name) throws InvalidPlayerException {
        if (!playerList.containsKey(name)) {
            Player p = Canary.getServer().matchPlayer(name);
            if (p == null) {
                throw new InvalidPlayerException("Cuboids2 cannot find player with this name: " + name + " (Player offline?)");
            }
            playerList.put(name, new CanaryPlayer(p));
        }
        return playerList.get(name);
    }

    @Override
    public CPlayer refreshPlayer(String name) throws InvalidPlayerException {
        Player p = Canary.getServer().matchPlayer(name);
        if (p == null) {
            throw new InvalidPlayerException("Cuboids2 cannot find player with this name: " + name + " (Player offline?)");
        }
        playerList.remove(name);
        playerList.put(name, new CanaryPlayer(p));
        return playerList.get(name);
    }

    @Override
    public void removePlayer(String player) {
        playerList.remove(player);
    }

    @Override
    public void scheduleTask(long delay, Runnable task) {/* This is unused */}

    @Override
    public void scheduleTask(long delay, long intervall, Runnable task) {/* This is unused */}

    @Override
    public CMob getMob(String name, CWorld world) {
        EntityMob mob = Canary.factory().getEntityFactory().newEntityMob(name);
        mob.setDimension(((CanaryWorld)world).getHandle());
        return new CanaryMob(mob);
    }

    @Override
    public int getItemId(String itemName) {
        ItemType t = ItemType.fromString(itemName);
        if(t != null) {
            return t.getId();
        }
        return -1;
    }

    @Override
    public String getItemName(int id) {
        ItemType t = ItemType.fromId(id);
        if(t != null) {
            return t.getMachineName();
        }
        return null;
    }

    @Override
    public int getPlayersOnline() {
        return Canary.getServer().getPlayerList().size();
    }

    @Override
    public int getMaxPlayers() {
        return Canary.getServer().getMaxPlayers();
    }

}
