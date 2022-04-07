package com.cheyennelabs.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheyennelabs.inventoryapp.data.InventoryContract;
import com.cheyennelabs.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * {@link InventorCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_item_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_item_view);

        // Find the columns of product attributes that we're interested in
        int rowIdColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);

        // Read the product attributes from the Cursor for the current product
        final int row = cursor.getInt(rowIdColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        int quantityTemp = cursor.getInt(quantityColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);


        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        quantityTextView.setText(Integer.toString(quantityTemp));
        priceTextView.setText(String.valueOf(price));

        // Button logic to allow for selling of products from main screen.
        Button buttonSell = (Button) view.findViewById(R.id.button_sell);
        buttonSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantitySell = Integer.parseInt(quantityTextView.getText().toString().trim());

                if (quantitySell <= 0) {
                    // The logic is so that if we are anything other than non zero or negative
                    // that we set it to zero (known good base), and throw out the toast.
                    quantitySell = 0;
                    Toast.makeText(context, R.string.stock_error, Toast.LENGTH_SHORT).show();
                } else {
                    // reduce quantity, format it, put it back in the Contract and then do error
                    // checking
                    quantitySell--;

                    String newQuantity = Integer.toString(quantitySell);
                    // Insert new quantity into the backend DB
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, newQuantity);

                    // Make sure we can append the data, and depending on how that happens throw out
                    // Different toasts
                    Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, row);
                    int rowsAffected = context.getContentResolver().update(currentInventoryUri, values, null, null);

                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(context, "Failed to update record",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(context, "One product succesfully sold",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }
}
