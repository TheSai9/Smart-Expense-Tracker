import java.sql.*;
import java.util.Scanner;

public class ExpenseManager {
    private static final String DB_URL = "jdbc:sqlite:finance.db";

    public static void main(String[] args) {
        createTables();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nExpense and Income Manager");
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View Transactions");
            System.out.println("4. Edit Transaction");
            System.out.println("5. Delete Transaction");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addTransaction(scanner, "Income");
                case 2 -> addTransaction(scanner, "Expense");
                case 3 -> viewTransactions();
                case 4 -> editTransaction(scanner);
                case 5 -> deleteTransaction(scanner);
                case 6 -> {
                    System.out.println("Exiting... Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createTables() {
        String sql = """
                     CREATE TABLE IF NOT EXISTS transactions (
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         type TEXT NOT NULL,
                         amount REAL NOT NULL,
                         notes TEXT,
                         timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                     );
                     """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    private static void addTransaction(Scanner scanner, String type) {
        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter notes (optional): ");
        String notes = scanner.nextLine();

        String sql = "INSERT INTO transactions(type, amount, notes) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, notes.isBlank() ? null : notes);
            pstmt.executeUpdate();
            System.out.println(type + " added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding " + type.toLowerCase() + ": " + e.getMessage());
        }
    }

    private static void viewTransactions() {
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nTransactions:");
            System.out.printf("%-5s %-10s %-10s %-30s %-20s%n", "ID", "Type", "Amount", "Notes", "Timestamp");
            System.out.println("-----------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                String notes = rs.getString("notes");
                String timestamp = rs.getString("timestamp");

                System.out.printf("%-5d %-10s %-10.2f %-30s %-20s%n",
                        id, type, amount, notes == null ? "N/A" : notes, timestamp);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: " + e.getMessage());
        }
    }

    private static void editTransaction(Scanner scanner) {
        System.out.print("Enter the ID of the transaction to edit: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter new amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter new notes (optional): ");
        String notes = scanner.nextLine();

        String sql = "UPDATE transactions SET amount = ?, notes = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, notes.isBlank() ? null : notes);
            pstmt.setInt(3, id);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Transaction updated successfully!");
            } else {
                System.out.println("Transaction not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }

    private static void deleteTransaction(Scanner scanner) {
        System.out.print("Enter the ID of the transaction to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Transaction deleted successfully!");
            } else {
                System.out.println("Transaction not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }
}
