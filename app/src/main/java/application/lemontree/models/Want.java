package application.lemontree.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Want {
    private String wantId;
    private String wantName;
    private String wantDescription;
    private String wantAvailableDate;
    private String wantCategory;
    private String imageUrl;
    private String createdBy;
    private String userProfileUrl;
    private String createdByUsername;
    private Timestamp createdAt;
    private GeoPoint location;

    public String distance;
    private String status;

    public Want() {
    }

    // Getters and Setters

    public String getWantId() {
        return wantId;
    }

    public void setWantId(String wantId) {
        this.wantId = wantId;
    }

    public String getWantName() {
        return wantName;
    }

    public void setWantName(String wantName) {
        this.wantName = wantName;
    }

    public String getWantDescription() {
        return wantDescription;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public void setWantDescription(String wantDescription) {
        this.wantDescription = wantDescription;
    }

    public String getWantAvailableDate() {
        return wantAvailableDate;
    }

    public void setWantAvailableDate(String wantAvailableDate) {
        this.wantAvailableDate = wantAvailableDate;
    }

    public String getWantCategory() {
        return wantCategory;
    }

    public void setWantCategory(String wantCategory) {
        this.wantCategory = wantCategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public void setUserProfileUrl(String userProfileUrl) {
        this.userProfileUrl = userProfileUrl;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
