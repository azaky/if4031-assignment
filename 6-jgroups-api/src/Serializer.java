import java.io.*;

/**
 * Serializer for byte to object and vice versa
 */
public class Serializer {

    public static byte[] serializeMessage(char topic, Object obj) throws IOException {
        byte[] objBytes = new byte[0];
        if (obj != null) {
            objBytes = serialize(obj);
        }
        byte[] result = new byte[1+objBytes.length];
        result[0] = (byte) topic;
        System.arraycopy(objBytes, 0, result, 1, objBytes.length);
        return result;
    }

    public static Object getObjectMessage(byte[] bytes) throws IOException, ClassNotFoundException {
        byte[] objBytes = new byte[bytes.length-1];
        System.arraycopy(bytes, 1, objBytes, 0, bytes.length-1);
        return deserialize(objBytes);
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        os.writeObject(obj);
        return bs.toByteArray();
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
        ObjectInputStream os = new ObjectInputStream(bs);
        return os.readObject();
    }

}
