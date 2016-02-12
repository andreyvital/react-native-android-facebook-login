/* @flow */
import { NativeModules } from 'react-native'

export type FacebookLoginBehavior =
  NativeModules.FacebookLogin.LOGIN_BEHAVIOR_NATIVE_WITH_FEEDBACK |
  NativeModules.FacebookLogin.LOGIN_BEHAVIOR_NATIVE_ONLY |
  NativeModules.FacebookLogin.LOGIN_BEHAVIOR_WEB_ONLY
