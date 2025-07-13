package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

import java.sql.SQLException;
public class RecoverAccountController {
    private final Context context;
    private final IRecoverAccountView view;
    private String recoveryCode;
    private String userEmail;
    public RecoverAccountController(Context context, IRecoverAccountView view){
        this.context = context;
        this.view = view;
    }
    public void handleVerify(String recoveryCode) throws SQLException {
        this.recoveryCode = recoveryCode.toLowerCase();
        boolean isValid = MySQLConnector.verifyRecoveryCode(userEmail, recoveryCode, context);
        if (isValid) {
            Intent intent = new Intent(context, ResetPasswordActivity.class);
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("recoveryCode",this.recoveryCode);
            context.startActivity(intent);
            view.finishActivity();
        } else {
            view.showErrorMessage_InvalidCode();
        }
    }

    public void handleCancel(){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
