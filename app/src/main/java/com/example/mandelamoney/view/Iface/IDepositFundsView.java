package com.example.mandelamoney.view.Iface;

public interface IDepositFundsView {

    void showMissingAmountError(String message);
    void hideMissingAmountError();
    void showInvalidAmountError(String message);
    void hideInvalidAmountError();
    void showMissingBankNameError(String message);
    void hideMissingBankNameError();
    void showInvalidBankNameError(String message);
    void hideInvalidBankNameError();
    void showMissingBranchCodeError(String message);
    void hideMissingBranchCodeError();
    void showInvalidBranchCodeError(String message);
    void hideInvalidBranchCodeError();
    void showMissingAccountNumberError(String message);
    void hideMissingAccountNumberError();
    void showInvalidAccountNumberError(String message);
    void hideInvalidAccountNumberError();
    void showMissingCvvError(String message);
    void hideMissingCvvError();
    void showInvalidCvvError(String message);
    void hideInvalidCvvError();
    void showMissingExpiryDateError(String message);
    void hideMissingExpiryDateError();
    void showInvalidExpiryDateError(String message);
    void hideInvalidExpiryDateError();
    void showMissingAccountHolderError(String message);
    void hideMissingAccountHolderError();
    void showInvalidAccountHolderError(String message);
    void hideInvalidAccountHolderError();
    void showLoadingSpinner();
    void hideLoadingSpinner();
    void finishActivity();

}
