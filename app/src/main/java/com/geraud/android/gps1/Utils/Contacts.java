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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contacts {

    private Context mContext;
    private DatabaseReference mUserDatabaseReference;
    private ArrayList<String> mContacts = new ArrayList<>();
    private Cursor mContactsCursor;

    public Contacts(Context context) {
        this.mContext = context;
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("USER");
    }

    //getting ISO (CM,ENG,,, )
    private String getCountryISO() {
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null)
            if (!telephonyManager.getNetworkCountryIso().equals(""))
                iso = telephonyManager.getNetworkCountryIso();

        return CountryToPhonePrefix.getPhone(Objects.requireNonNull(iso, "ISO Code Can't Be Null"));
    }

    public List<String> getAllContacts() {
        String ISOPrefix = getCountryISO();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        mContactsCursor = mContext.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);

        if (mContactsCursor != null) {
            mContactsCursor.moveToFirst();

            while (mContactsCursor.moveToNext()) {
                //String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                String phone = mContactsCursor.getString(mContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                if (!String.valueOf(phone.charAt(0)).equals("+")) //if the phone number doesn't have a country code then the number is considered of your country
                    phone = ISOPrefix + phone;

                checkIfUsesApp(phone,mContactsCursor);
            }
        }
//        if (mContactsCursor != null)
//            mContactsCursor.close();

        return mContacts;
    }

    private void checkIfUsesApp( String phone,Cursor cursor) {
        //check if user exists in the app database
        mUserDatabaseReference.child(phone).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    mContacts.add(Objects.requireNonNull(dataSnapshot.child("phone").getValue(),"user phone number is null").toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, "CheckIfUsesApp ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        //since its async method it should only close cursor if it hs finished checking the last value in the database
//        if(!cursor.moveToNext())
//            cursor.close();
    }

}
