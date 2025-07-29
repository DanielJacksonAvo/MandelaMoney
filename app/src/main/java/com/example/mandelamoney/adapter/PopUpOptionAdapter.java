package com.example.mandelamoney.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.mandelamoney.R;

import java.util.List;

public class PopUpOptionAdapter extends ArrayAdapter<String> {
    private final LayoutInflater inflater;
    private final int selectedIndex;

    public PopUpOptionAdapter(Context context, List<String> items, int selectedIndex) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
        this.selectedIndex = selectedIndex;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_popup_option, parent, false);
        TextView textView = view.findViewById(R.id.txt_menu_option);
        textView.setText(getItem(position));

        if (position == selectedIndex) {
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey10));
        }

        return view;
    }
}
