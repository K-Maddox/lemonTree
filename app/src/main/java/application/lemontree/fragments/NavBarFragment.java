package application.lemontree.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import application.lemontree.R;
import application.lemontree.activities.MessageActivity;
import application.lemontree.activities.OfferActivity;
import application.lemontree.activities.Profile;
import application.lemontree.activities.WantedActivity;

public class NavBarFragment extends Fragment {

    private Button offerButton;
    private Button wantButton;
    private Button messageButton;
    private Button homeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nav_bar, container, false);

        offerButton = view.findViewById(R.id.btn_nav_offer);
        wantButton = view.findViewById(R.id.btn_nav_want);
        messageButton = view.findViewById(R.id.btn_nav_message);
        homeButton = view.findViewById(R.id.btn_nav_home);

        offerButton.setOnClickListener(v -> {
            updateNavBarColors(offerButton);
            startActivity(new Intent(getActivity(), OfferActivity.class));
            getActivity().overridePendingTransition(0, 0);
        });

        wantButton.setOnClickListener(v -> {
            updateNavBarColors(wantButton);
            startActivity(new Intent(getActivity(), WantedActivity.class));
            getActivity().overridePendingTransition(0, 0);
        });

        messageButton.setOnClickListener(v -> {
            updateNavBarColors(messageButton);
            startActivity(new Intent(getActivity(), MessageActivity.class));
            getActivity().overridePendingTransition(0, 0);
        });

        homeButton.setOnClickListener(v -> {
            updateNavBarColors(homeButton);
            startActivity(new Intent(getActivity(), Profile.class));
            getActivity().overridePendingTransition(0, 0);
        });

        return view;
    }

    // update nav bar color to show the current page
    private void updateNavBarColors(Button currentButton) {
        resetNavBarColors();

        // change the icon color
        currentButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        currentButton.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
    }

    // reset colors of all buttons of nav bar
    private void resetNavBarColors() {
        offerButton.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
        wantButton.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
        messageButton.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
        homeButton.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
    }

    @Override
    public void onResume() {
        super.onResume();

        // decide which button should be highlighted based on the current activity
        if (getActivity() instanceof OfferActivity) {
            updateNavBarColors(offerButton);
        } else if (getActivity() instanceof WantedActivity) {
            updateNavBarColors(wantButton);
        } else if (getActivity() instanceof MessageActivity) {
            updateNavBarColors(messageButton);
        } else if (getActivity() instanceof Profile) {
            updateNavBarColors(homeButton);
        }
    }
}
