package com.example.suitcase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.suitcase.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class EditActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    FloatingActionButton markItemAsPurchasedFAB;
    FloatingActionButton deleteItemFAB;
    FloatingActionButton shareItemFAB;
    FloatingActionButton tagItemFAB;
    FloatingActionButton openGalleryFAB;

    TextInputEditText itemName;
    TextInputEditText itemDescription;
    TextInputEditText itemPrice;

    ImageView itemImage;

    Button saveChangesButton;

    Item item;

    private Uri imageUri;
    byte[] imageData;
    private String id;
    private String name;
    private String description;
    private String price;
    private boolean isPurchased;
    private String imageUrl;
    private double latitude;
    private double longitude;

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    private boolean isDataChanged = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Initialize views
        markItemAsPurchasedFAB = findViewById(R.id.mark_purchased);
        deleteItemFAB = findViewById(R.id.delete);
        shareItemFAB = findViewById(R.id.forward_item);
        tagItemFAB = findViewById(R.id.map);
        openGalleryFAB = findViewById(R.id.open_gallery);
        itemName = findViewById(R.id.editTextItemName);
        itemDescription = findViewById(R.id.editTextItemDescription);
        itemPrice = findViewById(R.id.editTextItemPrice);
        itemImage = findViewById(R.id.item_image);
        saveChangesButton = findViewById(R.id.btnUpdate);

        // Retrieve intent extras
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        description = intent.getStringExtra("description");
        price = intent.getStringExtra("price");
        isPurchased = intent.getBooleanExtra("purchased", false);
        imageUrl = intent.getStringExtra("imageUrl");
        latitude = intent.getDoubleExtra("lat", 0.0);
        longitude = intent.getDoubleExtra("lon", 0.0);

        // Populate EditText fields with item details
        itemName.setText(name);
        itemDescription.setText(description);
        itemPrice.setText(price);
        Picasso.get().load(imageUrl).into(itemImage);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("items").child(id);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update item details
                name = itemName.getText().toString().trim();
                description = itemDescription.getText().toString().trim();
                price = itemPrice.getText().toString().trim();

                if (name.isEmpty() || description.isEmpty() || price.isEmpty()) {
                    Toast.makeText(EditActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update item in the database
                updateItem();
            }
        });


        // Set click listeners for FABs
        markItemAsPurchasedFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle item purchased status
                isPurchased = !isPurchased;
                // Update UI and database
                updatePurchasedStatus();
            }
        });

        deleteItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete item from database
                deleteItem();
            }
        });

        shareItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Share item details
                shareItemDetails();
            }
        });

        tagItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tag item on map
                tagItemOnMap();
            }
        });

        openGalleryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery to select image
                openGallery();
            }
        });
    }

    private void updatePurchasedStatus() {
        if (isPurchased) {
            markItemAsPurchasedFAB.setColorFilter(getResources().getColor(R.color.Green));
            Toast.makeText(getApplicationContext(), "Item marked as purchased.", Toast.LENGTH_SHORT).show();
        } else {
            markItemAsPurchasedFAB.setColorFilter(getResources().getColor(R.color.red));
            Toast.makeText(getApplicationContext(), "Item marked as NOT purchased.", Toast.LENGTH_SHORT).show();
        }
        // Update purchased status in the database
        databaseReference.child("purchased").setValue(isPurchased);
    }

    private void updateItem() {
        // Update item details
        String updatedName = itemName.getText().toString().trim();
        String updatedDescription = itemDescription.getText().toString().trim();
        String updatedPrice = itemPrice.getText().toString().trim();

        if (updatedName.isEmpty() || updatedDescription.isEmpty() || updatedPrice.isEmpty()) {
            Toast.makeText(EditActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference itemRef = databaseReference.child("name");
        itemRef.setValue(updatedName);
        itemRef = databaseReference.child("description");
        itemRef.setValue(updatedDescription);
        itemRef = databaseReference.child("price");
        itemRef.setValue(updatedPrice);

        Toast.makeText(EditActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }


    private void deleteItem() {
        // Delete item from database
        databaseReference.removeValue();
        // Navigate back to MainActivity
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void shareItemDetails() {
        // Get the item details
        String msg = "Hey, Check out this item\n" + "Name: " + name + "\n" +
                "Description: " + description + "\nPrice: " + price + "\nLocation" + longitude + latitude;
        // Create an intent to send SMS
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("smsto:")); // Opens the messaging app's contact picker

        // Set the message body
        sendIntent.putExtra("sms_body", msg);

        // Check if there's an app that can handle the intent
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(sendIntent);
        } else {
            // If no messaging app is found
            Toast.makeText(this, "No messaging app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void tagItemOnMap() {
        Intent intent = new Intent(EditActivity.this, ChangeMap.class);
        intent.putExtra("id", id);
        intent.putExtra("lat", latitude);
        intent.putExtra("lon", longitude);
        startActivity(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            compressAndUploadImage(imageUri);
            itemImage.setImageURI(imageUri);
        }
    }

    private void compressAndUploadImage(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);
            imageStream.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            imageData = baos.toByteArray();
            uploadCompressedImage(imageData);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Firebase", "Error compressing image: " + e.getMessage());
        }
    }

    private void uploadCompressedImage(byte[] imageData) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

        imageRef.putBytes(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        String imageUrl = downloadUrl.toString();
                        updateDatabaseWithNewImage(imageUrl);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firebase", "Image upload failed: " + e.getMessage());
            }
        });
    }

    private void updateDatabaseWithNewImage(String imageUrl) {
        databaseReference.child("imageUrl").setValue(imageUrl);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the back stack
        startActivity(intent);
    }
}
