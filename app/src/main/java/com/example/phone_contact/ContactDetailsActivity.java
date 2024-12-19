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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

public class ContactDetailsActivity extends AppCompatActivity  {
    // UI elements
    private ImageView contactImage;
    private TextView contactName, contactFullName, contactPhone, contactEmail;
    private Button goBackBtn, callBtn;

    // Strings
    private String name, phone, email, pendingPhoneNumber;
    // ints
    private int imageRes;
    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_WRITE_CONTACTS_PERMISSION = 2;
    // Intents
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        // Find views
       contactImage = findViewById(R.id.contact_image);
        contactName = findViewById(R.id.contact_name);
        contactFullName = findViewById(R.id.contact_full_name);
      contactPhone = findViewById(R.id.contact_phone);
        contactEmail = findViewById(R.id.contact_email);
        goBackBtn = findViewById(R.id.button_go_back);
        callBtn = findViewById(R.id.button_call);

        // Retrieve data from intent
        intent = getIntent();
        name = intent.getStringExtra("contactName");
        phone = intent.getStringExtra("contactPhone");
        email = intent.getStringExtra("contactEmail");
        imageRes = intent.getIntExtra("contactImage", R.drawable.placeholder);

        // Populate UI
        contactImage.setImageResource(imageRes);
        contactName.setText(name);
        contactFullName.setText(name);
        contactPhone.setText(phone);
        contactEmail.setText(email);

        // Listeners
        goBackBtn.setOnClickListener(v -> finish());

        callBtn.setOnClickListener(v -> makePhoneCall(phone));
    }

    private void makePhoneCall(String phoneNumber) {
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

        // convert photo to bitmap
        Bitmap photo = BitmapFactory.decodeResource(getResources(), imageRes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] photoData = bos.toByteArray();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Insert Name
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                .build());

        // Insert Phone Number
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        // Insert Email
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, contactEmail)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build());

        // Insert Photo
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoData)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
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
