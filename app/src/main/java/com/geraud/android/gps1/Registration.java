package com.geraud.android.gps1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.geraud.android.gps1.User_Setup.Setup;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class Registration extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //my app will support phone number authentication only
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );
        // show available sign in option to new user
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginTheme)
                        .setLogo(R.drawable.logi_logo)
                        .setIsSmartLockEnabled(false, false)
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
                Toasty.success(getApplicationContext(), "Successfully Authenticated", Toast.LENGTH_SHORT, true).show();
                startActivity(new Intent(getApplicationContext(), Setup.class));
                finish();
                //if the phone number data already exists in the database send the user directly to the maps activity else if new user send the
                //user to the setup activity
//                String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "User Cant Be Null").getPhoneNumber();
//                if (phoneNumber != null) {
//                    mDatabaseReference.child(phoneNumber).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists())
//                                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
//                            else
//                                startActivity(new Intent(getApplicationContext(), Setup.class));
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            Toast.makeText(Registration.this, "Registration ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                if (response == null)
                    Toasty.error(getApplicationContext(), "You cancelled the Operation", Toast.LENGTH_SHORT).show();

                if (Objects.requireNonNull(Objects.requireNonNull(response).getError()).getErrorCode() == ErrorCodes.NO_NETWORK)
                    Toasty.error(getApplicationContext(), "NO Network, Check Internet Connection", Toast.LENGTH_SHORT, true).show();

                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.UNKNOWN_ERROR)
                    Toasty.error(getApplicationContext(), "An Unknown Error Occurred", Toast.LENGTH_SHORT, true).show();

            }

        }
    }

}