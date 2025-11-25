# Banking-Management-System
# Banking Management System (Java Swing + JDBC)

## 📌 Overview

This project is a **Banking Management System** implemented as a **single-file Java application** using:

* **Swing** (GUI)
* **OOP Concepts** (Inheritance, Polymorphism, Encapsulation, Interfaces)
* **Exception Handling**
* **Collections & Generics**
* **Multithreading & Synchronization**
* **SQLite Database (JDBC)** for persistent storage

The system allows creation of bank accounts, performing deposits and withdrawals, viewing account details, and listing all accounts stored in the database.

---

## 🚀 Features

### ✔ Object-Oriented Design

* `Account` superclass
* `SavingsAccount` and `CheckingAccount` subclasses
* `AccountOperations` interface
* Custom exceptions (`InvalidAmountException`, `InsufficientFundsException`)

### ✔ Banking Operations

* Create new bank accounts
* Deposit money
* Withdraw money
* View account details
* View all accounts

### ✔ Database Integration

* SQLite database
* JDBC Prepared Statements
* CRUD operations
* Auto-generated account numbers using a sequence table

### ✔ GUI (Swing)

* Multi-panel user-friendly GUI
* JTextFields, JButton, JTable, JComboBox
* CardLayout-based screen switching

### ✔ Multithreading

* Background data refresh thread (10s)

---

## 📁 Project Structure

Everything exists in a **single Java file**, but divided into logical sections:

* **Data Models** (Account classes)
* **Exceptions**
* **Database Helper**
* **Main GUI Application**
* **Panels for Different Operations**
* **Utility Methods & Validators**

---

## 🗃 Database Schema

### Table: `Accounts`

| Column Name   | Type             |
| ------------- | ---------------- |
| accountNumber | TEXT PRIMARY KEY |
| name          | TEXT             |
| accountType   | TEXT             |
| balance       | REAL             |

### Table: `Meta`

Used for maintaining a sequence counter for generating account numbers.

---

## 🔧 How to Run

### Prerequisites

* JDK 8 or above
* SQLite JDBC Driver (usually included in classpath)

### Steps

1. Save the file as `BankingSystem.java`
2. Compile:

```
javac BankingSystem.java
```

3. Run:

```
java BankingSystem
```

An SQLite database file `BankDatabase.db` will be auto-created.

---

## 🧪 Testing

You can test the project by:

* Creating different types of accounts
* Making deposits & withdrawals
* Verifying updated values in the database
* Viewing accounts in the JTable

---

## 🎯 OOP Concepts Used

* **Inheritance:** SavingsAccount, CheckingAccount → Account
* **Polymorphism:** Overridden withdraw/deposit methods
* **Encapsulation:** Private fields with getters/setters
* **Abstraction:** AccountOperations interface
* **Exception Handling:** Custom banking exceptions

---

## 📡 JDBC Concepts Used

* Connection handling
* Prepared statements
* CRUD operations
* ResultSet iteration
* Try-with-resources

---

## 🔄 Multithreading

A background thread refreshes GUI data every 10 seconds using:

```
Thread dbRefresher = new Thread(...);
```

Ensures GUI always shows latest DB data.

---

## 📘 Future Improvements

* Add login/authentication system
* Add transaction history
* Add admin dashboard
* Separate MVC architecture (multiple files)
* Add delete/update account features

---

## 🏁 Conclusion

This project is a complete demonstration of:

* Core Java
* GUI development
* OOP principles
* JDBC-based database operations
* Multithreading

It is suitable for academic submissions, demonstrations, and learning enterprise-level application fundamentals in Java.

// BankingSystemSingleFile_Improved.java
// Improved single-file banking app with JDBC (SQLite) integration.
// Features added/fixed:
//  - Proper JDBC transactions for transfers and sequence increment
//  - Interfaces and custom exceptions for clearer OOP
//  - Collections & Generics usage
//  - Simple multithreading auto-refresh (synchronization safe)
//  - Better separation of concerns while keeping single-file layout
// Add sqlite-jdbc-x.x.x.jar to your classpath before running.

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/* ===========================
   Custom Exceptions
   =========================== */
class BankingException extends Exception {
    public BankingException(String message) { super(message); }
}

class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String acc) { super("Account not found: " + acc); }
}

class InsufficientFundsException extends BankingException {
    public InsufficientFundsException(String msg) { super(msg); }
}

/* ===========================
   Interfaces (demonstrate polymorphism)
   =========================== */
interface AccountOperations {
    String deposit(double amount) throws BankingException;
    String withdraw(double amount) throws BankingException;
    String getType();
}

/* ===========================
   Data model classes
   =========================== */
class Customer {
    private final int customerId;
    private final String name;
    private final String address;

    public Customer(int customerId, String name, String address) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;      
    }

    public Customer(String name, String address) { this(-1, name, address); }

    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }

    @Override
    public String toString() { return name + " (" + address + ")"; }
}

abstract class Account implements AccountOperations {
    protected final String accountNumber;
    protected final Customer accountHolder;
    protected double balance; // guarded by instance lock when mutated

    public Account(String accountNumber, Customer accountHolder, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public Customer getAccountHolder() { return accountHolder; }
    public synchronized double getBalance() { return balance; }

    public abstract String getType();

    @Override
    public String toString() {
        return String.format("%s | %s | Holder: %s | Balance: $%.2f",
                accountNumber, getType(), accountHolder.getName(), getBalance());
    }
}

class SavingsAccount extends Account {
    private final double interestRate;

    public SavingsAccount(String accountNumber, Customer accountHolder, double balance, double interestRate) {
        super(accountNumber, accountHolder, balance);
        this.interestRate = interestRate;
    }

    public double getInterestRate() { return interestRate; }

    @Override
    public String getType() { return "Savings"; }

    @Override
    public synchronized String deposit(double amount) throws BankingException {
        if (amount <= 0) throw new BankingException("Deposit amount must be positive.");
        balance += amount;
        return String.format("Deposit successful. New balance: $%.2f", balance);
    }

    @Override
    public synchronized String withdraw(double amount) throws BankingException {
        if (amount <= 0) throw new BankingException("Withdrawal amount must be positive.");
        if (balance < amount) throw new InsufficientFundsException("Withdrawal failed. Insufficient funds.");
        balance -= amount;
        return String.format("Withdrawal successful. New balance: $%.2f", balance);
    }

    @Override
    public String toString() { return super.toString() + String.format(" | Interest: %.2f%%", interestRate); }
}

class CheckingAccount extends Account {
    private final double overdraftLimit;

    public CheckingAccount(String accountNumber, Customer accountHolder, double balance, double overdraftLimit) {
        super(accountNumber, accountHolder, balance);
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() { return overdraftLimit; }

    @Override
    public String getType() { return "Checking"; }

    @Override
    public synchronized String deposit(double amount) throws BankingException {
        if (amount <= 0) throw new BankingException("Deposit amount must be positive.");
        balance += amount;
        return String.format("Deposit successful. New balance: $%.2f", balance);
    }

    @Override
    public synchronized String withdraw(double amount) throws BankingException {
        if (amount <= 0) throw new BankingException("Withdrawal amount must be positive.");
        if (balance + overdraftLimit < amount) throw new InsufficientFundsException("Withdrawal failed. Overdraft limit exceeded.");
        balance -= amount;
        return String.format("Withdrawal successful. New balance: $%.2f", balance);
    }

    @Override
    public String toString() { return super.toString() + String.format(" | Overdraft limit: $%.2f", overdraftLimit); }
}

/* ===========================
   Database helper (JDBC) - improved
   - transaction-safe transfer
   - sequence increment inside DB transaction
   =========================== */
class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:bank.db";

    public DatabaseHelper() {
        createTables();
    }

    private Connection connect() throws SQLException { return DriverManager.getConnection(DB_URL); }

    private void createTables() {
        final String customerTable =
                "CREATE TABLE IF NOT EXISTS Customer (" +
                        "customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "address TEXT NOT NULL" +
                        ");";

        final String accountTable =
                "CREATE TABLE IF NOT EXISTS Account (" +
                        "account_number TEXT PRIMARY KEY," +
                        "customer_id INTEGER NOT NULL," +
                        "type TEXT NOT NULL," +
                        "balance REAL DEFAULT 0," +
                        "FOREIGN KEY(customer_id) REFERENCES Customer(customer_id)" +
                        ");";

        final String savingsTable =
                "CREATE TABLE IF NOT EXISTS SavingsAccount (" +
                        "account_number TEXT PRIMARY KEY," +
                        "interest_rate REAL NOT NULL," +
                        "FOREIGN KEY(account_number) REFERENCES Account(account_number)" +
                        ");";

        final String checkingTable =
                "CREATE TABLE IF NOT EXISTS CheckingAccount (" +
                        "account_number TEXT PRIMARY KEY," +
                        "overdraft_limit REAL NOT NULL," +
                        "FOREIGN KEY(account_number) REFERENCES Account(account_number)" +
                        ");";

        final String metaTable =
                "CREATE TABLE IF NOT EXISTS Meta (" +
                        "key TEXT PRIMARY KEY," +
                        "value TEXT" +
                        ");";

        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute(customerTable);
            st.execute(accountTable);
            st.execute(savingsTable);
            st.execute(checkingTable);
            st.execute(metaTable);

            // Initialize account sequence if not present
            if (getMetaValue("account_seq") == null) setMetaValue("account_seq", "1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Read meta value (no transaction)
    private String getMetaValue(String key) {
        String sql = "SELECT value FROM Meta WHERE key=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString("value"); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Set meta value (no transaction)
    private void setMetaValue(String key, String value) {
        String update = "INSERT INTO Meta(key,value) VALUES(?,?) " +
                "ON CONFLICT(key) DO UPDATE SET value=excluded.value;";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Get next account number using DB-side locking inside a transaction to be safer across processes
    public String getNextAccountNumber(String prefix) {
        // We'll use a transaction to read->increment atomically
        String next = null;
        String sel = "SELECT value FROM Meta WHERE key = 'account_seq' FOR UPDATE;"; // FOR UPDATE ignored by sqlite, but we'll do SELECT then UPDATE in same conn
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            String current = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT value FROM Meta WHERE key = ?")) {
                ps.setString(1, "account_seq");
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) current = rs.getString("value"); }
            }
            int seq = 1;
            try { seq = Integer.parseInt(current); } catch (Exception ignored) {}
            next = prefix + String.format("%04d", seq);

            try (PreparedStatement ups = conn.prepareStatement("INSERT INTO Meta(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
                ups.setString(1, "account_seq");
                ups.setString(2, Integer.toString(seq + 1));
                ups.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return next;
    }

    // Insert customer and return generated id
    public int insertCustomer(String name, String address) {
        String sql = "INSERT INTO Customer(name,address) VALUES(?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    // Insert common account
    public boolean insertAccount(String accountNumber, int customerId, String type, double balance) {
        String sql = "INSERT INTO Account(account_number, customer_id, type, balance) VALUES(?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setInt(2, customerId);
            ps.setString(3, type);
            ps.setDouble(4, balance);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean insertSavingsAccount(String accountNumber, double interestRate) {
        String sql = "INSERT INTO SavingsAccount(account_number, interest_rate) VALUES(?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setDouble(2, interestRate);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean insertCheckingAccount(String accountNumber, double overdraftLimit) {
        String sql = "INSERT INTO CheckingAccount(account_number, overdraft_limit) VALUES(?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setDouble(2, overdraftLimit);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Update balance
    public boolean updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE Account SET balance=? WHERE account_number=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    // Convenience wrapper
    public boolean updateBalance(String accountNumber, double newBalance) {
        try (Connection conn = connect()) {
            return updateBalance(conn, accountNumber, newBalance);
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Load all accounts with details
    public List<Account> loadAllAccounts() {
        String sql = "SELECT a.account_number, a.type, a.balance, c.customer_id, c.name, c.address, " +
                "s.interest_rate, chk.overdraft_limit " +
                "FROM Account a " +
                "JOIN Customer c ON a.customer_id = c.customer_id " +
                "LEFT JOIN SavingsAccount s ON a.account_number = s.account_number " +
                "LEFT JOIN CheckingAccount chk ON a.account_number = chk.account_number " +
                "ORDER BY a.account_number;";

        List<Account> results = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String accNum = rs.getString("account_number");
                String type = rs.getString("type");
                double balance = rs.getDouble("balance");
                int custId = rs.getInt("customer_id");
                String name = rs.getString("name");
                String addr = rs.getString("address");
                Customer cust = new Customer(custId, name, addr);

                if ("Savings".equalsIgnoreCase(type)) {
                    double rate = rs.getDouble("interest_rate");
                    results.add(new SavingsAccount(accNum, cust, balance, rate));
                } else {
                    double od = rs.getDouble("overdraft_limit");
                    results.add(new CheckingAccount(accNum, cust, balance, od));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    // Load single account (non-transactional). Useful for read-only
    public Account loadAccountByNumber(String accountNumber) {
        String sql = "SELECT a.account_number, a.type, a.balance, c.customer_id, c.name, c.address, " +
                "s.interest_rate, chk.overdraft_limit " +
                "FROM Account a " +
                "JOIN Customer c ON a.customer_id = c.customer_id " +
                "LEFT JOIN SavingsAccount s ON a.account_number = s.account_number " +
                "LEFT JOIN CheckingAccount chk ON a.account_number = chk.account_number " +
                "WHERE a.account_number = ?;";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String accNum = rs.getString("account_number");
                    String type = rs.getString("type");
                    double balance = rs.getDouble("balance");
                    int custId = rs.getInt("customer_id");
                    String name = rs.getString("name");
                    String addr = rs.getString("address");
                    Customer cust = new Customer(custId, name, addr);

                    if ("Savings".equalsIgnoreCase(type)) {
                        double rate = rs.getDouble("interest_rate");
                        return new SavingsAccount(accNum, cust, balance, rate);
                    } else {
                        double od = rs.getDouble("overdraft_limit");
                        return new CheckingAccount(accNum, cust, balance, od);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Perform a transfer atomically using a single DB transaction. Returns message or throws BankingException
    public String performTransferAtomic(String fromAcc, String toAcc, double amount) throws BankingException {
        if (amount <= 0) throw new BankingException("Transfer amount must be positive.");

        String selectSql = "SELECT a.account_number, a.balance, a.type, s.interest_rate, chk.overdraft_limit " +
                "FROM Account a " +
                "LEFT JOIN SavingsAccount s ON a.account_number = s.account_number " +
                "LEFT JOIN CheckingAccount chk ON a.account_number = chk.account_number " +
                "WHERE a.account_number = ?;";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            // load from account
            double fromBalance;
            String fromType;
            double fromOverdraft = 0;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, fromAcc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); throw new AccountNotFoundException(fromAcc); }
                    fromBalance = rs.getDouble("balance");
                    fromType = rs.getString("type");
                    fromOverdraft = rs.getDouble("overdraft_limit");
                }
            }

            double toBalance;
            String toType;
            double toOverdraft = 0;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, toAcc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); throw new AccountNotFoundException(toAcc); }
                    toBalance = rs.getDouble("balance");
                    toType = rs.getString("type");
                    toOverdraft = rs.getDouble("overdraft_limit");
                }
            }

            // Validate withdrawal from "fromAcc"
            if ("Savings".equalsIgnoreCase(fromType)) {
                if (fromBalance < amount) { conn.rollback(); throw new InsufficientFundsException("Insufficient funds in source account."); }
            } else { // Checking
                if (fromBalance + fromOverdraft < amount) { conn.rollback(); throw new InsufficientFundsException("Overdraft limit exceeded in source account."); }
            }

            double newFrom = fromBalance - amount;
            double newTo = toBalance + amount;

            // update balances
            boolean u1 = updateBalance(conn, fromAcc, newFrom);
            boolean u2 = updateBalance(conn, toAcc, newTo);
            if (u1 && u2) {
                conn.commit();
                return "Transfer successful.";
            } else {
                conn.rollback();
                throw new BankingException("Transfer failed while updating database.");
            }
        } catch (SQLException e) {
            throw new BankingException("Transfer failed: " + e.getMessage());
        }
    }
}

/* ===========================
   Business logic (Bank)
   - keeps business rules here
   =========================== */
class Bank {
    private final DatabaseHelper db;

    public Bank(DatabaseHelper db) { this.db = db; }

    // Create savings account
    public Account createSavingsAccount(String customerName, String address, double initialDeposit, double interestRate) throws BankingException {
        if (initialDeposit < 0) throw new BankingException("Initial deposit must be non-negative.");
        int custId = db.insertCustomer(customerName, address);
        String accNum = db.getNextAccountNumber("SAV");
        boolean ok = db.insertAccount(accNum, custId, "Savings", initialDeposit);
        if (!ok) throw new BankingException("Failed to insert account row.");
        db.insertSavingsAccount(accNum, interestRate);
        Account a = db.loadAccountByNumber(accNum);
        if (a == null) throw new BankingException("Failed to load newly created account.");
        return a;
    }

    // Create checking account
    public Account createCheckingAccount(String customerName, String address, double initialDeposit, double overdraftLimit) throws BankingException {
        if (initialDeposit < 0) throw new BankingException("Initial deposit must be non-negative.");
        int custId = db.insertCustomer(customerName, address);
        String accNum = db.getNextAccountNumber("CHK");
        boolean ok = db.insertAccount(accNum, custId, "Checking", initialDeposit);
        if (!ok) throw new BankingException("Failed to insert account row.");
        db.insertCheckingAccount(accNum, overdraftLimit);
        Account a = db.loadAccountByNumber(accNum);
        if (a == null) throw new BankingException("Failed to load newly created account.");
        return a;
    }

    public Account findAccount(String accountNumber) { return db.loadAccountByNumber(accountNumber); }

    public String deposit(String accountNumber, double amount) throws BankingException {
        if (amount <= 0) throw new BankingException("Deposit amount must be positive.");
        Account acc = findAccount(accountNumber);
        if (acc == null) throw new AccountNotFoundException(accountNumber);
        String msg = acc.deposit(amount);
        boolean ok = db.updateBalance(accountNumber, acc.getBalance());
        if (!ok) throw new BankingException("Failed to persist deposit.");
        return msg;
    }

    public String withdraw(String accountNumber, double amount) throws BankingException {
        if (amount <= 0) throw new BankingException("Withdrawal amount must be positive.");
        Account acc = findAccount(accountNumber);
        if (acc == null) throw new AccountNotFoundException(accountNumber);
        String msg = acc.withdraw(amount);
        boolean ok = db.updateBalance(accountNumber, acc.getBalance());
        if (!ok) throw new BankingException("Failed to persist withdrawal.");
        return msg;
    }

    // Transfer: uses DB atomic transfer
    public String transfer(String fromAccNum, String toAccNum, double amount) throws BankingException {
        return db.performTransferAtomic(fromAccNum, toAccNum, amount);
    }

    public List<Account> getAllAccounts() { return Collections.unmodifiableList(db.loadAllAccounts()); }
}

/* ===========================
   GUI (BankingSystem) - with a background refresher thread demonstrating multithreading
   =========================== */
public class BankingSystemSingleFile extends JFrame {
    private final DatabaseHelper dbHelper;
    private final Bank bank;
    private final JTextArea displayArea;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public BankingSystemSingleFile() {
        dbHelper = new DatabaseHelper();
        bank = new Bank(dbHelper);

        setTitle("Banking Management System (Improved)");
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        displayArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(displayArea);
        add(scrollPane, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Create Account", createAccountPanel());
        tabs.addTab("Transactions", createTransactionPanel());
        tabs.addTab("Accounts", createAccountsPanel());
        add(tabs, BorderLayout.NORTH);

        JButton refreshBtn = new JButton("Refresh Account List");
        refreshBtn.addActionListener(e -> refreshDisplay());
        JPanel south = new JPanel();
        south.add(refreshBtn);
        add(south, BorderLayout.SOUTH);

        // start background refresher (demonstrates multithreading & synchronization)
        Thread refresher = new Thread(this::autoRefreshLoop, "AutoRefresher");
        refresher.setDaemon(true);
        refresher.start();

        refreshDisplay();
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Customer Name:"), c);
        c.gridx = 1; c.weightx = 1.0; JTextField nameField = new JTextField(); panel.add(nameField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; panel.add(new JLabel("Customer Address:"), c);
        c.gridx = 1; c.weightx = 1.0; JTextField addressField = new JTextField(); panel.add(addressField, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; panel.add(new JLabel("Initial Deposit:"), c);
        c.gridx = 1; c.weightx = 1.0; JTextField depositField = new JTextField(); panel.add(depositField, c);

        c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Type:"), c);
        c.gridx = 1; JRadioButton savBtn = new JRadioButton("Savings", true); JRadioButton chkBtn = new JRadioButton("Checking");
        ButtonGroup group = new ButtonGroup(); group.add(savBtn); group.add(chkBtn);
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); typePanel.add(savBtn); typePanel.add(chkBtn);
        panel.add(typePanel, c);

        c.gridx = 0; c.gridy = 4; JLabel rateOrLimitLabel = new JLabel("Interest Rate (%):"); panel.add(rateOrLimitLabel, c);
        c.gridx = 1; JTextField rateOrLimitField = new JTextField(); panel.add(rateOrLimitField, c);

        savBtn.addActionListener(e -> rateOrLimitLabel.setText("Interest Rate (%):"));
        chkBtn.addActionListener(e -> rateOrLimitLabel.setText("Overdraft Limit:"));

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        JButton createBtn = new JButton("Create Account"); panel.add(createBtn, c);

        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String addr = addressField.getText().trim();
            String depositStr = depositField.getText().trim();
            String rateStr = rateOrLimitField.getText().trim();
            if (name.isEmpty() || addr.isEmpty() || depositStr.isEmpty() || rateStr.isEmpty()) { showError("All fields are required."); return; }
            try {
                double deposit = Double.parseDouble(depositStr);
                double rate = Double.parseDouble(rateStr);
                Account created;
                if (savBtn.isSelected()) created = bank.createSavingsAccount(name, addr, deposit, rate);
                else created = bank.createCheckingAccount(name, addr, deposit, rate);
                showMessage("Account created: " + created.getAccountNumber());
                nameField.setText(""); addressField.setText(""); depositField.setText(""); rateOrLimitField.setText("");
                refreshDisplay();
            } catch (NumberFormatException ex) { showError("Please enter valid numeric values for deposit and rate/limit."); }
            catch (BankingException ex) { showError(ex.getMessage()); }
        });

        return panel;
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; panel.add(new JLabel("Account Number:"), c);
        c.gridx=1; c.weightx=1.0; JTextField accField = new JTextField(); panel.add(accField, c);

        c.gridx=0; c.gridy=1; c.weightx=0; panel.add(new JLabel("Amount:"), c);
        c.gridx=1; c.weightx=1.0; JTextField amountField = new JTextField(); panel.add(amountField, c);

        c.gridx=0; c.gridy=2; panel.add(new JLabel("To Account (For Transfer):"), c);
        c.gridx=1; JTextField toAccField = new JTextField(); panel.add(toAccField, c);

        c.gridx=0; c.gridy=3; c.gridwidth=1; JButton depositBtn = new JButton("Deposit"); panel.add(depositBtn, c);
        c.gridx=1; JButton withdrawBtn = new JButton("Withdraw"); panel.add(withdrawBtn, c);
        c.gridx=0; c.gridy=4; c.gridwidth=2; JButton transferBtn = new JButton("Transfer"); panel.add(transferBtn, c);

        depositBtn.addActionListener(e -> {
            String acc = accField.getText().trim(); String amt = amountField.getText().trim();
            if (acc.isEmpty() || amt.isEmpty()) { showError("Account and amount required."); return; }
            try { double amount = Double.parseDouble(amt); String msg = bank.deposit(acc, amount); showMessage(msg); refreshDisplay(); }
            catch (NumberFormatException ex) { showError("Invalid amount."); }
            catch (BankingException ex) { showError(ex.getMessage()); }
        });

        withdrawBtn.addActionListener(e -> {
            String acc = accField.getText().trim(); String amt = amountField.getText().trim();
            if (acc.isEmpty() || amt.isEmpty()) { showError("Account and amount required."); return; }
            try { double amount = Double.parseDouble(amt); String msg = bank.withdraw(acc, amount); showMessage(msg); refreshDisplay(); }
            catch (NumberFormatException ex) { showError("Invalid amount."); }
            catch (BankingException ex) { showError(ex.getMessage()); }
        });

        transferBtn.addActionListener(e -> {
            String from = accField.getText().trim(); String amt = amountField.getText().trim(); String to = toAccField.getText().trim();
            if (from.isEmpty() || to.isEmpty() || amt.isEmpty()) { showError("From, To and Amount are required."); return; }
            try { double amount = Double.parseDouble(amt); String msg = bank.transfer(from, to, amount); showMessage(msg); refreshDisplay(); }
            catch (NumberFormatException ex) { showError("Invalid amount."); }
            catch (BankingException ex) { showError(ex.getMessage()); }
        });

        return panel;
    }

    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel label = new JLabel("Accounts loaded from database:"); panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private synchronized void refreshDisplay() {
        List<Account> accounts = bank.getAllAccounts();
        StringBuilder sb = new StringBuilder(); sb.append("--- All Bank Accounts in DB ---\n\n");
        for (Account a : accounts) sb.append(a.toString()).append('\n');
        if (accounts.isEmpty()) sb.append("(no accounts yet)\n");
        displayArea.setText(sb.toString());
    }

    private void autoRefreshLoop() {
        while (running.get()) {
            try {
                Thread.sleep(10000); // refresh every 10s
            } catch (InterruptedException ignored) { }
            if (!running.get()) break;
            SwingUtilities.invokeLater(this::refreshDisplay);
        }
    }

    private void showMessage(String msg) { JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }

    public void stopBackgroundTasks() { running.set(false); }

    public static void main(String[] args) {
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "SQLite JDBC driver not found. Please add sqlite-jdbc jar to classpath.", "Driver Missing", JOptionPane.ERROR_MESSAGE);
            System.err.println("Missing sqlite-jdbc driver: " + e.getMessage());
            return;
        }

        SwingUtilities.invokeLater(() -> {
            BankingSystemSingleFile win = new BankingSystemSingleFile();
            win.setVisible(true);
            // add shutdown hook to stop background refresher
            Runtime.getRuntime().addShutdownHook(new Thread(() -> { win.stopBackgroundTasks(); }));
        });
    }
}
