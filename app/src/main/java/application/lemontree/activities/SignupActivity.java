package application.lemontree.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import application.lemontree.R;
import application.lemontree.viewmodels.UserViewModel;

public class SignupActivity extends AppCompatActivity {
    private UserViewModel userViewModel;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private TextInputLayout usernameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private Button signUpButton;

    private boolean validUsername = false;
    private boolean validEmail = false;
    private boolean validPassword = false;
    private boolean validConfirmPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        usernameEditText = findViewById(R.id.edittext_sup_username);
        emailEditText = findViewById(R.id.edittext_sup_email);
        passwordEditText = findViewById(R.id.edittext_sup_pwd);
        confirmPasswordEditText = findViewById(R.id.edittext_sup_confirmpwd);
        usernameInputLayout = findViewById(R.id.container_sup_username);
        emailInputLayout = findViewById(R.id.container_sup_email);
        passwordInputLayout = findViewById(R.id.container_sup_pwd);
        confirmPasswordInputLayout = findViewById(R.id.container_sup_confirmpwd);
        signUpButton = findViewById(R.id.btn_sup_signup);

        // Focus change listener for username field
        usernameEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String username = usernameEditText.getText().toString().trim();
                if (username.isEmpty()) {
                    usernameInputLayout.setHelperText("User name cannot be blank");
                    validUsername = false;
                } else {
                    usernameInputLayout.setHelperText(null);  // Clear the helper text if valid
                    validUsername = true;
                }
            }
        });

        // Focus change listener for email field
        emailEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String email = emailEditText.getText().toString().trim();
                if (email.isEmpty()) {
                    emailInputLayout.setHelperText("Required");
                    validEmail = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setHelperText("Invalid email address");
                    validEmail = false;
                } else {
                    emailInputLayout.setHelperText(null);
                    validEmail = true;
                }
            }
        });

        // Set focus change listener for password field
        passwordEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) { // When focus is lost
                String password = passwordEditText.getText().toString().trim();
                if (password.isEmpty()) {
                    passwordInputLayout.setHelperText("Password cannot be blank");
                    validPassword = false; // Mark password as invalid
                } else if (password.length() < 6) {
                    passwordInputLayout.setHelperText("Password must be at least 6 characters");
                    validPassword = false;
                } else if (password.contains(" ")) {
                    passwordInputLayout.setHelperText("Password cannot contain spaces");
                    validPassword = false;
                } else {
                    passwordInputLayout.setHelperText(null); // Clear the helper text if valid
                    validPassword = true; // Mark password as valid
                }
            }
        });

        // Focus change listener for confirm password field
        confirmPasswordEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                if (confirmPassword.isEmpty()) {
                    confirmPasswordInputLayout.setHelperText("Please confirm your password");
                    validConfirmPassword = false;
                } else if (!confirmPassword.equals(password)) {
                    confirmPasswordInputLayout.setHelperText("Passwords do not match");
                    validConfirmPassword = false;
                } else {
                    confirmPasswordInputLayout.setHelperText(null);
                    validConfirmPassword = true;
                }
            }
        });

        // Set sign-up button listener
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFocusableEditTextFocus();
                if (validUsername && validEmail && validPassword && validConfirmPassword) {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String username = usernameEditText.getText().toString().trim();
                    userViewModel.signupUser(email, password, username);
                } else {
                    Toast.makeText(SignupActivity.this, "Please correct the errors", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Monitor user sign-up success
        userViewModel.getUserLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    Toast.makeText(SignupActivity.this, "Sign Up Success!", Toast.LENGTH_SHORT).show();
                    showSignupSuccessAlert();
                }
            }
        });


        // Monitor sign-up error messages
        userViewModel.getSignupErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                // Use AlertDialog for larger error messages
                new AlertDialog.Builder(SignupActivity.this)
                        .setTitle("Sign-up Failed")
                        .setMessage(errorMessage)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    // Helper method to clear focus from all focusable EditText fields
    private void clearFocusableEditTextFocus() {
        emailEditText.clearFocus();
        usernameEditText.clearFocus();
        passwordEditText.clearFocus();
        confirmPasswordEditText.clearFocus();
    }

    // House rules alert
    private void showSignupSuccessAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Welcome To Lemon Tree!")
                .setMessage(getString(R.string.signup_success_message))
                .setPositiveButton("OK", (dialog, which) -> {
                    // After acknowledging, proceed to the next activity
                    startActivity(new Intent(SignupActivity.this, OfferActivity.class));
                    finish();
                })
                .setCancelable(false)  // The user can't dismiss without clicking OK
                .show();
    }
}
