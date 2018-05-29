package npc;

import condition.Condition;
import condition.Location;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import player.Player;
import server.instruction.Instruction;

/**
 *
 * @author XxOsirisxX
 */
public class NPC {

    private static int messageChainId = 0;
    private final int id;
    private Map<Integer, List<NPCMessage>> messages;

    public NPC(int id) {
        this.id = id;
    }

    public void applyConversation(int flow, Player player, int messageType, String message, byte messageButtons, int selectedOption, List<Instruction> instructions) {
        if (messages == null) {
            messages = new LinkedHashMap<Integer, List<NPCMessage>>();
        }
        if (flow == 0) {
            messageChainId++;
        }
        NPCMessage newMessage = new NPCMessage(messageChainId, player, messageType, message, messageButtons, selectedOption, instructions, null);
        List<NPCMessage> actualMessages = new LinkedList<NPCMessage>();
        if (messages.get(flow) != null) {
            boolean repeated = false;
            for (NPCMessage m : messages.get(flow)) {
                if (m.getMessage().equals(newMessage.getMessage())) {
                    repeated = true;
                    break;
                }
            }
            if (repeated) {
                return;
            }

            actualMessages = messages.get(flow);
            byte conditionType = 0;
            for (int i = 0; i < messages.get(flow).size(); i++) {
                List<Condition> oldCondition;
                NPCMessage storedMessage = messages.get(flow).get(i);
                if (storedMessage.getConditions() != null) {
                    oldCondition = storedMessage.getConditions();
                } else {
                    oldCondition = new LinkedList<Condition>();
                }
                if (storedMessage.getPlayer().getMapId() != player.getMapId()) {
                    conditionType |= 1;
                    boolean repeatedCondition = false;
                    if (storedMessage.getConditions() != null) {
                        for (Condition c : storedMessage.getConditions()) {
                            if (c instanceof Location) {
                                repeatedCondition = true;
                                break;
                            }
                        }
                    }
                    if (repeatedCondition) {
                        continue;
                    }
                    Location location = new Location();
                    location.setMapId(storedMessage.getPlayer().getMapId());
                    oldCondition.add(location);
                    storedMessage.setConditions(oldCondition);
                }
            }
            List<Condition> newCondition = new LinkedList<Condition>();
            if ((conditionType & 0x01) == 0x01) {
                Location location = new Location();
                location.setMapId(player.getMapId());
                newCondition.add(location);
                newMessage.setConditions(newCondition);
            }
        }
        actualMessages.add(newMessage);
        messages.put(flow, actualMessages);
    }

    public void adjustConditions() {
        for (Entry allMessages : messages.entrySet()) {
            List<NPCMessage> npcMessages = (List<NPCMessage>) allMessages.getValue();
            List<NPCMessage> npcMessages2 = (List<NPCMessage>) allMessages.getValue();
            for (NPCMessage message : npcMessages) {
                if (message.getConditions() != null) {
                    int uniqueId = 0;
                    Condition locationCondition = null;
                    for (Condition conditions : message.getConditions()) {
                        if (conditions instanceof Location) {
                            uniqueId = message.getMessageChainId();
                            locationCondition = conditions;
                        }
                    }
                    if (uniqueId > 0) {

                        for (List<NPCMessage> msg : messages.values()) {
                            for (NPCMessage messagex : msg) {
                                if (messagex.getMessageChainId() == uniqueId) {
                                    if (messagex.getConditions() == null) {
                                        messagex.setConditions(message.getConditions());
                                    } else {
                                        List<Condition> conditions = messagex.getConditions();
                                        boolean ignore = false;
                                        for (Condition all : conditions) {
                                            if (all instanceof Location) {
                                                ignore = true;
                                                break;
                                            }
                                        }
                                        if (ignore) {
                                            continue;
                                        }
                                        conditions.add(locationCondition);
                                        messagex.setConditions(conditions);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public Map<Integer, List<NPCMessage>> getMessages() {
        return messages;
    }

}  