package application.lemontree.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import application.lemontree.repositories.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = "UserViewModel";
    private UserRepository userRepository;
    private LiveData<FirebaseUser> userLiveData;
    private LiveData<String> loginErrorMessage;
    private LiveData<String> signupErrorMessage;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        userLiveData = userRepository.getUserLiveData();
        loginErrorMessage = userRepository.getLoginErrorMessage();
        signupErrorMessage = userRepository.getSignupErrorMessage();
    }

    public void signupUser(String email, String password, String username) {
        Log.d(TAG, "调用 userRepository.signupUser 方法");
        userRepository.signupUser(email, password, username);
    }

    public void loginUser(String email, String password) {
        userRepository.loginUser(email, password);
    }

    public void checkUserLoggedIn() {
        userRepository.checkUserLoggedIn();
    }

    public void updateUserLocation(GeoPoint geoPoint) {
        userRepository.updateUserLocation(geoPoint);
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
}
