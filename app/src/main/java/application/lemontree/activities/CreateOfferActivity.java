package application.lemontree.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import application.lemontree.R;
import application.lemontree.helpers.FieldValidationPair;
import application.lemontree.models.Offer;

public class CreateOfferActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_GALLERY_CODE = 103;
    private static final int REQUEST_LOCATION_PICK = 104;

    String currentPhotoPath;
    private Uri imageUri;

    String[] categoryList = {"Food", "Herbs", "Seeds", "Flowers", "Garden"};
    AutoCompleteTextView category;
    ArrayAdapter<String> categoryItems;
    private ImageView offerImageView;
    private Button cameraButton, galleryButton;
    private EditText offerNameEditText, offerDescriptionEditText, offerAvailableDateEditText, offerLocationEditText;
    private TextInputLayout offerNameInputLayout, categoryDropDown, offerDescriptionInputLayout, offerAvailableDateInputLayout, offerLocationInputLayout;
    private Button availDateButton, locationButton, submitButton;
    private List<FieldValidationPair> fieldValidationPairs = new ArrayList<>();
    private GeoPoint selectedGeoPoint;

    private boolean isValid = true;  // Single validation flag
    private boolean isImageValid = true;
    private boolean isImageSizeValid = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offer);

        offerImageView = findViewById(R.id.offerImageView);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);

        offerNameInputLayout = findViewById(R.id.offerNameInputLayout);
        offerNameEditText = findViewById(R.id.offerNameEditText);

        categoryDropDown = findViewById(R.id.categoryDropDown);
        category = findViewById(R.id.category);

        offerDescriptionEditText = findViewById(R.id.offerDescriptionEditText);
        offerDescriptionInputLayout = findViewById(R.id.offerDescriptionInputLayout);

        offerAvailableDateEditText = findViewById(R.id.offerAvailableDateEditText);
        offerAvailableDateInputLayout = findViewById(R.id.offerAvailableDateInputLayout);

        offerLocationEditText = findViewById(R.id.offerLocationEditText);
        offerLocationInputLayout = findViewById(R.id.offerLocationInputLayout);

        availDateButton = findViewById(R.id.availDateButton);
        locationButton = findViewById(R.id.locationButton);
        submitButton = findViewById(R.id.submitOfferButton);

        // Add field pairs to the list
        fieldValidationPairs.add(new FieldValidationPair(offerNameEditText, offerNameInputLayout));
        fieldValidationPairs.add(new FieldValidationPair(category, categoryDropDown));
        fieldValidationPairs.add(new FieldValidationPair(offerDescriptionEditText, offerDescriptionInputLayout));
        fieldValidationPairs.add(new FieldValidationPair(offerAvailableDateEditText, offerAvailableDateInputLayout));
        fieldValidationPairs.add(new FieldValidationPair(offerLocationEditText, offerLocationInputLayout));

        // Set focus change listeners to validate fields dynamically
        setOnFocusListeners();

        // category dropdown list
        categoryItems = new ArrayAdapter<String>(this, R.layout.category_item, categoryList);
        category.setAdapter(categoryItems);
        category.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
            }
        });

        // Set up DatePicker
        availDateButton.setOnClickListener(v -> {
            // Clear focus from other fields
            clearFocusableEditTextFocus();
            // Trigger the date picker
            showDatePicker();
        });
        offerAvailableDateEditText.setOnClickListener(v -> {
            // Clear focus from other fields
            clearFocusableEditTextFocus();
            // Trigger the date picker
            showDatePicker();
        });

        // Capture Image
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermission();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, REQUEST_GALLERY_CODE);
            }
        });

        // Location button opens this activity again
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear focus from other fields
                clearFocusableEditTextFocus();
                // Open the same activity on location button click
                Intent locationIntent = new Intent(CreateOfferActivity.this, LocationSelectActivity.class);
                startActivityForResult(locationIntent, REQUEST_LOCATION_PICK);
            }
        });

        // Set onClickListener for offerLocationEditText to behave like the Select Location button
        offerLocationEditText.setOnClickListener(v -> {
            // Clear focus from other fields
            clearFocusableEditTextFocus();
            Intent locationIntent = new Intent(CreateOfferActivity.this, LocationSelectActivity.class);
            startActivityForResult(locationIntent, REQUEST_LOCATION_PICK);
        });

        // Handle form submission
        submitButton.setOnClickListener(v -> {
            // Validate all fields again at submission time
            isValid = true;  // Reset the flag to true
            isImageValid = true;
            isImageSizeValid = true;
            for (FieldValidationPair pair : fieldValidationPairs) {
                if (!validateField(pair.editText, pair.inputLayout, "Required*")) {
                    isValid = false;
                }
            }
            // Validate Image
            Drawable defaultImage = getResources().getDrawable(R.drawable.empty_image);
            if (offerImageView.getDrawable().getConstantState().equals(defaultImage.getConstantState())) {
                isImageValid = false;
            } else {
                if (imageUri != null) {
                    isImageSizeValid = checkFileSize(imageUri);
                }
            }

            // Proceed if all validations are successful
            if (isValid && isImageValid && isImageSizeValid) {
                // Disable the button to prevent multiple clicks
                submitButton.setEnabled(false);
                submitButton.setText("Posting...");
                // All required fields are filled, proceed with saving or submitting the offer
                uploadOfferToFirebase();
            } else {
                if (!isValid) {
                    Toast.makeText(CreateOfferActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                }
                if (!isImageValid) {
                    Toast.makeText(CreateOfferActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
                if (!isImageSizeValid) {
                    Toast.makeText(CreateOfferActivity.this, "Image size not accepted, max image size is 20MB", Toast.LENGTH_SHORT).show();
                }
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

    // Helper method to clear focus from all focusable EditText fields
    private void clearFocusableEditTextFocus() {
        offerNameEditText.clearFocus();
        offerDescriptionEditText.clearFocus();
        category.clearFocus();
    }

    // Helper method to check if the EditText or AutoCompleteTextView is empty
    private boolean isEmptyText(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use Camera.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
