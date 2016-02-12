package com.centaurwarchief.facebooklogin;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import com.facebook.*;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.react.bridge.*;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.common.ReactConstants;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class FacebookLoginModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {
    private static final String NATIVE_MODULE_NAME = "FacebookLogin";

    private static final String LOGIN_BEHAVIOR_NATIVE_WITH_FALLBACK = "LOGIN_BEHAVIOR_NATIVE_WITH_FALLBACK";
    private static final String LOGIN_BEHAVIOR_NATIVE_ONLY = "LOGIN_BEHAVIOR_NATIVE_ONLY";
    private static final String LOGIN_BEHAVIOR_WEB_ONLY = "LOGIN_BEHAVIOR_WEB_ONLY";

    private static final String ERROR_LOG_IN_NO_ACTIVITY = "Tried to `%s.logIn(...)` without being attached to an Activity";
    private static final String ERROR_LOG_IN_MULTIPLE_CALLS = "Tried to call `%s.logIn(...)` multiple times";

    private ReactApplicationContext mContext;
    private FacebookLoginCallback mLoginCallback;
    private CallbackManager mCallbackManager;
    private Queue<Promise> mProfilePromiseQueue;
    private ProfileTracker mProfileTracker;

    public FacebookLoginModule(ReactApplicationContext context) {
        super(context);

        context.addActivityEventListener(this);
        context.addLifecycleEventListener(this);

        FacebookSdk.sdkInitialize(context.getApplicationContext());

        mContext             = context;
        mLoginCallback       = new FacebookLoginCallback();
        mCallbackManager     = CallbackManager.Factory.create();
        mProfileTracker      = new ResolvingPromiseProfileTracker();
        mProfilePromiseQueue = new LinkedList<Promise>();

        mProfileTracker.startTracking();

        LoginManager.getInstance().registerCallback(
            mCallbackManager,
            mLoginCallback
        );
    }

    private class ResolvingPromiseProfileTracker extends ProfileTracker {
        @Override
        protected void onCurrentProfileChanged(Profile old, Profile current) {
            if (mProfilePromiseQueue.isEmpty()) {
                return;
            }

            final WritableMap profileObjectMap = getProfileObjectMap(current);

            while (mProfilePromiseQueue.iterator().hasNext()) {
                mProfilePromiseQueue.element().resolve(profileObjectMap);
                mProfilePromiseQueue.remove();
            }
        }
    }

    private WritableMap getProfileObjectMap(Profile profile) {
        WritableMap profileObjectMap = new WritableNativeMap();

        profileObjectMap.putString("id", profile.getId());
        profileObjectMap.putString("name", profile.getName());
        profileObjectMap.putString("firstName", profile.getFirstName());
        profileObjectMap.putString("middleName", profile.getMiddleName());
        profileObjectMap.putString("lastName", profile.getLastName());
        profileObjectMap.putString("picture", profile.getProfilePictureUri(200, 200).toString());

        return profileObjectMap;
    }

    @Override
    public String getName() {
        return NATIVE_MODULE_NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = MapBuilder.newHashMap();

        constants.put(LOGIN_BEHAVIOR_NATIVE_WITH_FALLBACK, LoginBehavior.NATIVE_WITH_FALLBACK.toString());
        constants.put(LOGIN_BEHAVIOR_NATIVE_ONLY, LoginBehavior.NATIVE_ONLY.toString());
        constants.put(LOGIN_BEHAVIOR_WEB_ONLY, LoginBehavior.WEB_ONLY.toString());

        return constants;
    }

    private void logIn(Promise promise, Set<String> permissions, boolean withReadPermissions) {
        if (mLoginCallback.isAwaitingToRespond()) {
            promise.reject(String.format(ERROR_LOG_IN_MULTIPLE_CALLS, NATIVE_MODULE_NAME));
            return;
        }

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            promise.reject(String.format(ERROR_LOG_IN_NO_ACTIVITY, NATIVE_MODULE_NAME));
            return;
        }

        mLoginCallback.respondsToPromise(promise);

        if (withReadPermissions) {
            LoginManager.getInstance().logInWithReadPermissions(currentActivity, permissions);
            return;
        }

        LoginManager.getInstance().logInWithPublishPermissions(
            currentActivity,
            permissions
        );
    }

    @ReactMethod
    public void changeLoginBehavior(final String toBehavior) {
        LoginBehavior desiredBehavior = null;

        switch (toBehavior) {
            case LOGIN_BEHAVIOR_NATIVE_WITH_FALLBACK:
                desiredBehavior = LoginBehavior.NATIVE_WITH_FALLBACK;
                break;
            case LOGIN_BEHAVIOR_NATIVE_ONLY:
                desiredBehavior = LoginBehavior.NATIVE_ONLY;
                break;
            case LOGIN_BEHAVIOR_WEB_ONLY:
                desiredBehavior = LoginBehavior.WEB_ONLY;
                break;
        }

        if (desiredBehavior != null) {
            LoginManager.getInstance().setLoginBehavior(desiredBehavior);
        }
    }

    @ReactMethod
    public void isLoggedIn(final Promise promise) {
        promise.resolve(AccessToken.getCurrentAccessToken() != null);
    }

    @ReactMethod
    public void tryToRefreshCurrentAccessToken(final Promise promise) {
        if (AccessToken.getCurrentAccessToken() == null) {
            promise.resolve(false);
            return;
        }

        AccessToken.refreshCurrentAccessTokenAsync();
        promise.resolve(true);
    }

    @ReactMethod
    public void getCurrentAccessToken(final Promise promise) {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token == null) {
            promise.resolve(null);
            return;
        }

        WritableMap result = new WritableNativeMap();

        result.putInt("expiresAt", (int) token.getExpires().getTime());

        if ((int) token.getLastRefresh().getTime() > 0) {
            result.putInt(
                "lastRefreshedAt",
                (int) token.getLastRefresh().getTime()
            );
        } else {
            result.putNull("lastRefreshedAt");
        }

        result.putString("userId", token.getUserId());
        result.putString("applicationId", token.getApplicationId());
        result.putString("tokenString", token.getToken());

        result.putArray(
            "grantedPermissions",
            FacebookLoginUtils.translatePermissionsSet(token.getPermissions())
        );

        result.putArray(
            "declinedPermissions",
            FacebookLoginUtils.translatePermissionsSet(token.getDeclinedPermissions())
        );

        promise.resolve(result);
    }

    @ReactMethod
    public void logInWithReadPermissions(
        ReadableArray permissions,
        final Promise promise
    ) {
        logIn(
            promise,
            FacebookLoginUtils.extractPermissionsFromReadableArray(permissions),
            true
        );
    }

    @ReactMethod
    public void logInWithPublishPermissions(
        ReadableArray permissions,
        final Promise promise
    ) {
        logIn(
            promise,
            FacebookLoginUtils.extractPermissionsFromReadableArray(permissions),
            false
        );
    }

    @ReactMethod
    public void getLoggedInUserProfile(final Promise promise) {
        if (AccessToken.getCurrentAccessToken() == null) {
            return;
        }

        Profile currentProfile = Profile.getCurrentProfile();

        if (currentProfile != null) {
            promise.resolve(getProfileObjectMap(currentProfile));
            return;
        }

        mProfilePromiseQueue.offer(promise);
        Profile.fetchProfileForCurrentAccessToken();
    }

    @ReactMethod
    public void logOut() {
        LoginManager.getInstance().logOut();
    }

    @ReactMethod
    public void __logCurrentApplicationSignature() {
        final String applicationSignature
            = FacebookSdk.getApplicationSignature(mContext.getApplicationContext());

        if (applicationSignature == null) {
            return;
        }

        Log.d(
            ReactConstants.TAG,
            Base64.encodeToString(
                Base64.decode(applicationSignature, Base64.NO_PADDING | Base64.URL_SAFE),
                Base64.DEFAULT
            )
        );
    }

    @Override
    public void onHostResume() {
        if (! mProfileTracker.isTracking()) {
            mProfileTracker.startTracking();
        }
    }

    @Override
    public void onHostPause() {
        if (mProfileTracker.isTracking()) {
            mProfileTracker.stopTracking();
        }
    }

    @Override
    public void onHostDestroy() {
    }

    public void onActivityResult(
        final int requestCode,
        final int resultCode,
        final Intent result
    ) {
        mCallbackManager.onActivityResult(requestCode, resultCode, result);
    }
}
