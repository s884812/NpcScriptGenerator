package server.instruction;

/**
 *
 * @author XxOsirisxX
 */
public class Warp extends Instruction {
    
    private int mapId;
    private short spawnPoint;
    
    public Warp() {
        super(true);
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public short getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(short spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
}  