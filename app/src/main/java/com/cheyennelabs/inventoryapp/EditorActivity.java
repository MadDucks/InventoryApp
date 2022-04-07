package com.cheyennelabs.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.cheyennelabs.inventoryapp.data.InventoryContract.InventoryEntry;
import com.cheyennelabs.inventoryapp.data.InventoryDbHelper;


public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Setting the private variables for Editor Activity
     */
    private static final int EXISTING_INVENTORY_LOADER = 0;

    private Uri mCurrentInventoryUri;

    private EditText mProductEditText;

    private EditText mSupplierNameEditText;

    private EditText mPhoneEditText;

    private EditText mPriceEditText;

    private EditText mQuantityEditText;

    private boolean mInventoryHasChanged = false;

    private Button mIncreaseQuantityButton;

    private Button mDecreaseQuantityButton;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mInventoryHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Build out button and intent to call the dialer with the supplier's phone number
        Button buttonSupplierPhone = findViewById(R.id.button_supplier_contact);

        buttonSupplierPhone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText phoneNumberText = (EditText) findViewById(R.id.edit_supplier_number);
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumberText.getText().toString(), null));
                startActivity(intent);

            }
        });

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        // If the intent DOES NOT contain a inventory content URI, then we know that we are
        // creating a new product.
        if (mCurrentInventoryUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mPhoneEditText = (EditText) findViewById(R.id.edit_supplier_number);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity_text);
        mIncreaseQuantityButton = (Button) findViewById(R.id.increase_quantity_btn);
        mDecreaseQuantityButton = (Button) findViewById(R.id.decrease_quantity_btn);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.

        mProductEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        // Create two buttons - pulled in the Resources themselves to make sure we get the correct values
        // Change the quantity unless it's going negative, make a toast.
        mDecreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                EditText mQuantityEditTextTemp = (EditText) findViewById(R.id.edit_quantity_text);
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                    quantity = Integer.parseInt(mQuantityEditTextTemp.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext() , getString(R.string.nonulls_quantity_toast),
                            Toast.LENGTH_SHORT).show();
                }
                if (quantity <= 0) {
                    Toast.makeText(EditorActivity.this, "You must have a positive Quantity", Toast.LENGTH_SHORT).show();
                } else {
                    mQuantityEditTextTemp.setText(String.valueOf(--quantity));
                }
            }
        });

        mIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                EditText mQuantityEditTextTemp = (EditText) findViewById(R.id.edit_quantity_text);
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                    quantity = Integer.parseInt(mQuantityEditTextTemp.getText().toString());
                }  else {
                Toast.makeText(getApplicationContext() , getString(R.string.nonulls_quantity_toast),
                        Toast.LENGTH_SHORT).show();
            }
                if (quantity >= 0) {
                    mQuantityEditTextTemp.setText(String.valueOf(++quantity));
                }
            }
        });
    }


    public boolean isValidProduct() {


        // big if/else if tree that looks at each edit text box and determines if it's empty
        // To be executed whenever we save
        if (TextUtils.isEmpty(mProductEditText.getText().toString().trim())) {
            // Show the error in a toast message.
            Toast.makeText(this, getString(R.string.required_name_toast),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(mPriceEditText.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.required_price_toast),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(mSupplierNameEditText.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.required_supplier_toast),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(mPhoneEditText.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.required_supplierphone_toast),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.required_quantity_toast),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productString = mProductEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString());

        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(productString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(phoneString)
                && (quantityInt == 0)) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT, productString);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIERNAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIERPHONENUMBER, phoneString);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantityInt);


        // Determine if this is a new or existing product by checking if mCurrentInventoryUri is null or not
        if (mCurrentInventoryUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentInventoryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database only if all fields are filled in
                if (isValidProduct()) {
                    saveProduct();
                    // Exit activity
                    finish();
                    return true;
                } else {
                    return false;
                }
                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link InventoryActivity}.
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIERNAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIERPHONENUMBER,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int suppliernameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIERNAME);
            int supplierphoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIERPHONENUMBER);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String suppliername = cursor.getString(suppliernameColumnIndex);
            String supplierphone = cursor.getString(supplierphoneColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);

            // Update the views on the screen with the values from the database
            mProductEditText.setText(name);
            mPriceEditText.setText(price);
            mSupplierNameEditText.setText(suppliername);
            mPhoneEditText.setText(supplierphone);
            mQuantityEditText.setText(quantity);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductEditText.setText("");
        mPriceEditText.setText("");
        mSupplierNameEditText.setText("");
        mPhoneEditText.setText("");
        mQuantityEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventorytUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}

