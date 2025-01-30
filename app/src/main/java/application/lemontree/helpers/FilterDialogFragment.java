package application.lemontree.helpers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import application.lemontree.R;

public class FilterDialogFragment {

    // Create interface for the filter listener
    public interface OnFilterAppliedListener {
        void onFilterApplied(int radius);
    }

    private OnFilterAppliedListener listener;

    // Method to set the listener from outside
    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @SuppressLint({"InflateParams", "ResourceType", "DialogFragmentCallbacksDetector"})
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        setCancelable(true);
        final View view = inflater.inflate(R.layout.filters_layout, null);

//        // set text to previous values
//        if (getContext() instanceof OfferActivity) {
//            EditText radiusText = view.findViewById(R.id.editRadius);
//            radiusText.setText(String.valueOf(((OfferActivity) getContext()).radius));
//        }

        // Retrieve the radius from arguments (default to 5 if not found)
        int currentRadius = getArguments() != null ? getArguments().getInt("radius", 5) : 5;

        // Set the radius text in the dialog
        EditText radiusText = view.findViewById(R.id.editRadius);
        radiusText.setText(String.valueOf(currentRadius));

        builder.setTitle("Filter")
                .setView(view)
                .setPositiveButton("Apply", (dialogInterface, i) -> {
                    int radius;
                    try {
                        radius = Integer.parseInt(String.valueOf(radiusText.getText()));
                        if (radius < 0) {
                            radius = -radius;
                        }
                    } catch (NumberFormatException ignored) {
                        radius = 5;
                    }
                    if (listener != null) {
                        listener.onFilterApplied(radius);
                    }
                })
                .setNegativeButton("Cancel", ((dialogInterface, i) -> {
                    Objects.requireNonNull(this.getDialog()).cancel();
                }));


        return builder.create();
    }
}
