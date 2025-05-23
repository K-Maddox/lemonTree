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

public class LocationSelectActivity extends AppCompatActivity implements OnMapReadyCallback {

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

    // Method to open the Bottom Sheet Dialog for map types
    private void showBottomSheetDialog() {
        // Create a BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(LocationSelectActivity.this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_map_type, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Set up the click listeners for each map type option
        Button mapTypeNormal = bottomSheetView.findViewById(R.id.mapTypeNormal);
        Button mapTypeSatellite = bottomSheetView.findViewById(R.id.mapTypeSatellite);
        ImageView defaultIcon = bottomSheetView.findViewById(R.id.defaultIcon);
        ImageView satelliteIcon = bottomSheetView.findViewById(R.id.satelliteIcon);

        // Set click listeners for the buttons
        mapTypeNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                bottomSheetDialog.dismiss();  // Close the dialog after selection
            }
        });

        mapTypeSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                bottomSheetDialog.dismiss();  // Close the dialog after selection
            }
        });

        // Set click listeners for the ImageViews
        defaultIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                bottomSheetDialog.dismiss();
            }
        });

        satelliteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                bottomSheetDialog.dismiss();
            }
        });

        // Show the BottomSheetDialog
        bottomSheetDialog.show();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        // Get the initial location immediately
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = location;
                isInitialLocationSet = true;
                SupportMapFragment mapFragment =
                        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(LocationSelectActivity.this);
                }
            }

        });

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        // Enable Zoom Controls
        myMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable Compass (will only show when the map is rotated)
        myMap.getUiSettings().setCompassEnabled(true);

        // Enable gesture-based zoom (pinch-to-zoom)
        myMap.getUiSettings().setZoomGesturesEnabled(true);

        // Enable gesture-based change angle (scroll)
        myMap.getUiSettings().setScrollGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        // Enable the “my location” button, which centers the map on the user’s current location.
        myMap.setMyLocationEnabled(true);

        if (currentLocation != null) {
            LatLng defaultLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));  // Move to current location initially
        }

        // Add listener to handle "my location" button click
        myMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Get the current location and show the select location button
                if (currentLocation != null) {
                    LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    // Move the camera to the current location and add a marker
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    myMap.clear();  // Clear previous markers
                    myMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title("Your Location")
                            .icon(getMarkerIconFromColor(R.color.colorPrimary)));

                    // Show the select location button
                    selectLocationButton.setVisibility(View.VISIBLE);

                    // Optionally, set this location as selected
                    selectedLocation = currentLatLng;
                    selectedLocationName = getSuburbNameFromLocation(currentLatLng);
                }

                // Return false to allow default behavior (centering on location)
                return false;
            }
        });

        // Add listener for long-press on map to set location
        myMap.setOnMapLongClickListener(latLng -> {
            // Clear existing markers
            myMap.clear();
            myMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
                    .icon(getMarkerIconFromColor(R.color.colorPrimary)));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            // Store the selected location
            selectedLocation = latLng;
            selectedLocationName = getSuburbNameFromLocation(latLng);

            // Show the select location button
            selectLocationButton.setVisibility(View.VISIBLE);
        });
    }

    // Method to convert a color from colors.xml to a BitmapDescriptor for the marker
    private BitmapDescriptor getMarkerIconFromColor(int colorResId) {
        // Get the color from the resources
        int color = ContextCompat.getColor(this, colorResId);

        // Convert the color to HSV (Hue, Saturation, Value)
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // Return the marker icon based on the hue (hsv[0])
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    // Method to get the suburb (locality) name from the location
    private String getSuburbNameFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Get the list of addresses for the given latitude and longitude
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                // Return the locality (suburb) from the address
                String suburbName = addresses.get(0).getLocality();
                if (suburbName != null && !suburbName.isEmpty()) {
                    return suburbName;
                } else {
                    // Fall back to sub-admin area or address line if locality is not available
                    return addresses.get(0).getSubAdminArea() != null ? addresses.get(0).getSubAdminArea() : addresses.get(0).getAddressLine(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Location"; // Return a default value if location name is not available
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