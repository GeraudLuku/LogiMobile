package com.geraud.android.gps1.Chat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.ChatListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {
    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;

    List<Chat> chatList;
    public ChatsFragment (){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        //initialising recycler view
        chatList = new ArrayList<>();
        mChatList = view.findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(false);
        mChatList.setHasFixedSize(false);
        mChatListLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL , false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(chatList,getContext());
        mChatList.setAdapter(mChatListAdapter);

        //getting chats
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat");
        mUserChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot dc : dataSnapshot.getChildren()){
                        Chat mChat = new Chat(dc.getKey()); //getting all the ids of the chat children
                        Boolean exists = false;
                        for (Chat mChatIterator : chatList){  //method used not to duplicate chat ids in the chats activity
                            if (mChatIterator.getChatId().equals(mChat.getChatId()))
                                exists = true;
                        }
                        if (exists)
                            continue;
                        chatList.add(mChat);
                        getChatData(mChat.getChatId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return view;
    }

    //thi is where you will get the info of all the chats including the destinator , last message and timestamp
    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("info");
        mChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String chatId = "";

                    //gettting id of the chat
                    if (dataSnapshot.child("id").getValue() != null)
                        chatId = dataSnapshot.child("id").getValue().toString();

                    //gettting all users
                    for (DataSnapshot userSnaphshot : dataSnapshot.child("users").getChildren())
                        for (Chat mChat : chatList){
                        if (mChat.getChatId().equals(chatId)){
                            User mUser = new User(userSnaphshot.getKey());
                            mChat.addUserToArrayList(mUser);
                            getUserData(mUser);
                        }
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserData(User mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getPhone());
        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = new User(dataSnapshot.getKey());

                //getting notification key
                if (dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());

                for (Chat mChat : chatList){
                    for (User mUserIt : mChat.getUserObjectArrayList()){
                        if (mUserIt.getPhone().equals(mUser.getPhone())){
                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }

                mChatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

}
