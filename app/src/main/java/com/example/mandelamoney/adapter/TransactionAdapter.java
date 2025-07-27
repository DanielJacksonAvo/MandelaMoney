package com.example.mandelamoney.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.TransactionDetails;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<TransactionDetails> transactionList;
    private final String currentUserEmail;
    private static final String TAG = "TransactionAdapter";

    // Constructor
    public TransactionAdapter(List<TransactionDetails> transactionList, String currentUserEmail) {
        // Always ensure transactionList is not null, and make a defensive copy
        this.transactionList = (transactionList != null) ? new ArrayList<>(transactionList) : new ArrayList<>();
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionDetails transaction = transactionList.get(position);
        holder.txtDate.setText(transaction.getDate());
        holder.txtTime.setText(transaction.getTime());

        // Display name already formatted in the controller
        String fromUser = transaction.getFromUser();
        String toUser = transaction.getToUser();
        String displayName;

        if (fromUser.equals(currentUserEmail)) {
            displayName = toUser != null ? toUser : "Unknown";
        } else {
            displayName = fromUser;
        }

        holder.txtToFrom.setText(displayName);
        float amount = transaction.getAmount();
        String amountText;
        int amountColor;
        if (transaction.isSelfTransaction()) {
            amountText = String.format("± R %.2f", Math.abs(amount));
            amountColor = holder.itemView.getContext().getResources().getColor(R.color.white);
        } else if (amount > 0) {
            amountText = String.format("+ R %.2f", amount);
            amountColor = holder.itemView.getContext().getResources().getColor(R.color.mandelaYellow);
        } else {
            amountText = String.format("- R %.2f", Math.abs(amount));
            amountColor = holder.itemView.getContext().getResources().getColor(R.color.white);
        }

        holder.txtAmount.setText(amountText);
        holder.txtAmount.setTextColor(amountColor);

    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateData(List<TransactionDetails> newList) {
        Log.d(TAG, "Updating adapter with new data. New size: " + newList.size() + ", Old size: " + transactionList.size());
        // Ensure newList is not null before clearing and adding
        if (newList != null) {
            transactionList.clear();
            transactionList.addAll(newList);
            // Notify the adapter that the data set has changed to refresh the RecyclerView
            notifyDataSetChanged();
        } else {
            Log.w(TAG, "Attempted to update data with a null list.");
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtTime, txtToFrom, txtAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txt_transaction_date_card);
            txtTime = itemView.findViewById(R.id.txt_transaction_time_card);
            txtToFrom = itemView.findViewById(R.id.txt_transaction_tofrom_name_card);
            txtAmount = itemView.findViewById(R.id.txt_transaction_amount_card);
        }
    }
}