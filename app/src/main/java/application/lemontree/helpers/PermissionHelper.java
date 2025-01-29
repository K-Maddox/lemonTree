package application.lemontree.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    private Activity activity;

    public PermissionHelper(Activity activity) {
        this.activity = activity;
    }

}
