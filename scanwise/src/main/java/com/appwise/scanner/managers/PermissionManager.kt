package com.appwise.scanner.managers

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionManager {
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    /**
     * Initialize and setup camera permissions from an activity.
     *
     * This function needs to be executed inside of the [Activity.onCreate] otherwise it will throw
     * an [IllegalStateException].
     *
     */
    fun initPermissionRequests(
        activity: ComponentActivity,
        autoPermissionRequest: Boolean? = true,
        onSuccess: () -> Unit
    ) {
        requestCameraPermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    onSuccess()
                }
            }
        if (autoPermissionRequest == true)
            requestPermission(activity, onSuccess)

    }

    /**
     * Initialize and setup camera permissions from a fragment.
     *
     * This function needs to be executed inside of the [Fragment.onCreate] otherwise it will throw
     * an [IllegalStateException].
     *
     */
    fun initPermissionRequests(
        fragment: Fragment,
        autoPermissionRequest: Boolean? = true,
        onSuccess: () -> Unit
    ) {
        requestCameraPermission =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    onSuccess()
                }
            }
        if (autoPermissionRequest == true)
            requestPermission(fragment.requireActivity(), onSuccess)
    }

    /**
     * Request camera permission from the user.
     *
     * @param activity is the activity that will be used to request the permission.
     */
    fun requestPermission(activity: Activity, onSuccess: () -> Unit) {
        require(::requestCameraPermission.isInitialized) {
            "You need to call initPermissionRequests before requesting permissions."
        }

        if (!hasCameraPermission(activity)) {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        } else {
            onSuccess()
        }
    }

    fun hasCameraPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}