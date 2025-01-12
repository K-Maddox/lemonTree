package application.lemontree.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;

import application.lemontree.repositories.UserRepository;

public class LocationGetService {

    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    private UserRepository userRepository;

    public LocationGetService(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        userRepository = new UserRepository(context);
    }

    public void getLastLocation(OnLocationReceivedListener listener) {
        // Check for permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listener.onLocationError("Location Permissions not granted, cant get offers in area.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                listener.onLocationReceived(location);
                //Send location to repository UserRepository (for last location update)
                userRepository.updateUserLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
            } else {
                listener.onLocationError("Location is null");
            }
        });
    }

    public interface OnLocationReceivedListener {
        void onLocationReceived(Location location);

        void onLocationError(String error);
    }
}
