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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_PICK && resultCode == Activity.RESULT_OK && data != null) {
            // Get the selected location name and coordinates from the intent
            String selectedLocationName = data.getStringExtra("selected_location_name");
            double latitude = data.getDoubleExtra("selected_latitude", 0.0);
            double longitude = data.getDoubleExtra("selected_longitude", 0.0);

            if (selectedLocationName != null) {
                // Update the offerLocationEditText with the selected location name
                offerLocationEditText.setText(selectedLocationName);

                // Save the latitude and longitude for later use when submitting the form
                selectedGeoPoint = new GeoPoint(latitude, longitude);

                // Clear the helper text for the location field after a valid selection
                offerLocationInputLayout.setHelperText(null);
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            File f = new File(currentPhotoPath);
            imageUri = Uri.fromFile(f);
            offerImageView.setImageURI(imageUri);
            Log.d("tag", "Absolute Url for Image is " + imageUri);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(imageUri);
            this.sendBroadcast(mediaScanIntent);
        }

        if (requestCode == REQUEST_GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                offerImageView.setImageURI(imageUri);
                Log.d("tag", "onActivityResult: Gallery Image Uri:  " + imageUri);
            }
        }
    }

    private void uploadOfferToFirebase() {
        // Collect data from UI elements
        String offerName = offerNameEditText.getText().toString().trim();
        String offerCategory = category.getText().toString().trim();
        String offerDescription = offerDescriptionEditText.getText().toString().trim();
        String offerAvailableDate = offerAvailableDateEditText.getText().toString().trim();
        String offerPickUpLocation = offerLocationEditText.getText().toString().trim();

        if (imageUri != null) {
            if (selectedGeoPoint != null) {
                uploadImageAndOffer(offerName, offerCategory, offerDescription, offerAvailableDate, offerPickUpLocation, selectedGeoPoint);
            } else {
                submitButton.setEnabled(true);
                submitButton.setText("Post Offer");
                Toast.makeText(this, "Please select a valid location", Toast.LENGTH_SHORT).show();
            }
        } else {
            submitButton.setEnabled(true);
            submitButton.setText("Post Offer");
            Toast.makeText(this, "Please select a valid image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndOffer(String offerName, String offerCategory, String offerDescription,
                                     String offerAvailableDate, String offerPickUpLocation, GeoPoint offerGeoPoint) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            submitButton.setText("Post Offer");
            Log.d("CreateOfferActivity", "User is not authenticated");
            return;
        }
        Log.d("CreateOfferActivity", "User is authenticated: " + user.getUid());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Generate a new offer ID
        DocumentReference offerRef = db.collection("offers").document();
        String offerId = offerRef.getId();

        // Use offerId in storage path
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        StorageReference imageRef = storageRef.child("OfferPictures/" + offerId + "/" + timeStamp + "." + getFileExt(imageUri));

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            submitButton.setEnabled(true);
            submitButton.setText("Post Offer");
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
            Log.e("CreateOfferActivity", "Image upload failed", exception);
        }).addOnSuccessListener(taskSnapshot -> {
            // Get the download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                // Now, create the Offer object and upload to Firestore
                uploadOfferData(offerId, offerName, offerCategory, offerDescription, offerAvailableDate, offerPickUpLocation, offerGeoPoint, imageUrl);
            });
        });
    }

    private void uploadOfferData(String offerId, String offerName, String offerCategory, String offerDescription,
                                 String offerAvailableDate, String offerPickUpLocation, GeoPoint offerGeoPoint, String imageUrl) {
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

        db.collection("profiles").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userProfileUrl = documentSnapshot.getString("profilePictureURL");
                    String username = documentSnapshot.getString("username");

                    // Create an Offer object
                    Offer offer = new Offer();
                    offer.setOfferId(offerId); // Set the offerId
                    offer.setOfferName(offerName);
                    offer.setOfferCategory(offerCategory);
                    offer.setOfferDescription(offerDescription);
                    offer.setOfferAvailableDate(offerAvailableDate);
                    offer.setOfferPickUpLocation(offerPickUpLocation);
                    offer.setLocation(offerGeoPoint);
                    offer.setImageUrl(imageUrl);
                    offer.setCreatedBy(userId);
                    offer.setUserProfileUrl(userProfileUrl);
                    offer.setCreatedByUsername(username);
                    offer.setCreatedAt(com.google.firebase.Timestamp.now());
                    offer.setStatus("active");

                    // Upload to Firestore using the same offerId
                    db.collection("offers").document(offerId)
                            .set(offer)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(CreateOfferActivity.this, "Offer uploaded successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                submitButton.setEnabled(true);
                                submitButton.setText("Post Offer");
                                Toast.makeText(CreateOfferActivity.this, "Error uploading offer", Toast.LENGTH_SHORT).show();
                                Log.e("CreateOfferActivity", "Error uploading offer", e);
                            });
                })
                .addOnFailureListener(e -> {
                    submitButton.setEnabled(true);
                    submitButton.setText("Post Offer");
                    Toast.makeText(CreateOfferActivity.this, "Failed to get user profile", Toast.LENGTH_SHORT).show();
                    Log.e("CreateOfferActivity", "Failed to get user profile", e);
                });
    }

    private String getFileExt(Uri uri) {
        if (uri == null) return null;
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(uri));
    }

    // Create image file
    private File createImageFile() throws IOException {
        // Create an image file name with timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,   /* prefix */
                ".jpg",          /* suffix */
                storageDir       /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Capture photo
    private void dispatchTakePictureIntent() {
        // Get location before capturing the photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Handle error
            ex.printStackTrace();
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "comp5216.sydney.edu.au.lemontreefirebase.activities.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            // Check the size of the captured image
//            checkFileSize(photoFile);
        }
//        }
    }

    // Method to check the file size
    private boolean checkFileSize(Uri uri) {
        long fileSizeInBytes = 0;
        try {
            // For images captured by camera (File Uri)
            if ("file".equals(uri.getScheme())) {
                File imageFile = new File(uri.getPath());
                fileSizeInBytes = imageFile.length();
            }
            // For images selected from gallery (Content Uri)
            else if ("content".equals(uri.getScheme())) {
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    fileSizeInBytes = cursor.getLong(sizeIndex);
                    cursor.close();
                } else {
                    // Unable to retrieve file size
                    return false;
                }
            } else {
                // Unsupported URI scheme
                return false;
            }

            // Convert bytes to MB
            long fileSizeInMB = fileSizeInBytes / (1024 * 1024);

            // Log the file size in MB
            Log.d("Image Size", "File size: " + fileSizeInMB + " MB");

            // Check if file size exceeds 20MB
            return fileSizeInMB <= 20;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // Unable to get file size, consider invalid
        }
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
                offerAvailableDateInputLayout.setHelperText("Please Choose A Future Date*");
                offerAvailableDateInputLayout.setHelperTextColor(getResources().getColorStateList(R.color.red));  // Set helper text color to red
            } else {
                // If the date is valid, clear any error and update the EditText with the selected date
                offerAvailableDateInputLayout.setHelperText(null);  // Clear the helper text
                offerAvailableDateEditText.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
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
                    CreateOfferActivity.super.onBackPressed();
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // If 'No' is clicked, dismiss the dialog and stay in the activity
                    dialog.dismiss();
                })
                .show();
    }
}
