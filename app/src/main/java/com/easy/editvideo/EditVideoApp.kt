package com.easy.editvideo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class required by Hilt.
 *
 * @HiltAndroidApp triggers Hilt's code generation, which is mandatory for
 * the entire Hilt dependency graph to function.
 *
 * Registered in AndroidManifest.xml via android:name=".EditVideoApp".
 */
@HiltAndroidApp
class EditVideoApp : Application()
