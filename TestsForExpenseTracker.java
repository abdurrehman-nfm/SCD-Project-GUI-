package project;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import javax.swing.DefaultListModel;
import java.io.File;
import java.util.Map;

class TestsForExpenseTracker {

    @Test
    void testAddValidExpense() throws Exception {
        ExpenseService service = new ExpenseService();
        service.addExpense("Food", "100");
        service.addExpense("Transport", "50");

        assertEquals(150.0, service.calculateTotal(),
            "Total should equal sum of expenses");
        assertEquals(2, service.getExpenses().size(),
            "Should contain 2 expenses");
    }

    @Test
    void testAddExpenseWithEmptyCategory() {
        ExpenseService service = new ExpenseService();
        Exception ex = assertThrows(EmptyFieldException.class, () -> {
            service.addExpense("", "100");
        });
        assertEquals("Category cannot be empty!", ex.getMessage());
    }

    @Test
    void testAddExpenseWithInvalidAmount() {
        ExpenseService service = new ExpenseService();
        Exception ex = assertThrows(InvalidAmountException.class, () -> {
            service.addExpense("Food", "abc");
        });
        assertEquals("Amount must be a number!", ex.getMessage());
    }

    @Test
    void testAddExpenseWithNegativeAmount() {
        ExpenseService service = new ExpenseService();
        Exception ex = assertThrows(InvalidAmountException.class, () -> {
            service.addExpense("Food", "-50");
        });
        assertEquals("Amount must be positive!", ex.getMessage());
    }

    @Test
    void testCalculateTotalWithNoExpenses() {
        ExpenseService service = new ExpenseService();
        assertEquals(0.0, service.calculateTotal(),
            "Total should be 0 when no expenses added");
    }

    @Test
    void testDeleteExpense() throws Exception {
        ExpenseService service = new ExpenseService();
        service.addExpense("Food", "100");
        service.addExpense("Transport", "50");
        assertEquals(2, service.getExpenses().size());

        service.deleteExpense(0);
        assertEquals(1, service.getExpenses().size(),
            "After deletion, should contain 1 expense");
        assertEquals("Transport", service.getExpenses().get(0).category);
    }

    @Test
    void testCategoryTotals() throws Exception {
        ExpenseService service = new ExpenseService();
        service.addExpense("Food", "100");
        service.addExpense("Food", "50");
        service.addExpense("Transport", "30");

        Map<String, Double> totals = service.categoryTotals();
        assertEquals(150.0, totals.get("Food"));
        assertEquals(30.0, totals.get("Transport"));
    }

    @Test
    void testSaveAndLoadExpenses() throws Exception {
        ExpenseService service = new ExpenseService();
        service.addExpense("Food", "100");
        service.addExpense("Transport", "50");
        service.saveExpenses();

        // Load into a fresh service
        ExpenseService newService = new ExpenseService();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        newService.loadExpenses(listModel);

        assertEquals(2, newService.getExpenses().size(),
            "Should reload 2 expenses from file");
        assertTrue(new File("expenses.txt").exists(),
            "expenses.txt file should exist after saving");
    }
}
