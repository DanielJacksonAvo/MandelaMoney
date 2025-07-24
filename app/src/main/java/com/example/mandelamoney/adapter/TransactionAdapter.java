package com.example.mandelamoney.adapter;

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

    // Constructor
    public TransactionAdapter(List<TransactionDetails> transactionList, String currentUserEmail) {
        this.transactionList = new ArrayList<>(transactionList); // Defensive copy for mutability
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionDetails transaction = transactionList.get(position);
        holder.txtDate.setText(transaction.getDate());
        holder.txtTime.setText(transaction.getTime());

        String fromUser = transaction.getFromUser();
        String toUser = transaction.getToUser();
        String displayName;

        if (currentUserEmail != null && fromUser != null && fromUser.equals(currentUserEmail)) {
            displayName = toUser != null ? toUser : "Unknown";
        } else if (fromUser != null) {
            displayName = fromUser;
        } else {
            displayName = "Unknown";
        }

        holder.txtToFrom.setText(displayName);
        holder.txtAmount.setText(String.format("R %.2f", transaction.getAmount()));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateData(List<TransactionDetails> newList) {
        Log.d("TransactionAdapter", "Updating adapter with new data. Size: " + newList.size());
        transactionList.clear();
        transactionList.addAll(newList);
        notifyDataSetChanged();
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
