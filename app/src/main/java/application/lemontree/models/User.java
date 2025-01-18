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

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public GeoPoint getLastLocation() {
        return lastLocation;
    }

    public boolean isNewOfferNotification() {
        return newOfferNotification;
    }

    public boolean isNewChatNotification() {
        return newChatNotification;
    }

    public void setNewChatNotification(boolean newChatNotification) {
        this.newChatNotification = newChatNotification;
    }

    public int getNewOfferRadius() {
        return newOfferRadius;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLastLocation(GeoPoint lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void setNewOfferNotification(boolean newOfferNotification) {
        this.newOfferNotification = newOfferNotification;
    }

    public void setNewOfferRadius(int newOfferRadius) {
        this.newOfferRadius = newOfferRadius;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }
}
