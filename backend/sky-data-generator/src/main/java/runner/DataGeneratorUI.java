package runner;

import config.DBConfig;
import generator.OrderGenerator;
import generator.UserGenerator;
import util.EnvFileLoader;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataGeneratorUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DataGeneratorUI::createAndShow);
    }

    private static void createAndShow() {
        Map<String, String> env = EnvFileLoader.loadDotEnvLocal();

        JFrame frame = new JFrame("sky-data-generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField urlField = new JTextField(firstNonBlank(env.get("SKY_DB_URL"), "jdbc:mysql://localhost:3306/sky_take_out?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8"), 46);
        JTextField usernameField = new JTextField(firstNonBlank(env.get("SKY_DB_USERNAME"), "root"), 18);
        JPasswordField passwordField = new JPasswordField(firstNonBlank(env.get("SKY_DB_PASSWORD"), ""), 18);

        JRadioButton usersOnly = new JRadioButton("只生成用户", false);
        JRadioButton ordersOnly = new JRadioButton("只生成订单", false);
        JRadioButton all = new JRadioButton("用户+订单", true);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(usersOnly);
        modeGroup.add(ordersOnly);
        modeGroup.add(all);

        JSpinner userCountSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 1000000, 1000));
        JSpinner orderCountSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 1000000, 1000));
        JSpinner batchSizeSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 20000, 100));
        JSpinner userIdLimitSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 1000));

        JCheckBox dryRunBox = new JCheckBox("DryRun(不落库)", false);
        JComboBox<String> localeCombo = new JComboBox<>(new String[]{"zh-CN", "en"});

        JTextArea outputArea = new JTextArea(18, 80);
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        JButton runButton = new JButton("开始生成");
        JButton clearButton = new JButton("清空输出");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addRow(form, c, row++, new JLabel("JDBC URL"), urlField);
        addRow(form, c, row++, new JLabel("用户名"), usernameField);
        addRow(form, c, row++, new JLabel("密码"), passwordField);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        modePanel.add(all);
        modePanel.add(usersOnly);
        modePanel.add(ordersOnly);
        addRow(form, c, row++, new JLabel("模式"), modePanel);

        addRow(form, c, row++, new JLabel("用户数量"), userCountSpinner);
        addRow(form, c, row++, new JLabel("订单数量"), orderCountSpinner);
        addRow(form, c, row++, new JLabel("批量大小"), batchSizeSpinner);
        addRow(form, c, row++, new JLabel("读取用户ID上限(仅订单)"), userIdLimitSpinner);

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        optionsPanel.add(dryRunBox);
        optionsPanel.add(new JLabel("Locale"));
        optionsPanel.add(localeCombo);
        addRow(form, c, row++, new JLabel("选项"), optionsPanel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttons.add(runButton);
        buttons.add(clearButton);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.add(form, BorderLayout.NORTH);
        root.add(buttons, BorderLayout.CENTER);
        root.add(outputScroll, BorderLayout.SOUTH);
        frame.setContentPane(root);

        clearButton.addActionListener(e -> outputArea.setText(""));

        runButton.addActionListener(e -> {
            String url = urlField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            int userCount = (Integer) userCountSpinner.getValue();
            int orderCount = (Integer) orderCountSpinner.getValue();
            int batchSize = (Integer) batchSizeSpinner.getValue();
            int userIdLimit = (Integer) userIdLimitSpinner.getValue();
            boolean dryRun = dryRunBox.isSelected();
            Locale locale = parseLocale((String) localeCombo.getSelectedItem());

            String mode = all.isSelected() ? "all" : usersOnly.isSelected() ? "users" : "orders";
            runButton.setEnabled(false);
            appendLine(outputArea, "==> mode=" + mode + ", dryRun=" + dryRun + ", userCount=" + userCount + ", orderCount=" + orderCount + ", batchSize=" + batchSize);

            new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() {
                    try {
                        if (dryRun) {
                            UserGenerator ug = new UserGenerator(locale);
                            OrderGenerator og = new OrderGenerator(locale);
                            if (mode.equals("users") || mode.equals("all")) {
                                publish("DryRun sample users: " + ug.generateSample(Math.min(3, userCount)));
                            }
                            if (mode.equals("orders")) {
                                publish("DryRun sample orders: " + og.generateSample(Math.min(3, orderCount), List.of(1L, 2L, 3L)));
                            } else if (mode.equals("all")) {
                                publish("DryRun sample orders: " + og.generateSample(Math.min(3, orderCount), List.of(1L, 2L, 3L)));
                            }
                            return null;
                        }

                        try (Connection connection = DBConfig.open(url, username, password)) {
                            if (mode.equals("users")) {
                                UserGenerator ug = new UserGenerator(locale);
                                long start = System.currentTimeMillis();
                                List<Long> userIds = ug.generateAndInsert(connection, userCount, batchSize, false);
                                long elapsed = System.currentTimeMillis() - start;
                                publish("Inserted users: " + userIds.size() + ", elapsedMs=" + elapsed);
                            } else if (mode.equals("orders")) {
                                OrderGenerator og = new OrderGenerator(locale);
                                List<Long> userIds = new java.util.ArrayList<>();
                                try (var ps = connection.prepareStatement(userIdLimit > 0 ? "select id from user order by id limit ?" : "select id from user order by id")) {
                                    if (userIdLimit > 0) {
                                        ps.setInt(1, userIdLimit);
                                    }
                                    try (var rs = ps.executeQuery()) {
                                        while (rs.next()) {
                                            userIds.add(rs.getLong(1));
                                        }
                                    }
                                }
                                if (userIds.isEmpty()) {
                                    throw new IllegalStateException("No users found in DB; generate users first");
                                }
                                long start = System.currentTimeMillis();
                                long inserted = og.generateAndInsert(connection, orderCount, batchSize, userIds, false);
                                long elapsed = System.currentTimeMillis() - start;
                                publish("Inserted orders: " + inserted + ", elapsedMs=" + elapsed);
                            } else {
                                UserGenerator ug = new UserGenerator(locale);
                                OrderGenerator og = new OrderGenerator(locale);
                                long startUsers = System.currentTimeMillis();
                                List<Long> userIds = ug.generateAndInsert(connection, userCount, batchSize, false);
                                long usersElapsed = System.currentTimeMillis() - startUsers;
                                publish("Inserted users: " + userIds.size() + ", elapsedMs=" + usersElapsed);

                                long startOrders = System.currentTimeMillis();
                                long insertedOrders = og.generateAndInsert(connection, orderCount, batchSize, userIds, false);
                                long ordersElapsed = System.currentTimeMillis() - startOrders;
                                publish("Inserted orders: " + insertedOrders + ", elapsedMs=" + ordersElapsed);
                            }
                        }

                        return null;
                    } catch (Exception ex) {
                        publish("FAILED: " + ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String s : chunks) {
                        appendLine(outputArea, s);
                    }
                }

                @Override
                protected void done() {
                    runButton.setEnabled(true);
                    try {
                        get();
                        appendLine(outputArea, "DONE");
                    } catch (Exception ex) {
                        appendLine(outputArea, "DONE(with error)");
                        JOptionPane.showMessageDialog(frame, ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        frame.pack();
        frame.setMinimumSize(new Dimension(920, 620));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void addRow(JPanel panel, GridBagConstraints c, int row, JLabel label, java.awt.Component component) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(label, c);

        c.gridx = 1;
        c.gridy = row;
        c.weightx = 1;
        panel.add(component, c);
    }

    private static void appendLine(JTextArea area, String line) {
        String ts = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        area.append("[" + ts + "] " + line + "\n");
        area.setCaretPosition(area.getDocument().getLength());
    }

    private static Locale parseLocale(String value) {
        if (value == null || value.isBlank()) {
            return Locale.getDefault();
        }
        String normalized = value.trim().replace('_', '-');
        String[] parts = normalized.split("-", 3);
        if (parts.length == 1) {
            return new Locale(parts[0]);
        }
        return new Locale(parts[0], parts[1]);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b;
    }
}

