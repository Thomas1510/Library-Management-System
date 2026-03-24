package dao;

import model.Member;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public void addMember(String name, String email, String phone) {
        String sql = "INSERT INTO members(name, email, phone) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Member> getAllMembers() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY name";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Member(
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("join_date")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Member> searchMember(String keyword) {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE name LIKE ? OR email LIKE ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Member(
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("join_date")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void deleteMember(int member_id) {
        String sql = "DELETE FROM members WHERE member_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, member_id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int getTotalMembers() {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM members")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}