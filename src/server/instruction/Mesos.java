package server.instruction;

/**
 *
 * @author XxOsirisxX
 */
public class Mesos extends Instruction {
    
    private int amount;
    
    public Mesos() {
        super(false);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
   
}  