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
