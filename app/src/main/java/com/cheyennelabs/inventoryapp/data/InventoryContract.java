package com.cheyennelabs.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

// All the important part of setting up the Contract/SQL/URI variables

public final class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider,
     */
    public static final String CONTENT_AUTHORITY = "com.cheyennelabs.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * path for the app
     */
    public static final String PATH_INVENTORY = "inventory";

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single inventory item.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * Name of database table for invetory
         */
        public final static String TABLE_NAME = "inventory";

        /**
         * Unique ID number for the pet (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Product inventory.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_INVENTORY_PRODUCT = "product";

        /**
         * Price of the item.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_INVENTORY_PRICE = "price";

        /**
         * Quantity of items in inventory.
         * <p>
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_INVENTORY_QUANTITY = "quantity";

        /**
         * Supplier Name.
         * <p>
         * Type: STRING
         */
        public final static String COLUMN_INVENTORY_SUPPLIERNAME = "suppliername";

        /**
         * Supplier phone number.
         * <p>
         * Type: STRING
         */

        public final static String COLUMN_INVENTORY_SUPPLIERPHONENUMBER = "supplierphonenumber";

    }

}