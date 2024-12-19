package com.example.phone_contact;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context context;
    private List<Contact> contactList;

    // Constructor now requires both context and a contact list
    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        // Make a copy of the list to avoid direct manipulation
        this.contactList = new ArrayList<>(contactList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        // Get current contact
        Contact contact = contactList.get(position);

        // Bind data to views
        holder.contactName.setText(contact.getName());
        holder.contactEmail.setText(contact.getEmail());
        holder.contactPhone.setText(contact.getPhoneNumber());
        holder.contactImage.setImageResource(contact.getImageResource());

        // Set OnClickListener for phone icon to initiate a call
        holder.phoneIcon.setOnClickListener(v -> {
            String phoneNumber = contact.getPhoneNumber();
            ((MainActivity) context).makePhoneCall(phoneNumber);
        });

        // Set OnClickListener for the contact image
        holder.contactImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactDetailsActivity.class);
            intent.putExtra("contactName", contact.getName());
            intent.putExtra("contactEmail", contact.getEmail());
            intent.putExtra("contactPhone", contact.getPhoneNumber());
            intent.putExtra("contactImage", contact.getImageResource());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateContacts(List<Contact> newList) {
        contactList.clear();
        contactList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName, contactEmail, contactPhone;
        ImageView contactImage;
        ImageView phoneIcon; // Add reference to phone icon

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contact_name);
            contactEmail = itemView.findViewById(R.id.contact_email);
            contactPhone = itemView.findViewById(R.id.contact_phone);
            contactImage = itemView.findViewById(R.id.contact_image);
            phoneIcon = itemView.findViewById(R.id.phone_icon); // Initialize the phone icon
        }
    }
}
