package edu.ewubd.bookhive;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddBookActivity extends Activity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION_CODE = 100;

    private EditText etTitle, etAuthor, etPrice, etQuantity, etDescription, etTotalQuantity;
    private ImageView ivSelectedImage;
    private Button btnAddImage, btnSave;
    private Uri selectedImageUri;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etDescription = findViewById(R.id.etDescription);
        etTotalQuantity = findViewById(R.id.etQuantity);  // Correct initialization
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSave = findViewById(R.id.btnSave);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Check permissions for image picking
        checkPermissions();

        // Handle Add Image button click
        btnAddImage.setOnClickListener(view -> openImagePicker());

        // Handle Save button click
        btnSave.setOnClickListener(view -> saveBook());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                ivSelectedImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String priceString = etPrice.getText().toString().trim();
        String quantityString = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String totalQuantityString = etTotalQuantity.getText().toString().trim();  // Get totalQuantity as string

        // Validate inputs
        if (title.isEmpty() || author.isEmpty() || priceString.isEmpty() || quantityString.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert price to double
        double price = 0.0;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert quantity to int
        int quantity = 0;
        try {
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert totalQuantity to int
        int totalQuantity = 0;
        try {
            totalQuantity = Integer.parseInt(totalQuantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid total quantity format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                String base64Image = convertImageToBase64(bitmap);

                saveBookData(title, author, price, quantity, description, base64Image, totalQuantity);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveBookData(title, author, price, quantity, description, null, totalQuantity);
        }
    }

    private void saveBookData(String title, String author, double price, int quantity, String description, String base64Image, int totalQuantity) {
        // Create the new book object
        Book newBook = new Book(title, author, description, price, quantity, base64Image, totalQuantity);

        // Save to Firestore
        db.collection("books")
                .add(newBook)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddBookActivity.this, "Book added successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddBookActivity.this, "Failed to add book", Toast.LENGTH_SHORT).show();
                });
    }
}
