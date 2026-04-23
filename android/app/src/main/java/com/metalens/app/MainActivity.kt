package com.metalens.app

import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.INTERNET
import android.Manifest.permission.RECORD_AUDIO
import android.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import com.meta.wearable.dat.core.types.Permission
import com.meta.wearable.dat.core.types.PermissionStatus
import com.meta.wearable.dat.core.Wearables
import com.metalens.app.ui.navigation.MetaLensApp
import com.metalens.app.ui.theme.MetaLensTheme
import com.metalens.app.wearables.LocalWearablesPermissionRequester
import com.metalens.app.wearables.WearablesViewModel
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MainActivity : ComponentActivity() {
    companion object {
        val PERMISSIONS: Array<String> = arrayOf(BLUETOOTH, BLUETOOTH_CONNECT, INTERNET)
    }

    private val wearablesViewModel: WearablesViewModel by viewModels()

    private var permissionContinuation: CancellableContinuation<PermissionStatus>? = null
    private val permissionMutex = Mutex()
    private val permissionsResultLauncher =
        registerForActivityResult(Wearables.RequestPermissionContract()) { result ->
            val permissionStatus = result.getOrDefault(PermissionStatus.Denied)
            permissionContinuation?.resume(permissionStatus)
            permissionContinuation = null
        }

    // Continuation/callback storage for on-spot microphone permission requests.
    private var recordAudioContinuation: (() -> Unit)? = null
    private val recordPermissionLauncher =
        registerForActivityResult(RequestPermission()) { granted ->
            val cont = recordAudioContinuation
            recordAudioContinuation = null
            if (granted) {
                cont?.invoke()
            } else {
                wearablesViewModel.setRecentError("Microphone permission required to use voice features")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions {
            // Must be called before using any Wearables APIs
            Wearables.initialize(this)
            wearablesViewModel.startMonitoring()
        }

        setContent {
            MetaLensTheme {
                CompositionLocalProvider(
                    LocalWearablesPermissionRequester provides
                        com.metalens.app.wearables.WearablesPermissionRequester(::requestWearablesPermission),
                ) {
                    MetaLensApp()
                }
            }
        }
    }

    /**
     * Request RECORD_AUDIO with a short in-app pre-permission dialog so the user understands why
     * the OS prompt is about to appear. Call this from point-of-use (e.g., when user taps mic).
     * The provided `onGranted` lambda is invoked only if permission is granted.
     */
    fun requestRecordAudio(onGranted: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Microphone access")
            .setMessage("This feature needs access to your microphone to capture voice. Tap Continue to allow.")
            .setPositiveButton("Continue") { _, _ ->
                recordAudioContinuation = onGranted
                recordPermissionLauncher.launch(RECORD_AUDIO)
            }
            .setNegativeButton("Cancel") { _, _ ->
                wearablesViewModel.setRecentError("Microphone permission is required to use voice features")
            }
            .setOnCancelListener {
                wearablesViewModel.setRecentError("Microphone permission is required to use voice features")
            }
            .show()
    }

    private suspend fun requestWearablesPermission(permission: Permission): PermissionStatus {
        return permissionMutex.withLock {
            suspendCancellableCoroutine { continuation ->
                permissionContinuation = continuation
                continuation.invokeOnCancellation { permissionContinuation = null }
                permissionsResultLauncher.launch(permission)
            }
        }
    }

    private fun checkPermissions(onPermissionsGranted: () -> Unit) {
        registerForActivityResult(RequestMultiplePermissions()) { permissionsResult ->
            val granted = permissionsResult.entries.all { it.value }
            if (granted) {
                onPermissionsGranted()
            } else {
                wearablesViewModel.setRecentError(
                    "Allow All Permissions (Bluetooth, Bluetooth Connect, Internet)"
                )
            }
        }.launch(PERMISSIONS)
    }
}

