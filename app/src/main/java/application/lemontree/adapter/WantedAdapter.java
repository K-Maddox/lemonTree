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
