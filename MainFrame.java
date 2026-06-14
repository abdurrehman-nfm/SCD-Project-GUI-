package project;

import java.awt.CardLayout;

import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.*;
import java.io.*;

// Expense model
class Expense {
    String category;
    double amount;

    Expense(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return category + " - $" + amount;
    }
}

// Custom exceptions
class InvalidAmountException extends Exception {
    public InvalidAmountException(String msg) {
        super(msg);
    }
}

class EmptyFieldException extends Exception {
    public EmptyFieldException(String msg) {
        super(msg);
    }
}

// Service class
class ExpenseService {
    private ArrayList<Expense> expenses = new ArrayList<>();

    public void addExpense(String category, String amountStr) throws InvalidAmountException, EmptyFieldException {
        if (category == null || category.trim().isEmpty()) {
            throw new EmptyFieldException("Category cannot be empty!");
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Amount must be a number!");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive!");
        }
        expenses.add(new Expense(category, amount));
    }

    public double calculateTotal() {
        return expenses.stream().mapToDouble(e -> e.amount).sum();
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
        }
    }

    public Map<String, Double> categoryTotals() {
        Map<String, Double> totals = new HashMap<>();
        for (Expense exp : expenses) {
            totals.put(exp.category, totals.getOrDefault(exp.category, 0.0) + exp.amount);
        }
        return totals;
    }

    public void saveExpenses() {
        try (PrintWriter pw = new PrintWriter("expenses.txt")) {
            for (Expense exp : expenses) {
                pw.println(exp.category + "," + exp.amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadExpenses(DefaultListModel<Expense> listModel) {
        try (Scanner sc = new Scanner(new File("expenses.txt"))) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                addExpense(parts[0], parts[1]);
                listModel.addElement(new Expense(parts[0], Double.parseDouble(parts[1])));
            }
        } catch (Exception e) {
            System.out.println("No saved expenses found.");
        }
    }
}

// GUI class
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField categoryField, amountField;
    private ExpenseService service = new ExpenseService();
    private DefaultListModel<Expense> listModel = new DefaultListModel<>();
    private JList<Expense> expenseList = new JList<>(listModel);

    public static void main(String[] args) {
        runTests();

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MainFrame() {
        setTitle("Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 500);

        // Use CardLayout to switch between panels
        CardLayout cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        // --- Welcome Panel ---
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to Expense Tracker", SwingConstants.CENTER);
        welcomeLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        JButton startButton = new JButton("Start");

        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        welcomePanel.add(startButton, BorderLayout.SOUTH);

        // --- Expense Tracker Panel ---
        JPanel trackerPanel = new JPanel(new BorderLayout());

        categoryField = new JTextField(10);
        amountField = new JTextField(10);
        JButton addButton = new JButton("Add Expense");
        JButton deleteButton = new JButton("Delete Selected");
        JButton totalButton = new JButton("Show Total");
        JButton filterButton = new JButton("Filter by Category");
        JButton categoryTotalButton = new JButton("Category Totals");
        JButton saveButton = new JButton("Save Expenses");
        JButton loadButton = new JButton("Load Expenses");

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);
        inputPanel.add(totalButton);
        inputPanel.add(filterButton);
        inputPanel.add(categoryTotalButton);
        inputPanel.add(saveButton);
        inputPanel.add(loadButton);

        trackerPanel.add(inputPanel, BorderLayout.NORTH);
        trackerPanel.add(new JScrollPane(expenseList), BorderLayout.CENTER);

        // Add both panels to CardLayout
        contentPane.add(welcomePanel, "Welcome");
        contentPane.add(trackerPanel, "Tracker");

        // Show welcome panel first
        cardLayout.show(contentPane, "Welcome");

        // Start button action: switch to tracker
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPane, "Tracker");
            }
        });

        // --- Existing button actions (unchanged) ---
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    service.addExpense(categoryField.getText(), amountField.getText());
                    // Add the actual Expense object instead of a string
                    listModel.addElement(new Expense(categoryField.getText(), Double.parseDouble(amountField.getText())));
                    categoryField.setText("");
                    amountField.setText("");
                } catch (InvalidAmountException | EmptyFieldException ex) {
                    JOptionPane.showMessageDialog(MainFrame.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = expenseList.getSelectedIndex();
                if (index != -1) {
                    service.deleteExpense(index);   // remove from service
                    listModel.remove(index);        // remove from list model
                } else {
                    JOptionPane.showMessageDialog(MainFrame.this, "Select an expense to delete!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });



        totalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double total = service.calculateTotal();
                JOptionPane.showMessageDialog(MainFrame.this, "Total Expenses: $" + total);
            }
        });

        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String category = JOptionPane.showInputDialog("Enter category to filter:");
                if (category != null && !category.trim().isEmpty()) {
                    listModel.clear();
                    for (Expense exp : service.getExpenses()) {
                        if (exp.category.equalsIgnoreCase(category)) {
                            listModel.addElement(exp);  // add Expense object directly
                        }
                    }
                }
            }
        });


        categoryTotalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Map<String, Double> totals = service.categoryTotals();
                StringBuilder sb = new StringBuilder("Category Totals:\n");
                for (String cat : totals.keySet()) {
                    sb.append(cat).append(": $").append(totals.get(cat)).append("\n");
                }
                JOptionPane.showMessageDialog(MainFrame.this, sb.toString());
            }
        });

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                service.saveExpenses();
                JOptionPane.showMessageDialog(MainFrame.this, "Expenses saved successfully!");
            }
        });

        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listModel.clear();
                service.loadExpenses(listModel);
            }
        });

        // Load saved expenses on startup
        service.loadExpenses(listModel);
    }

    public static void runTests() {
        ExpenseService testService = new ExpenseService();
        try {
            testService.addExpense("Food", "100");
            testService.addExpense("Transport", "50");
            assert testService.calculateTotal() == 150 : "Test Failed!";
            System.out.println("Unit Test Passed!");
        } catch (Exception e) {
            System.out.println("Unit Test Failed: " + e.getMessage());
        }
    }
}
