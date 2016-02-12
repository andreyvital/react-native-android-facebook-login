## `react-native-android-facebook-login` [![react-native-android-facebook-login](https://badge.fury.io/js/react-native-android-facebook-login.svg)](https://badge.fury.io/js/react-native-android-facebook-login)
A react-native wrapper around facebook-android-sdk for authentication purposes. This library isn't a complete wrapper around facebook-android-sdk, it will only handle authentication-related process such as retrieving the access token and basic profile information.

### Key features
- Entirely based on promises instead of callbacks;
- Handles both `logInWithPublishPermissions` and `logInWithReadPermissions`;
- Simple JS API. You don't need to consume `NativeModules` directly.

## Usage
This library doesn't provide any custom or native component(s), so the integration will be done entirely using JS API. For example:

```JSX
import React, { Component } from 'react-native'
import { View, Text, TouchableHighlight } from 'react-native'
import FacebookLogin from 'react-native-android-facebook-login'

class LoginWithFacebook extends Component {
  constructor(nativeProps) {
    super(nativeProps)

    this.state = {
      profile: null,
      didCancelled: false
    }
  }

  async getUserProfileAndSetState() {
    const profile = await FacebookLogin.getLoggedInUserProfile()

    if (profile) {
      this.setState({
        profile
      })
    }
  }

  async logIn() {
    const loggedIn = FacebookLogin.isLoggedIn()

    if (loggedIn) {
      this.getUserProfileAndSetState()
      return
    }

    const loginResult = await FacebookLogin.logInWithReadPermissions(['public_profile'])

    if (loginResult.isCancelled) {
      this.setState({
        didCancelled: true
      })

      return
    }

    this.getUserProfileAndSetState()
  }

  render() {
    if (this.state.profile) {
      return (
        <View>
          <Text>I'm logged in as {this.state.profile.name}</Text>
        </View>
      )
    }

    return (
      <View>
        {this.state.didCancelled && (
          <Text>Why?</Text>
        )}
        <TouchableHighlight onPress={this.logIn.bind(this)}>
          <Text>Log in with Facebook</Text>
        </TouchableHighlight>
      </View>
    )
  }
}
```

## Installation
Firstly, you need to download the library from npm:
```
$ npm install --save react-native-android-facebook-login
```

Then you should go to [developers.facebook.com](https://developers.facebook.com) and choose your app, then under **Settings** (*+Add Platform* if you don't have **Android** configured yet) you'll notice that `Key Hashes` will be empty:

![Key Hashes (developers.facebook.com)](https://raw.githubusercontent.com/CentaurWarchief/react-native-android-facebook-login/master/res/1.png)

To get your application key hash to fill in, in your JS code you can call: `__logCurrentApplicationSignature` to log it. For example:

```JS
componentDidMount() {
  if (__DEV__) {
    FacebookLogin.__logCurrentApplicationSignature()
  }
}
```

So, `$ adb logcat -s 'React'` and grab the logged key hash.

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
