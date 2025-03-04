package application.lemontree.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import application.lemontree.R;
import application.lemontree.helpers.FilterDialogFragment;
import application.lemontree.helpers.FilterHelper;
import application.lemontree.models.Offer;
import application.lemontree.models.Want;
import application.lemontree.services.LocationGetService;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;
    private ImageButton filterLocationButton;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private boolean isInitialLocationSet = false;

    private LocationGetService locationGetService;

    private String source;
    public int radius;
    private FilterDialogFragment filterDialog;

    // Data pulled from the database
    private ArrayList<Offer> offerList;
    private ArrayList<Want> wantList;

    // Reference to the PopupWindow and its content view
    private PopupWindow popupWindow;
    private View popupView;
    private Marker previousMarker = null;  // To store the previously clicked marker

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        source = getIntent().getStringExtra("source");

        // Retrieve the radius from the intent
        radius = getIntent().getIntExtra("radius", 5);  // Default to 5 if no value is passed

        mapSearchView = findViewById(R.id.offerSearchView);
        filterLocationButton = findViewById(R.id.filterLocationButton);
        View topSection = findViewById(R.id.topSection);  // Reference to the top section

        offerList = new ArrayList<>();
        wantList = new ArrayList<>();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationGetService = new LocationGetService(this);

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
                currentLocation = locationResult.getLastLocation();

                if (!isInitialLocationSet && currentLocation != null) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.offerSearchMap);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(MapActivity.this);
                    }
                    isInitialLocationSet = true;
                }
            }
        };

        requestLocationUpdates();

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocationAndDisplayOffers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        filterDialog = new FilterDialogFragment();
        filterLocationButton.setOnClickListener(view -> {
            // Check if we are filtering offers or wants based on the source
            if (source != null && source.equals("offer")) {
                FilterHelper.showFilterDialog(getSupportFragmentManager(), filterDialog, radius, this::filterOffers);
            } else if (source != null && source.equals("want")) {
                FilterHelper.showFilterDialog(getSupportFragmentManager(), filterDialog, radius, this::filterWants);
            }
        });


        // Inflate the popup layout once
        popupView = getLayoutInflater().inflate(R.layout.offer_popup_layout, null);

        // Create the PopupWindow but don't show it yet
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);

        // Set a transparent background to enable elevation and outline
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Add a click listener to the top section
        topSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the popup window if it is showing
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });

        if (source != null && source.equals("offer")) {
            // Fetch offers
            if (currentLocation != null) {
                getOffersInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        } else if (source != null && source.equals("want")) {
            // Fetch wants
            if (currentLocation != null) {
                getWantsInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        }
    }

    // Method to filter offers based on a new radius
    private void filterOffers(int newRadius) {
        radius = newRadius; // Update radius based on filter
        if (currentLocation != null) {
            getOffersInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
        } else {
            Toast.makeText(MapActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to filter wants based on a new radius
    private void filterWants(int newRadius) {
        radius = newRadius; // Update radius based on filter
        if (currentLocation != null) {
            getWantsInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
        } else {
            Toast.makeText(MapActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchLocationAndDisplayOffers(String location) {
        if (location == null || location.isEmpty()) {
            return;
        }

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList == null || addressList.isEmpty()) {
            Toast.makeText(MapActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Address address = addressList.get(0);
        LatLng searchedLocation = new LatLng(address.getLatitude(), address.getLongitude());

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 13));

        // Fetch data in the new location based on the source
        if ("offer".equals(source)) {
            getOffersInRadius(new GeoPoint(address.getLatitude(), address.getLongitude()));
        } else if ("want".equals(source)) {
            getWantsInRadius(new GeoPoint(address.getLatitude(), address.getLongitude()));
        }
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        // Get the initial location immediately
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = location;
                isInitialLocationSet = true;
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.offerSearchMap);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(MapActivity.this);
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

        // Enable Compass
        myMap.getUiSettings().setCompassEnabled(true);

        // Enable gesture-based zoom
        myMap.getUiSettings().setZoomGesturesEnabled(true);

        // Enable gesture-based change angle
        myMap.getUiSettings().setScrollGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        // Enable the “my location” button
        myMap.setMyLocationEnabled(true);

        if (currentLocation != null) {
            LatLng defaultLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13));

            if ("offer".equals(source)) {
                getOffersInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            } else if ("want".equals(source)) {
                getWantsInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        }

        // Handle "my location" button click
        myMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (currentLocation != null) {
                    LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    // Move the camera to the current location
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));

                    // Check the source and fetch data accordingly
                    if ("offer".equals(source)) {
                        // Fetch offers in the current location
                        getOffersInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    } else if ("want".equals(source)) {
                        // Fetch wants in the current location
                        getWantsInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    }
                }
                return false;
            }
        });

        // Dismiss popup window when map is clicked
        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });

        // Set marker click listener
        myMap.setOnMarkerClickListener(this);
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

    public void getWantsInRadius(GeoPoint location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        double lowerLat = location.getLatitude() - ((double) radius / 110);
        double upperLat = location.getLatitude() + ((double) radius / 110);
        double lowerLng = location.getLongitude() - ((double) radius / (110 * Math.cos(Math.toRadians(location.getLatitude()))));
        double upperLng = location.getLongitude() + ((double) radius / (110 * Math.cos(Math.toRadians(location.getLatitude()))));

        db.collection("wants")
                .whereGreaterThanOrEqualTo("location", new GeoPoint(lowerLat, lowerLng))
                .whereLessThanOrEqualTo("location", new GeoPoint(upperLat, upperLng))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        wantList.clear();
                        myMap.clear();
                        Toast.makeText(getApplicationContext(), "No wants found in radius", Toast.LENGTH_SHORT).show();
                        Log.i("getWantsInRadius", "No wants found in radius");
                        return;
                    }

                    wantList.clear();
                    myMap.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Want w = getWantFromDocumentSnapshot(document);

                        // Filter out non-active wants
                        if (!"active".equalsIgnoreCase(w.getStatus())) {
                            continue;
                        }

                        GeoPoint wantLocation = document.getGeoPoint("location");
                        String title = document.getString("wantName");

                        // Determine distance to want
                        if (wantLocation != null) {
                            float[] results = new float[1];
                            Location.distanceBetween(location.getLatitude(),
                                    location.getLongitude(), wantLocation.getLatitude(),
                                    wantLocation.getLongitude(),
                                    results);
                            float distanceInMeters = results[0];
                            float distanceInKilometers = distanceInMeters / 1000;
                            Log.v("getWantsInRadius",
                                    "Want: " + title + " Distance: " + distanceInKilometers +
                                            " km");
                            w.distance = String.format("%.2f km", distanceInKilometers);

                            // Add marker to map
                            LatLng wantLatLng = new LatLng(wantLocation.getLatitude(), wantLocation.getLongitude());
                            Marker marker = myMap.addMarker(new MarkerOptions()
                                    .position(wantLatLng)
                                    .title(w.getWantName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                            marker.setTag(w); // Store the want object in the marker's tag
                        } else {
                            w.distance = "unknown";
                        }

                        wantList.add(w);
                    }
                })
                .addOnFailureListener(e -> Log.e("getWantsInRadius", "Error getting documents", e));
    }

    /**
     * Extracts data from the database and creates an Offer object.
     *
     * @param document the document snapshot to convert
     * @return An Offer object of the document.
     */
    protected Offer getOfferFromDocumentSnapshot(@NonNull DocumentSnapshot document) {
        Offer o = new Offer();

        o.setOfferName(document.getString("offerName"));
        o.setOfferId(document.getId());
        o.setImageUrl(document.getString("imageUrl"));
        o.setUserProfileUrl(document.getString("userProfileUrl"));
        o.setCreatedByUsername(document.getString("createdByUsername"));
        o.setCreatedBy(document.getString("createdBy"));
        o.setCreatedAt(document.getTimestamp("createdAt"));
        String status = document.getString("status");
        o.setStatus(status != null ? status : "active");
        o.setOfferPickUpLocation(document.getString("offerPickUpLocation"));

        return o;
    }

    protected Want getWantFromDocumentSnapshot(@NonNull DocumentSnapshot document) {
        Want w = new Want();
        w.setWantId(document.getId());
        w.setWantName(document.getString("wantName"));
        w.setWantDescription(document.getString("wantDescription"));
        w.setWantAvailableDate(document.getString("wantAvailableDate"));
        w.setWantCategory(document.getString("wantCategory"));
        w.setCreatedBy(document.getString("createdBy"));
        w.setUserProfileUrl(document.getString("userProfileUrl"));
        w.setCreatedByUsername(document.getString("createdByUsername"));
        w.setCreatedAt(document.getTimestamp("createdAt"));
        w.setStatus(document.getString("status"));
        w.setLocation(document.getGeoPoint("location"));
        return w;
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop location updates to save battery
        stopLocationUpdates();
    }

    // Handle location permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
