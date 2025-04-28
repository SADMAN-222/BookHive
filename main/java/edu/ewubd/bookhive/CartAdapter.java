package edu.ewubd.bookhive;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CartAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Book> cartItems;

    public CartAdapter(Context context, ArrayList<Book> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cart_item, null);
        }

        Book book = cartItems.get(position);

        // Initialize the views
        TextView tvTitle = convertView.findViewById(R.id.tvBookTitle);
        TextView tvAuthor = convertView.findViewById(R.id.tvAuthorName);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);

        // Set the data to the views
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());

        // Convert the double price to string and set it
        tvPrice.setText(String.format("$%.2f", book.getPrice()));  // Format the price to 2 decimal places

        return convertView;
    }
}
