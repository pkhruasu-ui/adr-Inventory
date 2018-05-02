package com.example.android.inventory.inventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventory.inventory.InventoryContract.InventoryEntry;

/**
 * Created by prajakkhruasuwan on 12/23/17.
 * Inventory db initializer/contract.
 */

public class InventoryDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PRICE + " REAL NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                + InventoryEntry.COLUMN_INVENTORY_IMAGE + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_SUPPLIER + " INTEGER NOT NULL);";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }
}
