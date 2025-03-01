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

    // Method to filter offers based on a new radius
    private void filterOffers(int newRadius) {
        radius = newRadius; // Update radius based on filter
        if (currentLocation != null) {
            getOffersInRadius(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
        } else {
            Toast.makeText(MapActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }
}
