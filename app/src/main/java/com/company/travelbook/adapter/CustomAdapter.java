package com.company.travelbook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.company.travelbook.R;
import com.company.travelbook.model.Place;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Place> {

    ArrayList<Place> places;
    Context context;

    public CustomAdapter(@NonNull Context context, ArrayList<Place> places) {
        super(context, R.layout.custom_list_row, places);
        this.places = places;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View customView = layoutInflater.inflate(R.layout.custom_list_row, parent,false);
        TextView nameTextView = customView.findViewById(R.id.textView);
        nameTextView.setText(places.get(position).getName());
        System.out.println(places.get(position).getName());

        return customView;
    }
}
