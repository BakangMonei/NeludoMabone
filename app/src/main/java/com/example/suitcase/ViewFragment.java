package com.example.suitcase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.suitcase.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewFragment extends Fragment {
    static final int PICK_IMAGE_REQUEST = 1;
    private ImageView clickableImage;
    private Uri selectedImageUri;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view, container, false);
        // Initialize Firebase Authorization
        firebaseAuth = FirebaseAuth.getInstance();
        // Find your clickable image view by its ID
        clickableImage = view.findViewById(R.id.imageViewProfile);
        // Set an onClickListener for the clickable image
        clickableImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Find your logout button by its ID
        MaterialButton logoutButton = view.findViewById(R.id.buttonLogout);
        // Set an onClickListener to handle the button click
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        // Display the logged-in user's email when the fragment is created
        displayLoggedInUserEmail(view);
        // Load the user's saved image (if any)
        loadUserImage();
        return view;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            // Set the selected image to your ImageView or CardView
            Glide.with(requireContext()).load(selectedImageUri).into(clickableImage);
        }
    }

    private void handleSaveButtonClick() {
        if (selectedImageUri != null) {
            saveImageToStorage();
        } else {
            Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToStorage() {
        // Get the email of the logged-in user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userEmail = user.getEmail();

        // Use Firebase Storage to save the image
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("user_images/" + userEmail + ".jpg");

        imagesRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, perform logout
                        logout();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked No, dismiss the dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void logout() {
        // Sign out the user from Firebase
        firebaseAuth.signOut();

        // Navigate to the login activity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        // Terminate the current activity
        requireActivity().finish();
    }

    private void displayLoggedInUserEmail(View view) {
        // Find the TextView in your layout where you want to display the email
        TextView emailTextView = view.findViewById(R.id.textViewEmail);
        // Get the currently authenticated user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        // Check if the user is not null
        if (user != null) {
            // Set the email in the TextView
            emailTextView.setText(user.getEmail());
        }
    }

    private void loadUserImage() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userEmail = user.getEmail();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("user_images/" + userEmail + ".jpg");

        imagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image using Glide or any other image loading library
            Glide.with(requireContext()).load(uri).into(clickableImage);
        }).addOnFailureListener(e -> {
            // Handle failure to load image
        });
    }
}
