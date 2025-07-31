package com.example.mandelamoney.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final String currentUserEmail;
    private static final String TAG = "TransactionAdapter";

    public TransactionAdapter(List<Transaction> transactionList, String currentUserEmail) {
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
        Transaction transaction = transactionList.get(position);
        holder.txtDate.setText(transaction.getDate());
        holder.txtTime.setText(transaction.getTime());
        String fromUser = transaction.getFromUser();
        String toUser = transaction.getToUser();
        String displayName;

        holder.txtToFrom.setText(transaction.getDisplayName());

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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Transaction> newList) {
        if (newList == null) {
            Log.w(TAG, "Attempted to update data with a null list.");
            if (!this.transactionList.isEmpty()) {
                this.transactionList.clear();
                notifyDataSetChanged();
            }
            return;
        }

        TransactionDiffCallback diffCallback = new TransactionDiffCallback(this.transactionList, newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.transactionList.clear();
        this.transactionList.addAll(newList);

        diffResult.dispatchUpdatesTo(this);
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

    private static class TransactionDiffCallback extends DiffUtil.Callback {

        private final List<Transaction> oldList;
        private final List<Transaction> newList;

        public TransactionDiffCallback(List<Transaction> oldList, List<Transaction> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}