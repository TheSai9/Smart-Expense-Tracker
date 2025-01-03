import java.sql.*;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class UserAuthentication {
    private static final String DB_URL = "jdbc:sqlite:users.db";

    public static void main(String[] args) {
        createUsersTable();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome! Choose an option: ");
        System.out.println("1. Register");
        System.out.println("2. Login");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (choice == 1) {
            registerUser(scanner);
        } else if (choice == 2) {
            loginUser(scanner);
        } else {
            System.out.println("Invalid option. Exiting...");
        }
        scanner.close();
    }

    private static void createUsersTable() {
        String sql = """
                     CREATE TABLE IF NOT EXISTS users (
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         username TEXT UNIQUE NOT NULL,
                         password TEXT NOT NULL
                     );
                     """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    private static void registerUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        if (username.isBlank()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        System.out.print("Enter password (min 8 chars, at least 1 digit, 1 uppercase, 1 special char): ");
        String password = scanner.nextLine();

        if (!isValidPassword(password)) {
            System.out.println("Password does not meet the criteria.");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            System.out.println("Registration successful!");
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
        }
    }

    private static void loginUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    System.out.println("Login successful! Welcome, " + username);
                } else {
                    System.out.println("Invalid username or password.");
                }
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
    }

    private static boolean isValidPassword(String password) {
        return password.length() >= 8 &&
               password.matches(".*\\d.*") &&          // At least one digit
               password.matches(".*[A-Z].*") &&       // At least one uppercase letter
               password.matches(".*[!@#$%^&*].*");    // At least one special character
    }
}
