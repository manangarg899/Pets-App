package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetsContract;

import org.w3c.dom.Text;

/**
 * Created by Manan on 05-03-2017.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context,c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView name = (TextView)view.findViewById(R.id.name);
        TextView breed = (TextView)view.findViewById(R.id.breed);

        String pet_name = cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_NAME));
        String pet_breed = cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_BREED));

        if(TextUtils.isEmpty(pet_breed))
        {
            pet_breed = "Unknown Breed";
        }
        name.setText(pet_name);
        breed.setText(pet_breed);
    }
}
