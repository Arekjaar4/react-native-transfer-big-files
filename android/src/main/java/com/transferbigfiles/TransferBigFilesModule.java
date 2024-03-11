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
import android.view.WindowManager;
import android.app.UiModeManager;
import android.util.DisplayMetrics;
import android.content.res.Configuration;
import android.provider.Settings;
import org.json.JSONException;
import org.json.JSONObject;

@ReactModule(name = TransferBigFilesModule.NAME)
public class TransferBigFilesModule extends ReactContextBaseJavaModule {
  private ReactApplicationContext reactContext;
  public static final String NAME = "TransferBigFiles";
  private static final String TAG = "RNWiFiTransferBigFiles";
  private WiFiTransferFilesDeviceMapper mapper = new WiFiTransferFilesDeviceMapper();
  private MessageServer messageServer;
  private MessageServer messageServerDiscoverPeers;
  private BroadcastReceiver broadcastReceiver;
  private BroadcastSender broadcastSender;

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
  public void receiveFile(
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
  public void receiveMessage(final Callback callback) {
Log.i(TAG, "receiveMessage ");
              if (messageServer == null) {
                Log.i(TAG, "messageServer ");
                messageServer = new MessageServer(reactContext, 8988);
              }
              Log.i(TAG, "messageServer start");
              messageServer.start(callback);

  }

  @ReactMethod
  public void stopReceivingMessage() {
    if (messageServer != null) {
      messageServer.stop();
    }
  }


  @ReactMethod
  public void discoverPeers(final Promise promise) {
    try {
    broadcastReceiver = new BroadcastReceiver();
    broadcastSender = new BroadcastSender();
    String deviceName = Build.MODEL;
    final String modelName = Build.MODEL;
    final String productName = Build.DEVICE;
    if (Build.VERSION.SDK_INT <= 31) {
          deviceName = Settings.Secure.getString(reactContext.getContentResolver(), "bluetooth_name");
    } else {
          deviceName = Settings.Global.getString(reactContext.getContentResolver(), Settings.Global.DEVICE_NAME);
    }
    final int type = DeviceUtils.getDeviceType(reactContext).JSValue;
    final String deviceNameFinal = deviceName;
    final JSONObject dispositivo = new JSONObject();
    dispositivo.put("deviceName", deviceName);
    dispositivo.put("modelName", modelName);
    dispositivo.put("productName", productName);
    dispositivo.put("type", type);
    dispositivo.put("message", "Send me your info");
    broadcastReceiver.receiveBroadcast(8989, new Callback() {
                        @Override
                        public void invoke(Object... args) {
                          try {
                          if (((String)args[0]).startsWith("Send me your info")) {
                            dispositivo.put("message", "Force refresh");
                            broadcastSender.sendBroadcast(dispositivo.toString(), 8989);
                          } else {
                            dispositivo.put("message", "Send after");
                            broadcastSender.sendBroadcast(dispositivo.toString(), 8989);
                          }
                          } catch (JSONException e) {
                              e.printStackTrace();
                          }

                        }
                      }, reactContext);
    Log.i(TAG, "dispositivo.toString(): " + dispositivo.toString());
    broadcastSender.sendBroadcast(dispositivo.toString(), 8989);
    promise.resolve("Searching...");
    } catch (JSONException e) {
            e.printStackTrace();
        }
    //promise.resolve("type: " + type + " deviceName: " + deviceName + " modelName: " + modelName + " productName: " + productName);
  }

  @ReactMethod
  public void stopDiscoverPeers() {
    if (broadcastReceiver != null) {
      broadcastReceiver.closeSocket();
    }
    if (broadcastSender != null) {
      try {
      final JSONObject dispositivo = new JSONObject();
      dispositivo.put("message", "Peer disconnect");
      broadcastSender.sendBroadcast(dispositivo.toString(), 8989);
      } catch (JSONException e) {
            e.printStackTrace();
        }
    }
  }




  public class DeviceUtils {
    public enum DeviceType {
        UNKNOWN(0),
        PHONE(1),
        TABLET(2),
        DESKTOP(3),
        TV(4);

        private final int JSValue;

        DeviceType(int JSValue) {
            this.JSValue = JSValue;
        }

        public int getJSValue() {
            return JSValue;
        }
    }


    public DeviceType getDeviceType(Context context) {
        // Detect TVs via UI mode (Android TVs) or system features (Fire TV).
        if (context.getApplicationContext().getPackageManager().hasSystemFeature("amazon.hardware.fire_tv")) {
            return DeviceType.TV;
        }

        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiManager != null && uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            return DeviceType.TV;
        }

        DeviceType deviceTypeFromResourceConfiguration = getDeviceTypeFromResourceConfiguration(context);
        if (deviceTypeFromResourceConfiguration != DeviceType.UNKNOWN) {
            return deviceTypeFromResourceConfiguration;
        } else {
            return getDeviceTypeFromPhysicalSize(context);
        }
    }

    private DeviceType getDeviceTypeFromResourceConfiguration(Context context) {
        int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;

        if (smallestScreenWidthDp == Configuration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
            return DeviceType.UNKNOWN;
        } else if (smallestScreenWidthDp >= 600) {
            return DeviceType.TABLET;
        } else {
            return DeviceType.PHONE;
        }
    }

    private DeviceType getDeviceTypeFromPhysicalSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager == null) {
            return DeviceType.UNKNOWN;
        }

        DisplayMetrics metrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
            float densityDpi = context.getResources().getConfiguration().densityDpi;
            double widthInches = windowManager.getCurrentWindowMetrics().getBounds().width() / densityDpi;
            double heightInches = windowManager.getCurrentWindowMetrics().getBounds().height() / densityDpi;
            return calculateDeviceType(widthInches, heightInches);
        } else {
            @SuppressWarnings("deprecation")
            //windowManager.getDefaultDisplay().getRealMetrics(metrics);
            double widthInches = metrics.widthPixels / metrics.xdpi;
            double heightInches = metrics.heightPixels / metrics.ydpi;
            return calculateDeviceType(widthInches, heightInches);
        }
    }

    private DeviceType calculateDeviceType(double widthInches, double heightInches) {
        double diagonalSizeInches = Math.sqrt(Math.pow(widthInches, 2.0) + Math.pow(heightInches, 2.0));

        if (diagonalSizeInches >= 3.0 && diagonalSizeInches <= 6.9) {
            return DeviceType.PHONE;
        } else if (diagonalSizeInches > 6.9 && diagonalSizeInches <= 18.0) {
            return DeviceType.TABLET;
        } else {
            return DeviceType.UNKNOWN;
        }
    }
}

}
