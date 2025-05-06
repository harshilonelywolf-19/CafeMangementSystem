package cafe;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CafeManagementSystem extends JFrame {
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private JTable tableItems = new JTable();
    private JTable tableOrders = new JTable();
    private DefaultTableModel itemModel;
    private DefaultTableModel orderModel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/harshidha";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "harshidha@2005";

    private CafeManagementSystem cafeManagementSystem;

    public CafeManagementSystem() {
        showLoginScreen();
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Admin Login");
        loginFrame.setSize(400, 200);
        loginFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(3, 2));
        loginFrame.getContentPane().setBackground(new Color(230, 240, 250));

        loginFrame.add(new JLabel("Username:"));
        loginFrame.add(txtUsername);
        loginFrame.add(new JLabel("Password:"));
        loginFrame.add(txtPassword);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(100, 149, 237));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.addActionListener((ActionEvent e) -> authenticateAdmin(loginFrame));
        loginFrame.add(new JLabel());  // Spacer
        loginFrame.add(btnLogin);

        loginFrame.setVisible(true);
    }

    private void authenticateAdmin(JFrame loginFrame) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        try (Connection conn = connect()) {
            if (conn != null) {
                String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    loginFrame.dispose();
                    showDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            return null;
        }
    }

    private void showDashboard() {
        setTitle("Cafe Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel navigationPanel = new JPanel(new GridLayout(3, 1));
        navigationPanel.setBackground(new Color(245, 222, 179));

        JButton btnItems = new JButton("Manage Items");
        btnItems.setBackground(new Color(60, 179, 113));
        btnItems.setForeground(Color.WHITE);
        btnItems.addActionListener((ActionEvent e) -> manageItems());

        JButton btnOrders = new JButton("Manage Orders");
        btnOrders.setBackground(new Color(255, 140, 0));
        btnOrders.setForeground(Color.WHITE);
        btnOrders.addActionListener((ActionEvent e) -> manageOrders());

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(178, 34, 34));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener((ActionEvent e) -> logout());

        navigationPanel.add(btnItems);
        navigationPanel.add(btnOrders);
        navigationPanel.add(btnLogout);
        add(navigationPanel, BorderLayout.WEST);

        getContentPane().setBackground(new Color(250, 250, 210));
        setVisible(true);
    }

    private void manageItems() {
        JFrame itemFrame = new JFrame("Manage Menu Items");
        itemFrame.setSize(800, 600);
        itemFrame.setLayout(new BorderLayout());
        itemFrame.getContentPane().setBackground(new Color(224, 255, 255));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.setBackground(new Color(224, 255, 255));
        JTextField txtName = new JTextField();
        JTextField txtCategory = new JTextField();
        JTextField txtPrice = new JTextField();

        inputPanel.add(new JLabel("Item Name:"));
        inputPanel.add(txtName);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(txtCategory);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(txtPrice);

        JButton btnAdd = new JButton("Add Item");
        btnAdd.setBackground(new Color(0, 191, 255));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener((ActionEvent e) -> addItem(txtName, txtCategory, txtPrice));

        JButton btnEdit = new JButton("Edit Item");
        btnEdit.setBackground(new Color(123, 104, 238));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.addActionListener((ActionEvent e) -> editItem(txtName, txtCategory, txtPrice));

        JButton btnRemove = new JButton("Remove Item");
        btnRemove.setBackground(new Color(220, 20, 60));
        btnRemove.setForeground(Color.WHITE);
        btnRemove.addActionListener((ActionEvent e) -> removeItem());

        String[] columns = {"Name", "Category", "Price"};
        itemModel = new DefaultTableModel(new Object[][]{}, columns);
        tableItems = new JTable(itemModel);
        loadItems();

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.setBackground(new Color(224, 255, 255));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnRemove);

        itemFrame.add(inputPanel, BorderLayout.NORTH);
        itemFrame.add(new JScrollPane(tableItems), BorderLayout.CENTER);
        itemFrame.add(buttonPanel, BorderLayout.SOUTH);
        itemFrame.setVisible(true);
    }

    private void addItem(JTextField txtName, JTextField txtCategory, JTextField txtPrice) {
        String name = txtName.getText();
        String category = txtCategory.getText();
        double price;
        try {
            price = Double.parseDouble(txtPrice.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid price.");
            return;
        }

        try (Connection conn = connect()) {
            String sql = "INSERT INTO items (name, category, price) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
            itemModel.addRow(new Object[]{name, category, price});
            JOptionPane.showMessageDialog(this, "Item added.");
            txtName.setText("");
            txtCategory.setText("");
            txtPrice.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void editItem(JTextField txtName, JTextField txtCategory, JTextField txtPrice) {
        int selected = tableItems.getSelectedRow();
        if (selected != -1) {
            String oldName = itemModel.getValueAt(selected, 0).toString();
            String name = txtName.getText();
            String category = txtCategory.getText();
            double price;
            try {
                price = Double.parseDouble(txtPrice.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Enter valid price.");
                return;
            }
            try (Connection conn = connect()) {
                String sql = "UPDATE items SET name = ?, category = ?, price = ? WHERE name = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2, category);
                pstmt.setDouble(3, price);
                pstmt.setString(4, oldName);
                pstmt.executeUpdate();
                itemModel.setValueAt(name, selected, 0);
                itemModel.setValueAt(category, selected, 1);
                itemModel.setValueAt(price, selected, 2);
                JOptionPane.showMessageDialog(this, "Item updated.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select an item first.");
        }
    }

    private void removeItem() {
        int selected = tableItems.getSelectedRow();
        if (selected != -1) {
            String name = itemModel.getValueAt(selected, 0).toString();
            try (Connection conn = connect()) {
                String sql = "DELETE FROM items WHERE name = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.executeUpdate();
                itemModel.removeRow(selected);
                JOptionPane.showMessageDialog(this, "Item removed.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select an item to remove.");
        }
    }

    private void loadItems() {
        try (Connection conn = connect()) {
            String sql = "SELECT name, category, price FROM items";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            itemModel.setRowCount(0);
            while (rs.next()) {
                itemModel.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + ex.getMessage());
        }
    }

    private void manageOrders() {
        JFrame orderFrame = new JFrame("Manage Orders");
        orderFrame.setSize(800, 600);
        orderFrame.setLayout(new BorderLayout());
        orderFrame.getContentPane().setBackground(new Color(255, 239, 213));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.setBackground(new Color(255, 239, 213));
        JTextField txtItemName = new JTextField();
        JTextField txtQuantity = new JTextField();

        inputPanel.add(new JLabel("Item Name:"));
        inputPanel.add(txtItemName);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(txtQuantity);

        JButton btnAddOrder = new JButton("Place Order");
        btnAddOrder.setBackground(new Color(72, 61, 139));
        btnAddOrder.setForeground(Color.WHITE);
        btnAddOrder.addActionListener((ActionEvent e) -> placeOrder(txtItemName, txtQuantity));

        String[] columns = {"Item", "Quantity"};
        orderModel = new DefaultTableModel(new Object[][]{}, columns);
        tableOrders = new JTable(orderModel);
        loadOrders();

        orderFrame.add(inputPanel, BorderLayout.NORTH);
        orderFrame.add(new JScrollPane(tableOrders), BorderLayout.CENTER);
        orderFrame.add(btnAddOrder, BorderLayout.SOUTH);
        orderFrame.setVisible(true);
    }

    private void placeOrder(JTextField txtItemName, JTextField txtQuantity) {
        String item = txtItemName.getText();
        int qty;
        try {
            qty = Integer.parseInt(txtQuantity.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity.");
            return;
        }

        try (Connection conn = connect()) {
            String sql = "INSERT INTO orders (item, quantity) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, item);
            pstmt.setInt(2, qty);
            pstmt.executeUpdate();
            orderModel.addRow(new Object[]{item, qty});
            JOptionPane.showMessageDialog(this, "Order placed.");
            txtItemName.setText("");
            txtQuantity.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadOrders() {
        try (Connection conn = connect()) {
            String sql = "SELECT item, quantity FROM orders";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            orderModel.setRowCount(0);
            while (rs.next()) {
                orderModel.addRow(new Object[]{
                        rs.getString("item"),
                        rs.getInt("quantity")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + ex.getMessage());
        }
    }

    private void logout() {
        dispose();
        cafeManagementSystem = new CafeManagementSystem();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CafeManagementSystem::new);
    }
}
