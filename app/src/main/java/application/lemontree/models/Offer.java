package application.lemontree.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Offer {
    private String offerId;
    private String offerName;
    private String offerDescription;
    private String offerAvailableDate;
    private String offerPickUpLocation;
    private GeoPoint location;
    private String offerCategory;
    private String imageUrl;
    private String createdBy;
    private String userProfileUrl;
    private String createdByUsername;
    private Timestamp createdAt;
    public String distance;
    private String status;

    public Offer() {
    }

    // Getters and Setters

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public String getOfferDescription() {
        return offerDescription;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public String getOfferAvailableDate() {
        return offerAvailableDate;
    }

    public void setOfferAvailableDate(String offerAvailableDate) {
        this.offerAvailableDate = offerAvailableDate;
    }

    public String getOfferPickUpLocation() {
        return offerPickUpLocation;
    }

    public void setOfferPickUpLocation(String offerPickUpLocation) {
        this.offerPickUpLocation = offerPickUpLocation;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getOfferCategory() {
        return offerCategory;
    }

    public void setOfferCategory(String offerCategory) {
        this.offerCategory = offerCategory;
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
