package com.example.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

public class ShareItemActivity extends AppCompatActivity {
    Button btn_send;
    EditText et_contact, et_message;

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_item);

        btn_send = (Button)findViewById(R.id.phoneButton);
        et_contact = (EditText)findViewById(R.id.enter_phonenumber);

        PermissionToConnect();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the item details from the intent
                Intent intent = getIntent();
                String name = intent.getStringExtra("name");
                String description = intent.getStringExtra("description");
                String price = intent.getStringExtra("price");

                // Create the message
                String msg = "Hey, Check out this item \n" + "Name: " + name + "\n" +
                        "Description: " + description + "\n" + "Price: " + price;

                // Create an intent to open the messaging app
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("smsto:"));  // Opens the messaging app's contact picker

                // Set the message text
                sendIntent.putExtra("sms_body", msg);

                // Check if there's an app that can handle the intent
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                } else {
                    // If no messaging app is found
                    Toast.makeText(ShareItemActivity.this, "No messaging app found", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void PermissionToConnect(){
        if(ContextCompat.checkSelfPermission(ShareItemActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(ShareItemActivity.this, Manifest.permission.SEND_SMS)){
                ActivityCompat.requestPermissions(ShareItemActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
            }else{
                ActivityCompat.requestPermissions(ShareItemActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(ShareItemActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Access", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}