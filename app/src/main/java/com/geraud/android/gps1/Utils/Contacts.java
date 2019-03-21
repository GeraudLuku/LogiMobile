package com.geraud.android.gps1.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Contacts {

    private Context mContext;
    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("USER");
    private List<String> mContacts = new ArrayList<>();

    public Contacts(Context context) {
        this.mContext = context;
    }

    //getting ISO (CM,ENG,,, )
    private String getCountryISO() {
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null)
            if (!telephonyManager.getNetworkCountryIso().equals(""))
                iso = telephonyManager.getNetworkCountryIso();

        return CountryToPhonePrefix.getPhone(iso);
    }

    public List<String> getAllContacts() {

        String ISOPrefix = getCountryISO();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
        cursor.moveToFirst();
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
        cursor.close();
        return mContacts;
    }

    private void checkIfUsesApp(final String phone) {
        //get the mContacts from firebase
        Query query = mDatabaseReference.orderByChild("phone").equalTo(phone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    mContacts.add(phone);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
