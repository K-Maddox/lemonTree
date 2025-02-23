package application.lemontree.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import application.lemontree.R;

public class LocationSelectActivity {

    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;
    private ImageButton mapTypeButton;
    private ImageButton selectLocationButton;
    private LatLng selectedLocation;
    private String selectedLocationName;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    private boolean isInitialLocationSet = false;  // flag to prevent camera reset after the first location update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_select);

        mapSearchView = findViewById(R.id.mapSearch);
        mapTypeButton = findViewById(R.id.mapTypeButton);
        selectLocationButton = findViewById(R.id.selectLocationButton);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Create LocationRequest with high accuracy and frequent updates
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(60000);  // Update every 5 seconds

        // Create LocationCallback to receive location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                    // Move the map to the current location only once when the app is first opened
                    if (!isInitialLocationSet & currentLocation != null) {
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        if (mapFragment != null) {
                            mapFragment.getMapAsync(LocationSelectActivity.this);
                        }
                        isInitialLocationSet = true;  // Set the flag to true after the first move
                    }
                }
            }
        };

        requestLocationUpdates();

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null && !location.isEmpty()) {
                    Geocoder geocoder = new Geocoder(LocationSelectActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        selectedLocation = new LatLng(address.getLatitude(), address.getLongitude()); // Store the searched location

                        // Extract the suburb/locality
                        selectedLocationName = address.getLocality();

                        // In case locality is not available, fall back to sub-admin area (county or district)
                        if (selectedLocationName == null || selectedLocationName.isEmpty()) {
                            selectedLocationName = address.getSubAdminArea();
                        }

                        // If still not available
                        if (selectedLocationName == null || selectedLocationName.isEmpty()) {
                            //selectedLocationName = address.getAddressLine(0);
                            selectedLocationName = location; // Use the search bar query as the location name
                        }

                        myMap.clear();  // Clear previous markers
                        myMap.addMarker(new MarkerOptions()
                                .position(selectedLocation)
                                .title(selectedLocationName)
                                .icon(getMarkerIconFromColor(R.color.colorPrimary)));
                        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));  // Stay at the searched location

                        selectLocationButton.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(LocationSelectActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Handle click on the "Select" button
        selectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLocation != null && selectedLocationName != null) {
                    // Create an intent to hold the selected location
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selected_location_name", selectedLocationName);
                    resultIntent.putExtra("selected_latitude", selectedLocation.latitude);
                    resultIntent.putExtra("selected_longitude", selectedLocation.longitude);

                    // Set the result of this activity, so the CreateOfferActivity can receive the selected location
                    setResult(Activity.RESULT_OK, resultIntent);

                    // Finish the activity and go back to CreateOfferActivity
                    finish();
                }
            }
        });

        // Set up click listener for the ImageButton to open the Bottom Sheet Dialog
        mapTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop location updates to save battery
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
