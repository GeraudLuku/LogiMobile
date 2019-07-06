package com.geraud.android.gps1.Dailogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.geraud.android.gps1.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Geraud on 6/21/2018.
 */

public class AddPlace extends AppCompatDialogFragment {

    private EditText mNameEditText,
            mDescriptionEditText;
    private ImageView mImageView;
    private String mPlaceType;
    private Uri mImageUri;

    private AddPlaceListener mAddPlaceListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()),R.style.alertDialog);

        LayoutInflater inflater = Objects.requireNonNull(getActivity(), "AddPlace Get Activity Cant Be Null").getLayoutInflater();
        View view = inflater.inflate(R.layout.add_place, null);

        //initialise the views
        mNameEditText = view.findViewById(R.id.place_name);
        mDescriptionEditText = view.findViewById(R.id.place_description);
        mImageView = view.findViewById(R.id.place_image);

        Spinner mSpinner = view.findViewById(R.id.place_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.places_types, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) //do nothing since its the hint
                    view.setClickable(false);
                mPlaceType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //open gallery to select an mImageView of the place
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CALL THE IMAGE CROPPER LIBRARY TO CROP THE SELECTED IMAGE
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(getContext(),AddPlace.this);
            }
        });

        builder.setView(view)
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing is going to happen
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String placeName = mNameEditText.getText().toString();
                        String placeDesc = mDescriptionEditText.getText().toString();
                        if (!TextUtils.isEmpty(placeName) && !TextUtils.isEmpty(placeDesc) && mImageUri != null && !mPlaceType.isEmpty()) {
                            mAddPlaceListener.AddPlaceOnMap(placeName, placeDesc, mImageUri, mPlaceType);
                        } else
                            Toasty.error(Objects.requireNonNull(getActivity(), "AddPlace Get Activity Cant Be Null"), "Enter An Image, Name,Description And Type", 2000, true).show();
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //here we will initialise our mAddPlaceListener
        try {
            mAddPlaceListener = (AddPlaceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement AddPlaceListener");
        }
    }

    //get the result form the mImageView cropper and displays it on the mImageView view
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                mImageView.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toasty.error(Objects.requireNonNull(getActivity(), "AddPlace Get Activity Cant Be Null"), result.getError().toString(), 2000, true).show();
            }
        }
    }

    //writing an interface to get data and send back to parent activity
    public interface AddPlaceListener {
        void AddPlaceOnMap(String name, String description, Uri image_uri, String dec2);
    }
}
