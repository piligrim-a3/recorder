package ftt;

import ftt.data.Client;

public class Line {
    Client client;
    String prop;

    public Line(Client client, String prop) {
        this.client = client;
        this.prop = prop;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    @Override
    public String toString() {
        return prop;
    }
}