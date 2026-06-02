package com.pinnacle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:library.db";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public void initializeDatabase() {
        String createBooks = "CREATE TABLE IF NOT EXISTS books (book_id INTEGER PRIMARY KEY, book_name TEXT, author TEXT, category TEXT, quantity INTEGER)";
        String createMembers = "CREATE TABLE IF NOT EXISTS members (student_id INTEGER PRIMARY KEY, name TEXT, department TEXT, phone TEXT)";
        String createIssues = "CREATE TABLE IF NOT EXISTS issues (issue_id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, book_id INTEGER, issue_date DATE DEFAULT CURRENT_DATE)";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createBooks);
            stmt.execute(createMembers);
            stmt.execute(createIssues);
        } catch (SQLException e) { System.out.println("⚠️ DB Init Error: " + e.getMessage()); }
    }

    // --- DASHBOARD DATA ---
    public int getCount(String tableName) {
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM " + tableName)) {
            return rs.getInt("total");
        } catch (SQLException e) { return 0; }
    }

    public List<Book> getRecentBooks(int limit) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id DESC LIMIT " + limit;
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) books.add(new Book(rs.getInt("book_id"), rs.getString("book_name"), rs.getString("author"), rs.getString("category"), rs.getInt("quantity")));
        } catch (SQLException e) { System.out.println("⚠️ Fetch Recent Error"); }
        return books;
    }

    // --- BOOKS CRUD ---
    public void addBook(int id, String name, String author, String category, int quantity) {
        String sql = "INSERT INTO books(book_id, book_name, author, category, quantity) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id); pstmt.setString(2, name); pstmt.setString(3, author); pstmt.setString(4, category); pstmt.setInt(5, quantity);
            pstmt.executeUpdate();
        } catch (SQLException e) { System.out.println("⚠️ Add Book Error (Might be duplicate ID): " + e.getMessage()); }
    }

    public void deleteBook(int id) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id); pstmt.executeUpdate();
        } catch (SQLException e) { System.out.println("⚠️ Delete Book Error: " + e.getMessage()); }
    }

    public List<Book> getAllBooks() {
        return getRecentBooks(1000); 
    }

    // --- MEMBERS CRUD ---
    public void addMember(int id, String name, String dept, String phone) {
        String sql = "INSERT INTO members(student_id, name, department, phone) VALUES(?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id); pstmt.setString(2, name); pstmt.setString(3, dept); pstmt.setString(4, phone);
            pstmt.executeUpdate();
        } catch (SQLException e) { System.out.println("⚠️ Add Member Error"); }
    }

    public void deleteMember(int id) {
        String sql = "DELETE FROM members WHERE student_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id); pstmt.executeUpdate();
        } catch (SQLException e) { System.out.println("⚠️ Delete Member Error: " + e.getMessage()); }
    }

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM members")) {
            while (rs.next()) members.add(new Member(rs.getInt("student_id"), rs.getString("name"), rs.getString("department"), rs.getString("phone")));
        } catch (SQLException e) { System.out.println("⚠️ Fetch Members Error"); }
        return members;
    }

    // --- ISSUE & RETURN LOGIC ---
    public String issueBook(int studentId, int bookId) {
        // 1. Verify Student Exists
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement("SELECT * FROM members WHERE student_id = ?")) {
            stmt.setInt(1, studentId);
            if (!stmt.executeQuery().next()) return "Student ID (" + studentId + ") does not exist. Please register the member first.";
        } catch (SQLException e) { return "Database error checking student: " + e.getMessage(); }

        // 2. Verify Book Exists and has Stock
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement("SELECT quantity FROM books WHERE book_id = ?")) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "Book ID (" + bookId + ") does not exist in the library.";
            if (rs.getInt("quantity") <= 0) return "This book is currently out of stock!";
        } catch (SQLException e) { return "Database error checking book: " + e.getMessage(); }

        // 3. Process the Issue
        String updateStock = "UPDATE books SET quantity = quantity - 1 WHERE book_id = ?";
        
        //Explicitly passing 'CURRENT_DATE' to satisfy the NOT NULL database constraint
        String logIssue = "INSERT INTO issues(student_id, book_id, issue_date) VALUES(?, ?, CURRENT_DATE)";
        
        try (Connection conn = connect(); 
             PreparedStatement stockStmt = conn.prepareStatement(updateStock); 
             PreparedStatement issueStmt = conn.prepareStatement(logIssue)) {
             
            stockStmt.setInt(1, bookId);
            stockStmt.executeUpdate();

            issueStmt.setInt(1, studentId);
            issueStmt.setInt(2, bookId);
            issueStmt.executeUpdate();
            return "SUCCESS";
        } catch (SQLException e) { return "Transaction Error: " + e.getMessage(); }
    }

    public String returnBook(int studentId, int bookId) {
        // 1. Verify the active issue exists
        String checkIssue = "SELECT * FROM issues WHERE student_id = ? AND book_id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(checkIssue)) {
            stmt.setInt(1, studentId); stmt.setInt(2, bookId);
            if (!stmt.executeQuery().next()) return "No active record found of Student " + studentId + " borrowing Book " + bookId + ".";
        } catch (SQLException e) { return "Database error checking records: " + e.getMessage(); }

        // 2. Process the Return
        String removeIssue = "DELETE FROM issues WHERE student_id = ? AND book_id = ?";
        String restock = "UPDATE books SET quantity = quantity + 1 WHERE book_id = ?";
        try (Connection conn = connect(); 
             PreparedStatement rmStmt = conn.prepareStatement(removeIssue); 
             PreparedStatement stockStmt = conn.prepareStatement(restock)) { // <-- Fixed here!
             
            rmStmt.setInt(1, studentId); rmStmt.setInt(2, bookId);
            rmStmt.executeUpdate();

            stockStmt.setInt(1, bookId);
            stockStmt.executeUpdate();
            return "SUCCESS";
        } catch (SQLException e) { return "Transaction Error: " + e.getMessage(); }
    }
}