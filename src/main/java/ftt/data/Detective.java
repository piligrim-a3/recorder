package ftt.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Detective  implements Runnable {

//    public final static String INET_ADDR = "224.0.0.3";
    public final static String INET_ADDR = "239.255.43.21";

    private FTTConnector connector;
    private ActionListener listener;

    private boolean close = false;
    byte[] receiveData = new byte[4096];
    byte[] sendData = new byte[4096];

    InetAddress group;

    ObservableList<Client> clients = FXCollections.observableList(new ArrayList<Client>());

    public Detective(FTTConnector connector) {
        System.out.println("CREATE DETECTIVE...");
        this.connector = connector;
        try {
            System.out.println("CREATE GROUP");
            group = InetAddress.getByName(INET_ADDR);
            System.out.println("CREATE EXECUTORS...");
            ExecutorService executor = Executors.newFixedThreadPool(1);
            System.out.println("SUBMIT...");
            executor.submit(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("RUN... "+connector.getPort());
        try(MulticastSocket multicastSocket = new MulticastSocket(connector.getPort())) { // <- port!
            System.out.println("CREATED SOCKED...");
            multicastSocket.joinGroup(group);
            System.out.println("DETECTIVE: START RECEIVE...");
            while (!close) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                multicastSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                try {
                    System.out.println("DETECTIVE: RECEIVE... ");
                    JSONObject mes = new JSONObject(sentence);
                    System.out.println(mes.toString());
                    int code = mes.getInt("code");
                    if(code == FTTConnector.CODE_FIND) {
                        System.out.println("DETECTIVE: OK.");
                        connector.sendOptions(receivePacket.getPort(),receivePacket.getAddress());
                    } else {
                        System.out.println("DETECTIVE: CANCEL.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("DETECTIVE: ERROR RECEIVE!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR RUN...");
        }
        System.out.println("END ...");
    }


    public void close() {
        close = true;
    }


    public ActionListener getListener() {
        return listener;
    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    public ObservableList<Client> getClients() {
        return clients;
    }
}
