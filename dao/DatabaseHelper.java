package dao;

import model.Account;
import model.SavingsAccount;
import model.CheckingAccount;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    public DatabaseHelper() {
        createTablesIfNeeded();
    }

    private void createTablesIfNeeded() {
        String customer = "CREATE TABLE IF NOT EXISTS Customer (customer_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, address TEXT);";
        String account = "CREATE TABLE IF NOT EXISTS Account (account_number TEXT PRIMARY KEY, customer_id INTEGER, type TEXT, balance REAL DEFAULT 0, FOREIGN KEY(customer_id) REFERENCES Customer(customer_id));";
        String savings = "CREATE TABLE IF NOT EXISTS SavingsAccount (account_number TEXT PRIMARY KEY, interest_rate REAL, FOREIGN KEY(account_number) REFERENCES Account(account_number));";
        String checking = "CREATE TABLE IF NOT EXISTS CheckingAccount (account_number TEXT PRIMARY KEY, overdraft_limit REAL, FOREIGN KEY(account_number) REFERENCES Account(account_number));";
        String meta = "CREATE TABLE IF NOT EXISTS Meta (key TEXT PRIMARY KEY, value TEXT);";

        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            s.execute(customer);
            s.execute(account);
            s.execute(savings);
            s.execute(checking);
            s.execute(meta);
            // initialize sequence
            if (getMeta("account_seq") == null) setMeta("account_seq", "1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // meta helpers
    private String getMeta(String key) {
        String sql = "SELECT value FROM Meta WHERE key=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void setMeta(String key, String value) {
        String up = "INSERT INTO Meta(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(up)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public synchronized String getNextAccountNumber(String prefix) {
        String next = null;
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            String cur = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT value FROM Meta WHERE key=?")) {
                ps.setString(1, "account_seq");
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) cur = rs.getString("value"); }
            }
            int seq = 1;
            try { seq = Integer.parseInt(cur); } catch (Exception ignored) {}
            next = prefix + String.format("%04d", seq);
            try (PreparedStatement ups = c.prepareStatement("INSERT INTO Meta(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
                ups.setString(1, "account_seq");
                ups.setString(2, Integer.toString(seq+1));
                ups.executeUpdate();
            }
            c.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return next;
    }

    public int insertCustomer(String name, String address) {
        String sql = "INSERT INTO Customer(name,address) VALUES(?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean insertAccount(String accountNumber, int customerId, String type, double balance) {
        String sql = "INSERT INTO Account(account_number, customer_id, type, balance) VALUES(?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setInt(2, customerId);
            ps.setString(3, type);
            ps.setDouble(4, balance);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean insertSavings(String accountNumber, double interest) {
        String sql = "INSERT INTO SavingsAccount(account_number, interest_rate) VALUES(?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setDouble(2, interest);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean insertChecking(String accountNumber, double overdraft) {
        String sql = "INSERT INTO CheckingAccount(account_number, overdraft_limit) VALUES(?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setDouble(2, overdraft);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Account> loadAllAccounts() {
        String sql = "SELECT a.account_number, a.type, a.balance, c.customer_id, c.name, c.address, s.interest_rate, chk.overdraft_limit FROM Account a JOIN Customer c ON a.customer_id=c.customer_id LEFT JOIN SavingsAccount s ON a.account_number=s.account_number LEFT JOIN CheckingAccount chk ON a.account_number=chk.account_number ORDER BY a.account_number";
        List<Account> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String accNum = rs.getString("account_number");
                String type = rs.getString("type");
                double balance = rs.getDouble("balance");
                String name = rs.getString("name");
                if ("Savings".equalsIgnoreCase(type)) {
                    double rate = rs.getDouble("interest_rate");
                    list.add(new SavingsAccount(accNum, name, balance, rate));
                } else {
                    double od = rs.getDouble("overdraft_limit");
                    list.add(new CheckingAccount(accNum, name, balance, od));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Account loadAccount(String accountNumber) {
        String sql = "SELECT a.account_number, a.type, a.balance, c.name, s.interest_rate, chk.overdraft_limit FROM Account a JOIN Customer c ON a.customer_id=c.customer_id LEFT JOIN SavingsAccount s ON a.account_number=s.account_number LEFT JOIN CheckingAccount chk ON a.account_number=chk.account_number WHERE a.account_number = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String accNum = rs.getString("account_number");
                    String type = rs.getString("type");
                    double balance = rs.getDouble("balance");
                    String name = rs.getString("name");
                    if ("Savings".equalsIgnoreCase(type)) {
                        double rate = rs.getDouble("interest_rate");
                        return new SavingsAccount(accNum, name, balance, rate);
                    } else {
                        double od = rs.getDouble("overdraft_limit");
                        return new CheckingAccount(accNum, name, balance, od);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // update balance with provided connection (transactional)
    public boolean updateBalance(Connection c, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE Account SET balance=? WHERE account_number=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }
}
