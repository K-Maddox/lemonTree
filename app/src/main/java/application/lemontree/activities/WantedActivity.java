package application.lemontree.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

import application.lemontree.R;
import application.lemontree.adaptors.WantedAdapter;
import application.lemontree.fragments.NavBarFragment;
import application.lemontree.helpers.FilterDialogFragment;
import application.lemontree.helpers.FilterHelper;
import application.lemontree.models.Want;
import application.lemontree.services.LocationGetService;

public class WantedActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    // data pulled from the database that we store locally to further filter (or not)
    private ArrayList<Want> dataList;
    // data to currently display to the user
    private ArrayList<Want> filteredDataList;

    private ImageButton filterButton;
    private ImageButton mapButton;
    private ImageButton addButton;

    private SearchView searchBar;

    private WantedAdapter wantedAdapter;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    // radius for finding wants in km. Default 5km
    public int radius = 5;
    private Location currentLocation;
    private FilterDialogFragment filterDialog;
    private LocationGetService locationGetService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wanted);

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request for it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Permission is granted, proceed with location-based functionality
            locationGetService = new LocationGetService(this);
        }

        locationGetService = new LocationGetService(this);

        recyclerView = findViewById(R.id.wantedRecyclerView);
        dataList = new ArrayList<>();
        filteredDataList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wantedAdapter = new WantedAdapter(filteredDataList, this);
        recyclerView.setAdapter(wantedAdapter);

        setupButtons();
        setupSearchBar();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_container, new NavBarFragment())
                .commit();

        filterDialog = new FilterDialogFragment();

        // Fetch last location when the activity starts
        fetchLocationAndData();
    }
}
