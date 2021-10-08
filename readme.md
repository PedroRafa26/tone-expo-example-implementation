
# Tone Framework Expo Integration for Android
This guide will walk you throught the Tone Framework Integration for Android development

## Project Preparation

Create a security commit to have a backup in case of get back to the Expo Managed Workflow 
after switching to the Bare workflow, in order to use this library.
  
## Expo Migration

Once the backup commit is done. Procced to run the following commands in the root of the project.

```bash
  expo run:android
  npm install react-native-community/cli
  npm install
```
To run the project you can follow the steps from the  
[React Native CLI Quickstart](https://reactnative.dev/docs/environment-setup) from the documentation
 
 ## Implementing native side

 - #### Add the framework as a dependency
 

1) Create a folder /libs into project-name/android and put the .aar file in there
#### project-name/android/libs
2) In project-name/build.gradle add the follow code
```
...
//The SDK works with that minSdkVersion
minSdkVersion = 22
...
...
allprojects {
    ...
    repositories {
        mavenLocal()
        flatDir{
            dirs "$rootDir/libs"
        }
    ...
}
```
3) Now project-name/app/build.gradle add the follow code

```
dependencies {
    ...
    //framework
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(name:'tonelisten-release', ext:'aar')

    //dependencies implemented on the framework
    def room_version = "2.3.0"
    implementation 'com.google.android.gms:play-services-location:18.0.0'
    implementation 'androidx.appcompat:appcompat:1.3.1'
    implementation 'com.google.android.material:material:1.4.0'
    testImplementation 'junit:junit:4.+'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
    implementation("com.google.guava:guava:30.1.1-android")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation 'com.google.code.gson:gson:2.8.8'
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-guava:$room_version"
    annotationProcessor "androidx.room:room-compiler:$room_version"
    testImplementation "androidx.room:room-testing:$room_version"
    ...
    implementation "com.facebook.react:react-native:+"  // From node_modules
}
```

- #### Using the framework 
1) Edit android\app\src\main\AndroidManifest.xml

add permissions
```
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
  <uses-permission android:name="android.permission.VIBRATE"/>
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
  <uses-permission android:name="android.permission.RECORD_AUDIO" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

```
#### NOTE: If you already have any of those ignore it and include the others rest

Inside <application> tag add the service and the receiver

```
<application>
  ...
  <service
        android:name="com.strutbebetter.tonelisten.core.ToneServiceManager"
        android:enabled="true"
        android:exported="true"
        android:usesCleartextTraffic="true"
        android:foregroundServiceType="location|microphone"
        android:process=":ToneListeningService">
    </service>
    <receiver android:name="com.strutbebetter.tonelisten.core.ToneBroadcastReceiver"
        android:exported="true">
      <intent-filter>
        <action android:name="com.strutbebetter.tonelisten.broadcast.TONERESPONSE"/>
      </intent-filter>
    </receiver>
</application>
```
and after application tag inside manifest tag add this lines to Linking support

```
<manifest>
  <application>
  ...
  </application>
  <queries>
    <intent>
      <action android:name="android.intent.action.VIEW" />
      <data android:scheme="https"/>
    </intent>
  </queries>
</manifest>
```

2) Edit android\app\src\main\java\com\project-name\MainActivity.java

Add the following imports in the top of the field

```
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
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
```

implements ToneUIEventListener on MainActivity

```
    public class MainActivity extends ReactActivity implements ToneUIEventListener {
        ...
    }
```

Define the following variables

```
  ToneFramework toneFramework;
  private final int TONE_PERMISSION_CODE = 302;
  private Activity mActivity;
  private ReactContext reactContext;
```

In the onCreate method add the follow code to intantiate the framework
```
@Override
  protected void onCreate(Bundle savedInstanceState) {
    ...
    //Here we are going to obtain the reactContext of the application
    ReactInstanceManager reactInstanceManager = getReactNativeHost().getReactInstanceManager();
    ReactApplicationContext reactApplicationContext = (ReactApplicationContext) reactInstanceManager.getCurrentReactContext();
    reactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
      @Override
      public void onReactContextInitialized(ReactContext context) {
        reactContext = context;
        Intent intent = getIntent();
        //This piece of code handle the activity from the notifications
       if(intent != null){
       if(intent.getAction().toString().equals(AppConstants.TONE_DETECTED_ACTION)){
           ToneModel toneModel = new ToneModel(intent.getStringExtra("actionType"),intent.getStringExtra("actionUrl"),  "Tone Body");
           WritableMap toneData = new WritableNativeMap();
           toneData.putString("actionType", toneModel.getActionType());
           toneData.putString("actionUrl", toneModel.getActionUrl());
           toneData.putString("body", toneModel.getBody());
           context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
               .emit("ToneResponse", toneData);
         }
       }
      }
    });

    //Here the service begin
    mActivity = MainActivity.this;
    //By the moment the apiKey: would be a debug one, later you'll need to provide your own key.
    toneFramework = new ToneFramework("apiKeyDebug", MainActivity.this);
    toneFramework.checkPermission(ToneFramework.TONE_PERMISSION_CODE, mActivity);
    ...
  }
```

We just need to implements 2 more Overrides one for handle checkPermissions and the other to create the Bridge and send the response to the frontPage
```
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
    if(reactContext != null){
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("ToneResponse", toneData);
    }
  }
```
## Used By

This project is used by the following companies:

- Company 1
- Company 2

  