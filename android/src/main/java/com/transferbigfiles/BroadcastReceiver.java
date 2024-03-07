package com.transferbigfiles;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import com.facebook.react.bridge.Promise;
import android.util.Log;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
public class BroadcastReceiver {
    private DatagramSocket socket;
    private JSONArray dispositivos;
    private Thread thread;
    private static final String TAG = "RNWiFiTransferBigFiles";
    public static String getDeviceIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        // Convertir la dirección IP de entero a String
        String ipAddressString = String.format(
            "%d.%d.%d.%d",
            (ipAddress & 0xff),
            (ipAddress >> 8 & 0xff),
            (ipAddress >> 16 & 0xff),
            (ipAddress >> 24 & 0xff)
        );

        return ipAddressString;
    }
    public static void sendEmit(final ReactApplicationContext reactContext, final String message) {
            reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("WiFi_TransferFiles:DISCOVER_PEER", message);
    }
    public static JSONArray eliminarDuplicados(JSONArray jsonArray, String campo) {
        JSONArray resultadosUnicos = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject elementoActual = jsonArray.getJSONObject(i);
                String valorCampoActual = elementoActual.getString(campo);

                // Verificar si el valor del campo actual ya existe en los resultados únicos
                boolean duplicado = false;
                for (int j = 0; j < resultadosUnicos.length(); j++) {
                    JSONObject elementoUnico = resultadosUnicos.getJSONObject(j);
                    String valorCampoUnico = elementoUnico.getString(campo);
                    if (valorCampoActual.equals(valorCampoUnico)) {
                        duplicado = true;
                        break;
                    }
                }

                // Si no es un duplicado, agregar el elemento al resultado único
                if (!duplicado) {
                  elementoActual.remove("message");
                    resultadosUnicos.put(elementoActual);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return resultadosUnicos;
    }
    public static JSONArray eliminarElemento(JSONArray jsonArray, String campo, String valor) {
        JSONArray jsonArrayActualizado = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject elementoActual = jsonArray.getJSONObject(i);
                String valorCampoActual = elementoActual.getString(campo);

                // Verificar si el valor del campo actual coincide con el valor que deseamos eliminar
                if (!valorCampoActual.equals(valor)) {
                    jsonArrayActualizado.put(elementoActual);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonArrayActualizado;
    }
    public void receiveBroadcast(int port, Callback callback, final ReactApplicationContext reactContext) {
      thread = new Thread(() -> {
        try {
            // Crea un DatagramSocket para escuchar mensajes de broadcast en el puerto especificado
            socket = new DatagramSocket(port);

            // Buffer para almacenar los datos recibidos
            byte[] buffer = new byte[1024];

            // Crea un DatagramPacket para recibir los datos
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Crear un HashSet para almacenar las direcciones IP de los dispositivos que ya han enviado mensajes
            dispositivos = new JSONArray();
            String myIpAddress = getDeviceIpAddress(reactContext.getApplicationContext());

            // Espera a recibir un mensaje de broadcast
            System.out.println("Esperando recibir un mensaje de broadcast...");
            Log.i(TAG, "Esperando recibir un mensaje de broadcast... ");
            while (true) {
              // Bloquea hasta que se recibe un mensaje
              socket.receive(packet);
              Log.i(TAG, "Recibido de broadcast... ");

              // Convierte los datos recibidos en una cadena y muestra el mensaje recibido
              String messageReceived = new String(packet.getData(), 0, packet.getLength());
              String ipAddress = packet.getAddress().getHostAddress();
              JSONObject dispositivo = new JSONObject(messageReceived);
                    dispositivo.put("ipAddress", ipAddress);
              System.out.println("Mensaje recibido: " + messageReceived);
              //socket.close();
              final String message = dispositivo.getString("message");
              Log.i(TAG, "message: " + message);
              if (!myIpAddress.equals(ipAddress)) {
                if (message.startsWith("Peer disconnect")) {
                  dispositivos = eliminarElemento(dispositivos, "ipAddress", ipAddress);
                  sendEmit(reactContext, dispositivos.toString());
                } else {


                    if (message.startsWith("Send me your info")) {
                      callback.invoke(message);
                    } else {
                    // Agregar la dirección IP del dispositivo a la lista de dispositivos conocidos
                      if (message.startsWith("Force refresh")) {
                        dispositivos = new JSONArray();
                      }
                      boolean existe = false;
                      for (int i = 0; i < dispositivos.length(); i++) {
                          JSONObject olddispositivo = dispositivos.getJSONObject(i);
                          if (olddispositivo.getString("ipAddress").equals(ipAddress)) {
                              existe = true;
                              break;
                          }
                      }
                      if (!existe) {
                        dispositivos.put(dispositivo);

                      // Invocar el callback con el mensaje recibido
                        Log.i(TAG, "Mensaje recibido de un dispositivo nuevo: " + message);
                        dispositivos = eliminarDuplicados(dispositivos, "ipAddress");
                        sendEmit(reactContext, dispositivos.toString());
                        callback.invoke("Mensaje recibido de un dispositivo nuevo: " + message);
                      }
                    }
                  }
                }
            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
      });

            // Inicia el hilo para escuchar mensajes
            thread.start();
    }

    public void closeSocket() {
      // Cierra el socket
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
      if (thread != null) {
        thread.interrupt();
      }
      if (dispositivos != null) {
        dispositivos = new JSONArray();
      }
    }
}
