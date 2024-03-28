package com.example.suitcase;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.health.connect.datatypes.units.Length;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.suitcase.model.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AddItemFragment extends Fragment implements SensorEventListener {

    private EditText fieldName;
    private EditText fieldDescription;
    private EditText fieldPrice;
    private FloatingActionButton fabOpenGallery;
    private FloatingActionButton fabOpenMap;
    private Button buttonSave;
    private ImageView displayImage;

    private DatabaseReference dbReference;
    private StorageReference storageReference, imgReference;
    private UploadTask uploadTask;

    private Bitmap imageBitmap;
    private String itemName, itemDescription, itemPrice, imageName;
    private double latitude, longitude;
    private boolean isPurchased;
    private boolean isTagged;
    private byte[] data;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7f;
    private long lastShakeTime;
    private Vibrator vibrator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        initializeViews(view);
        setClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        dbReference = FirebaseDatabase.getInstance().getReference("items");
        storageReference = FirebaseStorage.getInstance().getReference("item_images");

        fieldName = view.findViewById(R.id.field_name);
        fieldDescription = view.findViewById(R.id.field_description);
        fieldPrice = view.findViewById(R.id.field_price);
        displayImage = view.findViewById(R.id.item_image);
        fabOpenGallery = view.findViewById(R.id.fab_add_image);
        fabOpenMap = view.findViewById(R.id.fab_add_location);
        buttonSave = view.findViewById(R.id.button_add_item);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void setClickListeners() {
        fabOpenGallery.setOnClickListener(this::onGalleryButtonClick);
        fabOpenMap.setOnClickListener(this::onMapButtonClick);
        buttonSave.setOnClickListener(this::onSaveButtonClick);
    }

    private void onGalleryButtonClick(View v) {
        galleryLauncher.launch("image/*");
    }

    private void onMapButtonClick(View v) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        mapActivityResultLauncher.launch(intent);
    }

    private void onSaveButtonClick(View v) {
        itemName = fieldName.getText().toString();
        itemDescription = fieldDescription.getText().toString();
        itemPrice = fieldPrice.getText().toString();
        // Generate a random name for the image file
        imageName = UUID.randomUUID().toString();

        // Reference to store the image in Firebase Storage
        imgReference = storageReference.child(imageName);

        // Upload the compressed image data to Firebase Storage
        uploadTask = imgReference.putBytes(data);
        // Upload the image to Firebase Storage
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the image download URL
            imgReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Create a new Item object with the image URL
                isPurchased = false;
                if (latitude != 0 || longitude != 0) {
                    isTagged = true;
                }
                Item item = new Item(itemName, itemDescription, itemPrice, uri.toString(), isPurchased, isTagged, latitude, longitude);
                // Push the item to the Firebase database
                String key = dbReference.push().getKey();
                dbReference.child(key).setValue(item);
                Toast.makeText(getActivity(), "Item added successfully", Toast.LENGTH_SHORT).show();
                Intent x = new Intent(getActivity(), MainActivity.class);
                startActivity(x);

            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                    displayImage.setImageURI(uri);
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), uri);
                    imageBitmap = ImageDecoder.decodeBitmap(source);
                    displayImage.setImageURI(uri);
                }
                // Compress the image here before storing it to Firebase
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                data = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    private final ActivityResultLauncher<Intent> mapActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        latitude = data.getDoubleExtra("lat", 0.0);
                        longitude = data.getDoubleExtra("lon", 0.0);
                    }
                }
            }
    );

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > 1000) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
                if (acceleration > SHAKE_THRESHOLD_GRAVITY) {
                    lastShakeTime = currentTime;
                    clearFields();
                    // Vibrate for 200 milliseconds
                    vibrator.vibrate(200);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void clearFields() {
        fieldName.getText().clear();
        fieldDescription.getText().clear();
        fieldPrice.getText().clear();
        // Hide the keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(getActivity());
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Toast.makeText(getActivity(), "Fields cleared", Toast.LENGTH_SHORT).show();
    }
}
