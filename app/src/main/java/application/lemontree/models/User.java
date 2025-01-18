package application.lemontree.models;

import com.google.firebase.firestore.GeoPoint;

public class User {

    private String uid;
    private String email;
    private String username;
    private GeoPoint lastLocation;
    private boolean newOfferNotification;
    private boolean newChatNotification;
    private int newOfferRadius;
    private String profilePictureURL;

    public User() {
    }

    public User(String uid, String email, String username, GeoPoint lastLocation,
                boolean newOfferNotification, boolean newChatNotification, int newOfferRadius,
                String profilePictureURL) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.lastLocation = lastLocation;
        this.newOfferNotification = newOfferNotification;
        this.newChatNotification = newChatNotification;
        this.newOfferRadius = newOfferRadius;
        this.profilePictureURL = profilePictureURL;
    }
}
