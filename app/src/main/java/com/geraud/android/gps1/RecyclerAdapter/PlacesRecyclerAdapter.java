package com.geraud.android.gps1.RecyclerAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
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
import com.geraud.android.gps1.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    public void onBindViewHolder(@NonNull final ViewHolder holder,  int position) {

        //get address of location
        try {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            mAddress = geocoder.getFromLocation(mPlacesList.get(position).getLatitude(), mPlacesList.get(position).getLongitude(), 1);
        } catch (IOException e) {
            Toast.makeText(mContext, "Couldn't Get Address Of Area", Toast.LENGTH_SHORT).show();
        }
        // town and country name
        holder.mTown.setText(String.format(Locale.getDefault(), " %s - %s ", mAddress.get(position).getLocality(), mAddress.get(position).getCountryName()));

        //name of place
        holder.mName.setText(mPlacesList.get(position).getName());

        //creator of place
        mReferenceDB.child("USER").child(mPlacesList.get(position).getCreator()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot dc : dataSnapshot.getChildren())
                        holder.mCreator.setText(String.format(Locale.getDefault(), " Creator : %s", Objects.requireNonNull(dc.child("name").getValue(), "Name Cant Be Null").toString()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, "PlaceRecyclerAdapter ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // if its my place i can delete else i cant
        if (mPlacesList.get(position).getCreator().equals(mPhone))
            holder.mDeletePlace.setVisibility(View.VISIBLE);

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

        holder.mGoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send LatLng to Maps Activity
                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitude", mPlacesList.get(holder.getAdapterPosition()).getLatitude());
                returnIntent.putExtra("longitude", mPlacesList.get(holder.getAdapterPosition()).getLongitude());
                ((Activity) mContext).setResult(Activity.RESULT_OK, returnIntent);
                ((Activity) mContext).finish();
            }
        });

        holder.mDeletePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = mReferenceDB.child("PLACES").child(mPlacesList.get(holder.getAdapterPosition()).getCreator()).orderByChild("name").equalTo(mPlacesList.get(holder.getAdapterPosition()).getName());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            dataSnapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(mContext, "DeletePlace ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
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
        Button mGoto;
        ImageButton mDeletePlace;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTown = itemView.findViewById(R.id.primary_text);
            mName = itemView.findViewById(R.id.sub_text);
            mCreator = itemView.findViewById(R.id.creator_text);
            mPlaceImage = itemView.findViewById(R.id.media_image);
            mGoto = itemView.findViewById(R.id.action_button_1);
            mDeletePlace = itemView.findViewById(R.id.action_button_2);
        }
    }
}

