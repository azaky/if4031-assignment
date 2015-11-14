import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.Util;

import java.io.*;
import java.util.Stack;

/**
 * Replicated stack across network using JGroups API
 */
public class ReplStack<T> extends ReceiverAdapter {

    private JChannel channel;

    private final Stack<T> stack = new Stack<>();

    public ReplStack(String channelName, String channelConfigPath) throws Exception {
        channel = new JChannel(channelConfigPath); // use the assigned xml config
        connectChannel(channelName);
    }

    public ReplStack(String channelName) throws Exception {
        channel = new JChannel(); // use the default config, udp.xml
        connectChannel(channelName);
    }

    private void connectChannel(String channelName) throws Exception {
        channel.connect(channelName);
        channel.setReceiver(this);

        channel.getState(null, 0); // get the curr. state of stack
    }

    public void push(T obj) throws Exception {
        Message msg = new Message(null, null, Serializer.serializeMessage('p', obj));
        channel.send(msg);
    }

    public T pop() throws Exception {
        T ans = top();
        Message msg = new Message(null, null, Serializer.serializeMessage('o', null));
        channel.send(msg);
        return ans;
    }

    public T top() throws Exception {
        T ans;
        synchronized (stack) {
            ans = stack.peek();
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
        synchronized (stack) {
            if (operationType == 'p') {
                try {
                    T val = (T) Serializer.getObjectMessage(messageBuffer);
                    stack.add(val);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                stack.pop();
            }
        }
    }

    @Override
    public void getState(OutputStream outputStream) throws Exception {
        synchronized (stack) {
            Util.objectToStream(stack, new DataOutputStream(outputStream));
        }
    }

    @Override
    public void setState(InputStream inputStream) throws Exception {
        Stack<T> newStack;
        newStack = (Stack<T>) Util.objectFromStream(new DataInputStream(inputStream));
        synchronized (stack) {
            Stack<T> reversedNewStack = new Stack<T>();
            while (!newStack.empty()) {
                reversedNewStack.add(newStack.pop());
            }
            while (!reversedNewStack.empty()) {
                stack.add(reversedNewStack.pop());
            }
        }
    }
}