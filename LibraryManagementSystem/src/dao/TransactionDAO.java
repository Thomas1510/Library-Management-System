package dao;

import model.Transaction;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final double FINE_PER_DAY = 5.0;   // ₹5 per overdue day
    private static final int    LOAN_DAYS    = 14;     // default loan period

    // ── Issue ────────────────────────────────────────────────────────────────

    /**
     * Issues a book to a member.
     * Decrements book stock and inserts a transaction row.
     * Returns false if book is out of stock or member not found.
     */
    public boolean issueBook(int member_id, int book_id) {
        // Check availability
        String checkSql = "SELECT quantity FROM books WHERE book_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(checkSql)) {
            ps.setInt(1, book_id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || rs.getInt("quantity") < 1) return false;
        } catch (Exception e) { e.printStackTrace(); return false; }

        // Check member hasn't already borrowed this book and not returned it
        String dupSql = "SELECT COUNT(*) FROM transactions WHERE member_id=? AND book_id=? AND return_date IS NULL";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(dupSql)) {
            ps.setInt(1, member_id);
            ps.setInt(2, book_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false; // already has it
        } catch (Exception e) { e.printStackTrace(); return false; }

        // Insert transaction
        String sql = "INSERT INTO transactions(member_id, book_id, issue_date, due_date) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, member_id);
            ps.setInt(2, book_id);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setDate(4, Date.valueOf(LocalDate.now().plusDays(LOAN_DAYS)));
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return false; }

        // Decrement stock
        new BookDAO().decrementStock(book_id);
        return true;
    }

    // ── Return ────────────────────────────────────────────────────────────────

    /**
     * Returns a book for a member.
     * Calculates fine if overdue, updates transaction, increments stock.
     * Returns calculated fine (0 if on time).
     */
    public double returnBook(int transaction_id) {
        // Fetch the transaction
        String fetchSql = "SELECT book_id, due_date FROM transactions WHERE transaction_id=? AND return_date IS NULL";
        int book_id = -1;
        LocalDate dueDate = null;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(fetchSql)) {
            ps.setInt(1, transaction_id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return -1;   // already returned or not found
            book_id = rs.getInt("book_id");
            dueDate = rs.getDate("due_date").toLocalDate();
        } catch (Exception e) { e.printStackTrace(); return -1; }

        // Calculate fine
        LocalDate today = LocalDate.now();
        long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
        double fine = overdueDays > 0 ? overdueDays * FINE_PER_DAY : 0.0;

        // Update transaction
        String updateSql = "UPDATE transactions SET return_date=?, fine=? WHERE transaction_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(updateSql)) {
            ps.setDate(1, Date.valueOf(today));
            ps.setDouble(2, fine);
            ps.setInt(3, transaction_id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return -1; }

        // Increment stock
        new BookDAO().incrementStock(book_id);
        return fine;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Transaction> getAllTransactions() {
        return query("SELECT t.transaction_id, m.name, b.title, " +
                "t.issue_date, t.due_date, t.return_date, t.fine, " +
                "CASE WHEN t.return_date IS NOT NULL THEN 'Returned' " +
                "     WHEN CURDATE() > t.due_date THEN 'Overdue' " +
                "     ELSE 'Issued' END AS status " +
                "FROM transactions t " +
                "JOIN members m ON t.member_id = m.member_id " +
                "JOIN books   b ON t.book_id   = b.book_id " +
                "ORDER BY t.transaction_id DESC");
    }

    public List<Transaction> getActiveTransactions() {
        return query("SELECT t.transaction_id, m.name, b.title, " +
                "t.issue_date, t.due_date, t.return_date, t.fine, " +
                "CASE WHEN CURDATE() > t.due_date THEN 'Overdue' ELSE 'Issued' END AS status " +
                "FROM transactions t " +
                "JOIN members m ON t.member_id = m.member_id " +
                "JOIN books   b ON t.book_id   = b.book_id " +
                "WHERE t.return_date IS NULL " +
                "ORDER BY t.due_date ASC");
    }

    public List<Transaction> getTransactionsByMember(int member_id) {
        String sql = "SELECT t.transaction_id, m.name, b.title, " +
                "t.issue_date, t.due_date, t.return_date, t.fine, " +
                "CASE WHEN t.return_date IS NOT NULL THEN 'Returned' " +
                "     WHEN CURDATE() > t.due_date THEN 'Overdue' " +
                "     ELSE 'Issued' END AS status " +
                "FROM transactions t " +
                "JOIN members m ON t.member_id = m.member_id " +
                "JOIN books   b ON t.book_id   = b.book_id " +
                "WHERE t.member_id=? ORDER BY t.transaction_id DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, member_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(fromRS(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public int getTotalIssued() {
        return scalarQuery("SELECT COUNT(*) FROM transactions WHERE return_date IS NULL");
    }

    public int getOverdueCount() {
        return scalarQuery("SELECT COUNT(*) FROM transactions WHERE return_date IS NULL AND CURDATE() > due_date");
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private List<Transaction> query(String sql) {
        List<Transaction> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(fromRS(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private Transaction fromRS(ResultSet rs) throws SQLException {
        Date ret = rs.getDate("return_date");
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getString("name"),
                rs.getString("title"),
                rs.getString("issue_date"),
                rs.getString("due_date"),
                ret != null ? ret.toString() : null,
                rs.getDouble("fine"),
                rs.getString("status")
        );
    }

    private int scalarQuery(String sql) {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}