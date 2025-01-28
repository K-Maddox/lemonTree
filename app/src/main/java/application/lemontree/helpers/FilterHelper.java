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


    }
}
