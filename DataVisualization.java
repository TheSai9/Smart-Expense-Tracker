import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.sql.*;

public class DataVisualization {

    private static final String DB_URL = "jdbc:sqlite:finance.db";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Income and Expense Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JFreeChart chart = createChart();
            if (chart != null) {
                ChartPanel chartPanel = new ChartPanel(chart);
                frame.add(chartPanel);
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "No data available for visualization.");
                frame.dispose();
            }
        });
    }

    private static JFreeChart createChart() {
        DefaultCategoryDataset dataset = fetchDataForChart();

        if (dataset.getRowCount() == 0) {
            return null; // No data to display
        }

        return ChartFactory.createBarChart(
                "Income and Expenses by Month",
                "Month",             // X-axis label
                "Amount (USD)",      // Y-axis label
                dataset              // Dataset
        );
    }

    private static DefaultCategoryDataset fetchDataForChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sql = """
                     SELECT strftime('%Y-%m', timestamp) AS month,
                            type,
                            SUM(amount) AS total
                     FROM transactions
                     GROUP BY month, type
                     ORDER BY month;
                     """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String month = rs.getString("month");
                String type = rs.getString("type");
                double total = rs.getDouble("total");
                dataset.addValue(total, type, month);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching data for chart: " + e.getMessage());
        }

        return dataset;
    }
}
