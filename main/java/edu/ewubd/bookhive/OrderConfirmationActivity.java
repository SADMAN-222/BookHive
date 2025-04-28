package edu.ewubd.bookhive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "OrderConfirmation";

    private FirebaseFirestore db;
    private EditText etFullName, etPhoneNumber, etAddress;
    private RadioGroup rgPaymentMethod;
    private Button btnConfirmOrder;
    private TextView tvTotalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        // Initialize Firestore and Views
        db = FirebaseFirestore.getInstance();
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        // Retrieve total price and book details from intent
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        ArrayList<String> bookTitles = getIntent().getStringArrayListExtra("bookTitles");
        ArrayList<Double> bookPrices = (ArrayList<Double>) getIntent().getSerializableExtra("bookPrices");

        // Log retrieved data for debugging
        Log.d(TAG, "Total Price: " + totalPrice);
        Log.d(TAG, "Book Titles: " + bookTitles);
        Log.d(TAG, "Book Prices: " + bookPrices);

        // Display the total amount
        tvTotalAmount.setText("Total Amount: $" + String.format("%.2f", totalPrice));

        // Confirm Order button click listener
        btnConfirmOrder.setOnClickListener(v -> {
            if (bookTitles != null && bookPrices != null) {
                handleOrderConfirmation(totalPrice, bookTitles, bookPrices);
            } else {
                Toast.makeText(OrderConfirmationActivity.this, "Error retrieving book data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Book data is null.");
            }
        });
    }

    private void handleOrderConfirmation(double totalPrice, ArrayList<String> bookTitles, ArrayList<Double> bookPrices) {
        // Get the user input
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Get the selected payment method
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        RadioButton selectedPaymentMethod = findViewById(selectedPaymentId);
        String paymentMethod = selectedPaymentMethod != null ? selectedPaymentMethod.getText().toString() : "";

        // Validate input
        if (fullName.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() || paymentMethod.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Validation failed: Empty fields.");
        } else if (phoneNumber.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Validation failed: Invalid phone number.");
        } else {
            // Save order data to Firebase
            saveOrderToFirebase(fullName, phoneNumber, address, paymentMethod, totalPrice, bookTitles, bookPrices);
        }
    }

    private void saveOrderToFirebase(String fullName, String phoneNumber, String address, String paymentMethod, double totalPrice, ArrayList<String> bookTitles, ArrayList<Double> bookPrices) {
        // Create a map to store order details
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("fullName", fullName);
        orderData.put("phoneNumber", phoneNumber);
        orderData.put("address", address);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("totalPrice", totalPrice);
        orderData.put("timestamp", System.currentTimeMillis());

        // Add book details to the order
        ArrayList<Map<String, Object>> books = new ArrayList<>();
        for (int i = 0; i < bookTitles.size(); i++) {
            Map<String, Object> book = new HashMap<>();
            book.put("title", bookTitles.get(i));
            book.put("price", bookPrices.get(i));
            books.add(book);
        }
        orderData.put("books", books);

        // Save the order to Firestore
        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Order placed with ID: " + documentReference.getId());
                    clearCart(); // Clear the cart after successful order

                    // Return to BookListActivity
                    Intent intent = new Intent(OrderConfirmationActivity.this,BooklistActivity.class);
                    startActivity(intent);  // Start BookListActivity
                    setResult(RESULT_OK);   // Set result to OK
                    finish();               // Finish OrderConfirmationActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to place the order. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firestore Error: " + e.getMessage(), e);
                });
    }

    // Method to clear cart (SharedPreferences Example)
    private void clearCart() {
        // Get SharedPreferences instance
        SharedPreferences preferences = getSharedPreferences("Cart", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Clear all data in SharedPreferences
        editor.clear();  // Clears all keys and values
        editor.apply();  // Apply changes asynchronously

        // Log the status of clearing the cart
        Log.d(TAG, "Cart Cleared");

        // Check if cart is really cleared
        if (preferences.getAll().isEmpty()) {
            Toast.makeText(this, "Cart cleared successfully.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to clear cart.", Toast.LENGTH_SHORT).show();
        }
    }

}
