package com.transferbigfiles;

import static com.transferbigfiles.Utils.copyBytes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.facebook.react.bridge.Callback;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
  private static final String TAG = "RNWiFiTransferFiles";
  private ReactApplicationContext reactContext;
  private Callback customDefinedCallback;
  private String destination;
  private Context context;
  private String ip;

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

  /**
   * @param context
   * @param callback
   * @param destination
   */
  public FileServerAsyncTask(
    Context context,
      ReactApplicationContext reactContext,
      String destination,
      Callback customDefinedCallback) {
    this.reactContext = reactContext;
    this.destination = destination;
    this.customDefinedCallback = customDefinedCallback;
    this.context = context;
    this.ip = getDeviceIpAddress(reactContext.getApplicationContext());
  }




  @Override
  protected String doInBackground(Void... params) {
    try {
      InetAddress inetAddress = InetAddress.getByName(ip);
      ServerSocket serverSocket = new ServerSocket(5554, 0, inetAddress);
      Log.i(TAG, "Server: Socket opened");
      Socket client = serverSocket.accept();
      Log.i(TAG, "Server: connection done: " + destination);
      final File f = new File(destination);
      Log.i(TAG, "Server: connection done antes del getParent ");
      File dirs = new File(f.getParent());
      Log.i(TAG, "Server: connection done antes del mkdirs ");
      if (!dirs.exists()) dirs.mkdirs();
      Log.i(TAG, "Server: connection done antes del createNewFile ");
      f.createNewFile();
      Log.i(TAG, "Server: copying files " + f.toString());
      InputStream inputstream = client.getInputStream();
      copyBytes(inputstream, new FileOutputStream(f), reactContext);
      serverSocket.close();
      return f.getAbsolutePath();
    } catch (IOException e) {
      Log.e(TAG, "aqui sale el not permited: " + e.getMessage());
      return null;
    }
  }
  /*
   * (non-Javadoc)
   * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
   */
  @Override
  protected void onPostExecute(String result) {
    if (result != null) {
      Log.i(TAG, "File copied - " + result);
      customDefinedCallback.invoke(result);
    }
  }
  /*
   * (non-Javadoc)
   * @see android.os.AsyncTask#onPreExecute()
   */
  @Override
  protected void onPreExecute() {
    Log.i(TAG, "Opening a server socket");
  }
}
