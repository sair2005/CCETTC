import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Calendar;
import java.util.regex.PatternSyntaxException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.beans.PropertyChangeListener;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

public class ViewTransferCertificates extends JFrame {
    // Core components
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Buttons
    private JButton backButton, deleteButton, generatePDFButton, viewButton;
    private JButton refreshButton, editButton, bulkDeleteButton, exportAllButton;
    private JButton printButton, emailButton, archiveButton;
    
    // Search and filter components
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> statusFilter;
    private JDateChooser fromDateChooser, toDateChooser;
    private JButton clearFiltersButton;
    
    // Status and info components
    private JLabel statusLabel;
    private JLabel recordCountLabel;
    private JProgressBar progressBar;
    
    // Menu components
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, viewMenu, toolsMenu, helpMenu;
    
    // Popup menu
    private JPopupMenu rightClickMenu;
    
    // Date formatter for parsing string dates
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    // Constants
    private static final String[] FILTER_OPTIONS = {
        "All Records", "By Student Name", "By Admission No", "By Course", 
        "By Academic Year", "By Status", "By Date Range"
    };
    
    private static final String[] STATUS_OPTIONS = {
        "All Status", "Completed", "Pending", "Issued"
    };

    public ViewTransferCertificates() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
        updateRecordCount();
    }

    private void initializeComponents() {
        setTitle("CCET Transfer Certificate Management System - View & Manage Records");
        setSize(1400, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize table and model
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(true);
        
        // Setup table sorter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        // Add columns - Updated to match transfer certificate format
        String[] columns = {
    "ID", "Student Name", "Register No", "Serial No", "Father Name",
    "Date of Birth", "DOB (Words)", "Nationality", "Religion", "Caste", "Gender",
    "Admission Date", "Course", "Games/Activities", "Fee Concession", "Last Exam Result",
    "Leaving Date", "Class at Leaving", "Qualified for Promotion", "Reason for Leaving",
    "Issue Date", "Conduct Character", "Other Remarks", "UMIS No"
};
        
        for (String column : columns) {
            model.addColumn(column);
        }
        
        // Initialize search and filter components
        searchField = new JTextField(20);
        searchField.setToolTipText("Search across all fields...");
        
        filterComboBox = new JComboBox<>(FILTER_OPTIONS);
        statusFilter = new JComboBox<>(STATUS_OPTIONS);
        
        fromDateChooser = new JDateChooser();
        toDateChooser = new JDateChooser();
        
        // Initialize buttons
        initializeButtons();
        
        // Initialize status components
        statusLabel = new JLabel("Ready");
        recordCountLabel = new JLabel("Records: 0");
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        
        // Initialize menu bar
        initializeMenuBar();
        
        // Initialize popup menu
        initializePopupMenu();
        
        // Set up table appearance
        setupTableAppearance();
    }

private void initializeButtons() {
    // Main action buttons with solid vibrant colors
    generatePDFButton = createStyledButton("Generate PDF", "Create PDF for selected record", new Color(76, 175, 80)); // Bright Green
    deleteButton = createStyledButton("Delete Selected", "Delete selected records", new Color(244, 67, 54)); // Bright Red
    viewButton = createStyledButton("View Details", "View complete certificate details", new Color(33, 150, 243)); // Bright Blue
    backButton = createStyledButton("Back to Main", "Return to main menu", new Color(96, 125, 139)); // Blue Gray

    // Additional action buttons with distinct vibrant colors
    refreshButton = createStyledButton("Refresh", "Reload data from database", new Color(255, 152, 0)); // Orange
    editButton = createStyledButton("Edit", "Edit selected record", new Color(156, 39, 176)); // Purple
    bulkDeleteButton = createStyledButton("Bulk Delete", "Delete multiple selected records", new Color(211, 47, 47)); // Dark Red
    exportAllButton = createStyledButton("Export All", "Export all records to PDF", new Color(0, 150, 136)); // Teal
    printButton = createStyledButton("Print", "Print selected certificate", new Color(96, 125, 139)); // Blue Gray
    emailButton = createStyledButton("Email", "Email certificate to student", new Color(33, 150, 243)); // Light Blue
    archiveButton = createStyledButton("Archive", "Archive old records", new Color(255, 193, 7)); // Amber

    // Filter and search buttons
    clearFiltersButton = createStyledButton("Clear Filters", "Clear all filters and search", new Color(158, 158, 158)); // Gray
}

private JButton createStyledButton(String text, String tooltip, Color backgroundColor) {
    JButton button = new JButton(text);
    button.setToolTipText(tooltip);
    button.setFocusPainted(false);
    button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
    button.setPreferredSize(new Dimension(150, 38));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Remove all borders for solid appearance
    button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    button.setContentAreaFilled(true);
    button.setBorderPainted(false);

    // Background color
    button.setBackground(backgroundColor);
    button.setOpaque(true);

    // White text for all buttons for consistency like in the image
    button.setForeground(Color.WHITE);

    // Hover effect with slightly darker color
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            Color hoverColor = darkenColor(backgroundColor, 0.85f);
            button.setBackground(hoverColor);
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            button.setBackground(backgroundColor);
        }
    });

    return button;
}

// Enhanced brightness calculation for better text contrast
private int getBrightness(Color color) {
    // Using the standard luminance formula for better accuracy
    return (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
}

// Improved color darkening for hover effect
private Color darkenColor(Color color, float factor) {
    int r = Math.max((int)(color.getRed() * factor), 0);
    int g = Math.max((int)(color.getGreen() * factor), 0);
    int b = Math.max((int)(color.getBlue() * factor), 0);
    return new Color(r, g, b);
}

    private void initializeMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("New Certificate", "Ctrl+N", e -> openNewCertificateDialog()));
        fileMenu.add(createMenuItem("Import Data", "Ctrl+I", e -> importData()));
        fileMenu.add(createMenuItem("Export Data", "Ctrl+E", e -> exportData()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Print Preview", "Ctrl+P", e -> printPreview()));
        fileMenu.add(createMenuItem("Exit", "Ctrl+Q", e -> dispose()));
        
        // Edit Menu
        editMenu = new JMenu("Edit");
        editMenu.add(createMenuItem("Edit Selected", "Ctrl+E", e -> editSelectedRecord()));
        editMenu.add(createMenuItem("Duplicate", "Ctrl+D", e -> duplicateRecord()));
        editMenu.add(createMenuItem("Find", "Ctrl+F", e -> focusSearchField()));
        
        // View Menu
        viewMenu = new JMenu("View");
        viewMenu.add(createMenuItem("Refresh", "F5", e -> loadData()));
        viewMenu.add(createMenuItem("Full Screen", "F11", e -> toggleFullScreen()));
        viewMenu.add(createMenuItem("Column Settings", null, e -> showColumnSettings()));
        
        // Tools Menu
        toolsMenu = new JMenu("Tools");
        toolsMenu.add(createMenuItem("Database Backup", null, e -> backupDatabase()));
        toolsMenu.add(createMenuItem("Generate Reports", null, e -> generateReports()));
        toolsMenu.add(createMenuItem("Data Validation", null, e -> validateData()));
        toolsMenu.add(createMenuItem("Settings", null, e -> openSettings()));
        
        // Help Menu
        helpMenu = new JMenu("Help");
        helpMenu.add(createMenuItem("User Guide", "F1", e -> showUserGuide()));
        helpMenu.add(createMenuItem("Keyboard Shortcuts", null, e -> showKeyboardShortcuts()));
        helpMenu.add(createMenuItem("About", null, e -> showAbout()));
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String text, String accelerator, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        if (accelerator != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        item.addActionListener(action);
        return item;
    }

    private void initializePopupMenu() {
        rightClickMenu = new JPopupMenu();
        rightClickMenu.add("View Details").addActionListener(e -> viewSelectedCertificate());
        rightClickMenu.add("Edit").addActionListener(e -> editSelectedRecord());
        rightClickMenu.add("Generate PDF").addActionListener(e -> generateSelectedPDF());
        rightClickMenu.add("Delete").addActionListener(e -> deleteSelected());
        rightClickMenu.addSeparator();
        rightClickMenu.add("Copy Row Data").addActionListener(e -> copyRowData());
        rightClickMenu.add("Email Certificate").addActionListener(e -> emailCertificate());
    }

    private void setupTableAppearance() {
        table.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.black);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setShowGrid(true);
        
        // Set alternating row colors
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        
        // Add mouse listener for popup menu
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
            
            private void showPopupMenu(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    rightClickMenu.show(table, e.getX(), e.getY());
                }
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with search and filters
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transfer Certificates"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with buttons and status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Filter by:"));
        searchPanel.add(filterComboBox);
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusFilter);
        searchPanel.add(clearFiltersButton);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        
        // Date range panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        datePanel.setBorder(BorderFactory.createTitledBorder("Date Range Filter"));
        
        datePanel.add(new JLabel("From:"));
        datePanel.add(fromDateChooser);
        datePanel.add(new JLabel("To:"));
        datePanel.add(toDateChooser);
        
        topPanel.add(datePanel, BorderLayout.EAST);
        
        return topPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // First row of buttons
        JPanel buttonRow1 = new JPanel(new FlowLayout());
        buttonRow1.add(generatePDFButton);
        buttonRow1.add(viewButton);
        buttonRow1.add(editButton);
        buttonRow1.add(deleteButton);
        buttonRow1.add(bulkDeleteButton);
        
        // Second row of buttons
        JPanel buttonRow2 = new JPanel(new FlowLayout());
        buttonRow2.add(refreshButton);
        buttonRow2.add(exportAllButton);
        buttonRow2.add(printButton);
        buttonRow2.add(emailButton);
        buttonRow2.add(archiveButton);
        buttonRow2.add(backButton);
        
        buttonPanel.add(buttonRow1);
        buttonPanel.add(buttonRow2);
        
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftStatus.add(statusLabel);
        leftStatus.add(Box.createHorizontalStrut(20));
        leftStatus.add(recordCountLabel);
        
        statusPanel.add(leftStatus, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        return bottomPanel;
    }

    private void setupEventHandlers() {
        // Search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { performSearch(); }
            public void removeUpdate(DocumentEvent e) { performSearch(); }
            public void insertUpdate(DocumentEvent e) { performSearch(); }
        });
        
        // Filter functionality
        filterComboBox.addActionListener(e -> applyFilters());
        statusFilter.addActionListener(e -> applyFilters());
        
        // Date chooser listeners
        fromDateChooser.addPropertyChangeListener("date", e -> applyDateFilter());
        toDateChooser.addPropertyChangeListener("date", e -> applyDateFilter());
        
        // Button event handlers
        backButton.addActionListener(e -> {
            // Assuming MainMenu class exists
            try {
                new MainMenu().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "MainMenu class not found. Returning to previous window.");
            }
            dispose();
        });
        
        deleteButton.addActionListener(e -> deleteSelected());
        generatePDFButton.addActionListener(e -> generateSelectedPDF());
        viewButton.addActionListener(e -> viewSelectedCertificate());
        refreshButton.addActionListener(e -> {
            loadData();
            updateStatus("Data refreshed successfully");
        });
        
        editButton.addActionListener(e -> editSelectedRecord());
        bulkDeleteButton.addActionListener(e -> bulkDelete());
        exportAllButton.addActionListener(e -> exportAllRecords());
        printButton.addActionListener(e -> printSelectedCertificate());
        emailButton.addActionListener(e -> emailCertificate());
        archiveButton.addActionListener(e -> archiveOldRecords());
        clearFiltersButton.addActionListener(e -> clearAllFilters());
        
        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // Delete key for deletion
        table.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        table.getActionMap().put("delete", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
        
        // F5 for refresh
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
        
        // Ctrl+F for search focus
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
        getRootPane().getActionMap().put("focusSearch", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocus();
            }
        });
    }

    // Fixed loadData method to handle string dates properly
    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    updateStatus("Loading data...");
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                });
                
                try {
                    Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3310/college", "root", "sair2005@RDS");
                    Statement stmt = conn.createStatement();
                    
                    // Updated SQL query - treating all dates as strings initially
                    String query = "SELECT id, student_name, register_no, serial_no, father_name, " +
    "dob, dob_words, nationality, religion, caste, gender, " +
    "admission_date, course, games, fee_concession, result, " +
    "leaving_date, class_leaving, qualified, reason, " +
    "issue_date, conduct, remarks, umis_no " +
    "FROM transfer_certificates ORDER BY id DESC";

ResultSet rs = stmt.executeQuery(query);

SwingUtilities.invokeLater(() -> model.setRowCount(0));

while (rs.next()) {
    Object[] row = new Object[24];
    row[0] = rs.getInt("id");
    row[1] = rs.getString("student_name");
    row[2] = rs.getString("register_no");
    row[3] = rs.getString("serial_no");
    row[4] = rs.getString("father_name");
    row[5]  = formatDateString(rs.getString("dob"));
    row[7] = rs.getString("nationality");
    row[8] = rs.getString("religion");
    row[9] = rs.getString("caste");
    row[10] = rs.getString("gender");
row[11] = formatDateString(rs.getString("admission_date"));
    row[13] = rs.getString("games");
    row[14] = rs.getString("fee_concession");
    row[15] = rs.getString("result");
row[16] = formatDateString(rs.getString("leaving_date"));
    row[18] = rs.getString("qualified");
    row[19] = rs.getString("reason");
row[20] = formatDateString(rs.getString("issue_date"));
    row[22] = rs.getString("remarks");
    row[23] = rs.getString("umis_no");
    SwingUtilities.invokeLater(() -> model.addRow(row));
}

                    rs.close();
                    stmt.close();
                    conn.close();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(ViewTransferCertificates.this, 
                            "Error loading data: " + e.getMessage(), "Database Error", 
                            JOptionPane.ERROR_MESSAGE));
                }
                
                return null;
            }
            
            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    updateStatus("Data loaded successfully");
                    updateRecordCount();
                    updateButtonStates();
                });
            }
        };
        
        worker.execute();
    }

    // Helper method to format date strings
    private String formatDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            // Try different date formats
            Date date = null;
            String[] formats = {"dd-MM-yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy"};
            
            for (String format : formats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    date = sdf.parse(dateStr.trim());
                    break;
                } catch (ParseException e) {
                    // Try next format
                }
            }
            
            if (date != null) {
                return displayDateFormat.format(date);
            }
        } catch (Exception e) {
            // If all parsing fails, return original string
        }
        
        return dateStr;
    }

    private void performSearch() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            } catch (PatternSyntaxException e) {
                // Invalid regex, ignore
            }
        }
        updateRecordCount();
    }

    private void applyFilters() {
        String filterType = (String) filterComboBox.getSelectedItem();
        performSearch();
        updateRecordCount();
    }

    private void applyDateFilter() {
        Date fromDate = fromDateChooser.getDate();
        Date toDate = toDateChooser.getDate();
        
        if (fromDate != null && toDate != null) {
            updateRecordCount();
        }
    }

    private void clearAllFilters() {
        searchField.setText("");
        filterComboBox.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        fromDateChooser.setDate(null);
        toDateChooser.setDate(null);
        sorter.setRowFilter(null);
        updateRecordCount();
        updateStatus("All filters cleared");
    }

    private void updateRecordCount() {
        int totalRecords = model.getRowCount();
        int visibleRecords = table.getRowCount();
        recordCountLabel.setText(String.format("Records: %d / %d", visibleRecords, totalRecords));
    }

    private void updateButtonStates() {
        boolean hasSelection = table.getSelectedRowCount() > 0;
        boolean hasSingleSelection = table.getSelectedRowCount() == 1;
        boolean hasMultipleSelection = table.getSelectedRowCount() > 1;
        
        generatePDFButton.setEnabled(hasSingleSelection);
        viewButton.setEnabled(hasSingleSelection);
        editButton.setEnabled(hasSingleSelection);
        deleteButton.setEnabled(hasSelection);
        bulkDeleteButton.setEnabled(hasMultipleSelection);
        printButton.setEnabled(hasSingleSelection);
        emailButton.setEnabled(hasSingleSelection);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        Timer timer = new Timer(3000, e -> statusLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }

    private void deleteSelected() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select record(s) to delete.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            String.format("Are you sure you want to delete %d record(s)?", selectedRows.length),
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3310/college", "root", "sair2005@RDS");
            
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM transfer_certificates WHERE id = ?");
            
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int modelRow = table.convertRowIndexToModel(selectedRows[i]);
                int id = (int) model.getValueAt(modelRow, 0);
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            stmt.close();
            conn.close();
            loadData();
            updateStatus(String.format("%d record(s) deleted successfully", selectedRows.length));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting record(s): " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Enhanced PDF generation with all transfer certificate fields
    private void generateSelectedPDF() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to generate PDF.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        
        try {
            // Get data from selected row
            String studentName = getString(model.getValueAt(modelRow, 1));
String registerNo = getString(model.getValueAt(modelRow, 2));
String serialNo = getString(model.getValueAt(modelRow, 3));
String fatherName = getString(model.getValueAt(modelRow, 4));
String dateOfBirth = getString(model.getValueAt(modelRow, 5));
String dobWords = getString(model.getValueAt(modelRow, 6));
String nationality = getString(model.getValueAt(modelRow, 7));
String religion = getString(model.getValueAt(modelRow, 8));
String caste = getString(model.getValueAt(modelRow, 9));
String gender = getString(model.getValueAt(modelRow, 10));
String admissionDate = getString(model.getValueAt(modelRow, 11));
String course = getString(model.getValueAt(modelRow, 12));
String gamesActivities = getString(model.getValueAt(modelRow, 13));
String feeConcession = getString(model.getValueAt(modelRow, 14));
String lastExamResult = getString(model.getValueAt(modelRow, 15));
String leavingDate = getString(model.getValueAt(modelRow, 16));
String classAtLeaving = getString(model.getValueAt(modelRow, 17));
String qualifiedPromotion = getString(model.getValueAt(modelRow, 18));
String reasonLeaving = getString(model.getValueAt(modelRow, 19));
String issueDate = getString(model.getValueAt(modelRow, 20));
String conductCharacter = getString(model.getValueAt(modelRow, 21));
String otherRemarks = getString(model.getValueAt(modelRow, 22));
String umisNo = getString(model.getValueAt(modelRow, 23));

// Create PDF
Document document = new Document(PageSize.A4);
String fileName = "TC_" + studentName.replaceAll("\\s+", "_") + "_" + 
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf";

PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
document.open();

// Fonts
Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
Font fieldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

// Add logo if exists
try {
    Image logo = Image.getInstance("C:\\Users\\ins1f\\Downloads\\CollegeTransferCertificate\\top.png");
    logo.setAlignment(Image.ALIGN_CENTER);
    logo.scaleToFit(500, 100);
    document.add(logo);
} catch (Exception logoEx) {
    // Logo not found, continue without it
    System.out.println("Logo not found, continuing without header image");
}

// Header with border
Paragraph titleBorder = new Paragraph("TRANSFER CERTIFICATE", titleFont);
titleBorder.setAlignment(Element.ALIGN_CENTER);
titleBorder.setSpacingBefore(10);
titleBorder.setSpacingAfter(15);

// Create a bordered rectangle for title
PdfPTable titleTable = new PdfPTable(1);
titleTable.setWidthPercentage(40);
titleTable.setHorizontalAlignment(Element.ALIGN_CENTER);
PdfPCell titleCell = new PdfPCell(titleBorder);
titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
titleCell.setPadding(8);
titleCell.setBorderWidth(2);
titleTable.addCell(titleCell);
document.add(titleTable);

document.add(new Paragraph(" ", contentFont)); // Spacing

// Reg No and Serial No section
Paragraph regSerial = new Paragraph();
regSerial.add(new Chunk("Reg.No.     : ", fieldFont));
regSerial.add(new Chunk("9103224013010", contentFont));
regSerial.add(new Chunk("                                                           ", contentFont));
regSerial.add(new Chunk("Serial No :", fieldFont));
regSerial.add(new Chunk("  85", contentFont));
regSerial.setSpacingAfter(15);
document.add(regSerial);

// Content sections - Linear format
addLinearField(document, "1. Name of the Student", studentName, fieldFont, contentFont);

addLinearField(document, "2. Name of the Father / Guardian / Mother", fatherName, fieldFont, contentFont);

String dobText = dateOfBirth;
if (!dobWords.isEmpty()) {
    dobText += "\n    " + dobWords;
}
addLinearField(document, "3. Date of Birth as entered in the School Record (in words)", dobText, fieldFont, contentFont);

String nationalityInfo = nationality + " - " + caste + " - " + religion;
addLinearField(document, "4. Nationality, Religion & Caste", nationalityInfo, fieldFont, contentFont);

addLinearField(document, "5. Gender", gender, fieldFont, contentFont);

String admissionInfo = admissionDate + " & " + course;
addLinearField(document, "6. Date of Admission and Course in which admitted", admissionInfo, fieldFont, contentFont);

addLinearField(document, "7. Games played or extra-curricular activities in which the Student usually took part (mention achievement level there in)", gamesActivities, fieldFont, contentFont);

addLinearField(document, "8. Whether NCC Cadet / Scout & Guide", "NIL", fieldFont, contentFont);

addLinearField(document, "9. Any fee concession availed of if so, the nature of concession", feeConcession.isEmpty() ? "PMSS" : feeConcession, fieldFont, contentFont);

addLinearField(document, "10. Anna University Annual examination last taken result with class", lastExamResult, fieldFont, contentFont);

addLinearField(document, "11. Date on which the student left the college", leavingDate, fieldFont, contentFont);

addLinearField(document, "12. Class in which the student was studying at the time of leaving the college", classAtLeaving, fieldFont, contentFont);

addLinearField(document, "13. Whether qualified for promotion to the higher education", qualifiedPromotion, fieldFont, contentFont);

addLinearField(document, "14. Reason for leaving the Institution", reasonLeaving, fieldFont, contentFont);

addLinearField(document, "15. Date of issue of Transfer Certificate", issueDate, fieldFont, contentFont);

addLinearField(document, "16. Student conduct and character", conductCharacter, fieldFont, contentFont);

addLinearField(document, "17. Any other Remarks", otherRemarks, fieldFont, contentFont);

document.add(new Paragraph(" ", contentFont)); // Spacing

// UMIS No - right aligned
Paragraph umisSection = new Paragraph();
umisSection.add(new Chunk("                                                                                              ", contentFont));
umisSection.add(new Chunk("UMIS NO: ", fieldFont));
umisSection.add(new Chunk(umisNo, contentFont));
umisSection.setSpacingAfter(30);
document.add(umisSection);

// Signature section - right aligned
Paragraph principalSignature = new Paragraph("PRINCIPAL", headerFont);
principalSignature.setAlignment(Element.ALIGN_RIGHT);
principalSignature.setSpacingBefore(20);
document.add(principalSignature);

document.close();

        JOptionPane.showMessageDialog(this, 
            "Transfer Certificate PDF generated successfully!\nSaved as: " + fileName, 
            "PDF Generated", JOptionPane.INFORMATION_MESSAGE);
        
        updateStatus("PDF generated: " + fileName);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Error generating PDF: " + e.getMessage(), 
            "PDF Generation Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Helper method to safely convert object to string
private String getString(Object obj) {
    return obj != null ? obj.toString() : "";
}

// Helper method to add linear fields
private void addLinearField(Document document, String label, String value, Font labelFont, Font valueFont) throws DocumentException {
    Paragraph field = new Paragraph();
    field.add(new Chunk(label, labelFont));
    
    if (value != null && !value.trim().isEmpty()) {
        field.add(new Chunk(" : ", labelFont));
        field.add(new Chunk(value, valueFont));
    } else {
        field.add(new Chunk(" : ", labelFont));
    }
    
    field.setSpacingAfter(8);
    field.setIndentationLeft(0);
    document.add(field);
}

    // View selected certificate details in a dialog
    private void viewSelectedCertificate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to view.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        
        // Create detailed view dialog
        JDialog viewDialog = new JDialog(this, "Transfer Certificate Details", true);
        viewDialog.setSize(700, 800);
        viewDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add all fields for viewing
        String[] labels = {
    "ID:", "Student Name:", "Register No:", "Serial No:", "Father Name:",
    "Date of Birth:", "DOB (Words):", "Nationality:", "Religion:", "Caste:",
    "Gender:", "Admission Date:", "Course:", "Games/Activities:", "Fee Concession:",
    "Last Exam Result:", "Leaving Date:", "Class at Leaving:", "Qualified for Promotion:",
    "Reason for Leaving:", "Issue Date:", "Conduct & Character:", "Other Remarks:", "UMIS No:"
};
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            panel.add(new JLabel(labels[i]), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            
            Object value = model.getValueAt(modelRow, i);
            String displayValue = value != null ? value.toString() : "";
            
            JTextField field = new JTextField(displayValue);
            field.setEditable(false);
            field.setBackground(Color.WHITE);
            field.setPreferredSize(new Dimension(300, 25));
            panel.add(field, gbc);
        }
        
        // Add close button
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> viewDialog.dispose());
        panel.add(closeButton, gbc);
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        viewDialog.add(scrollPane);
        viewDialog.setVisible(true);
    }

    // Placeholder methods for menu items and other functionality
    private void openNewCertificateDialog() {
        JOptionPane.showMessageDialog(this, "New Certificate Dialog - To be implemented");
    }
    
    private void importData() {
        JOptionPane.showMessageDialog(this, "Import Data - To be implemented");
    }
    
    private void exportData() {
        JOptionPane.showMessageDialog(this, "Export Data - To be implemented");
    }
    
    private void printPreview() {
        JOptionPane.showMessageDialog(this, "Print Preview - To be implemented");
    }
    
    private void editSelectedRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to edit.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Edit Record feature - To be implemented");
    }
    
    private void duplicateRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to duplicate.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Duplicate Record - To be implemented");
    }
    
    private void focusSearchField() {
        searchField.requestFocus();
    }
    
    private void toggleFullScreen() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
    
    private void showColumnSettings() {
        JOptionPane.showMessageDialog(this, "Column Settings - To be implemented");
    }
    
    private void backupDatabase() {
        JOptionPane.showMessageDialog(this, "Database Backup - To be implemented");
    }
    
    private void generateReports() {
        JOptionPane.showMessageDialog(this, "Generate Reports - To be implemented");
    }
    
    private void validateData() {
        JOptionPane.showMessageDialog(this, "Data Validation - To be implemented");
    }
    
    private void openSettings() {
        JOptionPane.showMessageDialog(this, "Settings - To be implemented");
    }
    
    private void showUserGuide() {
        String guide = "Transfer Certificate Management System - User Guide\n\n" +
                      "Features:\n" +
                      "- View all transfer certificates in a table format\n" +
                      "- Search and filter records\n" +
                      "- Generate PDF certificates\n" +
                      "- Delete records (single or bulk)\n" +
                      "- View detailed certificate information\n\n" +
                      "Keyboard Shortcuts:\n" +
                      "- Delete: Delete selected record\n" +
                      "- F5: Refresh data\n" +
                      "- Ctrl+F: Focus search field\n" +
                      "- F11: Toggle full screen\n\n" +
                      "Right-click on any row for context menu options.";
        
        JTextArea textArea = new JTextArea(guide);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "User Guide", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showKeyboardShortcuts() {
        String shortcuts = "Keyboard Shortcuts:\n\n" +
                          "Delete - Delete selected record\n" +
                          "F5 - Refresh data\n" +
                          "Ctrl+F - Focus search field\n" +
                          "F11 - Toggle full screen\n" +
                          "Ctrl+N - New Certificate (Menu)\n" +
                          "Ctrl+E - Export Data (Menu)\n" +
                          "Ctrl+P - Print Preview (Menu)";
        JOptionPane.showMessageDialog(this, shortcuts, "Keyboard Shortcuts", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAbout() {
        String about = "Transfer Certificate Management System\n" +
                      "Version 2.0\n\n" +
                      "Features:\n" +
                      "- Complete certificate management\n" +
                      "- PDF generation with proper formatting\n" +
                      "- Advanced search and filtering\n" +
                      "- Data validation and error handling\n\n" +
                      "Developed for Educational Institutions\n" +
                      "© 2024 All Rights Reserved";
        JOptionPane.showMessageDialog(this, about, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void bulkDelete() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length <= 1) {
            JOptionPane.showMessageDialog(this, "Please select multiple records for bulk delete.");
            return;
        }
        deleteSelected(); // Use the same delete logic
    }
    
    private void exportAllRecords() {
        try {
            // Create a comprehensive PDF with all records
            Document document = new Document(PageSize.A4.rotate()); // Landscape for table
            String fileName = "All_Transfer_Certificates_" + 
                            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
            
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
            
            Paragraph title = new Paragraph("TRANSFER CERTIFICATES - COMPLETE REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Create table with main columns
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{8f, 15f, 15f, 12f, 12f, 12f, 12f, 14f});

            // Add headers
            String[] headers = {"ID", "Student Name", "Father Name", "Course", 
                              "Admission Date", "Leaving Date", "Issue Date", "Reason"};
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new BaseColor(200, 200, 200));
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add data rows
            for (int i = 0; i < model.getRowCount(); i++) {
                table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 0)), contentFont))); // ID
table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 1)), contentFont))); // Student Name
table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 4)), contentFont))); // Father Name
table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 12)), contentFont))); // Course
table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 11)), contentFont))); // Admission Date
table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 16)), contentFont))); // Leaving Date
table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 20)), contentFont))); // Issue Date
table.addCell(new PdfPCell(new Phrase(getString(model.getValueAt(i, 19)), contentFont)));
            }

            document.add(table);
            
            // Add summary
            document.add(new Paragraph("\n\nSummary:", headerFont));
            document.add(new Paragraph("Total Records: " + model.getRowCount(), contentFont));
            document.add(new Paragraph("Report Generated: " + 
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), contentFont));

            document.close();
            
            JOptionPane.showMessageDialog(this, 
                "All records exported successfully!\nSaved as: " + fileName, 
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            
            updateStatus("Export completed: " + fileName);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error exporting records: " + e.getMessage(), 
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void printSelectedCertificate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a certificate to print.");
            return;
        }
        
        // Generate PDF first, then suggest printing
        generateSelectedPDF();
        JOptionPane.showMessageDialog(this, 
            "PDF generated successfully!\nPlease open the PDF file and use your system's print function.", 
            "Print Certificate", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void emailCertificate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a certificate to email.");
            return;
        }
        
        // Get student email or show input dialog
        String email = JOptionPane.showInputDialog(this, 
            "Enter student's email address:", 
            "Email Certificate", JOptionPane.QUESTION_MESSAGE);
            
        if (email != null && !email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Email functionality not yet implemented.\nEmail address: " + email + 
                "\n\nFor now, please generate PDF and attach to email manually.", 
                "Email Feature", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void archiveOldRecords() {
        String yearInput = JOptionPane.showInputDialog(this, 
            "Enter the year before which records should be archived\n(e.g., 2020):", 
            "Archive Records", JOptionPane.QUESTION_MESSAGE);
            
        if (yearInput != null && !yearInput.trim().isEmpty()) {
            try {
                int year = Integer.parseInt(yearInput.trim());
                JOptionPane.showMessageDialog(this, 
                    "Archive functionality not yet implemented.\n" +
                    "Would archive records before year: " + year, 
                    "Archive Feature", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid year (e.g., 2020)", 
                    "Invalid Year", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void copyRowData() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to copy.");
            return;
        }
        
        StringBuilder rowData = new StringBuilder();
        for (int i = 0; i < table.getColumnCount(); i++) {
            rowData.append(table.getValueAt(selectedRow, i));
            if (i < table.getColumnCount() - 1) {
                rowData.append("\t");
            }
        }
        
        try {
            java.awt.datatransfer.StringSelection stringSelection = 
                new java.awt.datatransfer.StringSelection(rowData.toString());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(stringSelection, null);
            
            updateStatus("Row data copied to clipboard");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error copying data to clipboard: " + e.getMessage(), 
                "Copy Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom renderer for alternating row colors
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(245, 245, 245));
                }
            }
            return c;
        }
    }

    // Custom JDateChooser implementation
    private class JDateChooser extends JTextField {
        private Date date;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        public JDateChooser() {
            setPreferredSize(new Dimension(120, 25));
            setToolTipText("Enter date (dd/MM/yyyy) or double-click to select");
            
            // Add focus listener to validate date
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    validateAndSetDate();
                }
            });
            
            // Add action listener for Enter key
            addActionListener(e -> validateAndSetDate());
            
            // Add mouse listener to show date picker
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showSimpleDatePicker();
                    }
                }
            });
        }
        
        private void validateAndSetDate() {
            String text = getText().trim();
            if (text.isEmpty()) {
                date = null;
                firePropertyChange("date", null, null);
                return;
            }
            
            try {
                Date newDate = dateFormat.parse(text);
                Date oldDate = date;
                date = newDate;
                setText(dateFormat.format(date)); // Reformat for consistency
                firePropertyChange("date", oldDate, date);
            } catch (Exception e) {
                if (!text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid date format. Please use dd/MM/yyyy", 
                        "Date Error", JOptionPane.ERROR_MESSAGE);
                    setText(date != null ? dateFormat.format(date) : "");
                }
            }
        }
        
        private void showSimpleDatePicker() {
            // Create simple date input dialog
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
            dialog.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            // Current date or today
            Calendar cal = Calendar.getInstance();
            if (date != null) {
                cal.setTime(date);
            }
            
            // Day, Month, Year spinners
            SpinnerNumberModel dayModel = new SpinnerNumberModel(cal.get(Calendar.DAY_OF_MONTH), 1, 31, 1);
            SpinnerNumberModel monthModel = new SpinnerNumberModel(cal.get(Calendar.MONTH) + 1, 1, 12, 1);
            SpinnerNumberModel yearModel = new SpinnerNumberModel(cal.get(Calendar.YEAR), 1900, 2100, 1);
            
            JSpinner daySpinner = new JSpinner(dayModel);
            JSpinner monthSpinner = new JSpinner(monthModel);
            JSpinner yearSpinner = new JSpinner(yearModel);
            
            gbc.insets = new Insets(5, 5, 5, 5);
            
            gbc.gridx = 0; gbc.gridy = 0;
            dialog.add(new JLabel("Day:"), gbc);
            gbc.gridx = 1;
            dialog.add(daySpinner, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            dialog.add(new JLabel("Month:"), gbc);
            gbc.gridx = 1;
            dialog.add(monthSpinner, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            dialog.add(new JLabel("Year:"), gbc);
            gbc.gridx = 1;
            dialog.add(yearSpinner, gbc);
            
            JPanel buttonPanel = new JPanel();
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");
            JButton todayButton = new JButton("Today");
            
            okButton.addActionListener(e -> {
                try {
                    int day = (Integer) daySpinner.getValue();
                    int month = (Integer) monthSpinner.getValue() - 1; // Calendar months are 0-based
                    int year = (Integer) yearSpinner.getValue();
                    
                    Calendar newCal = Calendar.getInstance();
                    newCal.set(year, month, day);
                    Date oldDate = date;
                    date = newCal.getTime();
                    setText(dateFormat.format(date));
                    dialog.dispose();
                    firePropertyChange("date", oldDate, date);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid date selected", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            todayButton.addActionListener(e -> {
                Calendar today = Calendar.getInstance();
                dayModel.setValue(today.get(Calendar.DAY_OF_MONTH));
                monthModel.setValue(today.get(Calendar.MONTH) + 1);
                yearModel.setValue(today.get(Calendar.YEAR));
            });
            
            buttonPanel.add(todayButton);
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            gbc.gridx = 0; gbc.gridy = 3;
            gbc.gridwidth = 2;
            dialog.add(buttonPanel, gbc);
            
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
        
        public Date getDate() {
            return date;
        }
        
        public void setDate(Date date) {
            Date oldDate = this.date;
            this.date = date;
            if (date != null) {
                setText(dateFormat.format(date));
            } else {
                setText("");
            }
            firePropertyChange("date", oldDate, date);
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Set some global UI properties
            UIManager.put("Table.gridColor", Color.LIGHT_GRAY);
            UIManager.put("Table.showGrid", true);
            
            new ViewTransferCertificates().setVisible(true);
        });
    }
}