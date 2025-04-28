package edu.ewubd.bookhive;
import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private String title;
    private String author;
    private String description;
    private double price;
    private int quantity;
    private String imageUrl;
    private int totalQuantity;

    // No-argument constructor for Firestore deserialization
    public Book() {}

    // Constructor with parameters
    public Book(String title, String author, String description, double price, int quantity, String imageUrl, int totalQuantity) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.totalQuantity = totalQuantity;
    }

    // Parcelable constructor
    protected Book(Parcel in) {
        title = in.readString();
        author = in.readString();
        description = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        imageUrl = in.readString();
        totalQuantity = in.readInt();
    }

    // Write the data to a Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeInt(quantity);
        dest.writeString(imageUrl);
        dest.writeInt(totalQuantity);
    }

    // Describe contents (usually 0)
    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable.Creator to regenerate the object
    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    // Getters and setters for all fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    // Handle price field if it's a String in Firestore
    public void setPrice(Object price) {
        if (price instanceof String) {
            try {
                this.price = Double.parseDouble((String) price);  // Convert String to double
            } catch (NumberFormatException e) {
                this.price = 0.0;  // Default value in case of error
            }
        } else if (price instanceof Double) {
            this.price = (Double) price;  // Direct assignment if it's already a double
        } else {
            this.price = 0.0;  // Default value if the type is unknown
        }
    }

    public int getQuantity() {
        return quantity;
    }

    // Handle quantity field if it's a String in Firestore
    public void setQuantity(Object quantity) {
        if (quantity instanceof String) {
            try {
                this.quantity = Integer.parseInt((String) quantity);  // Convert String to int
            } catch (NumberFormatException e) {
                this.quantity = 0;  // Default value in case of error
            }
        } else if (quantity instanceof Integer) {
            this.quantity = (Integer) quantity;  // Direct assignment if it's already an int
        } else {
            this.quantity = 0;  // Default value if the type is unknown
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
