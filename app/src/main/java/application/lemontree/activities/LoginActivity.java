package application.lemontree.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;

import application.lemontree.R;
import application.lemontree.viewmodels.UserViewModel;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private UserViewModel userViewModel;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;

    private boolean validEmail = false;  // Boolean to track email validation
    private boolean validPassword = false;  // Boolean to track password validation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        emailEditText = findViewById(R.id.edittext_lgi_email);
        passwordEditText = findViewById(R.id.edittext_lgi_pwd);
        emailInputLayout = findViewById(R.id.container_lgi_email);
        passwordInputLayout = findViewById(R.id.container_lgi_pwd);
        loginButton = findViewById(R.id.btn_lgi_login);

        // Set focus change listener for email field
        emailEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) { // When focus is lost
                String email = emailEditText.getText().toString().trim();
                if (email.isEmpty()) {
                    emailInputLayout.setHelperText("Email cannot be blank");
                    validEmail = false; // Mark email as invalid
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setHelperText("Invalid email address");
                    validEmail = false; // Mark email as invalid
                } else {
                    emailInputLayout.setHelperText(null); // Clear the helper text if valid
                    validEmail = true; // Mark email as valid
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

        // monitor login state
        userViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Log.d(TAG, "user already logged in uid:" + firebaseUser.getUid());
                // user already logged in, jump to main page
                startActivity(new Intent(LoginActivity.this, OfferActivity.class));
                finish();
            }
        });

        // monitor login error
        userViewModel.getLoginErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Login Failed")
                        .setMessage(errorMessage)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Dismiss the dialog or add any additional logic if needed
                        })
                        .show();
            }
        });

        // Set login button listener
        loginButton.setOnClickListener(v -> {
            clearFocusableEditTextFocus();
            // Proceed with login if both email and password are valid
            if (validEmail && validPassword) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Perform the login action
                userViewModel.loginUser(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                // If not valid, trigger focus change to show the relevant helper texts
                if (!validEmail) {
                    emailEditText.requestFocus();
                } else if (!validPassword) {
                    passwordEditText.requestFocus();
                }
            }
        });

    }

    // Helper method to clear focus from all focusable EditText fields
    private void clearFocusableEditTextFocus() {
        emailEditText.clearFocus();
        passwordEditText.clearFocus();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        userViewModel.checkUserLoggedIn();
    }
}
