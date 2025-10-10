package com.example.mandelamoney.util;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricsManager {
    /**
     * Starts biometric authentication.
     * @param activity FragmentActivity context (e.g. AppCompatActivity)
     * @param onSuccess Runnable to run on successful auth
     * @param onFailure Runnable to run on failure or error
     */
    public static void authenticate(
                @NonNull FragmentActivity activity,
                @NonNull Runnable onSuccess,
                @NonNull Runnable onFailure
    ) {
            BiometricManager biometricManager = BiometricManager.from(activity);

            int canAuthenticate = biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG
            );

            if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
                onFailure.run();
                return;
            }

            Executor executor = ContextCompat.getMainExecutor(activity);

            BiometricPrompt biometricPrompt = new BiometricPrompt(
                    activity,
                    executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            onSuccess.run();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            onFailure.run();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            // Optionally notify or retry
                        }
                    }
            );

        BiometricPrompt.PromptInfo promptInfo = null;
        boolean hasWeak = hasWeakAuthentication(activity);

        if (hasWeak && UserSession.getUser().getWeakAuth()) { // Use else if
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Use your fingerprint, face, or screen lock to authenticate")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
        } else {
            if (hasWeak && UserSession.getUser().getStrongAuth()) {
                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric Authentication")
                        .setSubtitle("Use your fingerprint to authenticate")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                        // Add this line to fix the crash
                        .setNegativeButtonText("Cancel")
                        .build();
            }
        }



        if (promptInfo != null) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            onFailure.run();
        }


    }

    public static boolean hasWeakAuthentication(@NonNull Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static boolean hasStrongAuthentication(@NonNull Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }




}
