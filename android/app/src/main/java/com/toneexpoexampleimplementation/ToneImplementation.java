package com.toneexpoexampleimplementation;

import android.content.Intent;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.strutbebetter.tonelisten.common.AppConstants;
import com.strutbebetter.tonelisten.models.ToneModel;

public class ToneImplementation {

  public static void handleIntent(Intent intent, ReactContext reactContext){
    if(intent != null) {
      if (intent.getAction() != null) {
        if (intent.getAction().toString().equals(AppConstants.TONE_DETECTED_ACTION)) {
          ToneModel toneModel = new ToneModel(intent.getStringExtra("actionType"), intent.getStringExtra("actionUrl"), "Tone Body");
          responseData(toneModel, reactContext);
        }
      }
    }
  }

  public static void responseData(ToneModel toneModel, ReactContext reactContext){
    WritableMap toneData = new WritableNativeMap();
    toneData.putString("actionType", toneModel.getActionType());
    toneData.putString("actionUrl", toneModel.getActionUrl());
    toneData.putString("body", toneModel.getBody());
    if(reactContext != null){
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("ToneResponse", toneData);
    }
  }
}
