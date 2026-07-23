package com.easy.editvideo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public final class MediaPermissionHelper {

    private MediaPermissionHelper() {
    }

    public static final String PERMISSION_READ_MEDIA_VIDEO = "android.permission.READ_MEDIA_VIDEO";

    public static final String PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED";

    public static final String PERMISSION_READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO";

    public static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    @NonNull
    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{
                    PERMISSION_READ_MEDIA_VIDEO,
                    PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED
            };
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            return new String[]{PERMISSION_READ_MEDIA_VIDEO};
        } else {
            return new String[]{PERMISSION_READ_EXTERNAL_STORAGE};
        }
    }

    @NonNull
    public static String[] getRequiredAudioPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{PERMISSION_READ_MEDIA_AUDIO};
        } else {
            return new String[]{PERMISSION_READ_EXTERNAL_STORAGE};
        }
    }

    public static boolean hasVideoReadPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            boolean full = isGranted(context, PERMISSION_READ_MEDIA_VIDEO);
            boolean partial = isGranted(context, PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED);
            return full || partial;
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            return isGranted(context, PERMISSION_READ_MEDIA_VIDEO);
        } else {
            return isGranted(context, PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    public static boolean hasAudioReadPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return isGranted(context, PERMISSION_READ_MEDIA_AUDIO);
        } else {
            return isGranted(context, PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    private static boolean isGranted(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}