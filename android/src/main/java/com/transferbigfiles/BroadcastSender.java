package com.transferbigfiles;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.util.Log;

public class BroadcastSender {
    private static final String TAG = "RNWiFiTransferBigFiles";
    public void sendBroadcast(String message, int port) {
        DatagramSocket socket = null;
        try {
            // Crea un socket DatagramSocket
            socket = new DatagramSocket();

            // Obtén la dirección de broadcast
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            // Convierte el mensaje en bytes
            byte[] data = message.getBytes();
            socket.setBroadcast(true);
            // Crea un DatagramPacket para enviar el mensaje
            DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, port);
            Log.i(TAG, "Sending: ");
            // Envía el DatagramPacket
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
