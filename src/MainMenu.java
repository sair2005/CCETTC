
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainMenu extends JFrame {
    private JButton generateTCButton, viewTCButton, logoutButton, settingsButton;
    private JLabel welcomeLabel, timeLabel, statusLabel;
    private JMenuBar menuBar;
    private JMenu fileMenu, helpMenu;
    private JMenuItem aboutMenuItem, exitMenuItem;
    private Timer clockTimer;
    private JPanel headerPanel, buttonPanel, footerPanel;

    public MainMenu() {
        initializeComponents();
        setupLayout();
        setupMenuBar();
        setupEventHandlers();
        startClock();
        
        // Window properties
        setTitle("Transfer Certificate Management System - Main Menu");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set application icon (you can replace with your own icon)
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }

    private void initializeComponents() {
        // Header components
        welcomeLabel = new JLabel("Welcome to TC Management System", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(25, 118, 210));
        
        timeLabel = new JLabel("", JLabel.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(Color.GRAY);
        
        // Main action buttons with enhanced styling
        generateTCButton = createStyledButton("Generate Transfer Certificate", 
            new Color(76, 175, 80), Color.WHITE, "Create new TC");
        
        viewTCButton = createStyledButton("View Transfer Certificates", 
            new Color(33, 150, 243), Color.WHITE, "Browse existing TCs");
        
        settingsButton = createStyledButton("Settings", 
            new Color(156, 39, 176), Color.WHITE, "Application settings");
        
        logoutButton = createStyledButton("Logout", 
            new Color(244, 67, 54), Color.WHITE, "Exit application");
        
        // Status label
        statusLabel = new JLabel("Ready", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(76, 175, 80));
        
        // Menu items
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        helpMenu = new JMenu("Help");
        aboutMenuItem = new JMenuItem("About");
        exitMenuItem = new JMenuItem("Exit");
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(300, 45));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brightenColor(bgColor));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private Color brightenColor(Color color) {
        int r = Math.min(255, color.getRed() + 30);
        int g = Math.min(255, color.getGreen() + 30);
        int b = Math.min(255, color.getBlue() + 30);
        return new Color(r, g, b);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        headerPanel.add(timeLabel, BorderLayout.SOUTH);
        
        // Button panel with better spacing
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        
        gbc.gridy = 0;
        buttonPanel.add(generateTCButton, gbc);
        
        gbc.gridy = 1;
        buttonPanel.add(viewTCButton, gbc);
        
        gbc.gridy = 2;
        buttonPanel.add(settingsButton, gbc);
        
        gbc.gridy = 3;
        buttonPanel.add(logoutButton, gbc);
        
        // Footer panel
        footerPanel = new JPanel(new FlowLayout());
        footerPanel.setBackground(new Color(238, 238, 238));
        footerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        footerPanel.add(new JLabel("Status: "));
        footerPanel.add(statusLabel);
        
        // Add panels to frame
        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void setupMenuBar() {
        // File menu
        fileMenu.add(exitMenuItem);
        
        // Help menu
        helpMenu.add(aboutMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }

    private void setupEventHandlers() {
        generateTCButton.addActionListener(e -> {
            updateStatus("Opening Transfer Certificate Form...");
            SwingUtilities.invokeLater(() -> {
                new TransferCertificateForm().setVisible(true);
                dispose();
            });
        });

        viewTCButton.addActionListener(e -> {
            updateStatus("Loading Transfer Certificates...");
            SwingUtilities.invokeLater(() -> {
                new ViewTransferCertificates().setVisible(true);
                dispose();
            });
        });
        
        settingsButton.addActionListener(e -> {
            updateStatus("Opening Settings...");
            JOptionPane.showMessageDialog(this, 
                "Settings panel will be implemented in future version.", 
                "Settings", 
                JOptionPane.INFORMATION_MESSAGE);
            updateStatus("Ready");
        });

        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                updateStatus("Logging out...");
                SwingUtilities.invokeLater(() -> {
                    new LoginPage().setVisible(true);
                    dispose();
                });
            }
        });
        
        // Menu item handlers
        exitMenuItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        aboutMenuItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "CCET Transfer Certificate Management System\n" +
                "Version 1.0\n" +
                "Developed for TC Generating\n\n" +
                "© 2025 Chendhuran College of Engineering and Technology\n" +
                "All rights reserved.",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (clockTimer != null) {
                    clockTimer.stop();
                }
            }
        });
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void setupKeyboardShortcuts() {
        // Ctrl+G for Generate TC
        KeyStroke generateKey = KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(generateKey, "generate");
        getRootPane().getActionMap().put("generate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateTCButton.doClick();
            }
        });
        
        // Ctrl+V for View TCs
        KeyStroke viewKey = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(viewKey, "view");
        getRootPane().getActionMap().put("view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewTCButton.doClick();
            }
        });
        
        // Escape for Logout
        KeyStroke logoutKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(logoutKey, "logout");
        getRootPane().getActionMap().put("logout", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logoutButton.doClick();
            }
        });
    }
    
    private void startClock() {
        clockTimer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm:ss");
            timeLabel.setText(now.format(formatter));
        });
        clockTimer.start();
        
        // Initial time display
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm:ss");
        timeLabel.setText(now.format(formatter));
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
        
        // Reset status to "Ready" after 3 seconds
        Timer statusTimer = new Timer(3000, e -> statusLabel.setText("Ready"));
        statusTimer.setRepeats(false);
        statusTimer.start();
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default look and feel
        }
        
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}