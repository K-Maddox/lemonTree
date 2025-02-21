package application.lemontree.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import application.lemontree.R;
import application.lemontree.helpers.FieldValidationPair;
import application.lemontree.models.Want;
import application.lemontree.services.LocationGetService;

public class CreateWantActivity extends AppCompatActivity {

    String[] categoryList = {"Food", "Herbs", "Seeds", "Flowers", "Garden"};
    AutoCompleteTextView wantCategory;
    ArrayAdapter<String> categoryItems;
    private EditText wantNameEditText, wantDescriptionEditText, wantAvailableDateEditText;
    private TextInputLayout wantNameInputLayout, wantCategoryDropDown, wantDescriptionInputLayout, wantAvailableDateInputLayout;
    private List<FieldValidationPair> fieldValidationPairs = new ArrayList<>();
    private Button wantAvailDateButton, submitButton;
    private LocationGetService locationGetService;
    private GeoPoint userLocation;

    private boolean isValid = true;  // Single validation flag

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_want);

        wantNameEditText = findViewById(R.id.wantNameEditText);
        wantCategory = findViewById(R.id.wantCategory);
        wantDescriptionEditText = findViewById(R.id.wantDescriptionEditText);
        wantAvailableDateEditText = findViewById(R.id.wantAvailableDateEditText);

        wantNameInputLayout = findViewById(R.id.wantNameInputLayout);
        wantCategoryDropDown = findViewById(R.id.wantCategoryDropDown);
        wantDescriptionInputLayout = findViewById(R.id.wantDescriptionInputLayout);
        wantAvailableDateInputLayout = findViewById(R.id.wantAvailableDateInputLayout);

        submitButton = findViewById(R.id.submitWantButton);
        wantAvailDateButton = findViewById(R.id.wantAvailDateButton);

        // Add field pairs to the list
        fieldValidationPairs.add(new FieldValidationPair(wantNameEditText, wantNameInputLayout));
        fieldValidationPairs.add(new FieldValidationPair(wantCategory, wantCategoryDropDown));
        fieldValidationPairs.add(new FieldValidationPair(wantDescriptionEditText, wantDescriptionInputLayout));
        fieldValidationPairs.add(new FieldValidationPair(wantAvailableDateEditText, wantAvailableDateInputLayout));

        // Set focus change listeners to validate fields dynamically
        setOnFocusListeners();

        // wantCategory dropdown list
        categoryItems = new ArrayAdapter<String>(this, R.layout.category_item, categoryList);
        wantCategory.setAdapter(categoryItems);
        wantCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
            }
        });

    }

    private void setOnFocusListeners() {
        for (FieldValidationPair pair : fieldValidationPairs) {
            final EditText editText = pair.editText;
            final TextInputLayout inputLayout = pair.inputLayout;

            editText.setOnFocusChangeListener((view, hasFocus) -> {
                if (!hasFocus) {
                    if (!validateField(editText, inputLayout, "Required*")) {
                        isValid = false;  // Mark form as invalid if this field is invalid
                    }
                }
            });
        }
    }

    // Helper method to clear focus from all focusable EditText fields
    private void clearFocusableEditTextFocus() {
        wantNameEditText.clearFocus();
        wantDescriptionEditText.clearFocus();
        wantCategory.clearFocus();
    }

    // Helper methods
    private boolean validateField(EditText editText, TextInputLayout inputLayout, String errorMessage) {
        if (isEmptyText(editText)) {
            inputLayout.setHelperText(errorMessage);
            inputLayout.setHelperTextColor(getResources().getColorStateList(R.color.red));
            return false;  // Field is invalid
        } else {
            inputLayout.setHelperText(null); // Clear helper text if valid
            return true;  // Field is valid
        }
    }

    // Helper method to check if the EditText or AutoCompleteTextView is empty
    private boolean isEmptyText(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private void uploadWantToFirebase() {
        // Collect data from UI elements
        String wantName = wantNameEditText.getText().toString().trim();
        String wantCat = wantCategory.getText().toString().trim();
        String wantDescription = wantDescriptionEditText.getText().toString().trim();
        String wantAvailableDate = wantAvailableDateEditText.getText().toString().trim();

        // Get current user's ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            submitButton.setText("Post Offer");
            return;
        }
        String userId = user.getUid();

        // Get user's profile URL and username
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Generate a new document ID for the "want" post
        DocumentReference wantRef = db.collection("wants").document();
        String wantId = wantRef.getId();  // Generate a unique ID for the new document

        db.collection("profiles").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userProfileUrl = documentSnapshot.getString("profilePictureURL");
                    String username = documentSnapshot.getString("username");

                    // Create a want object
                    Want want = new Want();
                    want.setWantId(wantId); // Set the wantId
                    want.setWantName(wantName);
                    want.setWantCategory(wantCat);
                    want.setWantDescription(wantDescription);
                    want.setWantAvailableDate(wantAvailableDate);
                    want.setCreatedBy(userId);
                    want.setUserProfileUrl(userProfileUrl);
                    want.setCreatedByUsername(username);
                    want.setCreatedAt(new com.google.firebase.Timestamp(new Date()));
                    want.setLocation(userLocation);
                    want.setStatus("active");

                    // Upload to Firestore using the same wantId
                    db.collection("wants").document(wantId)
                            .set(want)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(CreateWantActivity.this, "Wanted offer uploaded successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                submitButton.setEnabled(true);
                                submitButton.setText("Post Offer");
                                Toast.makeText(CreateWantActivity.this, "Error uploading wanted offer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("CreateWantActivity", "Error uploading wanted offer", e);
                            });
                })
                .addOnFailureListener(e -> {
                    submitButton.setEnabled(true);
                    submitButton.setText("Post Offer");
                    Toast.makeText(CreateWantActivity.this, "Failed to get user profile", Toast.LENGTH_SHORT).show();
                    Log.e("CreateWantActivity", "Failed to get user profile", e);
                });

    }

    // Show DatePickerDialog
    private void showDatePicker() {
        // Get current date to set as the default
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Create a Calendar instance with the selected date
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, selectedDay);

            // Check if the selected date is in the past
            if (selectedDate.before(Calendar.getInstance())) {
                // If the date is in the past, set the helper text with the error message
                wantAvailableDateInputLayout.setHelperText("Please Choose A Future Date*");
                wantAvailableDateInputLayout.setHelperTextColor(getResources().getColorStateList(R.color.red));  // Set helper text color to red
            } else {
                // If the date is valid, clear any error and update the EditText with the selected date
                wantAvailableDateInputLayout.setHelperText(null);  // Clear the helper text
                wantAvailableDateEditText.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        // Create a confirmation dialog when the back button is pressed
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? Any unsaved changes will be lost.")
                .setCancelable(false)  // Prevents the dialog from being dismissed with back press or outside touch
                .setPositiveButton("Yes", (dialog, id) -> {
                    // If 'Yes' is clicked, call the default onBackPressed() to proceed
                    CreateWantActivity.super.onBackPressed();
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // If 'No' is clicked, dismiss the dialog and stay in the activity
                    dialog.dismiss();
                })
                .show();
    }
}
