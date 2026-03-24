package dao;

import model.Book;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // ── helpers ──────────────────────────────────────────────────────────────

    private Book fromResultSet(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getInt("total_quantity")
        );
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public void addBook(String title, String author, String category, int quantity) {
        String sql = "INSERT INTO books(title, author, category, quantity, total_quantity) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, category);
            ps.setInt(4, quantity);
            ps.setInt(5, quantity);   // total_quantity mirrors initial stock
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(fromResultSet(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void updateBook(int book_id, String title, String author, String category, int quantity) {
        String sql = "UPDATE books SET title=?, author=?, category=?, quantity=?, total_quantity=? WHERE book_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, category);
            ps.setInt(4, quantity);
            ps.setInt(5, quantity);
            ps.setInt(6, book_id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteBook(int book_id) {
        String sql = "DELETE FROM books WHERE book_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, book_id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Book> searchBook(String keyword) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR category LIKE ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(fromResultSet(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── stock helpers (called by TransactionDAO) ───────────────────────────

    public void decrementStock(int book_id) {
        String sql = "UPDATE books SET quantity = quantity - 1 WHERE book_id=? AND quantity > 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, book_id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void incrementStock(int book_id) {
        String sql = "UPDATE books SET quantity = quantity + 1 WHERE book_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, book_id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── dashboard stats ────────────────────────────────────────────────────

    public int getTotalBooks() {
        return scalarQuery("SELECT COUNT(*) FROM books");
    }

    public int getTotalStock() {
        return scalarQuery("SELECT COALESCE(SUM(quantity),0) FROM books");
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