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
}
