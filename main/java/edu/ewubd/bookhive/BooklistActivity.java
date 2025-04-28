package edu.ewubd.bookhive;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
public class BooklistActivity extends AppCompatActivity {

    private GridView gvBooks;
    private FloatingActionButton fabAddBook;
    private ImageView ivViewCart;
    private TextView tvCartBadge;
    private ArrayList<Book> bookList;
    private ArrayList<Book> selectedBooks;
    private BookAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference booksCollection;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean isAdmin = false;  // Declare the isAdmin flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booklist);

        // Initialize Firebase, Firestore and UI components
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        booksCollection = db.collection("books");

        gvBooks = findViewById(R.id.gvBooks);
        fabAddBook = findViewById(R.id.fabAddBook);
        ivViewCart = findViewById(R.id.ivViewCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        // Check user role
        checkUserRole();

        bookList = new ArrayList<>();
        selectedBooks = new ArrayList<>();

        // Pass the isAdmin flag when initializing the adapter
        adapter = new BookAdapter(this, bookList, selectedBooks, isAdmin);
        gvBooks.setAdapter(adapter);

        // Load books from Firestore
        fetchBooksFromFirestore();

        // Handle book selection
        gvBooks.setOnItemClickListener(this::onBookSelected);

        // Navigate to AddBookActivity
        fabAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(BooklistActivity.this, AddBookActivity.class);
            startActivityForResult(intent, 100);
        });

        // View selected books in CartActivity
        ivViewCart.setOnClickListener(v -> viewCart());
    }

    /**
     * Check if the user is an admin and adjust UI accordingly.
     */
    private void checkUserRole() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            isAdmin = document.getBoolean("isAdmin") != null && document.getBoolean("isAdmin");

                            Log.d("BooklistActivity", "User is admin: " + isAdmin);

                            if (isAdmin) {
                                // Admin can add books and shouldn't see cart
                                fabAddBook.setVisibility(View.VISIBLE);
                                ivViewCart.setVisibility(View.GONE);
                            } else {
                                // Non-admin users can view cart but can't add books
                                fabAddBook.setVisibility(View.GONE);
                                ivViewCart.setVisibility(View.VISIBLE);
                            }

                            // Re-initialize the adapter after fetching the user role
                            adapter = new BookAdapter(BooklistActivity.this, bookList, selectedBooks, isAdmin);
                            gvBooks.setAdapter(adapter);
                        } else {
                            Log.e("BooklistActivity", "Failed to fetch user role");
                        }
                    });
        } else {
            Log.e("BooklistActivity", "No user is logged in");
        }
    }

    /**
     * Handle book selection in the GridView.
     */
    private void onBookSelected(AdapterView<?> parent, View view, int position, long id) {
        Book selectedBook = bookList.get(position);

        if (selectedBooks.contains(selectedBook)) {
            selectedBooks.remove(selectedBook);
            Toast.makeText(this, selectedBook.getTitle() + " removed from cart.", Toast.LENGTH_SHORT).show();
        } else {
            selectedBooks.add(selectedBook);
            Toast.makeText(this, selectedBook.getTitle() + " added to cart.", Toast.LENGTH_SHORT).show();
        }

        updateCartBadge(selectedBooks.size());
    }

    /**
     * View the cart and navigate to CartActivity.
     */
    private void viewCart() {
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, CartActivity.class);
            intent.putParcelableArrayListExtra("cartItems", selectedBooks);
            startActivity(intent);
        }
    }

    /**
     * Update the cart badge based on the number of selected books.
     */
    public void updateCartBadge(int numSelectedBooks) {
        if (numSelectedBooks > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(numSelectedBooks));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    /**
     * Fetch books from Firestore and populate the GridView.
     */
    private void fetchBooksFromFirestore() {
        booksCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    bookList.clear();
                    for (DocumentSnapshot document : querySnapshot) {
                        Book book = document.toObject(Book.class);
                        if (book != null) {
                            bookList.add(book);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "No books found in the collection.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load books. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-fetch books from Firestore to ensure the data is up-to-date
        fetchBooksFromFirestore();
        clearCart();
    }

    private void clearCart() {
        SharedPreferences preferences = getSharedPreferences("Cart", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "Cart Cleared in BookListActivity");
    }

}
