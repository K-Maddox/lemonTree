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
}
