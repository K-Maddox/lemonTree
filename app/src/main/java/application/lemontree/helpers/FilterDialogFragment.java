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
}
