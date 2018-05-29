package server;

import condition.Condition;
import condition.Location;
import npc.NPCMessage;
import npc.NPC;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.DecoderException;
import packet.Reader;
import player.Player;
import server.instruction.Instruction;
import server.instruction.Mesos;
import server.instruction.Warp;

/**
 *
 * @author XxOsirisxX
 */
public class NPCScriptGenerator {

    private Player player;
    private Map<Integer, NPC> npcs;
    private int npcMessageFlow;
    private int currentNpcId;
    private boolean npcTalk;
    private boolean npcMoreTalk;
    private boolean readNpc;
    private boolean readPlayer;
    private int instructionType = -1;
    private int selectedOption = -1;

    public static void main(String[] args) {
        new NPCScriptGenerator().start();
    }

    private void start() {
        initiate();
        String filename = "dkpackets.txt";
        Reader read;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            restartAttributes();
            List<Instruction> instructions = new LinkedList<Instruction>();

            while ((line = br.readLine()) != null) {
                if (line.contains("Sent")) {
                    if (line.contains("NPC_TALK ")) {
                        npcTalk = true;
                        npcMessageFlow = 0;
                    } else if (line.contains("NPC_TALK_MORE ") && npcTalk) {
                        npcMoreTalk = true;
                    }
                    continue;
                }
                if (readPlayer) {
                    boolean playerLoggedIn = line.length() > 200;
                    read = new Reader(line);
                    read.skip(2);
                    int channel = read.readInt();
                    int mapId;
                    if (!playerLoggedIn) {
                        read.skip(5);
                        mapId = read.readInt();
                    } else {
                        read.skip(109);
                        mapId = read.readInt();
                    }
                    player = new Player();
                    player.setMapId(mapId);
                    player.setChannel(channel + 1);
                    readPlayer = false;
                }
                if (npcMoreTalk) {
                    read = new Reader(line);
                    read.skip(2);
                    byte previousMessageType = read.readByte();
                    npcTalk = read.readByte() == 1;
                    switch (previousMessageType) {
                        case 0x02: //Text
                            String textInput = read.readMapleString();
                            break;
                        case 0x03: //Numbers
                        case 0x04: //Options
                            int selection = read.readInt();
                            selectedOption = selection;
                            break;
                    }
                    npcMoreTalk = false;
                } else {
                    if (instructionType > -1) {
                        read = new Reader(line);
                        switch (instructionType) {
                            case 0x00: //WARP
                                read.skip(11);
                                int mapId = read.readInt();
                                byte spawnPoint = read.readByte();
                                Warp warp = new Warp();
                                warp.setMapId(mapId);
                                warp.setSpawnPoint(spawnPoint);
                                instructions.add(warp);
                                applyDummyConversation(instructions);
                                instructions = new LinkedList<Instruction>();
                                restartAttributes();
                                break;
                            case 0x01: //SHOW_STATUS_INFO
                                byte type = read.readByte();
                                if (type == 5) { //Mesos
                                    int amount = read.readInt();
                                    Mesos mesos = new Mesos();
                                    mesos.setAmount(amount);
                                    instructions.add(mesos);
                                } else if (type == 0) { //Inventory - Mesos Included

                                }
                                break;
                        }
                        instructionType = -1;
                        continue;
                    }
                    if (!npcTalk) {
                        if (line.contains("Received")) {
                            if (line.contains("WARP_TO_MAP ")) {
                                readPlayer = true;
                            }
                            continue;
                        }
                    }
                    if (npcTalk) {
                        if (line.contains("Received")) {
                            if (line.contains("NPC_TALK ")) {
                                readNpc = true;
                            } else if (line.contains("WARP_TO_MAP ")) {
                                instructionType = 0;
                            } else if (line.contains("SHOW_STATUS_INFO ")) {
                                instructionType = 1;
                            } else if (line.contains("OPEN_NPC_SHOP ")) {
                                restartAttributes();
                            } else if (line.contains("UPDATE_SKILLS")) {
                                restartAttributes();
                            } else if (line.contains("OPEN_STORAGE")) {
                                restartAttributes();
                            }
                            continue;
                        }

                        if (readNpc) {
                            read = new Reader(line);
                            read.skip(3);
                            int npcId = read.readInt();
                            byte messageType = read.readByte();
                            read.skip(1);
                            String message = read.readMapleString();
                            byte messageButtons = 0;
                            if (messageType == 0) {
                                messageButtons |= read.readByte() << 1; //Previous
                                messageButtons |= read.readByte(); //Next
                            }
                            currentNpcId = npcId;
                            NPC npc;
                            if (npcs.containsKey(npcId)) {
                                npc = npcs.get(npcId);
                            } else {
                                npc = new NPC(npcId);
                            }
                            npc.applyConversation(npcMessageFlow, player, messageType, message, messageButtons, selectedOption, instructions);
                            npcs.put(npcId, npc);
                            npcMessageFlow++;
                            instructions = new LinkedList<Instruction>();
                            readNpc = false;
                        }
                    }
                }
            }
            adjustCondition();
            generateScripts();
        } catch (IOException ioe) {
            System.err.println("Error while reading the file " + filename + ".");
        } catch (DecoderException de) {
            System.err.println("Error while trying to decode string in file " + filename + ".");
        }
    }
    
    private void adjustCondition() {
        for (NPC npc : npcs.values()) {
            npc.adjustConditions();
        }
    }

    private void initiate() {
        npcs = new LinkedHashMap<Integer, NPC>();
    }

    private void restartAttributes() {
        npcMessageFlow = 0;
        currentNpcId = 0;
        npcTalk = false;
        npcMoreTalk = false;
        readNpc = false;
        instructionType = -1;
        selectedOption = -1;
        readPlayer = false;
    }

    private void generateScripts() {
        for (NPC npc : npcs.values()) {
            String filename = npc.getId() + ".txt";
            try (final BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {

                StringBuilder sb = new StringBuilder();
                sb.append("status = -1;\r\n\r\n");
                sb.append("function start() {\r\n\t");
                for (int n = 0; n < npc.getMessages().get(0).size(); n++) {
                    List<Condition> conditions = npc.getMessages().get(0).get(n).getConditions();
                    int conditionSize = 0;
                    if (npc.getMessages().get(0).get(n).getConditions() != null) {
                        conditionSize = conditions.size();
                        sb.append(getScriptConditions(conditions));
                    }
                    sb.append(getScriptMessageType(npc.getMessages().get(0).get(n)));
                    for (int x = 0; x < conditionSize; x++) {
                        sb.append("\t}\r\n");
                    }
                }
                if (npc.getMessages().size() == 1) {
                    sb.append("\tcm.dispose();\r\n");
                }
                sb.append("}\r\n\r\n");
                if (npc.getMessages().size() > 1) {
                    sb.append("function action(mode, type, selection) {\r\n");
                    sb.append("\tstatus++;\r\n");
                    sb.append("\tif ((mode == 0 && type == 4) || mode == -1) {\r\n");
                    sb.append("\t\tcm.dispose();\r\n");
                    sb.append("\t\treturn;\r\n");
                    sb.append("\t}\r\n");
                    sb.append("\tif (mode == 0 && type == 0) {\r\n");
                    sb.append("\t\tstatus -= 2;\r\n");
                    sb.append("\t}\r\n");
                    for (int i = 1; i < npc.getMessages().size(); i++) {
                        if (i > 1) {
                            sb.append(" else if (status == ").append(i - 1).append(") {\r\n\t");
                        } else {
                            sb.append("\tif (status == ").append(i - 1).append(") {\r\n\t");
                        }
                        for (int n = 0; n < npc.getMessages().get(i).size(); n++) {
                            NPCMessage data = npc.getMessages().get(i).get(n);
                            if (data.getConditions() != null) {
                                sb.append(getScriptConditions(data.getConditions()));
                            }
                            sb.append(getScriptOptions(data.getSelectedOption()));
                            sb.append("\t");
                            if (data.getSelectedOption() > -1) {
                                sb.append("\t\t");
                            }
                            sb.append(getScriptInstructions(data.getInstructions()));
                            sb.append(getScriptMessageType(data));
                            if (data.getSelectedOption() > -1) {
                                sb.append("\t\t}\r\n");
                            }
                            if (data.getConditions() != null) {
                                for (int x = 0; x < data.getConditions().size(); x++) {
                                    sb.append("\t\t}\r\n");
                                }
                            }
                        }
                        sb.append("\t}");
                    }
                    sb.append("\r\n");
                    sb.append("}");
                }
                bw.append(sb);
                bw.close();
            } catch (IOException ioe) {
                System.err.println("Error while writing the file " + filename + ".");
            }
        }
    }

    private void applyDummyConversation(List<Instruction> instructions) {
        NPC npc;
        if (npcs.containsKey(currentNpcId)) {
            npc = npcs.get(currentNpcId);
        } else {
            npc = new NPC(currentNpcId);
        }
        npc.applyConversation(npcMessageFlow, player, -1, "", (byte) 0, -1, instructions);
    }

    private StringBuilder getScriptConditions(List<Condition> conditions) {
        StringBuilder ret = new StringBuilder();
        for (Condition condition : conditions) {
            if (condition instanceof Location) {
                Location location = (Location) condition;
                ret.append("\tif (cm.getMapId() == ").append(location.getMapId()).append(") {\r\n");
            }
        }
        return ret;
    }

    private StringBuilder getScriptOptions(int selectedOption) {
        StringBuilder ret = new StringBuilder();
        if (selectedOption > -1) {
            ret.append("\tif (selection == ").append(selectedOption).append(") {\r\n");
        }
        return ret;
    }

    private StringBuilder getScriptInstructions(List<Instruction> instructions) {
        StringBuilder ret = new StringBuilder();
        for (Instruction instruction : instructions) {
            if (instruction instanceof Mesos) {
                Mesos mesos = (Mesos) instruction;
                ret.append("\t\tcm.gainMesos(").append(mesos.getAmount()).append(");\r\n");
            } else if (instruction instanceof Warp) {
                Warp warp = (Warp) instruction;
                ret.append("\t\tcm.warp(").append(warp.getMapId()).append(", ").append(warp.getSpawnPoint()).append(");\r\n");
            }
            if (instruction.isDispose()) {
                ret.append("\t\tcm.dispose();\r\n");
            }
        }
        return ret;
    }

    private StringBuilder getScriptMessageType(NPCMessage message) {
        StringBuilder ret = new StringBuilder();
        switch (message.getMessageType()) {
            case 0x00: //Next | Prev | OK
                switch (message.getMessageButtons()) {
                    case 0x00: //OK
                        ret.append("cm.sendOk(\"").append(message.getMessage()).append("\");\r\n");
                        break;
                    case 0x01: //Next
                        ret.append("cm.sendNext(\"").append(message.getMessage()).append("\");\r\n");
                        break;
                    case 0x02: //Previous
                        ret.append("cm.sendPrev(\"").append(message.getMessage()).append("\");\r\n");
                        break;
                    case 0x03: //Next | Previous
                        ret.append("cm.sendNextPrev(\"").append(message.getMessage()).append("\");\r\n");
                        break;
                }
                break;
            case 0x01: //Yes | No
                ret.append("cm.sendYesNo(\"").append(message.getMessage()).append("\");\r\n");
                break;
            case 0x02: //Text
                ret.append("cm.sendGetText(\"").append(message.getMessage()).append("\");\r\n");
                break;
            case 0x03: //Numbers
                ret.append("cm.sendGetNumber(\"").append(message.getMessage()).append("\");\r\n");
                break;
            case 0x04: //Options
                ret.append("cm.sendSimple(\"").append(message.getMessage()).append("\");\r\n");
                break;
            case 0x07: //Style
                ret.append("cm.sendStyle(\"").append(message.getMessage()).append("\");\r\n");
                break;
            case 0x0C: //Accept | Decline
                ret.append("cm.sendAcceptDecline(\"").append(message.getMessage()).append("\");\r\n");
                break;
            case 0x0E: //Locations
                ret.append("cm.sendPlaces();\r\n");
                break;
        }
        return ret;
    }
}  