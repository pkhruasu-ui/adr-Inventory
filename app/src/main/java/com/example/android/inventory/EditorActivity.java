package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.inventory.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by prajakkhruasuwan on 12/23/17.
 * Add/delete/edit item
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    Uri itemUri;

    public static final int ITEM_LOADER = 1;

    private EditText nameEditTextView;
    private EditText priceEditTextView;
    private TextView quantityEditTextView;
    private TextView supplierTextView;
    private Spinner supplierSpinner;
    private ImageView itemImageView;
    private Button itemImageBtn;

    private String mCurrentPhotoPath;
    private String mOldPhotoPath;
    /**
     * ID of the selectedSupplier. The possible values are:
     * 0 for unknown, the rest from arrays.xml in res/values.
     */
    private int selectedSupplier = 0;

    // for ordering
    private EditText orderEditTextView;

    public static final long DEFAULT_PRICE = 0;
    public static final int DEFAULT_QUANTITY = 0;
    public static final int DEFAULT_ORDER_QUANTITY = 1;

    // callback detect if user make any change
    private boolean mItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_detail);

        Bundle bundles = getIntent().getExtras();
        if(bundles != null){
            itemUri = Uri.parse(bundles.getString("uri"));
        }

        if(itemUri != null){
            setTitle(getString(R.string.mode_edit_item));

            Bundle bundle = new Bundle();
            bundle.putString("uri", itemUri.toString());
            getLoaderManager().initLoader(ITEM_LOADER, bundle, this);
            //edit mode don't allow changing supplier. Switch to text

        }else{
            setTitle(getString(R.string.mode_add_item));
            invalidateOptionsMenu();
        }
        //bind view
        nameEditTextView = (EditText) findViewById(R.id.edit_item_name);
        priceEditTextView = (EditText) findViewById(R.id.edit_item_price);
        quantityEditTextView = (TextView) findViewById(R.id.edit_item_quantity);
        supplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);
        itemImageView = (ImageView) findViewById(R.id.item_image_view);
        itemImageBtn = (Button) findViewById(R.id.item_image_btn);
        supplierTextView = (TextView) findViewById(R.id.supplier_text);
        // if insert item. set default for quantity
        if(itemUri == null){
            quantityEditTextView.setText(String.valueOf(DEFAULT_QUANTITY));
        }else{
            // switch to text if in edit mode
            supplierSpinner.setVisibility(View.GONE);
            supplierTextView.setVisibility(View.VISIBLE);
        }

        nameEditTextView.setOnTouchListener(mTouchListener);
        priceEditTextView.setOnTouchListener(mTouchListener);
        quantityEditTextView.setOnTouchListener(mTouchListener);

        orderEditTextView = (EditText) findViewById(R.id.edit_order_item);
        orderEditTextView.setText(String.valueOf(DEFAULT_ORDER_QUANTITY));

        setupSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (itemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveItem();
                finish();
                break;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                break;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

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

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
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

    /**
     * Setup the dropdown spinner that allows the user to select the selectedSupplier for the item.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        supplierSpinner.setAdapter(supplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        supplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_1))) {
                        selectedSupplier = InventoryEntry.SUPPLIER_1;
                    } else if (selection.equals(getString(R.string.supplier_2))) {
                        selectedSupplier = InventoryEntry.SUPPLIER_2;
                    } else if (selection.equals(getString(R.string.supplier_3))) {
                        selectedSupplier = InventoryEntry.SUPPLIER_3;
                    } else {
                        selectedSupplier = InventoryEntry.SUPPLIER_UNKNOW; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSupplier = InventoryEntry.SUPPLIER_UNKNOW; // Unknown
            }
        });
    }


    private void saveItem(){
        if(itemUri == null &&
                TextUtils.isEmpty(quantityEditTextView.getText().toString().trim()) &&
                TextUtils.isEmpty(priceEditTextView.getText().toString().trim()) &&
                TextUtils.isEmpty(nameEditTextView.getText().toString().trim()) &&
                TextUtils.isEmpty(mCurrentPhotoPath)){
            return;
        }

        if(itemUri == null){
            insertItem();
        }else{
            updateItem();
        }
    }

    private void insertItem(){
        ContentValues values = prepareSaveValues();
        // insert
        Uri rowId = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (rowId == null) {
            Toast.makeText(this, R.string.editor_insert_item_failed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.editor_insert_item_success, Toast.LENGTH_LONG).show();
        }
    }

    private void updateItem(){
        ContentValues values = prepareSaveValues();
        // insert
        int rowsUpdated = getContentResolver().update(itemUri, values,null,null);

        if (rowsUpdated == 0) {
            Toast.makeText(this, R.string.editor_update_item_fail, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.editor_update_item_success, Toast.LENGTH_LONG).show();
        }
    }

    private ContentValues prepareSaveValues(){
        ContentValues contentValues = new ContentValues();

        contentValues.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameEditTextView.getText().toString().trim());

        long price = DEFAULT_PRICE;
        if(!TextUtils.isEmpty(priceEditTextView.getText().toString().trim())){
            price = Long.parseLong(priceEditTextView.getText().toString().trim());
        }
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);

        int quantity = DEFAULT_QUANTITY;
        if(!TextUtils.isEmpty(quantityEditTextView.getText().toString().trim())){
            quantity = Integer.parseInt(quantityEditTextView.getText().toString().trim());
        }
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);
        //selectedSupplier was prepared
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, selectedSupplier);
        //image
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, mCurrentPhotoPath);

        return contentValues;
    }

    private void deleteItem(){
        if(itemUri != null){
            int rowDeleted = getContentResolver().delete(itemUri,null,null);

            if(rowDeleted != 0){
                Toast.makeText(this, R.string.success_delete_item, Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(this, R.string.fail_delete_item, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri =  Uri.parse(args.getString("uri"));

        return new CursorLoader(this, uri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToNext()){
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String price = cursor.getString((priceColumnIndex));
            int supplier = cursor.getInt(supplierColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            nameEditTextView.setText(name);
            priceEditTextView.setText(price);
            quantityEditTextView.setText(quantity);

            switch (supplier) {
                case InventoryEntry.SUPPLIER_1:
                    selectedSupplier = 1;
                    supplierSpinner.setSelection(1);
                    supplierTextView.setText(R.string.supplier_1);
                    break;
                case InventoryEntry.SUPPLIER_2:
                    selectedSupplier = 2;
                    supplierSpinner.setSelection(2);
                    supplierTextView.setText(R.string.supplier_2);
                    break;
                case InventoryEntry.SUPPLIER_3:
                    selectedSupplier = 3;
                    supplierSpinner.setSelection(3);
                    supplierTextView.setText(R.string.supplier_3);
                    break;
                default:
                    supplierSpinner.setSelection(0);
                    supplierTextView.setText(R.string.supplier_unknown);
                    break;
            }
            // set image on view
            if(!TextUtils.isEmpty(image)){
                mCurrentPhotoPath = image;
                handleCameraPhoto();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
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

    public void increaseQuantity(View v){

        int quantity = DEFAULT_QUANTITY;
        if(!TextUtils.isEmpty(quantityEditTextView.getText().toString().trim())){
            quantity = Integer.parseInt(quantityEditTextView.getText().toString().trim());
        }

        int orderQuantity = DEFAULT_QUANTITY;
        if(!TextUtils.isEmpty(orderEditTextView.getText().toString().trim())){
            orderQuantity = Integer.parseInt(orderEditTextView.getText().toString().trim());
        }
        // add then set the text
        quantity += orderQuantity;
        quantityEditTextView.setText(String.valueOf(quantity));
        // because change has been made
        mItemHasChanged = true;
    }

    public void decreaseQuantity(View v){
        int quantity = DEFAULT_QUANTITY;
        if(!TextUtils.isEmpty(quantityEditTextView.getText().toString().trim())){
            quantity = Integer.parseInt(quantityEditTextView.getText().toString().trim());
        }

        int orderQuantity = DEFAULT_QUANTITY;
        if(!TextUtils.isEmpty(orderEditTextView.getText().toString().trim())){
            orderQuantity = Integer.parseInt(orderEditTextView.getText().toString().trim());
        }
        // add then set the text
        quantity -= orderQuantity;
        if(quantity < 0){
            quantity = 0;
        }
        quantityEditTextView.setText(String.valueOf(quantity));
        // because change has been made
        mItemHasChanged = true;
    }

    public void orderMoreSupply(View v){
        /*
         * Note: For simplicity sake, supplier url is static in string resource. Normally it would be dynamic from db but that
         * would increase complexity for no gain.
         */
        int supplier_r = getStringIdentifier(this, getString(R.string.__supplier_id_constructor, String.valueOf(selectedSupplier)));
        String supplierUrl;
        if(supplier_r != 0){
            supplierUrl = getResources().getString(supplier_r);
        }else{
            // no email, do nothing
            Toast.makeText(this, R.string.supplier_no_email, Toast.LENGTH_SHORT).show();
            return;
        }
        // prepare information
        String quantity = orderEditTextView.getText().toString().trim();
        if(TextUtils.isEmpty(quantity) || Integer.parseInt(quantity) <= 0){
            Toast.makeText(this, R.string.require_order_quantity, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                getString(R.string.mailto),supplierUrl, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.mail_quantity_order_subject, nameEditTextView.getText().toString().trim()));
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.mail_quantity_order_amount, quantity));

        Intent chooser = Intent.createChooser(emailIntent, getString(R.string.select_browser));

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    /*
    Util to help construct id as R string
     */
    public static int getStringIdentifier(Context context, String name) {
        return context.getResources().getIdentifier(name, "string", context.getPackageName());
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    public void dispatchTakePictureIntent(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this,"Error taking picture", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.inventory.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            handleCameraPhoto();
        }
    }

    private void handleCameraPhoto(){
        if(mCurrentPhotoPath != null){
            setPic();
        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = itemImageView.getWidth();
        int targetH = itemImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        itemImageView.setImageBitmap(bitmap);
        itemImageView.setVisibility(View.VISIBLE);
        itemImageBtn.setVisibility(View.GONE);
        // delete the old image if exist
        if(mOldPhotoPath != null){
            File fdelete = new File(mOldPhotoPath);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    // not sure if we should leave log or not
                    Log.i("DELETE OLD IMAGE", "SUCCESS " + mOldPhotoPath);
                } else {
                    Log.i("DELETE OLD IMAGE", "FAIL" + mOldPhotoPath);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // save old path, will delete image once new photo is taken
        if(mCurrentPhotoPath != null && !mCurrentPhotoPath.equalsIgnoreCase(mOldPhotoPath)){
            mOldPhotoPath = mCurrentPhotoPath;
        };
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
