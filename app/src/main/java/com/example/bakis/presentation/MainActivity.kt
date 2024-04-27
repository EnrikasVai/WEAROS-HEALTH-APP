package com.example.bakis.presentation

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.MaterialTheme
import com.example.bakis.presentation.theme.WATCHAPPTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private val viewModel: FitnessViewModel by viewModels {
        FitnessViewModelFactory(application)
    }

    private val fitnessOptions: FitnessOptions by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
            .build()
    }

    // Handling the result of Google Fit permissions request
    private val googleFitPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // TODO: Access Google Fit data here or indicate the user that the app is ready
                Toast.makeText(this, "Permissions granted, ready to access Google Fit data.", Toast.LENGTH_LONG).show()
                setupContent()
            } else {
                // TODO: Inform the user about the necessity of Google Fit permissions for functionality
                Toast.makeText(this, "Permissions denied, the app needs these permissions to function.", Toast.LENGTH_LONG).show()
                requestGoogleFitPermissions()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("EXIT", false)) {
            finish()
            return
        }
        // Check if permissions are already granted
        if (checkGoogleFitPermissionGranted()) {
            setupContent()
        } else {
            // Request Google Fit permissions
            requestGoogleFitPermissions()
        }
    }
    private fun setupContent() {
        setContent {
            WATCHAPPTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    val navController = rememberNavController()
                    SetupNavigation(navController, viewModel)
                }
            }
        }
    }
    private fun checkGoogleFitPermissionGranted(): Boolean {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    fun requestGoogleFitPermissions() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Or any other options you need
            .addExtension(fitnessOptions) // Important for Google Fit
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            googleFitPermissionLauncher.launch(googleSignInClient.signInIntent)
        } else {
            // TODO: Access Google Fit data here or indicate the user that the app is ready
            Toast.makeText(this, "Permissions already granted", Toast.LENGTH_LONG).show()
            setupContent()
        }
    }
    fun disconnect() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        val configClient = Fitness.getConfigClient(this, account)
        configClient.disableFit().addOnSuccessListener {
            Log.i(TAG, "Google Fit has been disabled")
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .addExtension(fitnessOptions)
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
            googleSignInClient.signOut().addOnCompleteListener {
                Log.i(TAG, "User signed out from Google account")
            }
            googleSignInClient.revokeAccess().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "All access has been revoked")
                    Toast.makeText(this, "Disconnected from Google Fit", Toast.LENGTH_LONG).show()
                } else {
                    Log.e(TAG, "Failed to revoke access", task.exception)
                    Toast.makeText(this, "Failed to disconnect from Google Fit", Toast.LENGTH_LONG).show()
                }
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Failed to disable Google Fit", e)
            Toast.makeText(this, "Error disconnecting from Google Fit", Toast.LENGTH_LONG).show()
        }
    }
    fun closeApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("EXIT", true)
        startActivity(intent)
    }
}





