package com.example.mandelamoney.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.TransactionDetails;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<TransactionDetails> transactionList;
    private final String currentUserEmail;

    // Constructor
    public TransactionAdapter(List<TransactionDetails> transactionList, String currentUserEmail) {
        this.transactionList = transactionList;
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

        // Set date and time
        holder.txtDate.setText(transaction.getDate());
        holder.txtTime.setText(transaction.getTime());

        // Display the "other" user
        String toFromUser;
        if (transaction.getFromUser().equals(currentUserEmail)) {
            toFromUser = transaction.getToUser(); // outgoing
        } else {
            toFromUser = transaction.getFromUser(); // incoming
        }
        holder.txtToFrom.setText(toFromUser);

        // Display amount with optional formatting
        holder.txtAmount.setText(String.format("R %.2f", transaction.getAmount()));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
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
