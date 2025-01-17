package application.lemontree.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import application.lemontree.R;
import application.lemontree.activities.WelcomeActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernamePreference = findPreference("profile_name");
        emailPreference = findPreference("profile_email");
        notificationPreference = findPreference("notifications");
        offernotificationPreference = findPreference("newoffers");
        chatnotificationPreference = findPreference("newchats");
        logoutPreference = findPreference("logout_preference");

        // Set up listener for Allow Notifications Settings
        notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isSwitchedOn = (Boolean) newValue;
            if (isSwitchedOn) {
                //check and request permission if not given previously
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)) {
                            // Show a rationale to explain why the permission is needed
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Permission needed")
                                    .setMessage("This app needs the POST_NOTIFICATIONS permission to send notifications.")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> {
                                        dialog.dismiss();
                                        notificationPreference.setChecked(false);
                                    })
                                    .create().show();
                        } else {
                            // Directly request the permission if no rationale is needed
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        }
                    }
                }
            } else {
                offernotificationPreference.setChecked(false);
                offernotificationPreference.callChangeListener(false);
                chatnotificationPreference.setChecked(false);
                chatnotificationPreference.callChangeListener(false);
            }
            return true;
        });

        // Set up listener for Allow Offer Notification Settings
        offernotificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isSwitchedOn = (Boolean) newValue;
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                DocumentReference userRef =
                        db.collection("profiles").document(mAuth.getCurrentUser().getUid());
                userRef.update("newOfferNotification", isSwitchedOn)
                        .addOnSuccessListener(aVoid -> {
                            //success
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to " +
                                    "update setting", Toast.LENGTH_SHORT).show();
                            offernotificationPreference.setChecked(false);
                        });
            }
            return true;
        });

        // Set up listener for Allow Chat Notification Settings
        chatnotificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isSwitchedOn = (Boolean) newValue;
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                DocumentReference userRef =
                        db.collection("profiles").document(mAuth.getCurrentUser().getUid());
                userRef.update("newChatNotification", isSwitchedOn)
                        .addOnSuccessListener(aVoid -> {
                            //success
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to " +
                                    "update setting", Toast.LENGTH_SHORT).show();
                            chatnotificationPreference.setChecked(false);
                        });
            }
            return true;
        });

        // Set up listener for Sign out
        logoutPreference.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(requireActivity())  // Use the appropriate context or activity
                    .setTitle("Sign out your profile")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DocumentReference userRef =
                                db.collection("profiles").document(mAuth.getCurrentUser().getUid());
                        userRef.update("FCMtoken", "")
                                .addOnSuccessListener(aVoid -> {
                                    mAuth.signOut();
                                    // Redirect to WelcomeActivity and clear the activity stack
                                    Intent intent = new Intent(requireActivity(), WelcomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireActivity(), "Connection needed to " +
                                                            "delete FCM Token",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                });

                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog if the user cancels
                        dialog.dismiss();
                    })
                    .show();
            return true;
        });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireActivity(), "Notifications permission granted", Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(requireActivity(), "FCM can't post notifications without POST_NOTIFICATIONS permission",
                            Toast.LENGTH_LONG).show();
                }
            });
}
