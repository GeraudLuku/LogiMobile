package com.geraud.android.gps1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.User_Setup.Setup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class Registration extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("USER");

        //my app will support phone number authentication only
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );
        // show available sign in option to new user
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginTheme)
                        .setLogo(R.drawable.logi_logo)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Toasty.success(getApplicationContext(), "Successfully Created an Account!", Toast.LENGTH_SHORT, true).show();

                //if the phone number data already exists in the database send the user directly to the maps activity else if new user send the
                //user to the setup activity
                String mUserPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                mDatabaseReference.child(mUserPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                        }else{
                            startActivity(new Intent(getApplicationContext(),Setup.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                if (response == null)
                    Toasty.error(getApplicationContext(),"You cancelled the Operation",Toast.LENGTH_SHORT).show();
                // response.getError().getErrorCode() and handle the error.
                // ...
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toasty.error(getApplicationContext(), "NO Network, Check Internet Connection", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toasty.error(getApplicationContext(), "An Unknown Error Occured", Toast.LENGTH_SHORT, true).show();
                    return;
                }
            }

        }
    }

}