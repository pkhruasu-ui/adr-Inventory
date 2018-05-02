package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.inventory.InventoryContract;

import static com.example.android.inventory.R.id.quantity;

/**
 * Created by prajakkhruasuwan on 12/23/17.
 * Bind and populate the list item
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.name);

        nameView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME)));

        TextView quantityView = (TextView) view.findViewById(quantity);
        quantityView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY)));

        TextView priceView = (TextView) view.findViewById(R.id.price);
        priceView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE)));

        //Handle buttons and add onClickListeners
        Button sellBtn = (Button) view.findViewById(R.id.action_sell);

        final int _id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID)));
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY));

        sellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update quantity
                int newQuantity = quantity - 1;
                if (quantity > 0) {
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, newQuantity);
                    String selection = InventoryContract.InventoryEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(_id)};

                    int rowsUpdated = context.getContentResolver().update(InventoryContract.InventoryEntry.CONTENT_URI, values, selection, selectionArgs);

                    if (rowsUpdated == 0) {
                        Toast.makeText(context, context.getResources().getString(R.string.editor_update_item_fail), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.editor_update_item_success), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //Might be better UX wise to set it on name instead
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditorActivity.class);
                intent.putExtra("uri", ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, _id).toString());
                context.startActivity(intent);
            }
        });

    }
}
