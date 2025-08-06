package com.example.mandelamoney.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.MakePaymentController;
import com.example.mandelamoney.controller.RequestPaymentController;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;

public class ShowFailedActivity extends AppCompatActivity implements ITransactionStatusDisplayView {

    private MakePaymentController makePaymentController;
    private RequestPaymentController requestPaymentController;
    private TextView txtToname, txtFromname, txtTonumber, txtFromnumber, txtAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_failed);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Object obj = DataShare.receive();
        if (obj instanceof MakePaymentController) {
            makePaymentController = (MakePaymentController) obj;
            makePaymentController.setTransactionStatusDisplayView(this);
            makePaymentController.setContext(this);
        } else if (obj instanceof RequestPaymentController) {
            requestPaymentController = (RequestPaymentController) obj;
            requestPaymentController.setTransactionStatusDisplayView(this);
            requestPaymentController.setContext(this);

        }
        connectToUi();
        if (makePaymentController != null) {
            makePaymentController.loadTransactionStatusData();
        } else if (requestPaymentController != null) {
            requestPaymentController.loadTransactionStatusData();
        }
    }

    private void connectToUi() {
        txtToname = findViewById(R.id.txt_toname_failed);
        txtFromname = findViewById(R.id.txt_fromname_failed);
        txtTonumber = findViewById(R.id.txt_tonumber_failed);
        txtFromnumber = findViewById(R.id.txt_fromnumber_failed);
        txtAmount = findViewById(R.id.txt_amount_failed);
        Button btnClose = findViewById(R.id.btn_generate_qr_failed);
        configureCloseButton(btnClose);

    }

    private void configureCloseButton(Button btnClose) {
        btnClose.setOnClickListener(v -> {
            if (makePaymentController != null) {
                try {
                    makePaymentController.handleCancel();
                } catch (Exception ignore) {}
            }
            if (requestPaymentController != null) {
                try {
                    requestPaymentController.handleCancelButton();
                } catch (Exception ignore) {}
            }
        });
    }

    @Override
    public void displayToUserName(String name) {
        txtToname.setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        txtFromname.setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        txtTonumber.setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        txtFromnumber.setText(number);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void displayAmount(double amount) {
        txtAmount.setText("R " + String.format("%.2f", amount));
    }

    @Override
    public void finishActivity() {
        finish();
    }
}