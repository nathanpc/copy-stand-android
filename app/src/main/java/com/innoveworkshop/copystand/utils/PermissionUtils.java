package com.innoveworkshop.copystand.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

/**
 * Utility functions to help us out with permissions.
 */
public final class PermissionUtils {
    /**
     * Checks if we have the permission to send notifications.
     *
     * @param context Application's context.
     *
     * @return True if we have permission to send notifications, false otherwise.
     */
    public static boolean hasNotificationPermission(Context context) {
        // Versions of Android lower than 13 didn't need to ask for a runtime permission.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return true;

        // Check if we have the runtime permission.
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the system blocks background clipboard access and there's nothing that we can do
     * about that.
     *
     * @return {@code true} if the Android version is 10+, {@code false} otherwise.
     */
    public static boolean systemBlocksBackgroundClipboardAccess() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
