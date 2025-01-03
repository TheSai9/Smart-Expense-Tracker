# Expense Manager

## Overview
Expense Manager is a Java-based application designed to help users track their income, expenses, and budgets. The application includes features for transaction management, monthly budget planning, data visualization, and report generation.

## Features
- **Add Transactions:** Record expenses and income with notes.
- **Budget Management:** Set monthly budgets and track progress.
- **Data Visualization:** Generate bar charts for income and expenses over time using JFreeChart.
- **Report Generation:** Export transaction reports as PDF or text files.

## Technologies Used
- **Java**: Programming language.
- **SQLite**: Database for storing transactions and budgets.
- **JFreeChart**: Library for creating charts.
- **iText**: Library for generating PDF reports.
  
## Installation
### Prerequisites
- Java Development Kit (JDK) 8 or higher
- SQLite database
- JFreeChart library
- iText library

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/expense-manager.git
   ```
2. Open the project in your favorite IDE (e.g., IntelliJ IDEA, Eclipse, VS Code).
3. Add the required JAR files (JFreeChart, iText) to your project's classpath.
4. Run the `ExpenseManagerGUI` class to start the application.

## Usage
### Main Menu
- **Add Expense/Income:** Input the amount and optional notes.
- **Set Budget:** Define a monthly budget to track your expenses.
- **View Summary:** See a monthly breakdown of income and expenses.
- **Generate Report:** Create and save reports as PDF or text files.
- **View Chart:** Visualize income and expenses as bar charts.

### Database
- The application uses an SQLite database (`finance.db`).
- Tables:
  - `transactions`: Stores all expenses and incomes.
  - `budget`: Stores monthly budgets.

### Reports
- Generated PDF and text reports are saved in the project directory.
- Bar charts are displayed in a new window and can be exported manually.

## Example Output
### PDF Report
```
Budget Report
Generated on: 2025-01-03

Month    Type      Amount
-------------------------
2025-01  Income    5000.00
2025-01  Expense   2000.00
```

