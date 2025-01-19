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
}
