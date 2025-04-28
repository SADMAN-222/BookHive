package edu.ewubd.bookhive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class BookAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Book> bookList;
    private ArrayList<Book> selectedBooks;
    private boolean isAdmin;  // Variable to check if user is admin

    // Add the isAdmin parameter to the constructor
    public BookAdapter(Context context, ArrayList<Book> bookList, ArrayList<Book> selectedBooks, boolean isAdmin) {
        this.context = context;
        this.bookList = bookList;
        this.selectedBooks = selectedBooks;
        this.isAdmin = isAdmin;  // Set isAdmin based on user role
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View bookView = convertView;
        if (bookView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            bookView = inflater.inflate(R.layout.book_item, null);
        }

        Book book = bookList.get(position);

        // Find views
        ImageView ivBookImage = bookView.findViewById(R.id.ivBookImage);
        TextView tvBookTitle = bookView.findViewById(R.id.tvBookTitle);
        TextView tvAuthorName = bookView.findViewById(R.id.tvAuthorName);
        TextView tvPrice = bookView.findViewById(R.id.tvPrice);
        TextView tvQuantity = bookView.findViewById(R.id.tvQuantity);  // Correct view for displaying quantity
        ImageView ivAddToCart = bookView.findViewById(R.id.ivAddToCart);

        // Ensure views are not null before updating them
        if (tvBookTitle != null) {
            tvBookTitle.setText(book.getTitle());
        }

        if (tvAuthorName != null) {
            tvAuthorName.setText(book.getAuthor());
        }

        // Handle price field if it's a String or Double
        if (tvPrice != null) {
            String priceText = formatPrice(book.getPrice());
            tvPrice.setText(priceText);  // Format price to 2 decimal places
        }

        // Handle quantity field if it's a String or Integer
        if (tvQuantity != null) {
            String quantityText = "Available Copies: " + book.getTotalQuantity();
            tvQuantity.setText(quantityText);  // Display available copies dynamically
        }

        // Set image based on URL or Base64
        String imageUrl = book.getImageUrl();
        if (ivBookImage != null) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (isBase64(imageUrl)) {
                    Bitmap decodedImage = decodeBase64ToBitmap(imageUrl);
                    ivBookImage.setImageBitmap(decodedImage);
                } else {
                    Glide.with(context).load(imageUrl).into(ivBookImage);
                }
            } else {
                ivBookImage.setImageResource(R.drawable.placeholder_image);  // Placeholder image if URL is empty
            }
        }

        // If the user is an admin, hide the cart icon
        if (ivAddToCart != null) {
            if (isAdmin) {
                ivAddToCart.setVisibility(View.GONE);  // Hide the cart icon if the user is admin
            } else {
                if (selectedBooks.contains(book)) {
                    ivAddToCart.setImageResource(R.drawable.cart_checked);  // "Cart with check" icon
                } else {
                    ivAddToCart.setImageResource(R.drawable.cart);  // Empty cart icon
                }

                // Handle "Add to Cart" click
                ivAddToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedBooks.contains(book)) {
                            selectedBooks.remove(book);
                            ivAddToCart.setImageResource(R.drawable.cart);  // Update icon
                            book.setTotalQuantity(book.getTotalQuantity() + 1);  // Update quantity when removed
                        } else {
                            selectedBooks.add(book);
                            ivAddToCart.setImageResource(R.drawable.cart_checked);  // Update icon
                            book.setTotalQuantity(book.getTotalQuantity() - 1);  // Update quantity when added
                        }

                        // Notify the adapter about the data change
                        notifyDataSetChanged();

                        // Update the cart badge in BooklistActivity
                        int numSelectedBooks = selectedBooks.size();
                        if (context instanceof BooklistActivity) {
                            ((BooklistActivity) context).updateCartBadge(numSelectedBooks);
                        }
                    }
                });
            }
        }

        return bookView;
    }

    // Method to check if the string is a Base64 encoded image
    private boolean isBase64(String str) {
        try {
            Base64.decode(str, Base64.DEFAULT);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Method to decode Base64 string to Bitmap
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    // Helper method to format the price
    private String formatPrice(double price) {
        return String.format("%.2f", price);  // Format price to 2 decimal places
    }
}
