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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.geraud.android.gps1.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Geraud on 6/21/2018.
 */

public class AddPlace extends AppCompatDialogFragment {
    private EditText name, description;
    private ImageView image;
    private Spinner spinner;
    private String dec2;
    private Uri image_uri;
    private AddPlaceListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_place, null);

        //initialise the views
        name = view.findViewById(R.id.place_name);
        description = view.findViewById(R.id.place_description);
        image = view.findViewById(R.id.place_image);
        spinner = view.findViewById(R.id.place_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.places_types, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) //do nothing since its the hint

                dec2 = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //open gallery to select an image of the place
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CALL THE IMAGE CROPPER LIBRARY TO CROP THE SELECTED IMAGE
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(getActivity());
            }
        });

        builder.setView(view)
                .setTitle("Add Place")
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


                        String place_name = name.getText().toString();
                        String place_desc = description.getText().toString();
                        if (!TextUtils.isEmpty(place_name) && !TextUtils.isEmpty(place_desc) && image_uri != null && !dec2.isEmpty()) {

                            listener.AddPlaceOnMap(place_name, place_desc, image_uri, dec2);

                        } else
                            Toasty.error(getActivity(), "Enter An Image, Name,Description And Type", 2000, true).show();
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //here we will initialise our listener
        try {
            listener = (AddPlaceListener) context;
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
    public interface AddPlaceListener {
        void AddPlaceOnMap(String name, String description, Uri image_uri, String dec2);
    }
}
