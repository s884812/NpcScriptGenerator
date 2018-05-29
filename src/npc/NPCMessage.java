package npc;

import condition.Condition;
import java.util.List;
import player.Player;
import server.instruction.Instruction;

/**
 *
 * @author XxOsirisxX
 */
public class NPCMessage {
    
    private int messageChainId;
    private int messageType;
    private int selectedOption;
    private byte messageButtons;
    private String message;
    private List<Instruction> instructions;
    private List<Condition> conditions;
    private Player player;
    
    public NPCMessage(int messageChainId, Player player, int messageType, String message, byte messageButtons, int selectedOption, List<Instruction> instructions, List<Condition> conditions) {
        this.messageChainId = messageChainId;
        this.player = player;
        this.messageType = messageType;
        this.message = message;
        this.messageButtons = messageButtons;
        this.instructions = instructions;
        this.selectedOption = selectedOption;
        this.conditions = conditions;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public byte getMessageButtons() {
        return messageButtons;
    }

    public void setMessageButtons(byte messageButtons) {
        this.messageButtons = messageButtons;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public int getMessageChainId() {
        return messageChainId;
    }

    public void setMessageChainId(int messageChainId) {
        this.messageChainId = messageChainId;
    }
    
}  
