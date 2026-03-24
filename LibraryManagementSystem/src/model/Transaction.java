package model;

public class Transaction {

    private int    transaction_id;
    private String memberName;
    private String bookTitle;
    private String issueDate;
    private String dueDate;
    private String returnDate;   // null if not yet returned
    private double fine;
    private String status;       // "Issued" | "Returned" | "Overdue"

    public Transaction(int transaction_id, String memberName, String bookTitle,
                       String issueDate, String dueDate, String returnDate,
                       double fine, String status) {
        this.transaction_id = transaction_id;
        this.memberName     = memberName;
        this.bookTitle      = bookTitle;
        this.issueDate      = issueDate;
        this.dueDate        = dueDate;
        this.returnDate     = returnDate;
        this.fine           = fine;
        this.status         = status;
    }

    public int    getTransaction_id() { return transaction_id; }
    public String getMemberName()     { return memberName; }
    public String getBookTitle()      { return bookTitle; }
    public String getIssueDate()      { return issueDate; }
    public String getDueDate()        { return dueDate; }
    public String getReturnDate()     { return returnDate != null ? returnDate : "-"; }
    public double getFine()           { return fine; }
    public String getStatus()         { return status; }
}