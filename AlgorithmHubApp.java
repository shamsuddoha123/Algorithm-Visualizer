import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AlgorithmHubApp extends JFrame {

    public AlgorithmHubApp() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Algorithm Visualizer Hub");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Increased size for better layout
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout(20, 20)); // Add some padding

        getContentPane().setBackground(new Color(240, 248, 255)); // Light background

        // Header Panel
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(60, 141, 188), getWidth(), 0, new Color(40, 96, 144));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 100));

        JLabel titleLabel = new JLabel("Welcome to the Algorithm Visualizer Hub", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subtitleLabel = new JLabel("Select an algorithm to visualize its process!", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(220, 220, 220));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 2, 25, 25)); // 4 rows, 2 columns, with gaps
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50)); // Padding around buttons
        buttonsPanel.setBackground(new Color(240, 248, 255));

        // Create and add buttons for each visualizer
        addButton(buttonsPanel, "Bubble Sort Visualizer", new Color(135, 206, 250), BubbleSortVisualizer.class);
        addButton(buttonsPanel, "Heap Sort Visualizer", new Color(106, 90, 205), HeapSortVisualizer.class);
        addButton(buttonsPanel, "Merge Sort Visualizer", new Color(52, 152, 219), MergeSortVisualizer.class);
        addButton(buttonsPanel, "Quick Sort Visualizer", new Color(255, 94, 77), QuickSortVisualizer.class);
        addButton(buttonsPanel, "Prime Number Visualizer", new Color(74, 144, 226), PrimeNumberVisualizer.class);
        addButton(buttonsPanel, "Insertion and Selection Sort Visualizer", new Color(46, 204, 113), SortingVisualizerApp.class);
        addButton(buttonsPanel, "Algorithm Complexity Analyzer", new Color(155, 89, 182), AlgorithmComplexityAnalyzer.class);

        // Add an empty panel to fill the last grid cell if needed for alignment
        buttonsPanel.add(new JPanel() {{ setOpaque(false); }});

        add(buttonsPanel, BorderLayout.CENTER);
    }

    private void addButton(JPanel panel, String text, Color color, Class<? extends JFrame> visualizerClass) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(color.darker(), 2));
        button.setPreferredSize(new Dimension(300, 60)); // Fixed size for consistency
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a new instance of the visualizer and make it visible
                    JFrame visualizer = visualizerClass.getDeclaredConstructor().newInstance();
                    visualizer.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AlgorithmHubApp.this,
                            "Error launching visualizer: " + ex.getMessage(),
                            "Launch Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(button);
    }

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AlgorithmHubApp().setVisible(true);
            }
        });
    }
}