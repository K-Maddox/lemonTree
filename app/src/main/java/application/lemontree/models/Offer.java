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
}
