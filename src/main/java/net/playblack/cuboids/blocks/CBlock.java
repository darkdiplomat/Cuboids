package net.playblack.cuboids.blocks;

import net.playblack.cuboids.exceptions.DeserializeException;
import net.playblack.mcutils.ToolBox;

/**
 * CBlock is a block that is somewhere in the world. It doesn't know which world
 * it is set in or where, it only knows what it is.
 * 
 * @author chris
 * 
 */
public class CBlock {
    protected short type = 0;
    protected byte data = 0;

    /**
     * Default CTOR
     */
    public CBlock() {
        type = 0;
        data = 0;
    }

    /**
     * Contruct Block with type and data
     * 
     * @param type
     * @param data
     */
    public CBlock(int type, int data) {
        this.type = (short) type;
        this.data = (byte) data;
    }

    /**
     * Construct a block with its type only
     * 
     * @param type
     */
    public CBlock(int type) {
        this.type = (short) type;
    }

    /**
     * Set this blocks type
     * 
     * @param type
     */
    public void setType(int type) {
        this.type = (short) type;
    }

    /**
     * get this blocks type
     * 
     * @return
     */
    public short getType() {
        return type;
    }

    /**
     * set this blocks data
     * 
     * @param data
     */
    public void setData(int data) {
        this.data = (byte) data;
    }

    /**
     * get this blocks data
     * 
     * @return
     */
    public byte getData() {
        return data;
    }

    /**
     * Check if type and data are equal
     * 
     * @param b
     * @return
     */
    public boolean equals(CBlock b) {
        if ((b.getType() == getType()) && (b.getData() == getData())) {
            return true;
        }
        return false;
    }

    /**
     * Check if this block has those numbers
     * 
     * @param type
     * @param data
     * @return
     */
    public boolean equals(short type, byte data) {
        return ((getType() == type) && (getData() == data));
    }

    /**
     * Only check if the type is equal
     * 
     * @param b
     * @return
     */
    public boolean equalsSlack(CBlock b) {
        if ((b.getType() == getType())) {
            return true;
        }
        return false;
    }

    /**
     * Serialize this Block into a StringBuilder. This returns [type,data]
     * 
     * @return
     */
    public StringBuilder serialize() {
        return new StringBuilder().append("[").append(Short.toString(type))
                .append(",").append(Byte.toString(data)).append("]");
    }

    public static CBlock deserialize(String serialized)
            throws DeserializeException {
        serialized = serialized.replace("[", "").replace("]", "");
        CBlock tr = null;
        String[] values = serialized.split(",");
        if (values.length != 2) {
            throw new DeserializeException(
                    "Could not deserialize CBlock object. Invalid serialized data!",
                    serialized);
        }
        short type = Short.parseShort(values[0]);
        byte data = Byte.parseByte(values[1]);
        if (type == 54) {
            tr = new ChestBlock();
            tr.setData(data);
        } else if (type == 63) {
            tr = new SignBlock();
            tr.setData(data);
        } else {
            tr = new CBlock(type, data);
        }
        return tr;
    }

    /**
     * Parse a new block from a string. Syntax: BlockId:Data or only blockId
     * 
     * @param input
     * @return
     */
    public static CBlock parseBlock(String input) {
        String[] split = input.split(":");
        short type = 0;
        byte data = 0;
        if (split.length > 1) {
            type = ToolBox.convertType(split[0]);
            data = ToolBox.convertData(split[1]);
        } else {
            type = ToolBox.convertType(split[0]);
        }
        if ((type == -1) || (data == -1)) {
            return null;
        }
        return new CBlock(type, data);
    }

    /**
     * Explain this block
     * 
     * @return
     */
    public String explain() {
        return "Type: " + getType() + ", Data: " + getData();
    }

}
