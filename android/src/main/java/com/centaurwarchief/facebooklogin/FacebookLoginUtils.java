package com.centaurwarchief.facebooklogin;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;

import java.util.HashSet;
import java.util.Set;

class FacebookLoginUtils {
    public final static Set<String> extractPermissionsFromReadableArray(
        ReadableArray unsafe
    ) {
        HashSet<String> permissions = new HashSet<String>();

        if (unsafe == null) {
            return permissions;
        }

        for (int i = 0; i < unsafe.size(); i += 1) {
            if (unsafe.getType(i) != ReadableType.String) {
                continue;
            }

            permissions.add(unsafe.getString(i));
        }

        return permissions;
    }

    public final static WritableArray translatePermissionsSet(
        Set<String> grantedOrDeclined
    ) {
        WritableArray permissions = new WritableNativeArray();

        for (String permission : grantedOrDeclined) {
            permissions.pushString(permission);
        }

        return permissions;
    }
}
