/* @flow */
import type { AccessToken } from './AccessToken'
import type { FacebookLoginBehavior } from './FacebookLoginBehavior'
import type { LoginResult } from './LoginResult'
import type { UserProfile } from './UserProfile'
import { NativeModules } from 'react-native'

export default {
  LOGIN_BEHAVIOR: {
    NATIVE_WITH_FEEDBACK: NativeModules.FacebookLogin.LOGIN_BEHAVIOR_NATIVE_WITH_FEEDBACK,
    NATIVE_ONLY: NativeModules.FacebookLogin.LOGIN_BEHAVIOR_NATIVE_ONLY,
    WEB_ONLY: NativeModules.FacebookLogin.LOGIN_BEHAVIOR_WEB_ONLY
  },

  changeLoginBehavior(
    toBehavior: FacebookLoginBehavior
  ): void {
    NativeModules.FacebookLogin.changeLoginBehavior(toBehavior)
  },

  isLoggedIn(): Promise<boolean> {
    return NativeModules.FacebookLogin.isLoggedIn()
  },

  refreshCurrentAccessToken(): Promise<boolean> {
    return NativeModules.FacebookLogin.tryToRefreshCurrentAccessToken()
  },

  getCurrentAccessToken(): Promise<?AccessToken> {
    return NativeModules.FacebookLogin.getCurrentAccessToken()
  },

  getLoggedInUserProfile(): Promise<?UserProfile> {
    return NativeModules.FacebookLogin.getCurrentLoggedInUserProfile()
  },

  logInWithReadPermissions(
    permissions: Array<string>
  ): Promise<LoginResult> {
    return NativeModules.FacebookLogin.logInWithReadPermissions(
      permissions
    )
  },

  logInWithPublishPermissions(
    permissions: Array<string>
  ): Promise<LoginResult> {
    return NativeModules.FacebookLogin.logInWithPublishPermissions(
      permissions
    )
  },

  logOut(): void {
    NativeModules.FacebookLogin.logOut()
  }

  __logCurrentApplicationSignature(): void {
    NativeModules.FacebookLogin.__logCurrentApplicationSignature()
  }
}
