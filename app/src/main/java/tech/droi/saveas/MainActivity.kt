package tech.droi.saveas

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import tech.droi.saveas.ui.theme.SaveAsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            performAction()
        } else {
            showPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED ->
                performAction()
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ->
                showInContextUI()
            else -> requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun performAction() {
        setContent {
            SaveAsTheme {
                var fabAction by remember { mutableStateOf<(() -> Unit)?>(null) }
                Scaffold(
                    floatingActionButton = {
                        if (fabAction != null) {
                            FloatingActionButton(onClick = { fabAction?.invoke() }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    SaveScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSetFabAction = { action ->
                            fabAction = action
                            Log.i(TAG, fabAction.toString())
                        }
                    )
                }
            }
        }
    }

    private fun showInContextUI() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage(getString(R.string.message_permission_request))
            .setTitle(getString(R.string.title_permission))
            .setPositiveButton(R.string.request) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            .setNegativeButton(R.string.refuse) { _, _ ->
                finishAffinity()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showPermissionDenied() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage(getString(R.string.message_permission_denied))
            .setTitle(getString(R.string.title_permission))
            .setPositiveButton(R.string.ok) { _, _ ->
                finishAffinity()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    companion object {
        const val TAG = "SaaS"
    }
}

@Preview(showBackground = true)
@Composable
fun SaveScreenPreview() {
    SaveAsTheme {
        SaveScreen(onSetFabAction = {})
    }
}
