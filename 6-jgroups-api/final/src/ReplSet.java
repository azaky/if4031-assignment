import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.Util;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Replicated set across network using JGroups API
 */
public class ReplSet<T> extends ReceiverAdapter {

    private JChannel channel;

    private final Set<T> set = new HashSet<>();

    public ReplSet(String channelName, String channelConfigPath) throws Exception {
        channel = new JChannel(channelConfigPath); // use the assigned xml config
        connectChannel(channelName);
    }

    public ReplSet(String channelName) throws Exception {
        channel = new JChannel(); // use the default config, udp.xml
        connectChannel(channelName);
    }

    private void connectChannel(String channelName) throws Exception {
        channel.connect(channelName);
        channel.setReceiver(this);

        channel.getState(null, 0); // get the curr. state of stack
    }

    public void add(T obj) throws Exception {
        Message msg = new Message(null, null, Serializer.serializeMessage('a', obj));
        channel.send(msg);
    }

    public boolean remove(T obj) throws Exception {
        boolean ans = contains(obj);
        Message msg = new Message(null, null, Serializer.serializeMessage('r', obj));
        channel.send(msg);
        return ans;
    }

    public boolean contains(T obj) throws Exception {
        boolean ans;
        synchronized (set) {
            ans = set.contains(obj);
        }
        return ans;
    }

    public void close() throws Exception {
        channel.close();
    }

    @Override
    public void receive(Message message) {

        byte[] messageBuffer = message.getBuffer();
        char operationType = (char) messageBuffer[0];
        synchronized (set) {
            try {
                T val = (T) Serializer.getObjectMessage(messageBuffer);
                if (operationType == 'a') {
                    set.add(val);
                } else {
                    set.remove(val);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getState(OutputStream outputStream) throws Exception {
        synchronized (set) {
            Util.objectToStream(set, new DataOutputStream(outputStream));
        }
    }

    @Override
    public void setState(InputStream inputStream) throws Exception {
        Set<T> newSet;
        newSet = (Set<T>) Util.objectFromStream(new DataInputStream(inputStream));
        synchronized (set) {
            set.addAll(newSet);
        }
    }

}
