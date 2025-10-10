package com.example.mandelamoney.view.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.UnlockController;
import com.example.mandelamoney.view.Iface.IUnlockView;
import java.util.concurrent.atomic.AtomicReference;

public class UnlockActivity extends AppCompatActivity implements IUnlockView {
    private UnlockController unlockController;
    private TextView txtError;
    private ConstraintLayout loadingSpinner;
    private Button btnBiometrics, btnUnlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unlock_account);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        unlockController = new UnlockController(this, this);
        connectToUI();
        hideLoadingSpinner();
        hideErrorMessage();

    }
    public void connectToUI() {
        btnUnlock = findViewById(R.id.btn_unlock);
        EditText tbxUserPassword = findViewById(R.id.tbx_password_unlock);
        txtError = findViewById(R.id.txt_error_unlock_application);
        ImageView imgPasswordIcon = findViewById(R.id.img_password_unlock);
        btnBiometrics = findViewById(R.id.btn_biometrics_unlock);
        TextView btnLogOut = findViewById(R.id.btn_logout_unlock);
        loadingSpinner = findViewById(R.id.unlock_loading_spinner);

        if (btnUnlock != null && tbxUserPassword != null) {
            configureUnlockButton(btnUnlock, tbxUserPassword);
        }
        if (imgPasswordIcon != null && tbxUserPassword != null) {
            configurePasswordVisibility(imgPasswordIcon, tbxUserPassword);
        }
        if (btnLogOut != null) {
            configureLogoutButton(btnLogOut);
        }
        if(btnBiometrics != null){
            configureBiometricsButton(btnBiometrics);
        }
    }

    private void configureBiometricsButton(Button btnBiometrics) {
        if (btnBiometrics != null) {
            unlockController.configureBiometricsStatus();
            btnBiometrics.setOnClickListener((view)-> unlockController.handleBiometrics());
        }
    }

    private void configureLogoutButton(TextView btnLogOut) {
        if (btnLogOut != null){
           btnLogOut.setOnClickListener((view)-> unlockController.handleLogout());
        }
    }

    private void configurePasswordVisibility(ImageView imgPasswordIcon, EditText tbxUserPassword) {
        if (tbxUserPassword == null || imgPasswordIcon == null) {
            Log.e("UnlockActivity", "Password EditText or Icon is null in configurePasswordVisibility.");
            return;
        }

        tbxUserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tbxUserPassword.setTransformationMethod(new PasswordTransformationMethod());
        imgPasswordIcon.setImageResource(R.drawable.img_password_icon);

        AtomicReference<Boolean> isPasswordVisible = new AtomicReference<>(false);

        tbxUserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    imgPasswordIcon.setImageResource(isPasswordVisible.get() ?
                            R.drawable.img_eye_closed : R.drawable.img_eye_open);
                } else {
                    imgPasswordIcon.setImageResource(R.drawable.img_password_icon);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        imgPasswordIcon.setOnClickListener(view -> {
            if (tbxUserPassword.getText().length() == 0) {
                return;
            }

            boolean temp = !isPasswordVisible.get();
            isPasswordVisible.set(temp);

            if (isPasswordVisible.get()) {
                tbxUserPassword.setTransformationMethod(null);
                imgPasswordIcon.setImageResource(R.drawable.img_eye_closed);
            } else {
                tbxUserPassword.setTransformationMethod(new PasswordTransformationMethod());
                imgPasswordIcon.setImageResource(R.drawable.img_eye_open);
            }

            tbxUserPassword.setSelection(tbxUserPassword.getText().length());
        });
    }


    private void configureUnlockButton(Button btnUnlock, EditText tbxUserPassword) {
        if (btnUnlock != null) {
            btnUnlock.setOnClickListener((view) -> {
                String userPassword = String.valueOf(tbxUserPassword.getText());
                unlockController.handleUnlock(userPassword);
            });
        } else {
            Log.e("UnlockActivity", "Unlock button is null in configureUnlockButton.");
        }
    }



    @Override
    public void showErrorMessage() {
        if(txtError != null){
            txtError.setVisibility(VISIBLE);
        }
    }

    @Override
    public void hideErrorMessage() {
        if(txtError != null){
            txtError.setVisibility(GONE);
        }
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void hideLoadingSpinner() {
        loadingSpinner.setVisibility(GONE);
    }

    @Override
    public void showLoadingSpinner() {
        loadingSpinner.setVisibility(VISIBLE);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void enabledBiometrics() {
        btnBiometrics.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mandelaYellow)));
        btnUnlock.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        btnBiometrics.setEnabled(true);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void disabledBiometrics() {
        btnBiometrics.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey20)));
        btnUnlock.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mandelaYellow)));
        btnBiometrics.setEnabled(false);
    }
}
