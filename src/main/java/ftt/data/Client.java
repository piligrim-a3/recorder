package ftt.data;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;

public class Client {

    InetAddress clientAddress;
    int          clientPort;
    UUID uuid;
    String name;

    public Client(Client client) {
        clientAddress = client.getClientAddress();
        clientPort = client.getClientPort();
        uuid = client.getUuid();
        name = client.getName();
    }

    public Client() {
    }

    HashMap<String, Integer> properties = new HashMap<>();

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public HashMap<String, Integer> getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String indexToName(String index) {
        Integer ind = Integer.parseInt(index);
        for(String k: properties.keySet()) {
            if(properties.get(k).equals(ind)) return k;
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Client) {
            return uuid.equals(((Client)obj).getUuid());
        } else return super.equals(obj);
    }
}
