package com.geraud.android.gps1.Dailogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.geraud.android.gps1.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class Add_GroupChat extends AppCompatDialogFragment {
    private EditText name;
    private ImageView image;
    private Uri image_uri;
    private AddGroupChatListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.create_group_chat, null);

        //initialise the views
        name = view.findViewById(R.id.name);
        image = view.findViewById(R.id.image);

        //open gallery to select an image of the place
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CALL THE IMAGE CROPPER LIBRARY TO CROP THE SELECTED IMAGE
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(150, 150)
                        .setAspectRatio(1, 1)
                        .start(getActivity());
            }
        });

        builder.setView(view)
                .setTitle("Info")
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing is going to happen
                    }
                })
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String groupName = name.getText().toString();
                        if (!TextUtils.isEmpty(groupName) && image_uri != null ) {
                            listener.applyGroupInfo(groupName,image_uri);
                        } else
                            Toasty.error(getActivity(), "Enter A Name and Description ", 2000, true).show();
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //here we will initialise our listener
        try {
            listener = (AddGroupChatListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement AddPlaceListener");
        }
    }

    //get the result form the image cropper and displays it on the image view
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                image_uri = result.getUri();
                image.setImageURI(image_uri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toasty.error(getActivity(), result.getError().toString(), 2000, true).show();
            }
        }
    }

    //writing an interface to get data and send back to parent activity
    public interface AddGroupChatListener {
        void applyGroupInfo(String name,Uri image_uri);
    }
}

