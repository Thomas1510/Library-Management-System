package model;

public class Book {

    private int book_id;
    private String title;
    private String author;
    private String category;
    private int quantity;
    private int totalQuantity;

    public Book(int book_id, String title, String author, String category, int quantity, int totalQuantity) {
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.totalQuantity = totalQuantity;
    }

    // Backward-compatible constructor
    public Book(int book_id, String title, String author, String category, int quantity) {
        this(book_id, title, author, category, quantity, quantity);
    }

    public int getBook_id()       { return book_id; }
    public String getTitle()       { return title; }
    public String getAuthor()      { return author; }
    public String getCategory()    { return category; }
    public int getQuantity()       { return quantity; }
    public int getTotalQuantity()  { return totalQuantity; }

    public String getStatus() {
        if (quantity == 0)          return "Out of Stock";
        else if (quantity <= 2)     return "Low Stock";
        else                        return "Available";
    }
}