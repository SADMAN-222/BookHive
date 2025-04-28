package edu.ewubd.bookhive;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private ListView lvCart;
    private TextView tvTotalPrice;
    private Button btnOrder;
    private ArrayList<Book> cartItems;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize UI components
        lvCart = findViewById(R.id.lvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnOrder = findViewById(R.id.btnOrder);

        // Retrieve the cart items passed from BooklistActivity
        cartItems = getIntent().getParcelableArrayListExtra("cartItems");

        // Check if cart is empty and handle accordingly
        if (cartItems == null || cartItems.isEmpty()) {
            handleEmptyCart();
        } else {
            // Set up adapter and populate cart items
            cartAdapter = new CartAdapter(this, cartItems);
            lvCart.setAdapter(cartAdapter);

            // Update the total price on the screen
            updateTotalPrice();

            // Set the order button listener
            btnOrder.setOnClickListener(v -> proceedToOrderConfirmation());
        }
    }

    /**
     * Calculate the total price of items in the cart.
     *
     * @return Total price of cart items.
     */
    private double calculateTotalPrice() {
        double totalPrice = 0.0;
        for (Book book : cartItems) {
            totalPrice += book.getPrice();
        }
        return totalPrice;
    }

    /**
     * Update the total price text view.
     */
    private void updateTotalPrice() {
        double totalPrice = calculateTotalPrice();
        tvTotalPrice.setText(String.format("Total Price: $%.2f", totalPrice));
    }

    /**
     * Handle the order button click and navigate to the order confirmation page.
     */
    private void proceedToOrderConfirmation() {
        double totalPrice = calculateTotalPrice();
        if (totalPrice == 0.0) {
            Toast.makeText(this, "Your cart is empty. Add books to proceed.", Toast.LENGTH_SHORT).show();
        } else {
            // Extract book titles and prices from cartItems
            ArrayList<String> bookTitles = new ArrayList<>();
            ArrayList<Double> bookPrices = new ArrayList<>();
            for (Book book : cartItems) {
                bookTitles.add(book.getTitle());
                bookPrices.add(book.getPrice());
            }

            Intent intent = new Intent(CartActivity.this, OrderConfirmationActivity.class);
            intent.putExtra("totalPrice", totalPrice);  // Pass total price to OrderConfirmationActivity
            intent.putStringArrayListExtra("bookTitles", bookTitles);  // Pass book titles
            intent.putExtra("bookPrices", bookPrices);  // Pass book prices
            startActivity(intent);
        }
    }

    /**
     * Display a message and disable actions for an empty cart.
     */
    private void handleEmptyCart() {
        Toast.makeText(this, "Your cart is empty. Please add books.", Toast.LENGTH_SHORT).show();
        tvTotalPrice.setText("Total Price: $0.00");
        btnOrder.setEnabled(false); // Disable the order button
    }
}
