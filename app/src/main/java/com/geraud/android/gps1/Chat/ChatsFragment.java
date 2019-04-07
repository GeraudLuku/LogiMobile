package com.geraud.android.gps1.Chat;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
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
import java.util.Objects;


public class ChatsFragment extends Fragment {

    private RecyclerView.Adapter mChatListAdapter;

    private List<Chat> mChatList;
    private List<ChatInfo> mChatInfo;

    private String mUserPhone;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        mUserPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cant Be Null").getPhoneNumber();

        //initialising recycler view
        mChatList = new ArrayList<>();
        mChatInfo = new ArrayList<>();

        RecyclerView chatRecyclerView = view.findViewById(R.id.chatList);
        chatRecyclerView.setNestedScrollingEnabled(false);
        chatRecyclerView.setHasFixedSize(false);
        chatRecyclerView.addItemDecoration(new DividerItemDecoration(chatRecyclerView.getContext(), DividerItemDecoration.VERTICAL)); // list divider
        RecyclerView.LayoutManager chatListLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        chatRecyclerView.setLayoutManager(chatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(mChatList, mChatInfo, getContext(),mUserPhone);
        chatRecyclerView.setAdapter(mChatListAdapter);

        getUserChatList();

        return view;
    }

    private void getUserChatList() {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("USER").child(mUserPhone).child("chat");
        mUserChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Chat mChat = new Chat(childSnapshot.getKey()); //getting all the ids of the chat children

                        for (Chat mChatIterator : mChatList)   //method used not to duplicate chat ids in the chats activity
                            if (mChatIterator.getChatId().equals(mChat.getChatId()))
                                //continue; //stop here in the code and  goto next iteration in for loop

                        mChatList.add(mChat);
                        getChatData(mChat.getChatId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "getUserChatList ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //thi is where you will get the info of all the chats including the destinator , last message and timestamp
    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chatId).child("info");
        mChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatInfo chatInfo = dataSnapshot.getValue(ChatInfo.class);
                    mChatInfo.add(chatInfo);
                    String chatId = "";

                    //getting id of the chat
                    if (dataSnapshot.child("id").getValue() != null)
                        chatId = Objects.requireNonNull(dataSnapshot.child("id").getValue(),"chatId Cant Be Null").toString();

                    //getting all users
                    for (DataSnapshot userSnaphshot : dataSnapshot.child("users").getChildren())
                        for (Chat mChat : mChatList) {
                            if (mChat.getChatId().equals(chatId)) {
                                User mUser = new User(userSnaphshot.getKey());
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser);
                            }
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "getChatData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserData(User mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("USER").child(mUser.getPhone()).child("userInfo");
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = new User(dataSnapshot.getKey());

                //getting notification key
                if (dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(Objects.requireNonNull(dataSnapshot.child("notificationKey").getValue(),"Notification Key Can't Be Null").toString());

                for (Chat mChat : mChatList) {
                    for (User mUserIt : mChat.getUserObjectArrayList()) {
                        if (mUserIt.getPhone().equals(mUser.getPhone())) {
                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                mChatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "getUserData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
