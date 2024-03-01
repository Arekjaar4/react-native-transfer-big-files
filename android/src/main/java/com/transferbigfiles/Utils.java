package com.transferbigfiles;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.util.Timer;
import java.util.TimerTask;

public class Utils {
  public static final String CHARSET = "UTF-8";
  private static final String TAG = "RNWiFiTransferFiles";

  public interface ProgressListener {
        void onProgress(int progress);
    }

  public static void sendEmit(final ReactApplicationContext reactContext, final int progress) {
            reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("WiFi_TransferFiles:COPY_FILE_PROGRESS", Integer.toString(progress));
    }



  public static boolean copyBytes(InputStream inputStream, OutputStream out, ReactApplicationContext reactContext) {
    byte buf[] = new byte[1024];
    int len;
    final int[] progress = {0};
    try {
      Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sendEmit(reactContext, progress[0]); // Envía una actualización de progreso
            }
        };

        // Programa la tarea de actualización de progreso para que se ejecute cada segundo
        timer.schedule(task, 0, 1000);
      while ((len = inputStream.read(buf)) != -1) {
        //final int finalProgress = progress;
        out.write(buf, 0, len);
        progress[0] += len;
        //callback.invoke(Integer.toString(progress));
        /*new Thread(() -> {
        sendEmit(reactContext, finalProgress);
        }).start();*/
        Log.e(TAG, Integer.toString(progress[0]));
      }
      out.close();
      inputStream.close();
      timer.cancel();
    } catch (IOException e) {
      Log.e(TAG, e.getMessage());
      return false;
    }
    return true;
  }

  public static boolean copyBytesNoCallback(InputStream inputStream, OutputStream out) {
    byte buf[] = new byte[1024];
    int len;
    try {
      while ((len = inputStream.read(buf)) != -1) {
        out.write(buf, 0, len);
      }
      out.close();
      inputStream.close();
    } catch (IOException e) {
      Log.e(TAG, e.getMessage());
      return false;
    }
    return true;
  }
}
