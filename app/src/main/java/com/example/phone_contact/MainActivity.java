package com.example.phone_contact;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_WRITE_CONTACTS_PERMISSION = 2;

    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList, filteredContactList;
    private SearchView searchView;
    private EditText searchEditText;

    private String pendingPhoneNumber; // To store the phone number when requesting permissions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize contact list and populate it
        contactList = new ArrayList<>();
        populateContactList();

        // Filtered contact list for search functionality
        filteredContactList = new ArrayList<>(contactList);

        // Set up the adapter with this activity's context and the filteredContactList
        contactAdapter = new ContactAdapter(this, filteredContactList);
        recyclerView.setAdapter(contactAdapter);

        // Set up the SearchView
        searchView = findViewById(R.id.search_view);
        setupSearchView(searchView);
        searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.neutral));
        searchEditText.setHint("Search Contacts by name");

    }

    // Populate contact list with sample data
    private void populateContactList() {
        contactList.add(new Contact("Ethan Matthews", "ethan.matthews@example.com", "7412 678123", R.drawable.man1));
        contactList.add(new Contact("Lucas Carter", "lucas.carter@example.com", "7930 558765", R.drawable.man2));
        contactList.add(new Contact("Benjamin Adams", "benjamin.adams@example.com", "020 7893 6298", R.drawable.man3));
        contactList.add(new Contact("Samuel Thomson", "samuel.thompson@example.com", "020 8636 6068", R.drawable.man4));
        contactList.add(new Contact("Emma Bennett", "emma.bennett@example.com", "7939 553456", R.drawable.woman1));
        contactList.add(new Contact("Liz Bacon", "liz.bacon@example.com", "7930 590511", R.drawable.woman2));
        contactList.add(new Contact("Sapphire Robinson", "sapphire.robinson@example.com", "7930 876239", R.drawable.woman3));
        contactList.add(new Contact("Barbara Brooks", "barbara.brooks@example.com", "7939 347600", R.drawable.woman4));

        contactList.add(new Contact("Will Smith", "willsmith@example.com", "7412 678123", R.drawable.man1));
        contactList.add(new Contact("Ben Dag", "bendag@example.com", "7930 558765", R.drawable.man2));
        contactList.add(new Contact("Adam Jones", "adamjones@example.com", "020 7893 6298", R.drawable.man3));
        contactList.add(new Contact("Sam Brown", "Sambrown@example.com", "020 8636 6068", R.drawable.man4));
        contactList.add(new Contact("Suki Milla", "sukimilla@example.com", "7939 553456", R.drawable.woman1));
        contactList.add(new Contact("Alexis Ada", "alexisada@example.com", "7930 590511", R.drawable.woman2));
        contactList.add(new Contact("Bekky Darcie", "bekkydarcie@example.com", "7930 876239", R.drawable.woman3));
        contactList.add(new Contact("Freya Thom", "freyathom@example.com", "7939 347600", R.drawable.woman4));
    }

    // Setup SearchView
    private void setupSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterContacts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        });
    }

    // Filter contacts based on query
    private void filterContacts(String query) {
        filteredContactList.clear();
        if (query.isEmpty()) {
            filteredContactList.addAll(contactList);
        } else {
            for (Contact contact : contactList) {
                if (contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                        contact.getEmail().toLowerCase().contains(query.toLowerCase())) {
                    filteredContactList.add(contact);
                }
            }
        }
        contactAdapter.updateContacts(filteredContactList);
    }

    // Initiate a phone call
    public void makePhoneCall(String phoneNumber) {
        if (!phoneNumber.startsWith("tel:")) {
            phoneNumber = "tel:" + phoneNumber;
        }

        // Check for CALL_PHONE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the CALL_PHONE permission
            pendingPhoneNumber = phoneNumber;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            // Permission granted for calling, start the call and then try to save the contact
            startCall(phoneNumber);
        }
    }

    private void startCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(phoneNumber));
        startActivity(callIntent);

        // After starting the call, request to write contacts if needed
        pendingPhoneNumber = phoneNumber;
        requestWriteContactsPermission();
    }

    private void requestWriteContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_WRITE_CONTACTS_PERMISSION);
        } else {
            // Already have permission, add the contact
            addContactToPhone(pendingPhoneNumber);
        }
    }

    private void addContactToPhone(String phoneNumber) {
        if (phoneNumber.startsWith("tel:")) {
            phoneNumber = phoneNumber.substring(4);
        }

        // Find the contact name, email, and image resource from the original contactList
        String contactName = "Unknown";
        String contactEmail = null;
        int imageResId = R.drawable.user; // fallback if no match found
        for (Contact c : contactList) {
            if (c.getPhoneNumber().equals(phoneNumber)) {
                contactName = c.getName();
                contactEmail = c.getEmail();
                imageResId = c.getImageResource(); // get the contact's image from the Contact object
                break;
            }
        }

        // If no email found, set a default
        if (contactEmail == null) {
            contactEmail = "noemail@example.com";
        }

        // Convert the contact's image resource to a byte array
        // Use the imageResId found from the contact above
        android.graphics.Bitmap photo = android.graphics.BitmapFactory.decodeResource(getResources(), imageResId);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        photo.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, bos);
        byte[] photoData = bos.toByteArray();

        ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<>();
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.RawContacts.CONTENT_URI)
                .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Insert Name
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE,
                        android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                .build());

        // Insert Phone Number
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE,
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE,
                        android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        // Insert Email
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE,
                        android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS, contactEmail)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Email.TYPE,
                        android.provider.ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build());

        // Insert Photo
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE,
                        android.provider.ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Photo.PHOTO, photoData)
                .build());

        try {
            getContentResolver().applyBatch(android.provider.ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pendingPhoneNumber = null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // CALL_PHONE permission granted
                if (pendingPhoneNumber != null) {
                    startCall(pendingPhoneNumber);
                }
            } else {
                // Permission denied for CALL_PHONE
                Toast.makeText(this, "CALL_PHONE permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_WRITE_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // WRITE_CONTACTS permission granted
                if (pendingPhoneNumber != null) {
                    addContactToPhone(pendingPhoneNumber);
                }
            } else {
                // Permission denied for WRITE_CONTACTS
                Toast.makeText(this, "WRITE_CONTACTS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
