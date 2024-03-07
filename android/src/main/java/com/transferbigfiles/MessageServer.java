package com.transferbigfiles;

import static com.transferbigfiles.Utils.CHARSET;

import android.os.Bundle;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.net.InetAddress;
import com.facebook.react.bridge.ReactApplicationContext;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class MessageServer {
  private static final String TAG = "RNWiFiTransferFiles";
  private final Executor executor;
  private volatile ServerSocket serverSocket;
  private String ip;
  private int port;

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

  public MessageServer(ReactApplicationContext reactContext, int port) {
    this.executor = Executors.newSingleThreadExecutor();
    this.ip = getDeviceIpAddress(reactContext.getApplicationContext());
    this.port = port;
  }

  public void start(Callback callback) {
    executor.execute(
        () -> {
          try {
            Log.i(TAG, "ip: " + ip);
            InetAddress inetAddress = InetAddress.getByName(ip);
            Boolean returnMeta = false;
            serverSocket = new ServerSocket(port, 0, inetAddress);
            Log.i(TAG, "Server: Socket opened");

            /*if (props != null) {
              Bundle bundle = Arguments.toBundle(props);
              returnMeta = bundle.getBoolean("meta");
            }*/

            Socket client = serverSocket.accept();
            String clientAddress = client.getInetAddress().getHostAddress();
            Log.i(TAG, "Server: connection done");

            InputStream inputstream = client.getInputStream();
            String message = convertStreamToString(inputstream);
            client.close();

            if (returnMeta) {
              WritableMap map = Arguments.createMap();
              map.putString("message", message);
              map.putString("fromAddress", clientAddress);
              callback.invoke(map);
            } else {
              callback.invoke(message);
            }

            this.stop();
          } catch (IOException e) {
            Log.e(TAG, e.getMessage());
          }
        });
  }

  public void stop() {
    if (serverSocket != null) {
      try {
        serverSocket.close();
        Log.i(TAG, "Server: Socket closed");
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }
    }
  }

  protected String convertStreamToString(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder(Math.max(16, is.available()));
    char[] tmp = new char[4096];

    try {
      InputStreamReader reader = new InputStreamReader(is, CHARSET);
      for (int cnt; (cnt = reader.read(tmp)) > 0; ) sb.append(tmp, 0, cnt);
    } finally {
      is.close();
    }
    return sb.toString();
  }
}
