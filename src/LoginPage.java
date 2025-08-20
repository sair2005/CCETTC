import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, closeButton, forgotPasswordButton;
    private JCheckBox rememberMeCheckBox, showPasswordCheckBox;
    private JLabel statusLabel, titleLabel;
    private JProgressBar loadingBar;
    private Timer loginTimer;

    // Color Scheme
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);

    public LoginPage() {
        initializeComponents();
        setupLayout();
        addEventListeners();
        applyModernStyling();
        validateInput(); // ensure login button state is set properly on startup
    }

    private void initializeComponents() {
        setTitle("CCET TRANSFER CERTIFICATE PORTAL");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        titleLabel = new JLabel("Transfer Certificate Portal", SwingConstants.CENTER);
        usernameField = new JTextField(10);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        closeButton = new JButton("Close");
        forgotPasswordButton = new JButton("Forgot Password?");
        rememberMeCheckBox = new JCheckBox("Remember Me");
        showPasswordCheckBox = new JCheckBox("Show Password");
        statusLabel = new JLabel(" ");
        loadingBar = new JProgressBar();
        loadingBar.setVisible(false);
        loadingBar.setIndeterminate(true);

        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setFocusPainted(false);
    }

    private void setupLayout() {
        setContentPane(new GradientBackgroundPanel());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JPanel titlePanel = new JPanel(new FlowLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);

        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(loadingBar, BorderLayout.SOUTH);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new RoundedBorder(15, BACKGROUND_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setOpaque(false);
        optionsPanel.add(rememberMeCheckBox);
        optionsPanel.add(Box.createHorizontalStrut(20));
        optionsPanel.add(showPasswordCheckBox);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(usernameLabel, gbc);
        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);
        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);
        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);
        gbc.gridy = 4;
        formPanel.add(optionsPanel, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(closeButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(forgotPasswordButton);

        return buttonPanel;
    }

    private void addEventListeners() {
        loginButton.addActionListener(e -> performLogin());
        closeButton.addActionListener(e -> System.exit(0));
        forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog());

        showPasswordCheckBox.addActionListener(e ->
                passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char) 0 : '\u2022'));

        KeyAdapter enterKeyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };

        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        DocumentListener inputValidator = new DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInput(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInput(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInput(); }
        };

        usernameField.getDocument().addDocumentListener(inputValidator);
        passwordField.getDocument().addDocumentListener(inputValidator);
    }

    private void applyModernStyling() {
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        usernameField.setFont(fieldFont);
        passwordField.setFont(fieldFont);

        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        styleButton(loginButton, PRIMARY_COLOR, Color.WHITE);
        styleButton(closeButton, ERROR_COLOR, Color.WHITE);

        forgotPasswordButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordButton.setForeground(SECONDARY_COLOR);

        rememberMeCheckBox.setOpaque(false);
        showPasswordCheckBox.setOpaque(false);
        rememberMeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private void styleButton(JButton button, Color bgColor, Color textColor) {
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 35));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please fill in all fields", ERROR_COLOR);
            return;
        }

        loginButton.setEnabled(false);
        loadingBar.setVisible(true);
        showStatus("Authenticating...", PRIMARY_COLOR);

        loginTimer = new Timer(2000, e -> {
            loadingBar.setVisible(false);
            loginButton.setEnabled(true);

            if (username.equals("admin") && password.equals("Ccet@9103")) {
                showStatus("Login Successful! Welcome " + username, SUCCESS_COLOR);
                Timer delay = new Timer(1000, evt -> {
                    dispose();
                    new MainMenu().setVisible(true); // Ensure MainMenu class exists
                });
                delay.setRepeats(false);
                delay.start();
            } else {
                showStatus("Invalid credentials. Please try again.", ERROR_COLOR);
                passwordField.selectAll();
                passwordField.requestFocus();
            }
        });
        loginTimer.setRepeats(false);
        loginTimer.start();
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "Password Recovery", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel label = new JLabel("Enter your username for password recovery:");
        JTextField emailField = new JTextField(20);
        JButton sendButton = new JButton("Send Recovery Email");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(label, gbc);
        gbc.gridy = 1;
        panel.add(emailField, gbc);
        gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(sendButton, gbc);
        gbc.gridx = 1;
        panel.add(cancelButton, gbc);

        sendButton.addActionListener(e -> {
            if (!emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Recovery email sent!");
                dialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void validateInput() {
        boolean hasInput = !usernameField.getText().trim().isEmpty()
                && passwordField.getPassword().length > 0;
        loginButton.setEnabled(hasInput);
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}

// Gradient background
class GradientBackgroundPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        GradientPaint gradient = new GradientPaint(0, 0, new Color(74, 144, 226),
                0, getHeight(), new Color(180, 220, 255));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}

// Rounded Border
class RoundedBorder extends AbstractBorder {
    private int radius;
    private Color backgroundColor;

    public RoundedBorder(int radius, Color backgroundColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
        g2.setColor(new Color(189, 195, 199));
        g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
    }
}
