package com.transferbigfiles;

import android.os.Bundle;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

/** Created by kiryl on 16.7.18. */
public class WiFiTransferFilesDeviceMapper {


  public WritableMap mapSendFileBundleToReactEntity(Bundle bundle) {
    WritableMap params = Arguments.createMap();

    params.putDouble("time", bundle.getLong("time"));
    params.putString("file", bundle.getString("file"));

    return params;
  }

  public WritableMap mapSendMessageBundleToReactEntity(Bundle bundle) {
    WritableMap params = Arguments.createMap();

    params.putDouble("time", bundle.getLong("time"));
    params.putString("message", bundle.getString("message"));

    return params;
  }
}
