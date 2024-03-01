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

public class MessageServer {
  private static final String TAG = "RNWiFiTransferFiles";
  private final Executor executor;
  private volatile ServerSocket serverSocket;
  private String ip;

  public MessageServer(String ip) {
    this.executor = Executors.newSingleThreadExecutor();
    this.ip = ip;
  }

  public void start(ReadableMap props, Callback callback) {
    executor.execute(
        () -> {
          try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            Boolean returnMeta = false;
            serverSocket = new ServerSocket(8988, 0, inetAddress);
            Log.i(TAG, "Server: Socket opened");

            if (props != null) {
              Bundle bundle = Arguments.toBundle(props);
              returnMeta = bundle.getBoolean("meta");
            }

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
