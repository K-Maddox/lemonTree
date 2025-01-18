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


}
