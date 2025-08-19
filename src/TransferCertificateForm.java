import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;


public class TransferCertificateForm extends JFrame {
    // ✅ Correct SQLite JDBC URL
    private static final String DB_URL = "jdbc:sqlite:college.db";
    // SQLite doesn’t need username/password
    private static final String DB_USERNAME = "";
    private static final String DB_PASSWORD = "";
    
    private JTextField studentNameField, registerNoField, serialNoField, fatherNameField,
            dobField, dobWordsField, nationalityField, religionField, casteField, 
            genderField, admissionDateField, courseField, gamesField, nccField, 
            feeConcessionField, resultField, leavingDateField, classLeavingField, 
            qualifiedField, reasonField, issueDateField, conductField,
            remarksField, umisField;
    
    private JButton generateBtn, importExcelBtn, saveToDbBtn, loadFromDbBtn, clearFieldsBtn, 
            backToMenuBtn, exportExcelBtn, searchBtn, deleteBtn, batchPrintBtn;
    private JComboBox<String> studentSelector;
    private JTextField searchField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer autoSaveTimer;
    private boolean isDarkMode = false;
    private JToggleButton darkModeToggle;

    public TransferCertificateForm() {
        initializeDatabase();
        setupGUI();
        setupAutoSave();
        applyTheme();
    }

    private void initializeDatabase() {
        try {
            // ✅ SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS transfer_certificates (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_name TEXT," +
                "register_no TEXT," +
                "serial_no TEXT," +
                "father_name TEXT," +
                "dob TEXT," +
                "dob_words TEXT," +
                "nationality TEXT," +
                "religion TEXT," +
                "caste TEXT," +
                "gender TEXT," +
                "admission_date TEXT," +
                "course TEXT," +
                "games TEXT," +
                "ncc TEXT," +
                "fee_concession TEXT," +
                "result TEXT," +
                "leaving_date TEXT," +
                "class_leaving TEXT," +
                "qualified TEXT," +
                "reason TEXT," +
                "issue_date TEXT," +
                "conduct TEXT," +
                "remarks TEXT," +
                "umis_no TEXT," +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
            stmt.close();
            conn.close();
            System.out.println("SQLite Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database initialization failed: " + e.getMessage());
        }
    }



    private void setupGUI() {
        setTitle("CCET Transfer Certificate Generator");
        setSize(900, 950);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set icon
        try {
            ImageIcon icon = new ImageIcon("icon.png");
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, continue without it
        }

        // Top panel with gradient background
        JPanel topPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(41, 128, 185);
                Color color2 = new Color(109, 213, 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        topPanel.setPreferredSize(new Dimension(900, 120));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Chendhuran College of Engineering and Technology");
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // Button panel with modern buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        // Create styled buttons
        backToMenuBtn = createStyledButton("← Back to Menu", new Color(231, 76, 60));
        importExcelBtn = createStyledButton("📥 Import Excel", new Color(52, 152, 219));
        exportExcelBtn = createStyledButton("📤 Export Excel", new Color(46, 204, 113));
        saveToDbBtn = createStyledButton("💾 Save", new Color(155, 89, 182));
        loadFromDbBtn = createStyledButton("📂 Load", new Color(241, 196, 15));
        deleteBtn = createStyledButton("🗑️ Delete", new Color(192, 57, 43));
        clearFieldsBtn = createStyledButton("🧹 Clear", new Color(149, 165, 166));
        generateBtn = createStyledButton("📄 Generate PDF", new Color(22, 160, 133));
        batchPrintBtn = createStyledButton("🖨️ Batch Print", new Color(211, 84, 0));
        
        // Dark mode toggle
        darkModeToggle = new JToggleButton("🌙");
        darkModeToggle.setToolTipText("Toggle Dark Mode");
        darkModeToggle.addActionListener(e -> toggleDarkMode());

        // Add action listeners
        backToMenuBtn.addActionListener(e -> backToMainMenu());
        importExcelBtn.addActionListener(e -> importFromExcel());
        exportExcelBtn.addActionListener(e -> exportToExcel());
        saveToDbBtn.addActionListener(e -> saveToDatabase());
        loadFromDbBtn.addActionListener(e -> loadFromDatabase());
        deleteBtn.addActionListener(e -> deleteRecord());
        clearFieldsBtn.addActionListener(e -> clearAllFields());
        generateBtn.addActionListener(e -> generatePDF());
        batchPrintBtn.addActionListener(e -> batchPrint());

        buttonPanel.add(backToMenuBtn);
        buttonPanel.add(importExcelBtn);
        buttonPanel.add(exportExcelBtn);
        buttonPanel.add(saveToDbBtn);
        buttonPanel.add(loadFromDbBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearFieldsBtn);
        buttonPanel.add(generateBtn);
        buttonPanel.add(batchPrintBtn);
        buttonPanel.add(darkModeToggle);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchBtn = createStyledButton("🔍 Search", new Color(52, 73, 94));
        searchBtn.addActionListener(e -> searchStudent());
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        // Student selector panel
        JPanel selectorPanel = new JPanel(new FlowLayout());
        selectorPanel.setOpaque(false);
        selectorPanel.add(new JLabel("Select Student:"));
        studentSelector = new JComboBox<>();
        studentSelector.setPreferredSize(new Dimension(250, 30));
        studentSelector.addActionListener(e -> loadSelectedStudent());
        selectorPanel.add(studentSelector);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        
        JPanel bottomTopPanel = new JPanel(new BorderLayout());
        bottomTopPanel.setOpaque(false);
        bottomTopPanel.add(searchPanel, BorderLayout.WEST);
        bottomTopPanel.add(selectorPanel, BorderLayout.EAST);
        topPanel.add(bottomTopPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);

        // Main form panel with better styling
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(new Color(236, 240, 241));

        // Add all fields with better styling
        addFormField(formPanel, "Reg. No:", registerNoField = createStyledTextField());
        addFormField(formPanel, "Serial No:", serialNoField = createStyledTextField());
        addFormField(formPanel, "1. Name of the Student:", studentNameField = createStyledTextField());
        addFormField(formPanel, "2. Name of the Father / Guardian / Mother:", fatherNameField = createStyledTextField());
        addFormField(formPanel, "3. Date of Birth (dd-MM-yyyy):", dobField = createStyledTextField());
        addFormField(formPanel, "Date of Birth in words:", dobWordsField = createStyledTextField());
        
        dobField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                convertDOBToWords();
            }
        });

        addFormField(formPanel, "4. Nationality:", nationalityField = createStyledTextField());
        addFormField(formPanel, "Religion:", religionField = createStyledTextField());
        addFormField(formPanel, "Caste:", casteField = createStyledTextField());
        addFormField(formPanel, "5. Gender:", genderField = createStyledTextField());
        addFormField(formPanel, "6. Date of Admission:", admissionDateField = createStyledTextField());
        addFormField(formPanel, "Course:", courseField = createStyledTextField());
        addFormField(formPanel, "7. Games / Extra-curricular activities:", gamesField = createStyledTextField());
        addFormField(formPanel, "8. Whether NCC Cadet / Scout & Guide:", nccField = createStyledTextField());
        addFormField(formPanel, "9. Any fee concession availed:", feeConcessionField = createStyledTextField());
        addFormField(formPanel, "10. Anna University Annual examination result:", resultField = createStyledTextField());
        addFormField(formPanel, "11. Date student left the college:", leavingDateField = createStyledTextField());
        addFormField(formPanel, "12. Class student was studying:", classLeavingField = createStyledTextField());
        addFormField(formPanel, "13. Qualified for promotion to higher education:", qualifiedField = createStyledTextField());
        addFormField(formPanel, "14. Reason for leaving the Institution:", reasonField = createStyledTextField());
        addFormField(formPanel, "15. Date of issue of Transfer Certificate:", issueDateField = createStyledTextField());
        addFormField(formPanel, "16. Student conduct and character:", conductField = createStyledTextField());
        addFormField(formPanel, "17. Any other Remarks:", remarksField = createStyledTextField());
        addFormField(formPanel, "UMIS No:", umisField = createStyledTextField());

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        refreshStudentSelector();
        
        // Add keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 35));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private void addFormField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
                panel.add(label);
        panel.add(field);
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+S for Save
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveToDatabase();
            }
        });
        
        // Ctrl+P for Print/Generate PDF
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "print");
        getRootPane().getActionMap().put("print", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                generatePDF();
            }
        });
        
        // Ctrl+N for New/Clear
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "new");
        getRootPane().getActionMap().put("new", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearAllFields();
            }
        });
        
        // Escape for Back to Menu
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        getRootPane().getActionMap().put("back", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                backToMainMenu();
            }
        });
    }

    private void setupAutoSave() {
        autoSaveTimer = new Timer(300000, e -> { // Auto-save every 5 minutes
            if (hasUnsavedChanges()) {
                saveToDatabase();
                showNotification("Auto-saved successfully!");
            }
        });
        autoSaveTimer.start();
    }

    private boolean hasUnsavedChanges() {
        return !studentNameField.getText().trim().isEmpty();
    }

    private void showNotification(String message) {
        statusLabel.setText(message);
        Timer timer = new Timer(3000, e -> statusLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        darkModeToggle.setText(isDarkMode ? "☀️" : "🌙");
        applyTheme();
    }

    private void applyTheme() {
        Color bgColor = isDarkMode ? new Color(44, 62, 80) : new Color(236, 240, 241);
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;
        Color fieldBg = isDarkMode ? new Color(52, 73, 94) : Color.WHITE;
        
        getContentPane().setBackground(bgColor);
        applyThemeToComponent(getContentPane(), bgColor, fgColor, fieldBg);
    }

    private void applyThemeToComponent(Container container, Color bgColor, Color fgColor, Color fieldBg) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(bgColor);
                applyThemeToComponent((Container) comp, bgColor, fgColor, fieldBg);
            } else if (comp instanceof JLabel) {
                comp.setForeground(fgColor);
            } else if (comp instanceof JTextField) {
                comp.setBackground(fieldBg);
                comp.setForeground(fgColor);
            } else if (comp instanceof JScrollPane) {
                ((JScrollPane) comp).getViewport().setBackground(bgColor);
                applyThemeToComponent((Container) comp, bgColor, fgColor, fieldBg);
            } else if (comp instanceof Container) {
                applyThemeToComponent((Container) comp, bgColor, fgColor, fieldBg);
            }
        }
    }

   private void backToMainMenu() {
    int confirm = JOptionPane.showConfirmDialog(this, 
        "Are you sure you want to go back to the main menu? Any unsaved changes will be lost.",
        "Confirm Exit", JOptionPane.YES_NO_OPTION);
    
    if (confirm == JOptionPane.YES_OPTION) {
        this.dispose(); // Close current window

        SwingUtilities.invokeLater(() -> {
            try {
                new MainMenu().setVisible(true); // Launch actual MainMenu class
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}


    private void searchStudent() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term");
            return;
        }
        
        try {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "SELECT DISTINCT student_name FROM transfer_certificates WHERE " +
                "student_name LIKE ? OR register_no LIKE ? OR father_name LIKE ? " +
                "ORDER BY student_name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            studentSelector.removeAllItems();
            studentSelector.addItem("Select Student");
            
            int count = 0;
            while (rs.next()) {
                studentSelector.addItem(rs.getString("student_name"));
                count++;
            }
            
            if (count == 0) {
                JOptionPane.showMessageDialog(this, "No students found matching: " + searchTerm);
            } else {
                showNotification("Found " + count + " student(s)");
            }
            
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage());
        } finally {
            progressBar.setVisible(false);
        }
    }

    private void deleteRecord() {
        String selectedStudent = (String) studentSelector.getSelectedItem();
        if (selectedStudent == null || selectedStudent.equals("Select Student")) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete the record for " + selectedStudent + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Class.forName("org.sqlite.JDBC");
                Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                String sql = "DELETE FROM transfer_certificates WHERE student_name = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selectedStudent);
                
                int deleted = pstmt.executeUpdate();
                if (deleted > 0) {
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!");
                    clearAllFields();
                    refreshStudentSelector();
                }
                
                pstmt.close();
                conn.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Delete error: " + e.getMessage());
            }
        }
    }

   private void exportToExcel() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setSelectedFile(new File("transfer_certificates_export.xlsx"));
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx");
    fileChooser.setFileFilter(filter);

    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Exporting to Excel...");

        try (
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transfer_certificates ORDER BY student_name");
            Workbook workbook = new XSSFWorkbook();
        ) {
            Sheet sheet = workbook.createSheet("Transfer Certificates");

            // Header row
            String[] headers = {"Student Name", "Register No", "Serial No", "Father Name", 
                "DOB", "DOB Words", "Nationality", "Religion", "Caste", "Gender", 
                "Admission Date", "Course", "Games", "NCC", "Fee Concession", "Result", 
                "Leaving Date", "Class Leaving", "Qualified", "Reason", "Issue Date", 
                "Conduct", "Remarks", "UMIS No"};

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data
            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getString("student_name"));
                row.createCell(1).setCellValue(rs.getString("register_no"));
                row.createCell(2).setCellValue(rs.getString("serial_no"));
                row.createCell(3).setCellValue(rs.getString("father_name"));
                row.createCell(4).setCellValue(rs.getString("dob"));
                row.createCell(5).setCellValue(rs.getString("dob_words"));
                row.createCell(6).setCellValue(rs.getString("nationality"));
                row.createCell(7).setCellValue(rs.getString("religion"));
                row.createCell(8).setCellValue(rs.getString("caste"));
                row.createCell(9).setCellValue(rs.getString("gender"));
                row.createCell(10).setCellValue(rs.getString("admission_date"));
                row.createCell(11).setCellValue(rs.getString("course"));
                row.createCell(12).setCellValue(rs.getString("games"));
                row.createCell(13).setCellValue(rs.getString("ncc"));
                row.createCell(14).setCellValue(rs.getString("fee_concession"));
                row.createCell(15).setCellValue(rs.getString("result"));
                row.createCell(16).setCellValue(rs.getString("leaving_date"));
                row.createCell(17).setCellValue(rs.getString("class_leaving"));
                row.createCell(18).setCellValue(rs.getString("qualified"));
                row.createCell(19).setCellValue(rs.getString("reason"));
                row.createCell(20).setCellValue(rs.getString("issue_date"));
                row.createCell(21).setCellValue(rs.getString("conduct"));
                row.createCell(22).setCellValue(rs.getString("remarks"));
                row.createCell(23).setCellValue(rs.getString("umis_no"));
            }

            // Auto-size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(this, 
                "Exported " + (rowNum - 1) + " records to Excel successfully!");
            showNotification("Export completed");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            progressBar.setVisible(false);
            statusLabel.setText("Ready");
        }
    }
}


    private void batchPrint() {
        List<String> selectedStudents = new ArrayList<>();
        
        // Create selection dialog
        JDialog dialog = new JDialog(this, "Select Students for Batch Print", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> studentList = new JList<>(listModel);
        studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "SELECT DISTINCT student_name FROM transfer_certificates ORDER BY student_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                listModel.addElement(rs.getString("student_name"));
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
            return;
        }
        
        JScrollPane scrollPane = new JScrollPane(studentList);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton selectAllBtn = new JButton("Select All");
        JButton printBtn = new JButton("Print Selected");
                JButton cancelBtn = new JButton("Cancel");
        
        selectAllBtn.addActionListener(e -> {
            int size = listModel.getSize();
            int[] indices = new int[size];
            for (int i = 0; i < size; i++) {
                indices[i] = i;
            }
            studentList.setSelectedIndices(indices);
        });
        
        printBtn.addActionListener(e -> {
            selectedStudents.addAll(studentList.getSelectedValuesList());
            dialog.dispose();
            
            if (!selectedStudents.isEmpty()) {
                progressBar.setVisible(true);
                progressBar.setMaximum(selectedStudents.size());
                progressBar.setValue(0);
                
                SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        int count = 0;
                        for (String studentName : selectedStudents) {
                            loadStudentData(studentName);
                            generatePDFSilently(studentName);
                            count++;
                            publish(count);
                        }
                        return null;
                    }
                    
                    @Override
                    protected void process(List<Integer> chunks) {
                        int progress = chunks.get(chunks.size() - 1);
                        progressBar.setValue(progress);
                        statusLabel.setText("Printing " + progress + " of " + selectedStudents.size());
                    }
                    
                    @Override
                    protected void done() {
                        progressBar.setVisible(false);
                        statusLabel.setText("Ready");
                        JOptionPane.showMessageDialog(TransferCertificateForm.this, 
                            "Batch print completed! Generated " + selectedStudents.size() + " PDFs.");
                    }
                };
                worker.execute();
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(selectAllBtn);
        buttonPanel.add(printBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void loadStudentData(String studentName) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "SELECT * FROM transfer_certificates WHERE student_name = ? ORDER BY id DESC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentName);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                studentNameField.setText(rs.getString("student_name") != null ? rs.getString("student_name") : "");
                registerNoField.setText(rs.getString("register_no") != null ? rs.getString("register_no") : "");
                serialNoField.setText(rs.getString("serial_no") != null ? rs.getString("serial_no") : "");
                fatherNameField.setText(rs.getString("father_name") != null ? rs.getString("father_name") : "");
                dobField.setText(rs.getString("dob") != null ? rs.getString("dob") : "");
                dobWordsField.setText(rs.getString("dob_words") != null ? rs.getString("dob_words") : "");
                nationalityField.setText(rs.getString("nationality") != null ? rs.getString("nationality") : "");
                religionField.setText(rs.getString("religion") != null ? rs.getString("religion") : "");
                casteField.setText(rs.getString("caste") != null ? rs.getString("caste") : "");
                genderField.setText(rs.getString("gender") != null ? rs.getString("gender") : "");
                admissionDateField.setText(rs.getString("admission_date") != null ? rs.getString("admission_date") : "");
                courseField.setText(rs.getString("course") != null ? rs.getString("course") : "");
                gamesField.setText(rs.getString("games") != null ? rs.getString("games") : "");
                nccField.setText(rs.getString("ncc") != null ? rs.getString("ncc") : "");
                feeConcessionField.setText(rs.getString("fee_concession") != null ? rs.getString("fee_concession") : "");
                resultField.setText(rs.getString("result") != null ? rs.getString("result") : "");
                leavingDateField.setText(rs.getString("leaving_date") != null ? rs.getString("leaving_date") : "");
                classLeavingField.setText(rs.getString("class_leaving") != null ? rs.getString("class_leaving") : "");
                qualifiedField.setText(rs.getString("qualified") != null ? rs.getString("qualified") : "");
                reasonField.setText(rs.getString("reason") != null ? rs.getString("reason") : "");
                issueDateField.setText(rs.getString("issue_date") != null ? rs.getString("issue_date") : "");
                conductField.setText(rs.getString("conduct") != null ? rs.getString("conduct") : "");
                remarksField.setText(rs.getString("remarks") != null ? rs.getString("remarks") : "");
                umisField.setText(rs.getString("umis_no") != null ? rs.getString("umis_no") : "");
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("Error loading student data: " + e.getMessage());
        }
    }

    private void generatePDFSilently(String studentName) {
        try {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            String filename = "batch_print/" + studentName.replaceAll("[^a-zA-Z0-9]", "_") + "_TC.pdf";
            
            // Create directory if it doesn't exist
            new File("batch_print").mkdirs();
            
            PdfWriter.getInstance(doc, new FileOutputStream(filename));
            doc.open();

            // Try to add logo
            try {
                Image logo = Image.getInstance("C:\\Users\\ins1f\\Downloads\\CollegeTransferCertificate\\top.png");
                logo.scaleToFit(500, 100);
                logo.setAlignment(Element.ALIGN_CENTER);
                doc.add(logo);
            } catch (Exception e) {
                System.out.println("Logo image not found: " + e.getMessage());
            }

            Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

            Paragraph title = new Paragraph("TRANSFER CERTIFICATE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("\n"));
            
            // Header with Reg No and Serial No
            Paragraph header = new Paragraph("Reg. No. : " + registerNoField.getText() + 
                "                                                      Serial No : " + serialNoField.getText(), bold);
            doc.add(header);
            doc.add(new Paragraph("\n"));

            // Create table for data
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.6f, 5.5f, 0.3f, 5.0f});
            table.setSpacingBefore(10f);

            // Helper to create styled cells
            java.util.function.BiFunction<String, Integer, PdfPCell> createCell = (text, alignment) -> {
                PdfPCell cell = new PdfPCell(new Phrase(text, font));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(alignment);
                cell.setVerticalAlignment(Element.ALIGN_TOP);
                cell.setPaddingBottom(6f);
                cell.setPaddingLeft(2f);
                return cell;
            };

            // Add all rows
            addTableRow(table, "1.", "Name of the Student", ":", studentNameField.getText(), createCell);
            addTableRow(table, "2.", "Name of the Father / Guardian / Mother", ":", fatherNameField.getText(), createCell);
            addTableRow(table, "3.", "Date of Birth as entered in the School Record (in figures)", ":", dobField.getText(), createCell);
            addTableRow(table, "", "(in words)", ":", dobWordsField.getText(), createCell);
            addTableRow(table, "4.", "Nationality, Religion & Caste", ":", 
                nationalityField.getText() + " - " + religionField.getText() + " - " + casteField.getText(), createCell);
            addTableRow(table, "5.", "Gender", ":", genderField.getText(), createCell);
            addTableRow(table, "6.", "Date of Admission and Course in which admitted", ":", 
                admissionDateField.getText() + " & " + courseField.getText(), createCell);
            addTableRow(table, "7.", "Games played or extra-curricular activities in which the Student usually took part (mention achievement level there in)", ":", gamesField.getText(), createCell);
            addTableRow(table, "8.", "Whether NCC Cadet / Scout & Guide", ":", nccField.getText(), createCell);
            addTableRow(table, "9.", "Any fee concession availed of if so, the nature of concession", ":", feeConcessionField.getText(), createCell);
            addTableRow(table, "10.", "Anna University Annual examination last taken result with class", ":", resultField.getText(), createCell);
            addTableRow(table, "11.", "Date on which the student left the college", ":", leavingDateField.getText(), createCell);
            addTableRow(table, "12.", "Class in which the student was studying at the time of leaving the college", ":", classLeavingField.getText(), createCell);
            addTableRow(table, "13.", "Whether qualified for promotion to the higher education", ":", qualifiedField.getText(), createCell);
            addTableRow(table, "14.", "Reason for leaving the Institution", ":", reasonField.getText(), createCell);
            addTableRow(table, "15.", "Date of issue of Transfer Certificate", ":", issueDateField.getText(), createCell);
            addTableRow(table, "16.", "Student conduct and character", ":", conductField.getText(), createCell);
            addTableRow(table, "17.", "Any other Remarks", ":", remarksField.getText(), createCell);
            addTableRow(table, "", "", "", "UMIS No: " + umisField.getText(), createCell);

            doc.add(table);
            doc.add(new Paragraph("\n\n\n"));
            
            Paragraph principal = new Paragraph("PRINCIPAL", bold);
            principal.setAlignment(Element.ALIGN_RIGHT);
            doc.add(principal);

            doc.close();
        } catch (Exception e) {
            System.err.println("PDF Generation Failed: " + e.getMessage());
        }
    }

    private void addTableRow(PdfPTable table, String num, String label, String colon, String value, 
            java.util.function.BiFunction<String, Integer, PdfPCell> createCell) {
        table.addCell(createCell.apply(num, Element.ALIGN_LEFT));
        table.addCell(createCell.apply(label, Element.ALIGN_LEFT));
        table.addCell(createCell.apply(colon, Element.ALIGN_CENTER));
        table.addCell(createCell.apply(value, Element.ALIGN_LEFT));
    }

    // Keep all the existing methods from the original code
    private void importFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xls", "xlsx");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                importExcelData(selectedFile);
                JOptionPane.showMessageDialog(this, "Excel data imported successfully!");
                refreshStudentSelector();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error importing Excel: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void importExcelData(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook;
        
        if (file.getName().toLowerCase().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(fis);
        } else {
            workbook = new HSSFWorkbook(fis);
        }

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        
        // Skip header row if exists
        if (rowIterator.hasNext()) {
            Row headerRow = rowIterator.next();
            // Check if first cell looks like a header
            Cell firstCell = headerRow.getCell(0);
            if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                String firstCellValue = firstCell.getStringCellValue().toLowerCase();
                if (!firstCellValue.contains("student") && !firstCellValue.matches(".*[0-9].*")) {
                    // This doesn't look like a header, process this row
                    importRowToDatabase(headerRow);
                }
            } else if (firstCell != null) {
                // Not a string, probably data
                importRowToDatabase(headerRow);
            }
        }

        // Import remaining rows to database
        int importedCount = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (importRowToDatabase(row)) {
                importedCount++;
            }
        }

        workbook.close();
        fis.close();
        
        JOptionPane.showMessageDialog(this, "Imported " + importedCount + " records successfully!");
    }

    private boolean importRowToDatabase(Row row) {
        try {
            // Check if row has any data
            boolean hasData = false;
            for (int i = 0; i < 24; i++) {
                Cell cell = row.getCell(i);
                if (cell != null && !getCellValueAsString(cell).trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            
            if (!hasData) {
                return false; // Skip empty rows
            }

            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "INSERT INTO transfer_certificates " +
                "(student_name, register_no, serial_no, father_name, dob, dob_words, nationality, " +
                "religion, caste, gender, admission_date, course, games, ncc, fee_concession, " +
                "result, leaving_date, class_leaving, qualified, reason, issue_date, conduct, " +
                "remarks, umis_no) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            for (int i = 0; i < 24; i++) {
                Cell cell = row.getCell(i);
                String value = getCellValueAsString(cell);
                pstmt.setString(i + 1, value);
            }
            
            pstmt.executeUpdate();
            pstmt.close();
                        conn.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error importing row: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    double numValue = cell.getNumericCellValue();
                    // Check if it's a whole number
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    private void saveToDatabase() {
        if (studentNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter student name before saving.");
            return;
        }
        
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "INSERT INTO transfer_certificates " +
                "(student_name, register_no, serial_no, father_name, dob, dob_words, nationality, " +
                "religion, caste, gender, admission_date, course, games, ncc, fee_concession, " +
                "result, leaving_date, class_leaving, qualified, reason, issue_date, conduct, " +
                "remarks, umis_no) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentNameField.getText());
            pstmt.setString(2, registerNoField.getText());
            pstmt.setString(3, serialNoField.getText());
            pstmt.setString(4, fatherNameField.getText());
            pstmt.setString(5, dobField.getText());
            pstmt.setString(6, dobWordsField.getText());
            pstmt.setString(7, nationalityField.getText());
            pstmt.setString(8, religionField.getText());
            pstmt.setString(9, casteField.getText());
            pstmt.setString(10, genderField.getText());
            pstmt.setString(11, admissionDateField.getText());
            pstmt.setString(12, courseField.getText());
            pstmt.setString(13, gamesField.getText());
            pstmt.setString(14, nccField.getText());
            pstmt.setString(15, feeConcessionField.getText());
            pstmt.setString(16, resultField.getText());
            pstmt.setString(17, leavingDateField.getText());
            pstmt.setString(18, classLeavingField.getText());
            pstmt.setString(19, qualifiedField.getText());
            pstmt.setString(20, reasonField.getText());
            pstmt.setString(21, issueDateField.getText());
            pstmt.setString(22, conductField.getText());
            pstmt.setString(23, remarksField.getText());
            pstmt.setString(24, umisField.getText());
            
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
            
            JOptionPane.showMessageDialog(this, "Data saved to database successfully!");
            showNotification("Saved successfully!");
            refreshStudentSelector();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromDatabase() {
        String selectedStudent = (String) studentSelector.getSelectedItem();
        if (selectedStudent == null || selectedStudent.equals("Select Student")) {
            JOptionPane.showMessageDialog(this, "Please select a student from the dropdown");
            return;
        }
        loadSelectedStudent();
    }

    private void loadSelectedStudent() {
        String selectedStudent = (String) studentSelector.getSelectedItem();
        if (selectedStudent == null || selectedStudent.equals("Select Student")) return;

        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "SELECT * FROM transfer_certificates WHERE student_name = ? ORDER BY id DESC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selectedStudent);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                studentNameField.setText(rs.getString("student_name") != null ? rs.getString("student_name") : "");
                registerNoField.setText(rs.getString("register_no") != null ? rs.getString("register_no") : "");
                serialNoField.setText(rs.getString("serial_no") != null ? rs.getString("serial_no") : "");
                fatherNameField.setText(rs.getString("father_name") != null ? rs.getString("father_name") : "");
                dobField.setText(rs.getString("dob") != null ? rs.getString("dob") : "");
                dobWordsField.setText(rs.getString("dob_words") != null ? rs.getString("dob_words") : "");
                nationalityField.setText(rs.getString("nationality") != null ? rs.getString("nationality") : "");
                religionField.setText(rs.getString("religion") != null ? rs.getString("religion") : "");
                casteField.setText(rs.getString("caste") != null ? rs.getString("caste") : "");
                genderField.setText(rs.getString("gender") != null ? rs.getString("gender") : "");
                admissionDateField.setText(rs.getString("admission_date") != null ? rs.getString("admission_date") : "");
                courseField.setText(rs.getString("course") != null ? rs.getString("course") : "");
                gamesField.setText(rs.getString("games") != null ? rs.getString("games") : "");
                nccField.setText(rs.getString("ncc") != null ? rs.getString("ncc") : "");
                feeConcessionField.setText(rs.getString("fee_concession") != null ? rs.getString("fee_concession") : "");
                resultField.setText(rs.getString("result") != null ? rs.getString("result") : "");
                leavingDateField.setText(rs.getString("leaving_date") != null ? rs.getString("leaving_date") : "");
                classLeavingField.setText(rs.getString("class_leaving") != null ? rs.getString("class_leaving") : "");
                qualifiedField.setText(rs.getString("qualified") != null ? rs.getString("qualified") : "");
                reasonField.setText(rs.getString("reason") != null ? rs.getString("reason") : "");
                issueDateField.setText(rs.getString("issue_date") != null ? rs.getString("issue_date") : "");
                conductField.setText(rs.getString("conduct") != null ? rs.getString("conduct") : "");
                remarksField.setText(rs.getString("remarks") != null ? rs.getString("remarks") : "");
                umisField.setText(rs.getString("umis_no") != null ? rs.getString("umis_no") : "");
                
                showNotification("Loaded: " + selectedStudent);
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshStudentSelector() {
        studentSelector.removeAllItems();
        studentSelector.addItem("Select Student");
        
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "SELECT DISTINCT student_name FROM transfer_certificates WHERE student_name IS NOT NULL AND student_name != '' ORDER BY student_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                String studentName = rs.getString("student_name");
                if (studentName != null && !studentName.trim().isEmpty()) {
                    studentSelector.addItem(studentName);
                }
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("Error refreshing student selector: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearAllFields() {
        studentNameField.setText("");
        registerNoField.setText("");
        serialNoField.setText("");
        fatherNameField.setText("");
        dobField.setText("");
        dobWordsField.setText("");
        nationalityField.setText("");
        religionField.setText("");
        casteField.setText("");
        genderField.setText("");
        admissionDateField.setText("");
        courseField.setText("");
        gamesField.setText("");
        nccField.setText("");
        feeConcessionField.setText("");
        resultField.setText("");
        leavingDateField.setText("");
        classLeavingField.setText("");
        qualifiedField.setText("");
        reasonField.setText("");
        issueDateField.setText("");
        conductField.setText("");
        remarksField.setText("");
        umisField.setText("");
        studentSelector.setSelectedIndex(0);
        searchField.setText("");
        showNotification("Form cleared");
    }

    private void convertDOBToWords() {
        try {
            String inputDate = dobField.getText().trim();
            if (inputDate.isEmpty()) return;

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date date = sdf.parse(inputDate);

            SimpleDateFormat dayFmt = new SimpleDateFormat("dd");
            SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM");
            SimpleDateFormat yearFmt = new SimpleDateFormat("yyyy");

            String dobWords = dayFmt.format(date) + " " + monthFmt.format(date).toUpperCase()
                    + " " + convertYearToWords(Integer.parseInt(yearFmt.format(date)));

            dobWordsField.setText(dobWords);
        } catch (Exception ex) {
            dobWordsField.setText("Invalid Date");
            System.err.println("Error converting DOB to words: " + ex.getMessage());
        }
    }

    private String convertYearToWords(int year) {
        String[] ones = {"", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE"};
        String[] tens = {"", "", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY"};
        String[] teens = {"TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN",
                          "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"};

        StringBuilder sb = new StringBuilder();
        int thousands = year / 1000;
        int hundreds = (year % 1000) / 100;
        int lastTwo = year % 100;

        if (thousands != 0) sb.append(ones[thousands]).append(" THOUSAND ");
        if (hundreds != 0) sb.append(ones[hundreds]).append(" HUNDRED ");

        if (lastTwo >= 10 && lastTwo <= 19) {
            sb.append(teens[lastTwo - 10]);
        } else {
            if (lastTwo / 10 != 0) sb.append(tens[lastTwo / 10]).append(" ");
            if (lastTwo % 10 != 0) sb.append(ones[lastTwo % 10]);
        }

        return sb.toString().trim();
    }

    private void generatePDF() {
        if (studentNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter student name before generating PDF.");
            return;
        }
        
        try {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            String filename = studentNameField.getText().replaceAll("[^a-zA-Z0-9]", "_") + "_TC.pdf";
            PdfWriter.getInstance(doc, new FileOutputStream(filename));
            doc.open();

            // Try to add logo
            try {
                Image logo = Image.getInstance("C:\\Users\\ins1f\\Downloads\\CollegeTransferCertificate\\top.png");
                logo.scaleToFit(500, 100);
                logo.setAlignment(Element.ALIGN_CENTER);
                doc.add(logo);
            } catch (Exception e) {
                System.out.println("Logo image not found: " + e.getMessage());
            }

            Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

            Paragraph title = new Paragraph("TRANSFER CERTIFICATE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("\n"));
            
            // Header with Reg No and Serial No
            Paragraph header = new Paragraph("Reg. No. : " + registerNoField.getText() + 
                "                                                      Serial No : " + serialNoField.getText(), bold);
            doc.add(header);
            doc.add(new Paragraph("\n"));

            // Create table with all the data (same as in original code)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.6f, 5.5f, 0.3f, 5.0f});
            table.setSpacingBefore(10f);

            // Helper to create styled cells
            java.util.function.BiFunction<String, Integer, PdfPCell> createCell = (text, alignment) -> {
                PdfPCell cell = new PdfPCell(new Phrase(text, font));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(alignment);
                cell.setVerticalAlignment(Element.ALIGN_TOP);
                cell.setPaddingBottom(6f);
                cell.setPaddingLeft(2f);
                return cell;
            };

            // Add all the table rows (same as original)
            addTableRow(table, "1.", "Name of the Student", ":", studentNameField.getText(), createCell);
            addTableRow(table, "2.", "Name of the Father / Guardian / Mother", ":", fatherNameField.getText(), createCell);
            addTableRow(table, "3.", "Date of Birth as entered in the School Record (in figures)", ":", dobField.getText(), createCell);
            addTableRow(table, "", "(in words)", ":", dobWordsField.getText(), createCell);
            addTableRow(table, "4.", "Nationality, Religion & Caste", ":", 
                nationalityField.getText() + " - " + religionField.getText() + " - " + casteField.getText(), createCell);
            addTableRow(table, "5.", "Gender", ":", genderField.getText(), createCell);
            addTableRow(table, "6.", "Date of Admission and Course in which admitted", ":", 
                admissionDateField.getText() + " & " + courseField.getText(), createCell);
            addTableRow(table, "7.", "Games played or extra-curricular activities in which the Student usually took part (mention achievement level there in)", ":", gamesField.getText(), createCell);
            addTableRow(table, "8.", "Whether NCC Cadet / Scout & Guide", ":", nccField.getText(), createCell);
            addTableRow(table, "9.", "Any fee concession availed of if so, the nature of concession", ":", feeConcessionField.getText(), createCell);
            addTableRow(table, "10.", "Anna University Annual examination last taken result with class", ":", resultField.getText(), createCell);
            addTableRow(table, "11.", "Date on which the student left the college", ":", leavingDateField.getText(), createCell);
            addTableRow(table, "12.", "Class in which the student was studying at the time of leaving the college", ":", classLeavingField.getText(), createCell);
            addTableRow(table, "13.", "Whether qualified for promotion to the higher education", ":", qualifiedField.getText(), createCell);
            addTableRow(table, "14.", "Reason for leaving the Institution", ":", reasonField.getText(), createCell);
            addTableRow(table, "15.", "Date of issue of Transfer Certificate", ":", issueDateField.getText(), createCell);
            addTableRow(table, "16.", "Student conduct and character", ":", conductField.getText(), createCell);
            addTableRow(table, "17.", "Any other Remarks", ":", remarksField.getText(), createCell);
            addTableRow(table, "", "", "", "UMIS No: " + umisField.getText(), createCell);
        


            doc.add(table);
            doc.add(new Paragraph("\n\n\n"));
            
            Paragraph principal = new Paragraph("PRINCIPAL", bold);
            principal.setAlignment(Element.ALIGN_RIGHT);
            doc.add(principal);

            doc.close();
            JOptionPane.showMessageDialog(this, "PDF Generated successfully: " + filename);
            showNotification("PDF generated: " + filename);
                    } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "PDF Generation Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Set Nimbus Look and Feel for modern appearance
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            TransferCertificateForm form = new TransferCertificateForm();
            form.setLocationRelativeTo(null); // Center the window
            form.setVisible(true);
            
            // Show welcome message
            Timer welcomeTimer = new Timer(500, e -> {
                form.showNotification("Welcome to Transfer Certificate Management System!");
            });
            welcomeTimer.setRepeats(false);
            welcomeTimer.start();
        });
    }
}