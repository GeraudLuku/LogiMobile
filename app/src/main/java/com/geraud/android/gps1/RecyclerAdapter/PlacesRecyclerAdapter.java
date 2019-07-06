package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Place;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {

    private List<Place> mPlacesList;
    private Context mContext;
    private String mPhone;

    private DatabaseReference mReferenceDB = FirebaseDatabase.getInstance().getReference();

    private List<Address> mAddress;

    public PlacesRecyclerAdapter(List<Place> placeList, Context context, String phone) {
        mContext = context;
        mPlacesList = placeList;
        mPhone = phone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_item, viewGroup, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        // if its my place i can delete else i cant
        if (mPlacesList.get(position).getCreator().equals(mPhone))
            holder.mDeletePlace.setVisibility(View.VISIBLE);

        //get address of location
        try {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            mAddress = geocoder.getFromLocation(mPlacesList.get(position).getLatitude(), mPlacesList.get(position).getLongitude(), 1);
        } catch (IOException e) {
            Toast.makeText(mContext, "Couldn't Get Address Of Area", Toast.LENGTH_SHORT).show();
        }
        // town and country name
        holder.mTown.setText(String.format(Locale.getDefault(), " %s - %s ", mAddress.get(0).getLocality(), mAddress.get(0).getCountryName()));

        //name of place
        holder.mName.setText(mPlacesList.get(position).getName());

        //creator of place
        mReferenceDB.child("USER").child(mPlacesList.get(position).getCreator()).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dc) {
                if (dc.exists()){
                    User user = dc.getValue(User.class);
                    holder.mCreator.setText(String.format(Locale.getDefault(), " Creator : %s",
                            user.getName()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, "PlaceRecyclerAdapter ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // place image full screen onClick
        Glide.with(mContext).load(mPlacesList.get(position).getImage_uri())
                .into(holder.mPlaceImage);
        holder.mPlaceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> list = new ArrayList<>();
                list.add(mPlacesList.get(holder.getAdapterPosition()).getImage_uri());
                new ImageViewer.Builder(mContext, list)
                        .setStartPosition(0)
                        .show();
            }
        });

           holder.mDeletePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReferenceDB.child("PLACE").child(mPlacesList.get(holder.getAdapterPosition()).getCreator()).child(mPlacesList.get(holder.getAdapterPosition()).getKey()).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null)
                            Toast.makeText(mContext, "Place deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPlacesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTown,
                mName,
                mCreator;
        ImageView mPlaceImage;
        ImageButton mDeletePlace;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTown = itemView.findViewById(R.id.primary_text);
            mName = itemView.findViewById(R.id.sub_text);
            mCreator = itemView.findViewById(R.id.creator_text);
            mPlaceImage = itemView.findViewById(R.id.media_image);
            mDeletePlace = itemView.findViewById(R.id.action_button_2);
        }
    }
}

