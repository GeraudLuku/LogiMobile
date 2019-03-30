package com.geraud.android.gps1.Chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.geraud.android.gps1.Camera.CameraActivity;
import com.geraud.android.gps1.R;

public class ChatsActivity extends AppCompatActivity {

    private BottomNavigationView mBottmNavigation;
    private FloatingActionButton mNewChatBtn;

    private ChatsFragment mChatsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialiseFragment();

        mChatsFragment = new ChatsFragment();

        //new chat button
        mNewChatBtn = findViewById(R.id.btn_new_chat);
        mNewChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FindUserActivity.class));
            }
        });

        //bottom navigation
        mBottmNavigation = findViewById(R.id.chat_activity_bottom_navigation);
        mBottmNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bottom_chat_camera:
                        //open camera
                        Intent cameraIntent = new Intent(getApplicationContext(),CameraActivity.class);
                        cameraIntent.putExtra("chat","chatsActivity");
                        startActivity(cameraIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    private void initialiseFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.chat_activity_main_container, mChatsFragment);
        fragmentTransaction.commit();
    }

}
