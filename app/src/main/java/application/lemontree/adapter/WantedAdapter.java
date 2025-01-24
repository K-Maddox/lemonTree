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

import com.google.firebase.Timestamp;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import application.lemontree.R;
import application.lemontree.activities.WantedDetailActivity;
import application.lemontree.models.Want;

public class WantedAdapter extends RecyclerView.Adapter<WantedAdapter.WantedViewHolder> {
    private List<Want> wantedList;
    private Context context;

    public WantedAdapter(List<Want> wantedList, Context context) {
        this.wantedList = wantedList;
        this.context = context;
    }

    @Override
    public WantedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.want_layout, parent, false);
        return new WantedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WantedViewHolder holder, int position) {
        Want want = wantedList.get(position);

            // Use Geocoder to get the suburb (locality) based on the GeoPoint
            String suburb = "Unknown suburb";  // Default value if we can't fetch the suburb
            if (want.getLocation() != null) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            want.getLocation().getLatitude(),
                            want.getLocation().getLongitude(),
                            1
                    );
                    if (addresses != null && !addresses.isEmpty()) {
                        suburb = addresses.get(0).getLocality();  // Get the locality (suburb)
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Set the text for subTextView with distance, suburb, and time
            holder.subTextView.setText(want.distance + " - " + suburb + " - " + timeText);
        } else {
            holder.subTextView.setText(want.distance + " - Unknown suburb - Unknown time");
        }

        // Set OnClickListener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WantedDetailActivity.class);
            intent.putExtra("wantId", want.getWantId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return wantedList.size();
    }

    public static class WantedViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, subTextView;
        ImageView profileImageView, previewImageView;

        public WantedViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.wantedTitleTextView);
            subTextView = itemView.findViewById(R.id.subTextView);
//            profileImageView = itemView.findViewById(R.id.wantedProfileImageView);
//            previewImageView = itemView.findViewById(R.id.wantedPreviewImageView);
        }
    }
}
