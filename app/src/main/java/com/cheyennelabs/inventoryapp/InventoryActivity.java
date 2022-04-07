/**
 * Author: Christofer Hardy
 * Date: 11-13-18
 * Purpose: This is a fuller featured Inventory application.  Logic is in the app to not take in
 * bad Data, as well as build UI elements.  Intents for phone as well as other requirements
 *
 */

package com.cheyennelabs.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheyennelabs.inventoryapp.data.InventoryContract.InventoryEntry;

// Main activty class.   Most core logic carried out here, as well as TextViews

public class InventoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    // required for Loader

    private static final int INVENTORY_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the inventory data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of inventory data in the Cursor.
        // There is no inventory data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> productView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific inventory that was clicked on,
                // by appending the "id" (passed as input to this method) \
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link EditorActivity} to display the data for the current inventory.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIERNAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIERPHONENUMBER,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link InventoryCursorAdapter} with this new cursor containing updated Inventory data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}





