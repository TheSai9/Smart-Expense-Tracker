import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class ReportGenerator {
    private static final String DB_URL = "jdbc:sqlite:finance.db";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nReport Generator");
        System.out.println("1. View Report");
        System.out.println("2. Export Report as Text File");
        System.out.println("3. Export Report as PDF");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1 -> viewReport();
            case 2 -> exportReportAsText();
            case 3 -> exportReportAsPDF();
            default -> System.out.println("Invalid choice. Exiting.");
        }

        scanner.close();
    }

    private static void viewReport() {
        String report = generateReportContent();
        if (report != null) {
            System.out.println("\n" + report);
        } else {
            System.out.println("No data available to generate a report.");
        }
    }

    private static void exportReportAsText() {
        String report = generateReportContent();
        if (report == null) {
            System.out.println("No data available to generate a report.");
            return;
        }

        try {
            String fileName = "BudgetReport.txt";
            java.nio.file.Files.write(java.nio.file.Path.of(fileName), report.getBytes());
            System.out.println("Report saved as " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving report as text: " + e.getMessage());
        }
    }

    private static void exportReportAsPDF() {
        String report = generateReportContent();
        if (report == null) {
            System.out.println("No data available to generate a report.");
            return;
        }

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("BudgetReport.pdf"));
            document.open();

            document.add(new Paragraph("Budget Report"));
            document.add(new Paragraph("Generated on: " + java.time.LocalDate.now()));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4); // 4 columns: Month, Type, Amount, Notes
            table.addCell("Month");
            table.addCell("Type");
            table.addCell("Amount");
            table.addCell("Notes");

            String sql = """
                         SELECT strftime('%Y-%m', timestamp) AS month, type, amount, notes
                         FROM transactions
                         ORDER BY timestamp;
                         """;

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    table.addCell(rs.getString("month"));
                    table.addCell(rs.getString("type"));
                    table.addCell(String.format("%.2f", rs.getDouble("amount")));
                    table.addCell(rs.getString("notes") == null ? "N/A" : rs.getString("notes"));
                }
            } catch (SQLException e) {
                System.err.println("Error fetching data for PDF report: " + e.getMessage());
            }

            document.add(table);
            document.close();
            System.out.println("Report saved as BudgetReport.pdf");
        } catch (Exception e) {
            System.err.println("Error saving report as PDF: " + e.getMessage());
        }
    }

    private static String generateReportContent() {
        StringBuilder report = new StringBuilder();
        String sql = """
                     SELECT strftime('%Y-%m', timestamp) AS month, type, SUM(amount) AS total
                     FROM transactions
                     GROUP BY month, type
                     ORDER BY month;
                     """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!rs.isBeforeFirst()) {
                return null; // No data
            }

            report.append("Budget Report\n");
            report.append("Generated on: ").append(java.time.LocalDate.now()).append("\n\n");
            report.append(String.format("%-10s %-10s %-10s\n", "Month", "Type", "Amount"));
            report.append("-------------------------------\n");

            while (rs.next()) {
                String month = rs.getString("month");
                String type = rs.getString("type");
                double total = rs.getDouble("total");
                report.append(String.format("%-10s %-10s %-10.2f\n", month, type, total));
            }
        } catch (SQLException e) {
            System.err.println("Error generating report content: " + e.getMessage());
            return null;
        }

        return report.toString();
    }
}
