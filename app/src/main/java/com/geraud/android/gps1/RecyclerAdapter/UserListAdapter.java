package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private List<User> mUserList;
    private Context mContext;

    public UserListAdapter(List<User> userList, Context context) {
        this.mUserList = userList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item, viewGroup, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        return new UserListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListViewHolder holder, int position) {

        holder.mName.setText(mUserList.get(position).getName());

        holder.mPhone.setText(mUserList.get(position).getPhone().toUpperCase());

        Glide.with(mContext).load(mUserList.get(position).getImage_uri())
                .into(holder.mImage);


        holder.mAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserList.get(holder.getAdapterPosition()).setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

     class UserListViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImage;
        private TextView mName, mPhone;
        private CheckBox mAdd;

        private UserListViewHolder(View view) {
            super(view);

            mName = view.findViewById(R.id.friend_name);
            mPhone = view.findViewById(R.id.friend_number);
            mImage = view.findViewById(R.id.friend_image);
            mAdd = view.findViewById(R.id.add);
        }
    }
}
