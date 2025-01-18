package application.lemontree.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import application.lemontree.adaptors.OfferAdapter;
import application.lemontree.fragments.NavBarFragment;
import application.lemontree.helpers.FilterDialogFragment;
import application.lemontree.helpers.FilterHelper;
import application.lemontree.models.Offer;
import application.lemontree.services.LocationGetService;

public class OfferActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private OfferAdapter recyclerAdapter;

    // data pulled from the database that we store locally to further filter (or not)
    private ArrayList<Offer> dataList;

    // data to currently display to the user
    private ArrayList<Offer> filteredDataList;

    private ImageButton filterButton;
    private ImageButton mapButton;
    private ImageButton addButton;

    private SearchView searchBar;

    private OfferAdapter offerAdapter;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private LocationGetService locationGetService;

    // radius for finding offers in km. Default 5km
    public int radius = 5;

    private Location currentLocation;
    private FilterDialogFragment filterDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

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


        recyclerView = findViewById(R.id.offerRecyclerView);
        dataList = new ArrayList<>();
        filteredDataList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        offerAdapter = new OfferAdapter(filteredDataList, this);
        recyclerView.setAdapter(offerAdapter);

        // Create a GestureDetector to detect single taps
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;  // Detect single tap
            }
        });

        setupButtons();
        setupSearchBar();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_container, new NavBarFragment())
                .commit();

        filterDialog = new FilterDialogFragment();
    }

    private void setupButtons() {
        // add offer button set up
        addButton = findViewById(R.id.addOfferButton);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OfferActivity.this,
                        CreateOfferActivity.class);
                startActivity(intent);
            }
        });

        // filter button set up
        filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FilterHelper.showFilterDialog(
                        getSupportFragmentManager(),
                        filterDialog,
                        radius, // The current radius
                        newRadius -> {
                            // This is the callback function that will be called when the filter is applied
                            radius = newRadius; // Update the radius in your activity
                            getOffersInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude())); // Re-fetch offers with the new radius
                        }
                );
            }
        });

        // map button set up
        mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OfferActivity.this, MapActivity.class);
                intent.putExtra("source", "offer");
                intent.putExtra("radius", radius);  // Pass the radius value
                startActivity(intent);
            }
        });
    }

    private void setupSearchBar() {
        searchBar = findViewById(R.id.offerSearchView);

        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            // this is called when the user searches something and presses enter
            public boolean onQueryTextSubmit(String s) {
                filteredDataList.clear();
                for (Offer o : dataList) {
                    String a = o.getOfferName().toLowerCase();
                    String b = s.toLowerCase();
                    if (a.contains(b)) {
                        filteredDataList.add(o);
                    }
                }

                offerAdapter.notifyDataSetChanged();
                return true;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            // this is called every time the text is changed in the search box
            public boolean onQueryTextChange(String s) {
                filteredDataList.clear();
                for (Offer o : dataList) {
                    String a = o.getOfferName().toLowerCase();
                    String b = s.toLowerCase();
                    if (a.contains(b)) {
                        filteredDataList.add(o);
                    }
                }

                offerAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Check if permission was granted or denied
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location
                locationGetService = new LocationGetService(this);
            } else {
                // Permission denied, show a fallback or explanation
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Required")
                        .setMessage("This app requires location permission to show nearby offers. Please enable it in settings.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }

    /**
     * Given a document snapshot, this method extracts the data from the database, and yields
     * an Offer object with the appropriate data.
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
        // Get status, default to "active" if null
        String status = document.getString("status");
        o.setStatus(status != null ? status : "active");
        o.setLocation(document.getGeoPoint("location"));

        return o;
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationGetService.getLastLocation(new LocationGetService.OnLocationReceivedListener() {
            @Override
            public void onLocationReceived(Location location) {
                currentLocation = location;
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                getOffersInRadius(new GeoPoint(latitude, longitude));
            }

            @Override
            public void onLocationError(String error) {
                // Handle the error
                Toast.makeText(OfferActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Get all offers from Firestore in a given radius (approx) from a given location
     *
     * @param location user's location
     */
    @SuppressLint({"NotifyDataSetChanged", "DefaultLocale"})
    public void getOffersInRadius(GeoPoint location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First do a bounding box query to find offers in an area that includes the radius.
        // This is done to only query only the offers that are the approximate area and prevents
        // loading offers that are far away.
        // 111 can be used as approximate conversion from kilometers to long/lat for Bounding Box
        // using 110 to make sure not missing entries on the edge (a bit of buffer space)
        double lowerLat = location.getLatitude() - ((double) radius / 110);
        double upperLat = location.getLatitude() + ((double) radius / 110);
        // Longitude needs to account for the the distance between lines of longitude -> using cos
        double lowerLng =
                location.getLongitude() - ((double) radius / (110 * Math.cos(Math.toRadians(location.getLatitude()))));
        double upperLng =
                location.getLongitude() + ((double) radius / (110 * Math.cos(Math.toRadians(location.getLatitude()))));

        db.collection("offers")
                .whereGreaterThanOrEqualTo("location", new GeoPoint(lowerLat, lowerLng))
                .whereLessThanOrEqualTo("location", new GeoPoint(upperLat, upperLng))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) Log.i("getOffersInRadius", "No offers " +
                            "found in radius");

                    dataList.clear();
                    filteredDataList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        return; // no reason to bother filtering.
                    }

                    // add all filtered documents.
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Offer o = getOfferFromDocumentSnapshot(document);

                        // filter out non-active offers
                        if (!"active".equalsIgnoreCase(o.getStatus())) {
                            continue;
                        }

                        GeoPoint offerLocation = document.getGeoPoint("location");
                        String title = document.getString("title");

                        // determine the actual distance to offer
                        if (offerLocation != null) {
                            float[] results = new float[1];
                            Location.distanceBetween(location.getLatitude(),
                                    location.getLongitude(), offerLocation.getLatitude(),
                                    offerLocation.getLongitude(),
                                    results);
                            float distanceInMeters = results[0];
                            float distanceInKilometers = distanceInMeters / 1000;
                            Log.v("getOffersInRadius",
                                    "Offer: " + title + " Distance: " + distanceInKilometers +
                                            " km");
                            o.distance = String.format("%.2f km", distanceInKilometers);

                            if (distanceInKilometers <= radius) {
                                dataList.add(o);
                                filteredDataList.add(o);
                            }

                        }
                    }
                    offerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("getOffersInRadius", "Error getting documents", e);
                });
    }
}
