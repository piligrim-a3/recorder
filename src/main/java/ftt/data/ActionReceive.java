package ftt.data;

import java.net.InetAddress;

public interface ActionReceive {
    public void receive(InetAddress address, int port, String message);
}
