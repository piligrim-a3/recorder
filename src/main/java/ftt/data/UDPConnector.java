package ftt.data;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPConnector implements Runnable {

    DatagramSocket serverSocket;
    ActionReceive listener;

    private boolean close = false;
    byte[] receiveData = new byte[4096];
    byte[] sendData;

    public UDPConnector() throws SocketException {
        serverSocket = new DatagramSocket();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this);
    }

    public void run() {
        while(!close) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData(),0,receivePacket.getLength(),"UTF-8");
                if(listener != null)
                    listener.receive(receivePacket.getAddress(), receivePacket.getPort(), sentence);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void send(String message, InetAddress clientAddress, int clientPort) {
        try {
            sendData = message.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        close = true;
        serverSocket.close();
    }


    public ActionReceive getListener() {
        return listener;
    }

    public void setListener(ActionReceive listener) {
        this.listener = listener;
    }
}
