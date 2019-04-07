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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.geraud.android.gps1.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class AddGroupChat extends AppCompatDialogFragment {

    private EditText mName;
    private ImageView mImage;
    private Uri mImageUri;

    private AddGroupChatListener mAddGroupChatListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.create_group_chat, null,false);

        //initialise the views
        mName = view.findViewById(R.id.name);
        mImage = view.findViewById(R.id.image);

        //open gallery to select an mImage of the place
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CALL THE IMAGE CROPPER LIBRARY TO CROP THE SELECTED IMAGE
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(250, 250)
                        .setAspectRatio(1, 1)
                        .start(Objects.requireNonNull(getActivity()));
            }
        });

        builder.setView(view)
                .setCancelable(true)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String groupName = mName.getText().toString();
                        if (!TextUtils.isEmpty(groupName) && mImageUri != null ) {
                            mAddGroupChatListener.applyGroupInfo(groupName, mImageUri);
                        } else
                            Toast.makeText(getContext(), "Fill All Information", Toast.LENGTH_SHORT).show();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAddGroupChatListener = (AddGroupChatListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement AddPlaceListener");
        }
    }

    //get the result form the mImage cropper and displays it on the mImage view
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getContext(),"Error in Crop Image Intent",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //writing an interface to get data and send back to parent activity
    public interface AddGroupChatListener {
        void applyGroupInfo(String name,Uri image_uri);
    }
}

