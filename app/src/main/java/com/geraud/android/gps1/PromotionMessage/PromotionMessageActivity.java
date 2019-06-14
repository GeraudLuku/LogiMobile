package com.geraud.android.gps1.PromotionMessage;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.geraud.android.gps1.Models.PromotionMessage;
import com.geraud.android.gps1.Models.Subscription;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PromotionMessageActivity extends AppCompatActivity {
    private PromotionMessage promotionMessage;
    private Button mSubscribeBtn;
    private DatabaseReference mDatabase;

    private String mUserPhone;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_message);

        //get phone
        mUserPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User is null").getPhoneNumber();
        //get notification content
        promotionMessage = (PromotionMessage) Objects.requireNonNull(getIntent().getExtras(), "promotion message is null")
                .getSerializable("promotionMessage");
        //get user object
        mDatabase.child("USER").child(mUserPhone).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PromotionMessageActivity.this, "Cant get user object", Toast.LENGTH_SHORT).show();
            }
        });

        //initialize views
        TextView title = findViewById(R.id.title);
        TextView body = findViewById(R.id.body);
        mSubscribeBtn = findViewById(R.id.button);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        title.setText(promotionMessage.getTitle());
        body.setText(promotionMessage.getBody());

    }

    public void subscribe(View v) {
        //subscription object
        Subscription subscription = new Subscription(promotionMessage.getBranchId(), promotionMessage.getCompanyId());
        //save branch under user
        mDatabase.child("USER").child(mUserPhone).child("subscriptions")
                .push()
                .setValue(subscription)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //save also under branch document
                        mDatabase.child("BRANCH").child(promotionMessage.getCompanyId()).child(promotionMessage.getBranchId())
                                .child("subscribedUsers")
                                .child(mUser.getNotificationKey()).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //remove button
                                mSubscribeBtn.setVisibility(View.GONE);
                            }
                        });
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
