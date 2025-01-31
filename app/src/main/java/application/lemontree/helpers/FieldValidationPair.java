package application.lemontree.helpers;

import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;

public class FieldValidationPair {
    public EditText editText;
    public TextInputLayout inputLayout;

    public FieldValidationPair(EditText editText, TextInputLayout inputLayout) {
        this.editText = editText;
        this.inputLayout = inputLayout;
    }
}
