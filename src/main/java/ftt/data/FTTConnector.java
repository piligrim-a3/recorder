package ftt.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FTTConnector implements ActionReceive, ActionListener, Runnable {

    ArrayList<DataReceiveListener> dataReceiveListeners = new ArrayList<>();
    private ChartDataProvider chartDataProvider;

    public static final int SERVER = 0;
    public static final int CLIENT = 1;

    public static final int CODE_FIND    = 11;
    public static final int CODE_DATA    = 12;
    public static final int CODE_OPTIONS = 13;
    public static final int CODE_SETUP   = 14;
    public static final int CODE_CLOSE   = 15;
    public static final int CODE_LIVE    = 16;
    public static final int CODE_OK      = 17;
    public static final int CODE_REQUEST = 18;
    public static final int CODE_REQUEST_DATA = 19;

    public static final int LIVE_TIMEOUT = 30;

    UDPConnector connector;
    CopyOnWriteArrayList<Client> clients = new CopyOnWriteArrayList();
    CopyOnWriteArrayList<Inspector> inspectors = new CopyOnWriteArrayList<>();
    int  port;
    UUID uuid;
    String name;
    int  type;

    Detective detective;

    HashMap<String, Integer> properties = new HashMap<>();

    public FTTConnector(int type) {
        this("Virtual", type);
    }

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(50);

    public FTTConnector(String name, int type) {
        this.name = name;
        try {
            this.name += " " + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.type = type;
        loadProperties();
        uuid = UUID.randomUUID();
        System.out.println("FTT_CONNECTOR: UUID "+uuid);
        System.out.println("FTT_CONNECTOR: TYPE "+(type==0?"SERVER":"CLIENT"));
        System.out.println("FTT_CONNECTOR: PORT "+port);
        detective = new Detective(this);
        try {
            connector = new UDPConnector();
            connector.setListener(this);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if(type == SERVER) {
            executor.scheduleWithFixedDelay(this, LIVE_TIMEOUT, LIVE_TIMEOUT, TimeUnit.SECONDS);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("del")) {
            for (Client client : clients) {
                if (client.getUuid().equals(e.getSource())) clients.remove(client);
            }
            for (Inspector inspector: inspectors) {
                if(inspector.getUuid().equals(e.getSource())) inspectors.remove(inspector);
            }
        }
    }

    @Override
    public void run() {
        for (Client c: clients) {
            inspectors.add(new Inspector(c.getUuid(),executor,this));
            sendLive(c);
        }
    }

    public HashMap<String, Integer> getProperties() {
        return properties;
    }

    public void addProperties(String prop) {
        properties.put(prop, properties.size());
    }

    public void addDataReceiveListener(DataReceiveListener listener) {
        dataReceiveListeners.add(listener);
    }

    public void removeDataReceiveListener(DataReceiveListener listener) {
        dataReceiveListeners.remove(listener);
    }


    private void loadProperties() {
        File file = new File("server.properties");
        Properties properties = new Properties();
        if(file.isFile()) {
            try(FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
                port = Integer.parseInt(properties.getProperty("port","8888"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            properties.setProperty("port","8888");
            try(FileOutputStream fos = new FileOutputStream(file)) {
                properties.store(fos,"FTT laboratory Project 2015");
            } catch (IOException e) {
                e.printStackTrace();
            }
            port = 8888;
        }
    }

    public void find() {
        JSONObject mes = new JSONObject();
        mes.put("uuid",getUuid().toString());
        mes.put("type",getType());
        mes.put("code", CODE_FIND);
        try {
            connector.send(mes.toString(), InetAddress.getByName(Detective.INET_ADDR),port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setup(Client client, List<String> propList) {
        System.out.println("SETUP CLIENT: "+client);
        HashMap<String, Integer> newMap = new HashMap<>();
        for(String key: propList) {
            newMap.put(key, client.getProperties().get(key));
            System.out.println("SET PROP " + key + " " + client.getProperties().get(key));
        }
        //copy
        Client newClient = new Client(client);
        newClient.getProperties().putAll(newMap);

        boolean find = false;
        for(Client c : getClients()) {
            if(find = newClient.getUuid().equals(c.getUuid())) {
                getClients().set(getClients().indexOf(c),newClient);
                break;
            }
        }
        if(!find) { getClients().add(newClient); };

        JSONObject mes = new JSONObject();
        mes.put("uuid",getUuid().toString());
        mes.put("type",getType());
        mes.put("code", CODE_SETUP);
        mes.put("name",name);
        JSONObject options = new JSONObject();
        for(String key: propList) {
            options.put(key, client.getProperties().get(key));
            System.out.println("ADD PROP " + key);
        }
        mes.put("options",options);
        connector.send(mes.toString(), client.getClientAddress(), client.getClientPort());
    }

    private void sendLive(Client c) {
        JSONObject mes = new JSONObject();
        mes.put("uuid",getUuid().toString());
        mes.put("type",getType());
        mes.put("code",CODE_LIVE);
        mes.put("name",name);
        connector.send(mes.toString(), c.getClientAddress(), c.getClientPort());
    }

    public void sendOptions(int lPort, InetAddress address) {
        JSONObject mes = new JSONObject();
        mes.put("uuid",getUuid().toString());
        mes.put("type",getType());
        mes.put("code",CODE_OPTIONS);
        mes.put("name",name);
        JSONObject options = new JSONObject();
        for(String key: properties.keySet()) {
            options.put(key, properties.get(key));
        }
        mes.put("options",options);
        connector.send(mes.toString(), address, lPort);
    }

    public void send(HashMap<String, Object> message) {
        JSONObject mes = new JSONObject();
        mes.put("uuid", getUuid().toString());
        mes.put("type", getType());
        mes.put("code", CODE_DATA);
        for(Client client: clients) {
            JSONObject data = new JSONObject();
            int count = 0;
            for(String key: message.keySet()) {
                if(client.getProperties().keySet().contains(key)) {
                    count++;
                    Object d = message.get(key);
                    data.put(properties.get(key).toString(),message.get(key));
                }
            }
            if(count == 0) continue;
            mes.put("data",data);
            connector.send(mes.toString(), client.getClientAddress(), client.getClientPort());
        }
    }

    public void send(String key, Object value ) {
        HashMap m = new HashMap();
        m.put(key,value);
        send(m);
    }

    public CopyOnWriteArrayList<Client> getClients() {
        return clients;
    }

    public int getPort() {
        return port;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getType() {
        return type;
    }

    @Override
    public void receive(InetAddress address, int port, String message) {
        try{
            JSONObject mes = new JSONObject(message);
            if(getType() == CLIENT) {
                switch (mes.getInt("code")) {
                    case CODE_OPTIONS:
                        // todo вернуть для того чтобы самописцы не видели друг друга
//                        if(mes.getInt("type") != SERVER) break;
                        System.out.println("CODE_OPTIONS");
                        System.out.println("PORT:   " + port);
                        System.out.println("ADRES: " + address);

                        Client client = createClient(mes);
                        client.setClientAddress(address);
                        client.setClientPort(port);
                        System.out.println("UUID:   " + client.getUuid());
                        for(String key : client.getProperties().keySet()) {
                            System.out.print(" "+key);
                        }
                        System.out.println();
                        boolean find = false;
                        for(Client c : detective.getClients()) {
                            if(find = client.getUuid().equals(c.getUuid())) {
                                detective.getClients().set(detective.getClients().indexOf(c),client);
                                break;
                            }
                        }
                        if(!find) { detective.getClients().add(client); };
                        break;
                    case CODE_DATA:
//                        System.out.println("CODE_DATA");
//                        System.out.println(mes.toString());
                        UUID uuid = UUID.fromString(mes.getString("uuid"));
//                        System.out.println("FROM: "+uuid);
                        Client cl = null;
                        for(Client c : getClients()) {
                            if(uuid.equals(c.getUuid())) {
                                cl = c; break;
                            }
                        }
                        if(cl != null) {
                            JSONObject p = mes.getJSONObject("data");
                            HashMap<String, Object> out = new HashMap<>();
                            for (String key : p.keySet()) {
                                out.put(cl.indexToName(key), p.getDouble(key));
                            }

                            for (DataReceiveListener l : dataReceiveListeners) {
                                l.receiveData(cl, out);
                            }
                        }
                        break;
                    case CODE_LIVE:
//                        System.out.println("CODE_LIVE");
                        UUID uuid2 = UUID.fromString(mes.getString("uuid"));
                        for(Client c : getClients()) {
                            if(uuid2.equals(c.getUuid())) {
                                sendLive(c); //break;
                            }
                        }
                        break;
                    case CODE_REQUEST_DATA:
                        System.out.println("CODE_REQUEST received from client - processing chart data response");
                        System.out.println(mes);
                        if (mes.has("request_id") && "chart_data".equals(mes.getString("request_id"))) {
                            JSONObject response = mes.getJSONObject("response");
                            System.out.println("Received chart data with " + response.getJSONArray("data").length() + " points");
                        }
                        break;
                    case CODE_REQUEST:
                        System.out.println("CODE_REQUEST received from " + address + ":" + port);
                        System.out.println("chartDataProvider: " +chartDataProvider);
                        UUID requestUuid = UUID.fromString(mes.getString("uuid"));
                        Client requestClient = new Client();
                        requestClient.setClientAddress(address);
                        requestClient.setClientPort(port);
                        requestClient.setUuid(requestUuid);
                        if (chartDataProvider != null) {
                            List<Map<String, Object>> chartData = chartDataProvider.getChartData();
                            Map<String, String> metadata = chartDataProvider.getChartMetadata();
                            sendChartData(requestClient, chartData, metadata);
                        } else {
                            sendRequestResponse(requestClient, "chart_data", new JSONObject().put("error", "No chart data provider available"));
                        }
                        break;
                }
            } else if(getType() == SERVER) {
                switch (mes.getInt("code")) {
                    case CODE_SETUP:
                        System.out.println("CODE_SETUP");
                        System.out.println("PORT:   " + port);
                        System.out.println("ADRES: " + address);
                        System.out.println("MESSAGE: " + mes);
                        Client client = createClient(mes);
                        client.setClientAddress(address);
                        client.setClientPort(port);
                        boolean find = false;
                        for(Client c : getClients()) {
                            if(find = client.getUuid().equals(c.getUuid())) {
                                getClients().set(getClients().indexOf(c),client);
                                break;
                            }
                        }
                        if(!find) { getClients().add(client); };
                        break;
                    case CODE_CLOSE:
                        UUID uuid = UUID.fromString(mes.getString("uuid"));
                        Client cl = findClient(uuid);
                        if(cl != null) {
                            getClients().remove(cl);
                        }
                        break;
                    case CODE_LIVE:
//                        System.out.println("CODE_LIVE");
                        UUID uuid2 = UUID.fromString(mes.getString("uuid"));
//                        System.out.println("LIVE FROM "+uuid2);
                        for(Inspector i : inspectors) {
//                            System.out.println(uuid2 + " - " + i.getUuid());
                            if(uuid2.equals(i.getUuid())) {
                                i.close();
//                                System.out.println("CLOSE INSP");
                                inspectors.remove(i);
                                break;
                            }
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Client findClient(UUID uuid) {
        for(Client c : getClients()) {
            if(uuid.equals(c.getUuid())) {
                return c;
            }
        }
        return null;
    }

    private Client createClient(JSONObject mes) {
        HashMap<String, Integer> prop = new HashMap<>();
        JSONObject p = mes.getJSONObject("options");
        for(String key: p.keySet()) {
            prop.put(key, p.getInt(key));
        }
        Client client = new Client();
        client.setUuid(UUID.fromString(mes.getString("uuid")));
        client.setName(mes.getString("name"));
        client.getProperties().putAll(prop);
        return client;
    }


    public Detective getDetective() {
        return detective;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void sendClose(Client client) {
        JSONObject mes = new JSONObject();
        mes.put("uuid",getUuid().toString());
        mes.put("type",getType());
        mes.put("code", CODE_CLOSE);
        connector.send(mes.toString(), client.getClientAddress(), client.getClientPort());
    }

    public void close() {
        for(Client c: getClients()) {
            sendClose(c);
        }
    }

    public void exit() {
        connector.close();
    }


    public void reset() {
        detective.getClients().removeAll(detective.getClients());
        clients.removeAll(clients);
        for(Inspector i: inspectors) {
            i.close();
        }
        inspectors.removeAll(inspectors);
        properties.clear();
    }

    public ChartDataProvider getChartDataProvider() {
        return chartDataProvider;
    }

    public void setChartDataProvider(ChartDataProvider chartDataProvider) {
        System.out.println("setChartDataProvider "+chartDataProvider);
        this.chartDataProvider = chartDataProvider;
    }

    public void sendChartData(Client client, List<Map<String, Object>> chartData, Map<String, String> metadata) {
        if (client == null || chartData == null) return;
        
        Map<String, Object> mes = new HashMap<>();
        mes.put("uuid", getUuid().toString());
        mes.put("type", getType());
        mes.put("code", CODE_REQUEST_DATA);
        mes.put("request_id", "chart_data");

        Map<String, Object> response = new HashMap<>();
        if (metadata != null) {
            response.put("metadata", metadata);
        }

        response.put("data", chartData);
        mes.put("response", response);

        ObjectMapper mapper = new ObjectMapper();

        try {
            connector.send(mapper.writeValueAsString(mes), client.getClientAddress(), client.getClientPort());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRequestResponse(Client client, String requestId, Object data) {
        JSONObject mes = new JSONObject();
        mes.put("uuid", getUuid().toString());
        mes.put("type", getType());
        mes.put("code", CODE_REQUEST_DATA);
        mes.put("request_id", requestId);
        mes.put("response", data);
        connector.send(mes.toString(), client.getClientAddress(), client.getClientPort());
    }

    public void requestChartData(Client client) {
        if (client == null) return;
        
        JSONObject mes = new JSONObject();
        mes.put("uuid", getUuid().toString());
        mes.put("type", getType());
        mes.put("code", CODE_REQUEST);
        mes.put("name", name);
        mes.put("request_type", "chart_data");
        
        connector.send(mes.toString(), client.getClientAddress(), client.getClientPort());
        System.out.println("Chart data request sent to " + client.getName());
    }

}
