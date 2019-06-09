package com.geraud.android.gps1.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contacts {

    private Context mContext;

    private DatabaseReference mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("USER");

    private List<String> mContacts = new ArrayList<>();

    public Contacts(Context context) {
        this.mContext = context;
    }

    //getting ISO (CM,ENG,,, )
    private String getCountryISO() {
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null)
            if (!telephonyManager.getNetworkCountryIso().equals(""))
                iso = telephonyManager.getNetworkCountryIso();

        return CountryToPhonePrefix.getPhone(Objects.requireNonNull(iso,"ISO Code Cant Be Null"));
    }

    public List<String> getAllContacts() {

        String ISOPrefix = getCountryISO();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor != null) {
            while (cursor.moveToNext()) {

                //String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                if (!String.valueOf(phone.charAt(0)).equals("+")) //if the phone number doesn't have a country code then the number is considered of your country
                    phone = ISOPrefix + phone;

                checkIfUsesApp(phone);
                cursor.moveToNext();
            }
        }
        if (cursor != null)
            cursor.close();

        return mContacts;
    }

    private void checkIfUsesApp(final String phone) {
        Query query = mUserDatabaseReference.orderByChild("phone").equalTo(phone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    mContacts.add(phone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, "CheckIfUsesApp ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
