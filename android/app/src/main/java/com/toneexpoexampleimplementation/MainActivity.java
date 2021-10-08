package com.toneexpoexampleimplementation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.strutbebetter.tonelisten.ToneFramework;
import com.strutbebetter.tonelisten.common.AppConstants;
import com.strutbebetter.tonelisten.core.ToneUIEventListener;
import com.strutbebetter.tonelisten.models.ToneModel;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

import expo.modules.splashscreen.singletons.SplashScreen;
import expo.modules.splashscreen.SplashScreenImageResizeMode;

public class MainActivity extends ReactActivity implements ToneUIEventListener {

  //Import Tone Framework Singleton
  ToneFramework toneFramework;
  private final int TONE_PERMISSION_CODE = 302;
  private Activity mActivity;
  private ReactContext reactContext;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // Set the theme to AppTheme BEFORE onCreate to support 
    // coloring the background, status bar, and navigation bar.
    // This is required for expo-splash-screen.
    setTheme(R.style.AppTheme);
    super.onCreate(null);
    // SplashScreen.show(...) has to be called after super.onCreate(...)
    // Below line is handled by '@expo/configure-splash-screen' command and it's discouraged to modify it manually
    SplashScreen.show(this, SplashScreenImageResizeMode.CONTAIN, ReactRootView.class, false);

    //Here we are going to obtain the reactContext of the application
    ReactInstanceManager reactInstanceManager = getReactNativeHost().getReactInstanceManager();
    ReactApplicationContext reactApplicationContext = (ReactApplicationContext) reactInstanceManager.getCurrentReactContext();
    reactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
      @Override
      public void onReactContextInitialized(ReactContext context) {
        reactContext = context;
        boolean checkReactContext = (reactContext == null);
        Log.d("InitFramework", String.valueOf(checkReactContext));
        //This piece of code handle the activity from the notification
        Intent intent = getIntent();
        if(intent != null) {
          if (intent.getAction() != null) {
            if (intent.getAction().toString().equals(AppConstants.TONE_DETECTED_ACTION)) {
              ToneModel toneModel = new ToneModel(intent.getStringExtra("actionType"), intent.getStringExtra("actionUrl"), "Tone Body");
              WritableMap toneData = new WritableNativeMap();
              toneData.putString("actionType", toneModel.getActionType());
              toneData.putString("actionUrl", toneModel.getActionUrl());
              toneData.putString("body", toneModel.getBody());
              if (reactContext != null) {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("ToneResponse", toneData);
              }
            }
          }
        }
      }
    });

    //Here the service begin
    mActivity = MainActivity.this;
    //By the moment the apiKey: would be a debug one, later you'll need to provide your own key.
    Log.d("Init Framework", "Tone Framework can start");
    toneFramework = new ToneFramework("apiKeyDebug", MainActivity.this);
    toneFramework.checkPermission(ToneFramework.TONE_PERMISSION_CODE, mActivity);
  }


    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "main";
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected ReactRootView createRootView() {
                return new RNGestureHandlerEnabledRootView(MainActivity.this);
            }
        };
    }

    //This override start the service after permissions request
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == ToneFramework.TONE_PERMISSION_CODE) {
      // Checking whether user granted the permission or not.
      if (grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
        // Start Service
        toneFramework.start();
      } else {
        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
      }
    }
  }

  //This override handle the response from the service with the app open
  @Override
  public void onToneReceived(ToneModel toneModel) {
    WritableMap toneData = new WritableNativeMap();
    toneData.putString("actionType", toneModel.getActionType());
    toneData.putString("actionUrl", toneModel.getActionUrl());
    toneData.putString("body", toneModel.getBody());
    boolean checkReactContext = (reactContext == null);
    Log.d("InitFramework", "verify React Context");
    Log.d("InitFramework", String.valueOf(checkReactContext));
    if(reactContext != null){
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("ToneResponse", toneData);
    }
  }
}
