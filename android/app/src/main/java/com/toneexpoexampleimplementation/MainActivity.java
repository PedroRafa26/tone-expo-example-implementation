package com.toneexpoexampleimplementation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.strutbebetter.tonelisten.ToneFramework;
import com.strutbebetter.tonelisten.core.ToneUIEventListener;
import com.strutbebetter.tonelisten.models.ToneModel;
import com.strutbebetter.tonelisten.rn.RNToneImplementation;
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
    setTheme(R.style.AppTheme);
    super.onCreate(null);
    SplashScreen.show(this, SplashScreenImageResizeMode.CONTAIN, ReactRootView.class, false);

    //Here we are going to obtain the reactContext of the application
    getReactNativeHost().getReactInstanceManager().addReactInstanceEventListener(context -> {
      reactContext = context;
      Intent intent = getIntent();
      RNToneImplementation.handleIntent(intent, context);
      mActivity = MainActivity.this;
      //By the moment the apiKey would be a debug one, later you'll need to provide your own key.
      toneFramework = new ToneFramework("apiKeyDebug", MainActivity.this);
      toneFramework.checkPermission(ToneFramework.TONE_PERMISSION_CODE, mActivity);
    });
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
    RNToneImplementation.responseData(reactContext, toneModel);
  }
}
