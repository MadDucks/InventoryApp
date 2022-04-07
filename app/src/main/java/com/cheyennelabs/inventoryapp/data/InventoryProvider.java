package com.cheyennelabs.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.cheyennelabs.inventoryapp.InventoryActivity;
import com.cheyennelabs.inventoryapp.data.InventoryContract.InventoryEntry;


/**
 * {@link ContentProvider} for Inventory app.
 */
public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the Inventory table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single item in the inventory table
     */
    private static final int INVENTORY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.


        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    /**
     * Database helper object
     */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // For the INVENTORY code, query the inventory table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inventory table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ID:

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the inventory table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a inventory into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInventory(Uri uri, ContentValues values) {


        String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_PRODUCT);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Double price = values.getAsDouble(InventoryEntry.COLUMN_INVENTORY_PRICE);
        if ((price < 0) && (price != null)) {
            throw new IllegalArgumentException("Product requires a price");
        }

        String suppliername = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIERNAME);
        if (suppliername == null) {
            throw new IllegalArgumentException("Product requires a Supplier Name");
        }

        String supplierphonenumber = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIERPHONENUMBER);
        if (supplierphonenumber == null) {
            throw new IllegalArgumentException("Product requires a Supplier Phone Number");
        }

        int quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        if ((quantity < 0) && (quantity != 0)) {
            throw new IllegalArgumentException("Product requires a quantity");
        }


        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update inventory in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link InventoryEntry#COLUMN_INVENTORY_PRODUCT key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRODUCT)) {
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_PRODUCT);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        // check that the price value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            Double price = values.getAsDouble(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if ((price < 0) && (price != null)) {
                throw new IllegalArgumentException("Product requires a price");
            }
        }

        // check that the supplier value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIERNAME)) {
            String suppliername = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIERNAME);
            if (suppliername == null) {
                throw new IllegalArgumentException("Product requires a supplier name");
            }
        }
        // check that the supplierphone value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIERPHONENUMBER)) {
            String supplierphonenumber = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIERPHONENUMBER);
            if (supplierphonenumber == null) {
                throw new IllegalArgumentException("Product requires a Supplier Phone Number");
            }
        }
        // check that the quantity value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            int quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if ((quantity < 0) && (quantity != 0)) {
                throw new IllegalArgumentException("Product requires a Quantity");
            }
        }


        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}