package application.lemontree.repositories;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.messaging.FirebaseMessaging;

import application.lemontree.models.User;
import application.lemontree.services.MyFirebaseMessagingService;

public class UserRepository {
    private static final String TAG = "inUserRepository";
    private Context context;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MutableLiveData<FirebaseUser> userLiveData;
    private MutableLiveData<String> loginErrorMessage;
    private MutableLiveData<String> signupErrorMessage;

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userLiveData = new MutableLiveData<>();
        loginErrorMessage = new MutableLiveData<>();
        signupErrorMessage = new MutableLiveData<>();
        Log.d(TAG, "UserRepository initialized");
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getLoginErrorMessage() {
        return loginErrorMessage;
    }

    public LiveData<String> getSignupErrorMessage() {
        return signupErrorMessage;
    }

    /**
     * Register a new user with email and password (also signs the user in automatically)
     * Creates a basic user profile with username in Firestore after successful registration.
     *
     * @param username
     * @param email
     * @param password
     */
    public void signupUser(String email, String password, String username) {
        Log.d(TAG, "start User sign up with email：" + email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                User newUser = new User(
                                        user.getUid(),
                                        email,
                                        username,
                                        new GeoPoint(0.0, 0.0),
                                        true,
                                        true,
                                        5,
                                        "https://firebasestorage.googleapis.com/v0/b/lemontreedev-eef1e.appspot.com/o/public%2Fdefaultprofile.jpg?alt=media&token=fef2920a-098c-4fec-9002-9fb95fe5e932"
                                );

                                db.collection("profiles").document(user.getUid())
                                        .set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "DocumentSnapshot successfully written! uid:" + user.getUid());
                                            userLiveData.postValue(user);
                                            retrieveAndUploadFCMtoken();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error writing document", e);
                                            signupErrorMessage.postValue("Error writing user data to Firestore.");
                                        });
                                Log.d(TAG, "user doc create successfully");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            signupErrorMessage.postValue(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Sign in a user with email and password
     *
     * @param email
     * @param password
     */
    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // login success, save user information to LiveData
                        FirebaseUser user = mAuth.getCurrentUser();
                        userLiveData.postValue(user);
                        Log.d(TAG, "login success" + user.getUid());
                        retrieveAndUploadFCMtoken();
                    } else {
                        // login failed, update LiveData with error message
                        loginErrorMessage.postValue(task.getException().getMessage());
                    }
                });
    }

    /**
     * check whether the user is logged in
     */
    public void checkUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userLiveData.postValue(currentUser);
        }
    }

    /**
     * update user location in Firestore
     *
     * @param lastLocation
     */
    public void updateUserLocation(GeoPoint lastLocation) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            final long TIME_BETWEEN_UPDATES = 300000; // 5 minutes in milliseconds
            long currentTime = System.currentTimeMillis();

            // Retrieve last update time from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("location_prefs", MODE_PRIVATE);
            long lastUpdateTime = prefs.getLong("last_update_time", 0);

            // Check if enough time has passed
            if (currentTime - lastUpdateTime < TIME_BETWEEN_UPDATES) {
                Log.d(TAG, "Skipping location update: time threshold not met");
                return;
            }

            DocumentReference userRef = db.collection("profiles").document(currentUser.getUid());
            userRef.update("lastLocation", lastLocation)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Location updated successfully");
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong("last_update_time", currentTime);
                        editor.apply();
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating location", e));
        }
    }

    /**
     * Retrieves the app instances FCMtoken and adds it to the user profile on Firebase.
     * The methods calls the onNewToken method to force a upload now that the user is authenticated.
     */
    private void retrieveAndUploadFCMtoken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("notification", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d("notification", "FCM registration Token: " + token);

                    //force token upload to server
                    new MyFirebaseMessagingService().onNewToken(token);

                }).addOnFailureListener(e -> {
                    Log.e("notification", "Fetching FCM registration token failed", e);
                });
    }

}
