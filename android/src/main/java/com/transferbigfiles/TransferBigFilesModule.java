package com.transferbigfiles;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.bridge.ReadableMap;
import java.io.File;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import androidx.annotation.Nullable;
import org.json.JSONObject;
import org.json.JSONException;

@ReactModule(name = TransferBigFilesModule.NAME)
public class TransferBigFilesModule extends ReactContextBaseJavaModule {
  private ReactApplicationContext reactContext;
  public static final String NAME = "TransferBigFiles";
  private static final String TAG = "RNWiFiTransferBigFiles";
  private WiFiTransferFilesDeviceMapper mapper = new WiFiTransferFilesDeviceMapper();
  private MessageServer messageServer;

  public TransferBigFilesModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  @ReactMethod
  public void sendFileTo(final String filePath, final String address, final Promise promise) {
    // User has picked a file. Transfer it to group owner i.e peer using FileTransferService
    Uri uri = Uri.fromFile(new File(filePath));
    Log.i(TAG, "Sending: " + uri);
    Log.i(TAG, "Intent----------- " + uri);
    Intent serviceIntent = new Intent(getCurrentActivity(), FileTransferService.class);
    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
    serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, address);
    serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, 5554);
    serviceIntent.putExtra(
        FileTransferService.REQUEST_RECEIVER_EXTRA,
        new ResultReceiver(null) {
          @Override
          protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) { // successful transfer
              promise.resolve(mapper.mapSendFileBundleToReactEntity(resultData));
            } else { // error
              promise.reject(String.valueOf(resultCode), resultData.getString("error"));
            }
          }
        });
    getCurrentActivity().startService(serviceIntent);
  }

  private void sendEvent(String eventName, @Nullable String params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @ReactMethod
  public void receiveFile(String ip,
      String folder, String fileName, final Boolean forceToScanGallery, final Callback callback) {
    final String destination = folder + fileName;
            Callback progressCallback = new Callback() {
                @Override
                public void invoke(Object... args) {
                  Log.e(TAG, (String) args[0]);
                  sendEvent("WiFi_TransferFiles:COPY_FILE_PROGRESS", (String) args[0]);
                    // Maneja el progreso aquí, puedes enviarlo de vuelta a React Native si es necesario
                    // Por ejemplo: callbackfinal.invoke((Integer) args[0]);
                }
            };
              new FileServerAsyncTask(
                      getCurrentActivity(),
                      reactContext,
                      destination,
                      ip,
                      new Callback() {
                        @Override
                        public void invoke(Object... args) {
                          try {
                            JSONObject jsonData = new JSONObject();
                          jsonData.put("filepath", (String) args[0]);
                          jsonData.put("folder", folder);
                          jsonData.put("fileName", fileName);
                          sendEvent("WiFi_TransferFiles:COPY_FILE_END", jsonData.toString());
                          } catch (JSONException e) {
                              // Manejar la excepción aquí
                              e.printStackTrace(); // O cualquier otro método de manejo de excepciones
                          }

                          if (forceToScanGallery) { // fixes:
                            // https://github.com/kirillzyusko/react-native-wifi-p2p/issues/31
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                              final Intent scanIntent =
                                  new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                              final File file = new File(destination);
                              final Uri contentUri = Uri.fromFile(file);
                              scanIntent.setData(contentUri);
                              reactContext.sendBroadcast(scanIntent);
                            } else {
                              final Intent intent =
                                  new Intent(
                                      Intent.ACTION_MEDIA_MOUNTED,
                                      Uri.parse(
                                          "file://" + Environment.getExternalStorageDirectory()));
                              reactContext.sendBroadcast(intent);
                            }
                          }
                        }
                      })
                  .execute();
  }

  @ReactMethod
  public void sendMessageTo(final String message, final String address, final Promise promise) {
    Log.i(TAG, "Sending message: " + message);
    Intent serviceIntent = new Intent(getCurrentActivity(), MessageTransferService.class);
    serviceIntent.setAction(MessageTransferService.ACTION_SEND_MESSAGE);
    serviceIntent.putExtra(MessageTransferService.EXTRAS_DATA, message);
    serviceIntent.putExtra(MessageTransferService.EXTRAS_ADDRESS, address);
    serviceIntent.putExtra(MessageTransferService.EXTRAS_PORT, 8988);
    serviceIntent.putExtra(
        MessageTransferService.REQUEST_RECEIVER_EXTRA,
        new ResultReceiver(null) {
          @Override
          protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) { // successful transfer
              promise.resolve(mapper.mapSendMessageBundleToReactEntity(resultData));
            } else { // error
              promise.reject(String.valueOf(resultCode), resultData.getString("error"));
            }
          }
        });
    getCurrentActivity().startService(serviceIntent);
  }

  @ReactMethod
  public void receiveMessage(final String ip, final ReadableMap props, final Callback callback) {

              if (messageServer == null) {
                messageServer = new MessageServer(ip);
              }
              messageServer.start(props, callback);

  }

  @ReactMethod
  public void stopReceivingMessage() {
    if (messageServer != null) {
      messageServer.stop();
    }
  }
}
