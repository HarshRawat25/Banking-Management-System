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
