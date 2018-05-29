package condition;

/**
 *
 * @author XxOsirisxX
 */
public class Location extends Condition {
    
    private int mapId;
    private short xposition;
    private short yposition;

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public short getXposition() {
        return xposition;
    }

    public void setXposition(short xposition) {
        this.xposition = xposition;
    }

    public short getYposition() {
        return yposition;
    }

    public void setYposition(short yposition) {
        this.yposition = yposition;
    }
    
} 