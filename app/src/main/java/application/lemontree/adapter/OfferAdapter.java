package application.lemontree.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.Timestamp;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import application.lemontree.R;
import application.lemontree.activities.OfferDetailActivity;
import application.lemontree.models.Offer;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
    private List<Offer> offerList;
    private Context context;

    public OfferAdapter(List<Offer> offerList, Context context) {
        this.offerList = offerList;
        this.context = context;
    }

    @Override
    public OfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_layout, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OfferViewHolder holder, int position) {
        Offer offer = offerList.get(position);
        String offerSub = offer.distance;

        // Use Geocoder to get the suburb (locality) based on the GeoPoint
        String suburb = "Unknown suburb";  // Default value if we can't fetch the suburb
        if (offer.getLocation() != null) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        offer.getLocation().getLatitude(),
                        offer.getLocation().getLongitude(),
                        1
                );
                if (addresses != null && !addresses.isEmpty()) {
                    suburb = addresses.get(0).getLocality();  // Get the locality (suburb)
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Calculate the number of days ago
        if (offer.getCreatedAt() != null) {
            Timestamp createdAt = offer.getCreatedAt();
            Date createdAtDate = createdAt.toDate();
            long differenceInMillis = new Date().getTime() - createdAtDate.getTime();

            // Calculate the difference in days
            long differenceInDays = differenceInMillis / (1000 * 60 * 60 * 24);

            // Determine the time text (Today or X days ago)
            String timeText;
            if (differenceInDays == 0) {
                timeText = "Today";
            } else {
                timeText = differenceInDays + " days ago";
            }

            // Combine distance, suburb, and time into offerSub
            offerSub = offer.distance + " - " + suburb + " - " + timeText;
        } else {
            offerSub = offer.distance + " - " + suburb + " - Unknown time";
        }

        // Set the title
        holder.titleTextView.setText(offer.getOfferName());
        holder.subTextView.setText(offerSub);

        // Set an OnClickListener on the itemView
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OfferDetailActivity.class);
            intent.putExtra("offerId", offer.getOfferId()); // Assuming getId() gets the offerId
            context.startActivity(intent);
        });


        try {
            Glide.with(holder.itemView.getContext())
                    .load(offer.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.previewImageView);

            // Load image using Glide or Picasso (if applicable)
            Glide.with(holder.itemView.getContext())
                    .load(offer.getUserProfileUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profileImageView);
        } catch (Exception ignored) {

        }

    }
}
