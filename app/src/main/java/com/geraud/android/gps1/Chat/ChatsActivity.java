package com.geraud.android.gps1.Chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.geraud.android.gps1.R;

public class ChatsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;

    private ChatsFragment chatsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        //set title on actionbar
        getSupportActionBar().setTitle("GPS App Name");

        initialilizeFragment();

        chatsFragment = new ChatsFragment();

        //new chat button
        floatingActionButton = findViewById(R.id.btn_new_chat);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext() , FindUserActivity.class ));
            }
        });

        //bottom navigation
        bottomNavigationView =findViewById(R.id.chat_activity_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment container = getSupportFragmentManager().findFragmentById(R.id.chat_activity_main_container);

                switch (menuItem.getItemId()){

                    case R.id.bottom_chat_chats:
                        replaceFragment(chatsFragment,container);
                        return true;

                   default:
                       return false;
                }
            }
        });

    }

    private void initialilizeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.chat_activity_main_container, chatsFragment);

        //fragmentTransaction.hide(notificationFragment);

        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment,Fragment curentFragment){
        FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();

        if(fragment == chatsFragment){
            //fragmentTransaction.hide(accountFragment);
        }

        fragmentTransaction.show(fragment);

        fragmentTransaction.commit();
    }

    }
