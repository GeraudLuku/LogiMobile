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

public class TransferRecyclerAdapter extends RecyclerView.Adapter<TransferRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUserList;

    public TransferRecyclerAdapter(Context mContext, List<User> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public TransferRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.mContactName.setText(mUserList.get(position).getName());
        Glide.with(mContext).load(mUserList.get(position).getImage_uri())
                .into(holder.mImageView);


        holder.mIsSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserList.get(position).setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mContactName;
        public CheckBox mIsSelected;

        public ViewHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.friend_image);
            mContactName = itemView.findViewById(R.id.friend_name);
            mIsSelected = itemView.findViewById(R.id.add);

        }

    }
}
