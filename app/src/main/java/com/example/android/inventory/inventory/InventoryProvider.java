package com.example.android.inventory.inventory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventory.inventory.InventoryContract.InventoryEntry;

/**
 * Created by prajakkhruasuwan on 12/23/17.
 * Content Provider for UI
 */

public class InventoryProvider extends ContentProvider {

    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;
    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int ITEMS = 100;
    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int ITEM_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long rowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowDeleted = 0;
        int match = sUriMatcher.match(uri);

        switch (match){
            case ITEMS:
                rowDeleted = db.delete(InventoryEntry.TABLE_NAME,null,null);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowDeleted = db.delete(InventoryEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if(rowDeleted != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = sUriMatcher.match(uri);
        switch (match){
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, values, selection, selectionArgs);
        }

        return 0;
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Check that the name is not null
        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_NAME)){
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)){
            Long price = values.getAsLong(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if(price == null){
                throw new IllegalArgumentException("Item requires a price or 0");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)){
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if(quantity == null){
                throw new IllegalArgumentException("Item requires a quantity or 0");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER)){
            Integer supplier = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_SUPPLIER);
            if(supplier == null){
                throw new IllegalArgumentException("Item requires a supplier");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_IMAGE)){
            String supplier = values.getAsString(InventoryEntry.COLUMN_INVENTORY_IMAGE);
            if(supplier == null){
                throw new IllegalArgumentException("Item requires an image");
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowUpdated = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowUpdated >= 1){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match " + match);
        }
    }
}
