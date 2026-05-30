import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

// ============================================================
//  PARENT CLASS — Account (Base / Parent)
// ============================================================
abstract class Account {
    protected String accountHolderName;
    protected int accountNumber;
    protected double balance;
    protected ArrayList<String> transactionHistory;

    public Account(String accountHolderName) {
        this.accountHolderName = accountHolderName;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
        this.accountNumber = generateAccountNumber();
    }

    private int generateAccountNumber() {
        Random random = new Random();
        return random.nextInt(900000) + 100000; // 6-digit account number
    }

    // Abstract method — overridden in child classes (Polymorphism)
    public abstract String getAccountType();

    // Method Overloading (Polymorphism)
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        logTransaction("DEPOSIT", amount);
        return true;
    }

    public boolean deposit(double amount, String note) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        logTransaction("DEPOSIT [" + note + "]", amount);
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        logTransaction("WITHDRAWAL", amount);
        return true;
    }

    protected void logTransaction(String type, double amount) {
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        transactionHistory.add("[" + timestamp + "] " + type + " : RM " + String.format("%.2f", amount)
                + " | Balance: RM " + String.format("%.2f", balance));
    }

    public String getTransactionHistory() {
        if (transactionHistory.isEmpty()) {
            return "No transactions found.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < transactionHistory.size(); i++) {
            sb.append((i + 1)).append(". ").append(transactionHistory.get(i)).append("\n");
        }
        return sb.toString();
    }

    // Getters
    public String getName()          { return accountHolderName; }
    public int    getAccountNumber() { return accountNumber; }
    public double getBalance()       { return balance; }
}

// ============================================================
//  CHILD CLASS 1 — SavingsAccount (Inherits Account)
// ============================================================
class SavingsAccount extends Account {
    private int loanCount;
    private double loanBalance;
    private boolean loanEnabled;

    public SavingsAccount(String name) {
        super(name);
        this.loanCount   = 0;
        this.loanBalance = 0.0;
        this.loanEnabled = true;
    }

    // Method Overriding (Polymorphism)
    @Override
    public String getAccountType() {
        return "Savings Account";
    }

    public boolean takeLoan(double amount) {
        if (!loanEnabled) {
            return false; // loan feature disabled by admin
        }
        if (loanCount >= 2) {
            return false; // max 2 loans
        }
        balance     += amount;
        loanBalance += amount;
        loanCount++;
        logTransaction("LOAN RECEIVED", amount);
        return true;
    }

    public boolean transfer(double amount, SavingsAccount recipient) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        recipient.balance += amount;
        logTransaction("TRANSFER TO " + recipient.getName(), amount);
        recipient.logTransaction("TRANSFER FROM " + this.accountHolderName, amount);
        return true;
    }

    public void setLoanEnabled(boolean enabled) { this.loanEnabled = enabled; }
    public boolean isLoanEnabled()              { return loanEnabled; }
    public double  getLoanBalance()             { return loanBalance; }
    public int     getLoanCount()               { return loanCount; }
}

// ============================================================
//  CHILD CLASS 2 — CurrentAccount (Inherits Account)
// ============================================================
class CurrentAccount extends Account {
    private double overdraftLimit;

    public CurrentAccount(String name) {
        super(name);
        this.overdraftLimit = 500.0; // RM 500 overdraft
    }

    // Method Overriding (Polymorphism)
    @Override
    public String getAccountType() {
        return "Current Account";
    }

    // Overrides parent withdraw — allows overdraft
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > (balance + overdraftLimit)) {
            return false;
        }
        balance -= amount;
        logTransaction("WITHDRAWAL (Overdraft allowed)", amount);
        return true;
    }

    public double getOverdraftLimit() { return overdraftLimit; }
}

// ============================================================
//  ADMIN CLASS
// ============================================================
class BankAdmin {
    private final String adminUsername = "admin";
    private final String adminPassword = "admin123";
    private ArrayList<SavingsAccount> accounts;
    private double bankReserve;
    private double totalLoanIssued;

    public BankAdmin() {
        accounts       = new ArrayList<>();
        bankReserve    = 100000.0;
        totalLoanIssued = 0.0;
    }

    public boolean authenticate(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }

    public SavingsAccount findAccount(String name, String password) {
        for (SavingsAccount acc : accounts) {
            if (acc.getName().equals(name) &&
                    getPasswordMap().getOrDefault(acc.getAccountNumber(), "").equals(password)) {
                return acc;
            }
        }
        return null;
    }

    // Password storage (simple map keyed by account number)
    private HashMap<Integer, String> passwordMap = new HashMap<>();

    private HashMap<Integer, String> getPasswordMap() { return passwordMap; }

    public boolean createAccount(String name, String password) {
        // Duplicate name check
        for (SavingsAccount acc : accounts) {
            if (acc.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        SavingsAccount acc = new SavingsAccount(name);
        accounts.add(acc);
        passwordMap.put(acc.getAccountNumber(), password);
        return true;
    }

    public boolean deleteAccount(String name) {
        SavingsAccount target = null;
        for (SavingsAccount acc : accounts) {
            if (acc.getName().equalsIgnoreCase(name)) {
                target = acc;
                break;
            }
        }
        if (target != null) {
            accounts.remove(target);
            passwordMap.remove(target.getAccountNumber());
            return true;
        }
        return false;
    }

    public void toggleLoan(String name, boolean enabled) {
        for (SavingsAccount acc : accounts) {
            if (acc.getName().equalsIgnoreCase(name)) {
                acc.setLoanEnabled(enabled);
            }
        }
    }

    public String getAllAccounts() {
        if (accounts.isEmpty()) return "No accounts registered.";
        StringBuilder sb = new StringBuilder();
        for (SavingsAccount acc : accounts) {
            sb.append("Name: ").append(acc.getName())
              .append(" | Acc No: ").append(acc.getAccountNumber())
              .append(" | Type: ").append(acc.getAccountType())
              .append(" | Balance: RM ").append(String.format("%.2f", acc.getBalance()))
              .append("\n");
        }
        return sb.toString();
    }

    public void recordLoanIssued(double amount) { totalLoanIssued += amount; bankReserve -= amount; }
    public double getBankReserve()              { return bankReserve; }
    public double getTotalLoanIssued()          { return totalLoanIssued; }
    public ArrayList<SavingsAccount> getAccounts() { return accounts; }
}

// ============================================================
//  MAIN GUI CLASS — BankingSystem
// ============================================================
public class BankingSystem extends JFrame {

    private BankAdmin bank = new BankAdmin();
    private SavingsAccount currentUser = null;

    // Color palette
    private final Color DARK_BLUE   = new Color(13, 71, 161);
    private final Color LIGHT_BLUE  = new Color(227, 242, 253);
    private final Color ACCENT      = new Color(25, 118, 210);
    private final Color WHITE       = Color.WHITE;
    private final Color SUCCESS     = new Color(46, 125, 50);
    private final Color DANGER      = new Color(198, 40, 40);

    private CardLayout cardLayout   = new CardLayout();
    private JPanel mainPanel        = new JPanel(cardLayout);

    public BankingSystem() {
        setTitle("AIU Banking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel.add(buildWelcomePanel(),   "WELCOME");
        mainPanel.add(buildLoginPanel(),     "LOGIN");
        mainPanel.add(buildRegisterPanel(),  "REGISTER");
        mainPanel.add(buildDashboard(),      "DASHBOARD");
        mainPanel.add(buildAdminLogin(),     "ADMIN_LOGIN");
        mainPanel.add(buildAdminPanel(),     "ADMIN");

        add(mainPanel);
        cardLayout.show(mainPanel, "WELCOME");
        setVisible(true);
    }

    // ── Helper methods ──────────────────────────────────────

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 40));
        return btn;
    }

    private JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        lbl.setForeground(DARK_BLUE);
        return lbl;
    }

    private void showMsg(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    // ── WELCOME PANEL ───────────────────────────────────────

    private JPanel buildWelcomePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(DARK_BLUE);

        JLabel title = new JLabel("AIU Banking System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(60, 20, 10, 20));

        JLabel sub = new JLabel("Secure. Simple. Reliable.", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.ITALIC, 15));
        sub.setForeground(new Color(187, 222, 251));
        sub.setBorder(BorderFactory.createEmptyBorder(0, 20, 30, 20));

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 10, 12));
        btnPanel.setBackground(DARK_BLUE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 150, 60, 150));

        JButton loginBtn    = styledButton("User Login",     ACCENT);
        JButton registerBtn = styledButton("Register",       new Color(56, 142, 60));
        JButton adminBtn    = styledButton("Admin Login",    new Color(121, 85, 72));

        loginBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        adminBtn.addActionListener(e -> cardLayout.show(mainPanel, "ADMIN_LOGIN"));

        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        btnPanel.add(adminBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(DARK_BLUE);
        top.add(title, BorderLayout.NORTH);
        top.add(sub,   BorderLayout.CENTER);

        p.add(top,      BorderLayout.NORTH);
        p.add(btnPanel, BorderLayout.CENTER);
        return p;
    }

    // ── LOGIN PANEL ─────────────────────────────────────────

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LIGHT_BLUE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JLabel hdr = titleLabel("User Login");
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        form.add(hdr, g);

        g.gridwidth = 1;
        JTextField nameField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);

        g.gridy = 1; g.gridx = 0; form.add(new JLabel("Username:"), g);
        g.gridx = 1; form.add(nameField, g);
        g.gridy = 2; g.gridx = 0; form.add(new JLabel("Password:"), g);
        g.gridx = 1; form.add(passField, g);

        JButton loginBtn = styledButton("Login", ACCENT);
        JButton backBtn  = styledButton("Back",  new Color(117, 117, 117));
        g.gridy = 3; g.gridx = 0; form.add(backBtn,  g);
        g.gridx = 1; form.add(loginBtn, g);

        loginBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            if (name.isEmpty() || pass.isEmpty()) {
                showMsg("Please enter username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentUser = bank.findAccount(name, pass);
            if (currentUser != null) {
                nameField.setText("");
                passField.setText("");
                refreshDashboard();
                cardLayout.show(mainPanel, "DASHBOARD");
            } else {
                showMsg("Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(LIGHT_BLUE);
        wrapper.add(form);

        p.add(wrapper, BorderLayout.CENTER);
        return p;
    }

    // ── REGISTER PANEL ──────────────────────────────────────

    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LIGHT_BLUE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JLabel hdr = titleLabel("Create Account");
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        form.add(hdr, g);
        g.gridwidth = 1;

        JTextField nameField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        g.gridy = 1; g.gridx = 0; form.add(new JLabel("Full Name:"),       g);
        g.gridx = 1; form.add(nameField, g);
        g.gridy = 2; g.gridx = 0; form.add(new JLabel("Password:"),        g);
        g.gridx = 1; form.add(passField, g);
        g.gridy = 3; g.gridx = 0; form.add(new JLabel("Confirm Password:"),g);
        g.gridx = 1; form.add(confirmField, g);

        JButton regBtn  = styledButton("Register", new Color(56, 142, 60));
        JButton backBtn = styledButton("Back",     new Color(117, 117, 117));
        g.gridy = 4; g.gridx = 0; form.add(backBtn,  g);
        g.gridx = 1; form.add(regBtn,  g);

        regBtn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            String pass    = new String(passField.getPassword()).trim();
            String confirm = new String(confirmField.getPassword()).trim();

            if (name.isEmpty() || pass.isEmpty()) {
                showMsg("All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pass.equals(confirm)) {
                showMsg("Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pass.length() < 4) {
                showMsg("Password must be at least 4 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (bank.createAccount(name, pass)) {
                showMsg("Account created successfully!\nWelcome, " + name + "!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                passField.setText("");
                confirmField.setText("");
                cardLayout.show(mainPanel, "LOGIN");
            } else {
                showMsg("Username already exists. Please choose a different name.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(LIGHT_BLUE);
        wrapper.add(form);
        p.add(wrapper, BorderLayout.CENTER);
        return p;
    }

    // ── DASHBOARD ───────────────────────────────────────────

    private JLabel balanceLabel   = new JLabel();
    private JLabel accInfoLabel   = new JLabel();

    private void refreshDashboard() {
        if (currentUser == null) return;
        balanceLabel.setText("RM " + String.format("%.2f", currentUser.getBalance()));
        accInfoLabel.setText(currentUser.getName() + "  |  Acc No: " + currentUser.getAccountNumber()
                + "  |  " + currentUser.getAccountType());
    }

    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LIGHT_BLUE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        accInfoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        accInfoLabel.setForeground(WHITE);
        JLabel balTitle = new JLabel("Available Balance");
        balTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        balTitle.setForeground(new Color(187, 222, 251));
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 26));
        balanceLabel.setForeground(WHITE);

        JPanel balPanel = new JPanel(new GridLayout(2, 1));
        balPanel.setBackground(DARK_BLUE);
        balPanel.add(balTitle);
        balPanel.add(balanceLabel);

        header.add(accInfoLabel, BorderLayout.NORTH);
        header.add(balPanel,     BorderLayout.CENTER);

        // Buttons grid
        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setBackground(LIGHT_BLUE);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        String[] labels = {"💰 Deposit","💸 Withdraw","🔁 Transfer","📋 History","🏦 Take Loan","🚪 Logout"};
        Color[]  colors = {
            new Color(56, 142, 60), new Color(198, 40, 40), new Color(2, 119, 189),
            new Color(94, 53, 177), new Color(230, 81, 0), new Color(97, 97, 97)
        };

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = new JButton("<html><center>" + labels[i] + "</center></html>");
            btn.setBackground(colors[i]);
            btn.setForeground(WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> handleDashboardAction(idx));
            grid.add(btn);
        }

        p.add(header, BorderLayout.NORTH);
        p.add(grid,   BorderLayout.CENTER);
        return p;
    }

    private void handleDashboardAction(int idx) {
        switch (idx) {
            case 0: doDeposit();   break;
            case 1: doWithdraw();  break;
            case 2: doTransfer();  break;
            case 3: showHistory(); break;
            case 4: doLoan();      break;
            case 5:
                currentUser = null;
                cardLayout.show(mainPanel, "WELCOME");
                break;
        }
    }

    private void doDeposit() {
        String input = JOptionPane.showInputDialog(this, "Enter deposit amount (RM):", "Deposit",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            double amount = Double.parseDouble(input.trim());
            if (currentUser.deposit(amount, "ATM")) {
                refreshDashboard();
                showMsg("Deposit successful!\nNew Balance: RM " +
                        String.format("%.2f", currentUser.getBalance()), "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showMsg("Invalid amount. Please enter a positive value.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            showMsg("Invalid input. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doWithdraw() {
        String input = JOptionPane.showInputDialog(this, "Enter withdrawal amount (RM):", "Withdraw",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            double amount = Double.parseDouble(input.trim());
            if (currentUser.withdraw(amount)) {
                refreshDashboard();
                showMsg("Withdrawal successful!\nNew Balance: RM " +
                        String.format("%.2f", currentUser.getBalance()), "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showMsg("Insufficient balance or invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            showMsg("Invalid input. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doTransfer() {
        // Collect recipient name
        String recipient = JOptionPane.showInputDialog(this, "Enter recipient username:", "Transfer",
                JOptionPane.QUESTION_MESSAGE);
        if (recipient == null || recipient.trim().isEmpty()) return;
        if (recipient.trim().equalsIgnoreCase(currentUser.getName())) {
            showMsg("You cannot transfer to yourself.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find recipient
        SavingsAccount target = null;
        for (SavingsAccount acc : bank.getAccounts()) {
            if (acc.getName().equalsIgnoreCase(recipient.trim())) {
                target = acc;
                break;
            }
        }
        if (target == null) {
            showMsg("Recipient account not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amtStr = JOptionPane.showInputDialog(this, "Enter transfer amount (RM):", "Transfer",
                JOptionPane.QUESTION_MESSAGE);
        if (amtStr == null) return;
        try {
            double amount = Double.parseDouble(amtStr.trim());
            if (currentUser.transfer(amount, target)) {
                refreshDashboard();
                showMsg("Transfer successful!\nRM " + String.format("%.2f", amount) +
                        " sent to " + target.getName() + "\nNew Balance: RM " +
                        String.format("%.2f", currentUser.getBalance()), "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showMsg("Insufficient balance or invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            showMsg("Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHistory() {
        String history = currentUser.getTransactionHistory();

        JTextArea area = new JTextArea(history);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBackground(new Color(245, 245, 245));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 300));

        JOptionPane.showMessageDialog(this, scroll, "Transaction History — " + currentUser.getName(),
                JOptionPane.PLAIN_MESSAGE);
    }

    private void doLoan() {
        if (!currentUser.isLoanEnabled()) {
            showMsg("Loan feature is disabled for your account.\nPlease contact the bank.", "Loan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentUser.getLoanCount() >= 2) {
            showMsg("You have reached the maximum of 2 loans.", "Loan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this,
                "Enter loan amount (RM):\n(Loans taken: " + currentUser.getLoanCount() + "/2)",
                "Take Loan", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            double amount = Double.parseDouble(input.trim());
            if (amount <= 0) {
                showMsg("Invalid loan amount.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (amount > bank.getBankReserve()) {
                showMsg("Loan amount exceeds bank reserve. Request a smaller amount.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (currentUser.takeLoan(amount)) {
                bank.recordLoanIssued(amount);
                refreshDashboard();
                showMsg("Loan of RM " + String.format("%.2f", amount) + " approved!\nNew Balance: RM " +
                        String.format("%.2f", currentUser.getBalance()), "Loan Approved",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showMsg("Loan could not be processed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            showMsg("Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── ADMIN LOGIN ─────────────────────────────────────────

    private JPanel buildAdminLogin() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LIGHT_BLUE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel hdr = titleLabel("Admin Login");
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        form.add(hdr, g);
        g.gridwidth = 1;

        JTextField adminUser = new JTextField(20);
        JPasswordField adminPass = new JPasswordField(20);

        g.gridy = 1; g.gridx = 0; form.add(new JLabel("Username:"), g);
        g.gridx = 1; form.add(adminUser, g);
        g.gridy = 2; g.gridx = 0; form.add(new JLabel("Password:"), g);
        g.gridx = 1; form.add(adminPass, g);

        JButton loginBtn = styledButton("Login",  new Color(121, 85, 72));
        JButton backBtn  = styledButton("Back",   new Color(117, 117, 117));
        g.gridy = 3; g.gridx = 0; form.add(backBtn,  g);
        g.gridx = 1; form.add(loginBtn, g);

        loginBtn.addActionListener(e -> {
            if (bank.authenticate(adminUser.getText().trim(),
                    new String(adminPass.getPassword()).trim())) {
                adminUser.setText("");
                adminPass.setText("");
                cardLayout.show(mainPanel, "ADMIN");
            } else {
                showMsg("Incorrect admin credentials.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(LIGHT_BLUE);
        wrapper.add(form);
        p.add(wrapper, BorderLayout.CENTER);
        return p;
    }

    // ── ADMIN PANEL ─────────────────────────────────────────

    private JPanel buildAdminPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LIGHT_BLUE);

        JPanel header = new JPanel();
        header.setBackground(new Color(121, 85, 72));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Admin Control Panel", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(WHITE);
        header.add(title);

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setBackground(LIGHT_BLUE);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        String[] labels = {"👤 Create Account","🗑 Delete Account","👥 All Accounts",
                           "💰 Bank Reserve","📊 Total Loans","🔒 Toggle Loan"};
        Color[] colors = {
            new Color(56, 142, 60),  new Color(198, 40, 40),  new Color(2, 119, 189),
            new Color(94, 53, 177),  new Color(230, 81, 0),   new Color(97, 97, 97)
        };

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = new JButton("<html><center>" + labels[i] + "</center></html>");
            btn.setBackground(colors[i]);
            btn.setForeground(WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> handleAdminAction(idx));
            grid.add(btn);
        }

        JButton logoutBtn = styledButton("Logout", new Color(97, 97, 97));
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));
        JPanel bottom = new JPanel();
        bottom.setBackground(LIGHT_BLUE);
        bottom.add(logoutBtn);

        p.add(header, BorderLayout.NORTH);
        p.add(grid,   BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private void handleAdminAction(int idx) {
        switch (idx) {
            case 0: adminCreateAccount(); break;
            case 1: adminDeleteAccount(); break;
            case 2: adminShowAll();       break;
            case 3: adminBankReserve();   break;
            case 4: adminTotalLoans();    break;
            case 5: adminToggleLoan();    break;
        }
    }

    private void adminCreateAccount() {
        String name = JOptionPane.showInputDialog(this, "Enter full name:", "Create Account",
                JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        String pass = JOptionPane.showInputDialog(this, "Enter password:", "Create Account",
                JOptionPane.QUESTION_MESSAGE);
        if (pass == null || pass.trim().isEmpty()) return;

        if (bank.createAccount(name.trim(), pass.trim())) {
            showMsg("Account for '" + name.trim() + "' created successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            showMsg("Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adminDeleteAccount() {
        String name = JOptionPane.showInputDialog(this, "Enter username to delete:", "Delete Account",
                JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete account: " + name.trim() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (bank.deleteAccount(name.trim())) {
                showMsg("Account deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showMsg("Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void adminShowAll() {
        JTextArea area = new JTextArea(bank.getAllAccounts());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(500, 250));
        JOptionPane.showMessageDialog(this, scroll, "All Accounts", JOptionPane.PLAIN_MESSAGE);
    }

    private void adminBankReserve() {
        showMsg("Bank Reserve: RM " + String.format("%.2f", bank.getBankReserve()),
                "Bank Reserve", JOptionPane.INFORMATION_MESSAGE);
    }

    private void adminTotalLoans() {
        showMsg("Total Loans Issued: RM " + String.format("%.2f", bank.getTotalLoanIssued()),
                "Total Loans", JOptionPane.INFORMATION_MESSAGE);
    }

    private void adminToggleLoan() {
        String name = JOptionPane.showInputDialog(this, "Enter username:", "Toggle Loan Feature",
                JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        String[] options = {"Enable Loan", "Disable Loan"};
        int choice = JOptionPane.showOptionDialog(this,
                "Set loan feature for: " + name.trim(), "Toggle Loan",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            bank.toggleLoan(name.trim(), true);
            showMsg("Loan feature ENABLED for " + name.trim(), "Done", JOptionPane.INFORMATION_MESSAGE);
        } else if (choice == 1) {
            bank.toggleLoan(name.trim(), false);
            showMsg("Loan feature DISABLED for " + name.trim(), "Done", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── MAIN ────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankingSystem::new);
    }
}
