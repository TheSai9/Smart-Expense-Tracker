import java.sql.*;
import java.util.Scanner;

public class BudgetingTool {
    private static final String DB_URL = "jdbc:sqlite:finance.db";

    public static void main(String[] args) {
        createTables();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nBudgeting Tool");
            System.out.println("1. Set Monthly Budget");
            System.out.println("2. View Budget Summary");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> setMonthlyBudget(scanner);
                case 2 -> viewBudgetSummary();
                case 3 -> {
                    System.out.println("Exiting... Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createTables() {
        String budgetTable = """
                             CREATE TABLE IF NOT EXISTS budget (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 month TEXT UNIQUE NOT NULL,
                                 amount REAL NOT NULL
                             );
                             """;

        String transactionsTable = """
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
            stmt.execute(budgetTable);
            stmt.execute(transactionsTable);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    private static void setMonthlyBudget(Scanner scanner) {
        System.out.print("Enter the month (e.g., January): ");
        String month = scanner.nextLine();
        System.out.print("Enter your budget amount: ");
        double amount = scanner.nextDouble();

        String sql = """
                     INSERT INTO budget(month, amount)
                     VALUES(?, ?)
                     ON CONFLICT(month) DO UPDATE SET amount = excluded.amount;
                     """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, month);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
            System.out.println("Budget set successfully for " + month + "!");
        } catch (SQLException e) {
            System.err.println("Error setting budget: " + e.getMessage());
        }
    }

    private static void viewBudgetSummary() {
        System.out.print("Enter the month to view budget summary: ");
        Scanner scanner = new Scanner(System.in);
        String month = scanner.nextLine();

        String budgetSql = "SELECT amount FROM budget WHERE month = ?";
        String expenseSql = """
                            SELECT SUM(amount) AS totalExpense
                            FROM transactions
                            WHERE type = 'Expense' AND strftime('%m', timestamp) = strftime('%m', 'now', ?);
                            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement budgetStmt = conn.prepareStatement(budgetSql);
             PreparedStatement expenseStmt = conn.prepareStatement(expenseSql)) {

            // Get the budget for the month
            budgetStmt.setString(1, month);
            ResultSet budgetRs = budgetStmt.executeQuery();

            if (!budgetRs.next()) {
                System.out.println("No budget set for " + month + ".");
                return;
            }

            double budgetAmount = budgetRs.getDouble("amount");

            // Get the total expenses for the month
            expenseStmt.setString(1, "-" + month);
            ResultSet expenseRs = expenseStmt.executeQuery();
            double totalExpense = expenseRs.next() ? expenseRs.getDouble("totalExpense") : 0.0;

            // Display the summary
            System.out.println("\nBudget Summary for " + month + ":");
            System.out.printf("Budget Amount: %.2f\n", budgetAmount);
            System.out.printf("Total Expenses: %.2f\n", totalExpense);

            if (totalExpense > budgetAmount) {
                System.out.println("Alert: You have exceeded your budget by " + (totalExpense - budgetAmount) + "!");
            } else {
                System.out.println("You are within your budget. Remaining amount: " + (budgetAmount - totalExpense));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving budget summary: " + e.getMessage());
        }
    }
}
