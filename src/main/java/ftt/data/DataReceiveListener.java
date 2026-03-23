package ftt.data;

import java.util.Map;

public interface DataReceiveListener {
    public void receiveData(Client client, Map<String, Object> values);
}
