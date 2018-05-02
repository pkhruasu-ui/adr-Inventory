package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.inventory.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    InventoryCursorAdapter inventoryCursorAdapter;

    // These are the Contacts rows that we will retrieve
    static final String[] PROJECTION = new String[]{InventoryEntry._ID,
            InventoryEntry.COLUMN_INVENTORY_NAME,
            InventoryEntry.COLUMN_INVENTORY_PRICE,
            InventoryEntry.COLUMN_INVENTORY_QUANTITY};

    public static final int INVENTORY_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView inventoryListView = (ListView) findViewById(R.id.list_view_inventory);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(inventoryCursorAdapter);

        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        LayoutInflater myinflater = getLayoutInflater();
        ViewGroup myHeader = (ViewGroup) myinflater.inflate(R.layout.header_layout, inventoryListView, false);
        inventoryListView.addHeaderView(myHeader, null, false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_delete_all_entires:
                showDeleteConfirmationDialog();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteData();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteData() {
        int rowDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);

        if (rowDeleted != 0) {
            Toast.makeText(this, R.string.success_delete_all_items, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.fail_delete_all_items, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, InventoryEntry.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        inventoryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryCursorAdapter.swapCursor(null);
    }

    public void gotoEdit(View v){
        Intent intent = new Intent(MainActivity.this, EditorActivity.class);
        startActivity(intent);
    }
}
