package com.example.contacts.contactssscce;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PICK_CONTACT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void pickContact(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        // if we want to limit to ONLY show contacts with an Address use this below
        // intent.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_CONTACT);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactDataUri = data.getData();

            // WORKS without READ_CONTACTS but doesn't contain details, such as address details or phone details (may contain primary_number)
            // https://developer.android.com/guide/components/intents-common#Contacts - states that we should be able to retrieve details for contact
            queryByContactUri(contactDataUri);

            // Get Lookup and raw contactId info - WORKS without READ_CONTACTS
            String contactId = null;
            Cursor cursorLookUpKey = getContentResolver().query(contactDataUri, null, null, null, null);
            String lookupKey = null;
            if (cursorLookUpKey.moveToFirst()) {
                lookupKey = cursorLookUpKey.getString(cursorLookUpKey.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                contactId = cursorLookUpKey.getString(cursorLookUpKey.getColumnIndex(ContactsContract.Data.NAME_RAW_CONTACT_ID));
            }

            try {
                // WORKS with READ_CONTACTS
                // DOESN'T work without READ_CONTACTS (although it should, since we should have temp permission to view Picked Contact rows)
                queryAllDataByContactId(contactId);
            } catch (Exception e) {
                Toast.makeText(this, "queryAllDataByContactId() failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            try {
                // WORKS with READ_CONTACTS
                // DOESN'T work without READ_CONTACTS
                queryAddressData(lookupKey);
            } catch (Exception e) {
                Toast.makeText(this, "queryAddressData() failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            try {
                // WORKS with READ_CONTACTS
                // DOESN'T work without READ_CONTACTS
                queryPhoneData(lookupKey);
            } catch (Exception e) {
                Toast.makeText(this, "queryPhoneDataByContactId() failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }

    private void queryByContactUri(Uri contactDataUri) {
        Log.i(TAG, "contactDataUri: " + contactDataUri);
        Cursor cursor = getContentResolver().query(contactDataUri, null, null, null, null);

        cursor.moveToFirst();

        for (String column : cursor.getColumnNames()) {
            Log.i(TAG, "cursor - column: " + column + ", value: " + cursor.getString(cursor.getColumnIndex(column)));
        }

        cursor.close();
    }

    private void queryAllDataByContactId(String contactId) {

        // Query the table
        Cursor cursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI, null,
                ContactsContract.Data.NAME_RAW_CONTACT_ID + " = " + contactId, null, null);

        if (cursor.moveToFirst()) {

            for (String column : cursor.getColumnNames()) {
                Log.i(TAG, "allDataCursor - column: " + column + ", value: " + cursor.getString(cursor.getColumnIndex(column)));
            }

        }

        cursor.close();
    }

    private void queryAddressData(String lookupKey) {

        String addrWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};

        Cursor cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,null, addrWhere, addrWhereParams, null);

        cursor.moveToNext();

        Log.d(TAG, "queryAddressData: street: " + cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));

        for (String column : cursor.getColumnNames()) {
            Log.i(TAG, "addressCursor - column: " + column + ", value: " + cursor.getString(cursor.getColumnIndex(column)));
        }

        cursor.close();
    }

    private void queryPhoneData(String lookupKey) {

        String addrWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[]{lookupKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

        Cursor cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,null, addrWhere, addrWhereParams, null);

        cursor.moveToNext();

        Log.d(TAG, "queryPhoneData: " + cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        for (String column : cursor.getColumnNames()) {
            Log.i(TAG, "phoneCursor - column: " + column + ", value: " + cursor.getString(cursor.getColumnIndex(column)));
        }

        cursor.close();
    }

}