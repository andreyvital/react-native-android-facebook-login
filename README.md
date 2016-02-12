## `react-native-android-facebook-login`
A react-native wrapper around facebook-android-sdk for authentication purposes.

### Key features
- Entirely based on promises instead of callbacks;
- Handles both `logInWithPublishPermissions` and `logInWithReadPermissions`;
- Simple JS API. You don't need to consume `NativeModules` directly.

## Installation
Firstly, you need to download the library from npm:
```
$ npm install --save react-native-android-facebook-login
```

Then, in your ***android/settings.gradle***:
```
include ':ReactNativeAndroidFacebookLogin'
project(':ReactNativeAndroidFacebookLogin').projectDir = new File(
  rootProject.projectDir,
  '../node_modules/react-native-android-facebook-login/android'
)
```

***android/app/build.gradle***
```
compile project(':ReactNativeAndroidFacebookLogin')
```

***android/app/src/main/AndroidManifest.xml***
```XML
<activity
  android:name="com.facebook.FacebookActivity"
  android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
  android:label="@string/app_name"
  android:theme="@android:style/Theme.Translucent.NoTitleBar"/>

<meta-data
  android:name="com.facebook.sdk.ApplicationId"
  android:value="@string/facebook_app_id" />
```

***android/app/src/main/res/values/strings.xml***
```XML
<string name="facebook_app_id">(...)</string>
```

...and then finally ***MainActivity.java***
```Java
import com.centaurwarchief.facebooklogin.FacebookLoginPackage;
```

```Java
@Override
protected List<ReactPackage> getPackages() {
  return Arrays.<ReactPackage>asList(
    new MainReactPackage(),
    new FacebookLoginPackage(),
    // (....)
  );
}
```
