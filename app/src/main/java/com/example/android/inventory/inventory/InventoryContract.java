package com.example.android.inventory.inventory;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by prajakkhruasuwan on 12/23/17.
 * Contract for inventory
 */

public class InventoryContract {
    private InventoryContract(){
        // no instantiation for this class
    };

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public final static String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_INVENTORY_NAME = "name";
        public final static String COLUMN_INVENTORY_QUANTITY = "quantity";
        public final static String COLUMN_INVENTORY_PRICE = "price_unit";
        public final static String COLUMN_INVENTORY_SUPPLIER = "supplier";
        public final static String COLUMN_INVENTORY_IMAGE = "image";

        //for simplicity sake, we will use static data for suppliers
        public final static int SUPPLIER_UNKNOW = 0;
        public final static int SUPPLIER_1 = 1;
        public final static int SUPPLIER_2 = 2;
        public final static int SUPPLIER_3 = 3;
    }
}
