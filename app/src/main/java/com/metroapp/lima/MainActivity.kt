package com.metroapp.lima

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.metroapp.lima.ui.navigation.AppNavigation
import com.metroapp.lima.ui.theme.MetroAppTheme
import com.metroapp.lima.ui.viewmodel.MetroViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MetroViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission granted or denied handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check if intent launched with extra from notification
        val pendingTxId = intent.getLongExtra("EXTRA_TRANSACTION_ID", -1L)
        if (pendingTxId != -1L) {
            viewModel.confirmPendingTrip(pendingTxId) { _, _ -> }
        }

        setContent {
            MetroAppTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
