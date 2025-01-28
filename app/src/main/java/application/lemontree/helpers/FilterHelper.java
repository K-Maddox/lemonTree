package application.lemontree.helpers;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import java.util.function.Consumer;

public class FilterHelper {

    // This method will show the filter dialog and apply the filter callback when a radius is selected
    public static void showFilterDialog(FragmentManager fragmentManager, FilterDialogFragment dialog, int radius, Consumer<Integer> filterCallback) {
        if (dialog == null) {
            dialog = new FilterDialogFragment();
        }

        // Pass the current radius as an argument to the dialog fragment
        Bundle args = new Bundle();
        args.putInt("radius", radius);
        dialog.setArguments(args);

        // Set the listener for when the filter is applied
        dialog.setOnFilterAppliedListener(newRadius -> {
            filterCallback.accept(newRadius);  // Pass the new radius to the callback
        });

        // Show the filter dialog
        dialog.show(fragmentManager, "filterDialog");
    }
}
