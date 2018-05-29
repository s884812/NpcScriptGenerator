package packet;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author XxOsirisxX
 */
public class Reader {
    
    private StringBuilder packetString;
    
    public Reader(String packetString) {
        this.packetString = new StringBuilder(packetString.replace(" ", ""));
    }
    
    public byte readByte() {
        return Byte.parseByte(readString(0, 2), 16);
    }
    
    public short readShort() {
        return Short.parseShort(readString(2, 4) + readString(0, 2), 16);
    }
    
    public int readInt() {
        return (int) Long.parseLong(readString(6, 8) + readString(4, 6) + readString(2, 4) + readString(0, 2), 16);
    }
    
    public long readLong() {
        return Long.parseLong(readString(14, 16) + readString(12, 14) + readString(10, 12) + readString(8, 10) + readString(6, 8) + readString(4, 6) + readString(2, 4) + readString(0, 2), 16);
    }
    
    public String readMapleString() throws DecoderException, UnsupportedEncodingException {
        short messageLenght = readShort();
        return new String(Hex.decodeHex(readString(0, messageLenght * 2).toCharArray()), "UTF8");
    }
    
    public void skip(int pairs) {
        readString(0, pairs * 2);
    }
    
    private String readString(int start, int end) {
        String data = packetString.substring(start, end);
        packetString = packetString.delete(start, end);
        return data;
    }
}  