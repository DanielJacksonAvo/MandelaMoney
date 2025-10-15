package com.example.mandelamoney.view.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DepositFundsController;
import com.example.mandelamoney.controller.MakePaymentController;
import com.example.mandelamoney.controller.RequestPaymentController;
import com.example.mandelamoney.controller.WithdrawFundsController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;

public class ShowFailedActivity extends AppCompatActivity implements ITransactionStatusDisplayView {

    private MakePaymentController makePaymentController;
    private RequestPaymentController requestPaymentController;

    private DepositFundsController depositFundsController;
    private WithdrawFundsController withdrawFundsController;
    private TextView txtToname, txtFromname, txtTonumber, txtFromnumber, txtAmount, txtError, txtToLabel, txtFromLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_failed);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        Object obj = DataShare.receive();
        if (obj instanceof MakePaymentController) {
            makePaymentController = (MakePaymentController) obj;
            makePaymentController.setTransactionStatusDisplayView(this);
            makePaymentController.setContext(this);
        } else if (obj instanceof RequestPaymentController) {
            requestPaymentController = (RequestPaymentController) obj;
            requestPaymentController.setTransactionStatusDisplayView(this);
            requestPaymentController.setContext(this);
        } else if (obj instanceof DepositFundsController) {
            depositFundsController = (DepositFundsController) obj;
            depositFundsController.setTransactionStatusDisplayView(this);
        } else if (obj instanceof WithdrawFundsController) {
            withdrawFundsController = (WithdrawFundsController) obj;
            withdrawFundsController.setTransactionStatusDisplayView(this);
        }
        connectToUi();
        String errorReason = getIntent().getStringExtra("ERROR_REASON");
        if (errorReason == null || errorReason.trim().isEmpty()) {
            errorReason = getString(R.string.failed);}
        displayErrorMessage(errorReason);
        if (makePaymentController != null) {
            makePaymentController.loadTransactionStatusData();
        } else if (requestPaymentController != null) {
            requestPaymentController.loadTransactionStatusData();
        } else if (depositFundsController != null) {
            depositFundsController.loadTransactionStatusDataForFailed();
        } else if (withdrawFundsController != null) {
            withdrawFundsController.loadTransactionStatusDataForFailed();
        }
    }

    private void connectToUi() {
        txtToname = findViewById(R.id.txt_toname_failed);
        txtFromname = findViewById(R.id.txt_fromname_failed);
        txtTonumber = findViewById(R.id.txt_tonumber_failed);
        txtFromnumber = findViewById(R.id.txt_fromnumber_failed);
        txtAmount = findViewById(R.id.txt_amount_failed);
        txtError = findViewById(R.id.txt_error_failed);
        txtToLabel = findViewById(R.id.txt_transactiontypeto_failed);
        txtFromLabel = findViewById(R.id.txt_transactiontypefrom_failed);
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
            if(depositFundsController != null){
                try{
                    depositFundsController.handleCancelDepositFunds();
                }catch (Exception ignore){}
            }
            if(withdrawFundsController != null){
                try{ withdrawFundsController.handleCancelWithdrawFunds();}catch(Exception ignore){}
            }
            finish();
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

    @Override
    public void setFromUserLabelAsBank() {
        txtFromLabel.setText(R.string.bank_account);
    }

    @Override
    public void setToUserLabelAsBank() {
        txtToLabel.setText(R.string.bank_account);
    }

    @Override
    public void setToUserLabelAsMandelaMoney() {
        txtToLabel.setText(R.string.mandela_money);
    }

    @Override
    public void setFromUserLabelAsMandelaMoney() {
        txtFromLabel.setText(R.string.mandela_money);
    }

    private void displayErrorMessage(String error) {
        txtError.setText(error);
    }
}