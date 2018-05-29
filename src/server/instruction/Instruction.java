package server.instruction;

/**
 *
 * @author XxOsirisxX
 */
public abstract class Instruction {
    
    private boolean dispose;
    
    public Instruction(boolean dispose) {
        this.dispose = dispose;
    }

    public boolean isDispose() {
        return dispose;
    }
    
} 