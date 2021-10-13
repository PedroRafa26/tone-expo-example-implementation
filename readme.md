
# Tone Framework Expo Integration for Android
This guide will walk you throught the Tone Framework Integration for Android with the Expo SDK

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

####WARNING <br>
After migration it'll probably shows an error referent to  _**requireNativeComponent: "RNCSafeAreaProvider"**_ to fix this you can follow [this instructions](https://github.com/react-navigation/react-navigation/issues/8964#issuecomment-754122115)
 
 ## Implementing native side

 - #### Add the framework as a dependency
 

1)  In project-name/build.gradle upgrade the minSdkVersion to 22

``` java 
//The SDK works with this minSdkVersion
minSdkVersion = 22

```
And add the following code inside **repositories**
```
...
allprojects {
    ...
    repositories {
        mavenLocal()
        maven { url 'https://jitpack.io' }
    }
    ...
}
```
3) Now project-name/app/build.gradle add the following code

``` java
dependencies {
    ...
    //framework
    implementation 'com.github.The-TONE-Knows-Inc:framework-core-tone-android:1.8'
    ...
    implementation "com.facebook.react:react-native:+"  // From node_modules
}
```

- #### Using the framework 

1) Edit android\app\src\main\AndroidManifest.xml

add permissions
``` xml
  <!-- Allows internet connection -->
  <uses-permission android:name="android.permission.INTERNET"/>
  <!-- Allows execute foreground services -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
  <!-- Allows access to listen microphone -->
  <uses-permission android:name="android.permission.RECORD_AUDIO" />
  <!-- Allows access to device location -->
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

```

#### NOTE: If you already have any of those ignore it and include the others

Inside **application** tag add the service and the receiver

``` xml
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
*Optional if you use [Linking](https://reactnative.dev/docs/linking)* and under **application** inside **manifest** tag add the **query** tag to [Linking](https://reactnative.dev/docs/linking) support  

``` xml
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

3) Edit android\app\src\main\java\com\project-name\MainActivity.java

Add the following imports in the top of the field

``` java
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;
import com.facebook.react.bridge.ReactContext;
import com.strutbebetter.tonelisten.ToneFramework;
import com.strutbebetter.tonelisten.core.ToneUIEventListener;
import com.strutbebetter.tonelisten.models.ToneModel;
import com.strutbebetter.tonelisten.rn.RNToneImplementation;
```

Implements ToneUIEventListener on MainActivity and import Singleton.

``` java
    public class MainActivity extends ReactActivity implements ToneUIEventListener {
      //Import Tone Framework Singleton
      ToneFramework toneFramework;
      ... 
    }
```
####Frameworks Methods
 
***RNToneImplementation.handleIntent(Intent, ReactContext)*** 
This method look for a response at the background it recieves two params the first one is an `Intent` and the second `ReactContext`

***RNToneImplementation.responseData(ReactContext, ToneModel)***
This method emit an event to the UI listener everytime a tone it's detected. It recieves two params the first one is `ReactContext` and the second a `ToneModel`

Inside the Override _**onCreate**_ method add the follow code to obtain the ReactContext and instantiate the framework.

``` java
@Override
  protected void onCreate(Bundle savedInstanceState) {
    ...
    
    //Here we are going to obtain the reactContext of the application
    getReactNativeHost().getReactInstanceManager().addReactInstanceEventListener(context -> {
      reactContext = context;
      Intent intent = getIntent();      
      RNToneImplementation.handleIntent(intent, context);
      mActivity = MainActivity.this;
      //Intantiation
      toneFramework = new ToneFramework("apiKeyDebug", MainActivity.this);
      toneFramework.checkPermission(ToneFramework.TONE_PERMISSION_CODE, mActivity);
    });
    ...
  }
```

There's just  2 more Overrides to implement, one to handle permission request and the other to create the Bridge and send the response to the UI
``` java
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
   RNToneImplementation.responseData(toneModel, reactContext);
  }
```

MainActivity.java should look similar to [this](https://github.com/PedroRafa26/tone-expo-example-implementation/blob/master/android/app/src/main/java/com/toneexpoexampleimplementation/MainActivity.java)
 ## Receiving the Data 
 Once the native side is ready just need to create a listener to receive the data and handling it properly. Here is an example.

#### Using the useEffect Hook

By using the useEffect hook the app can suscribe to the framework like this
1) Import DeviceEventEmitter
``` js
import {DeviceEventEmitter} from 'react-native';
```
2) Declare the listener and handle the event with a Switch Case 

``` js
useEffect(()=>{
  DeviceEventEmitter.addListener(
    "ToneReponse",
    (event)=>{
      ...
      // Handle response
      switch(){
        switch (event.actionType) {
          case 'image':{
            //Show Image
            break;
          }
          case 'webpage': {
            //Launch Webpage
            break;
          } 
          case 'sms': {
            //Open Message
            break;
          } 
          case 'tel': {
            //Prepare phone call
            break;
          } 
          case 'mail': {
            //Send email
            break;
          } 
          default:
            break;
        }
      }
      ...
    }
  )
})
```
 [Linking from React Native](https://reactnative.dev/docs/linking) it's a good option to handle most events. Look for [`navigation/index.tsx`](https://github.com/PedroRafa26/tone-expo-example-implementation/blob/master/navigation/index.tsx) as an example


The event receive in this listener is a JSON with the following structure:


| Key | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `actionType` | `string` | actions availables are: `image` - `mail` - `sms` - `tel` - `webpage` |
| `actionUrl` | `string` | for `image` and `webpage` returns an url. <br/> for `sms` and `tel` returns a cellphone number<br/>and  `mail` returns an email address.|
| `body` | `string` | It's a complement of the response.|

Example:
```json
{
  "actionType": "image",
  "actionUrl": "https://www.tonedemo.com/TONE/TONEReceived.png",
  "body": "body"
}
```
