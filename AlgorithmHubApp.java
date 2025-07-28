import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.Timer;

public class AlgorithmHubApp extends JFrame {

    public AlgorithmHubApp() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Algorithm Visualizer Hub - Your Coding Adventure Starts Here!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        // Darker warm background
        getContentPane().setBackground(new Color(245, 222, 179)); // Wheat

        // Header Panel with deeper sunset gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deeper sunset gradient - burnt orange to coral
                GradientPaint headerGradient = new GradientPaint(
                        0, 0, new Color(255, 165, 79), // Darker peach/orange
                        0, getHeight(), new Color(240, 128, 128) // Light coral
                );
                g2d.setPaint(headerGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 150));

        // Title with deeper colors
        JLabel titleLabel = new JLabel("ALGORITHM VISUALIZER HUB", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;

                // Darker brown outline
                g2d.setColor(new Color(139, 69, 19)); // Saddle brown
                g2d.drawString(text, x - 1, y - 1);
                g2d.drawString(text, x + 1, y + 1);

                // Main text in warm cream
                g2d.setColor(new Color(255, 248, 220)); // Cornsilk
                g2d.drawString(text, x, y);
            }
        };
        titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subtitleLabel = new JLabel("Choose Your Algorithm Adventure!", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        subtitleLabel.setForeground(new Color(101, 67, 33)); // Dark brown
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Buttons Panel with deeper gradient
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 2, 30, 30)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deeper gradient - rose to sage green
                GradientPaint bgGradient = new GradientPaint(
                        0, 0, new Color(219, 112, 147), // Pale violet red (deeper pink)
                        0, getHeight(), new Color(143, 188, 143) // Dark sea green
                );
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Create enhanced cartoon-style buttons with richer colors
        addEnhancedButton(buttonsPanel, "Bubble Sort", "Watch bubbles float to the top!", new Color(65, 105, 225), BubbleSortVisualizer.class); // Royal blue
        addEnhancedButton(buttonsPanel, "Heap Sort", "Build heaps like a tree master!", new Color(123, 104, 238), HeapSortVisualizer.class); // Medium slate blue
        addEnhancedButton(buttonsPanel, "Merge Sort", "Divide and conquer like a pro!", new Color(70, 130, 180), MergeSortVisualizer.class); // Steel blue
        addEnhancedButton(buttonsPanel, "Quick Sort", "Lightning fast sorting magic!", new Color(220, 20, 60), QuickSortVisualizer.class); // Crimson
        addEnhancedButton(buttonsPanel, "Prime Numbers", "Discover the secrets of primes!", new Color(255, 127, 80), PrimeNumberVisualizer.class); // Coral
        addEnhancedButton(buttonsPanel, "Insertion & Selection", "Master the sorting basics!", new Color(46, 139, 87), SortingVisualizerApp.class); // Sea green
        addEnhancedButton(buttonsPanel, "Algorithm Analyzer", "Compare algorithm performance!", new Color(199, 21, 133), AlgorithmComplexityAnalyzer.class); // Medium violet red

        // Add an empty panel with simple decoration
        JPanel decorativePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Cute character with richer colors
                // Body
                g2d.setColor(new Color(205, 92, 92)); // Indian red
                g2d.fillOval(30, 40, 40, 50);

                // Head
                g2d.setColor(new Color(222, 184, 135)); // Burlywood
                g2d.fillOval(35, 20, 30, 30);

                // Eyes
                g2d.setColor(new Color(101, 67, 33)); // Dark brown
                g2d.fillOval(42, 28, 4, 4);
                g2d.fillOval(54, 28, 4, 4);

                // Smile
                g2d.drawArc(45, 35, 10, 8, 0, -180);

                // Richer text colors
                Color[] textColors = {
                        new Color(184, 134, 11),  // Dark goldenrod
                        new Color(178, 34, 34),   // Fire brick
                        new Color(72, 61, 139)    // Dark slate blue
                };

                g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();

                String[] lines = {"More", "Coming", "Soon!"};
                int startY = 80;
                for (int i = 0; i < lines.length; i++) {
                    g2d.setColor(textColors[i]);
                    int x = (getWidth() - fm.stringWidth(lines[i])) / 2;
                    g2d.drawString(lines[i], x, startY);
                    startY += 15;
                }
            }
        };
        decorativePanel.setOpaque(false);
        buttonsPanel.add(decorativePanel);

        add(buttonsPanel, BorderLayout.CENTER);

        // Footer with deeper ocean gradient
        JPanel footerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Deeper ocean gradient - teal to dark cyan
                GradientPaint footerGradient = new GradientPaint(
                        0, 0, new Color(72, 209, 204), // Medium turquoise
                        0, getHeight(), new Color(0, 139, 139) // Dark cyan
                );
                g2d.setPaint(footerGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        footerPanel.setLayout(new FlowLayout());

        JLabel footerLabel = new JLabel("Learn • Explore • Understand • Master Algorithms!");
        footerLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);

        add(footerPanel, BorderLayout.SOUTH);
    }

    // Custom button class to handle hover state properly
    private class EnhancedButton extends JButton {
        private boolean isHovered = false;
        private Timer glowTimer;
        private float glowIntensity = 0.0f;
        private Color buttonColor;

        public EnhancedButton(String text, Color color) {
            super(text);
            this.buttonColor = color;

            // Add glow animation
            glowTimer = new Timer(50, e -> {
                if (isHovered) {
                    glowIntensity = Math.min(1.0f, glowIntensity + 0.1f);
                } else {
                    glowIntensity = Math.max(0.0f, glowIntensity - 0.1f);
                }
                repaint();
            });
            glowTimer.start();
        }

        public void setHovered(boolean hovered) {
            this.isHovered = hovered;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw glow effect
            if (glowIntensity > 0) {
                for (int i = 10; i >= 0; i--) {
                    g2d.setColor(new Color(255, 255, 255, (int)(glowIntensity * 20)));
                    g2d.fillRoundRect(-i, -i, getWidth() + 2*i, getHeight() + 2*i, 25 + i, 25 + i);
                }
            }

            // Create 3D effect with multiple gradients
            Color lightColor = buttonColor.brighter().brighter();
            Color darkColor = buttonColor.darker();

            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 20, 20);

            // Draw main button with gradient
            GradientPaint mainGradient = new GradientPaint(0, 0, lightColor, 0, getHeight(), darkColor);
            g2d.setPaint(mainGradient);
            g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);

            // Draw highlight on top
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillRoundRect(5, 5, getWidth() - 15, getHeight() / 3, 15, 15);

            // Draw border
            g2d.setColor(darkColor.darker());
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);

            // Draw text with shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.setFont(getFont());
            FontMetrics fm = g2d.getFontMetrics();
            String buttonText = getText().replaceAll("<[^>]*>", ""); // Remove HTML tags
            int textX = (getWidth() - fm.stringWidth(buttonText)) / 2 + 2;
            int textY = (getHeight() + fm.getAscent()) / 2 + 2;
            g2d.drawString(buttonText, textX, textY);

            // Draw main text
            g2d.setColor(Color.WHITE);
            g2d.drawString(buttonText, textX - 2, textY - 2);
        }
    }

    private void addEnhancedButton(JPanel panel, String text, String tooltip, Color color, Class<? extends JFrame> visualizerClass) {
        EnhancedButton button = new EnhancedButton("<html><center>" + text + "</center></html>", color);

        button.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(320, 80));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);

        // Add mouse listener for hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setHovered(false);
            }
        });

        // Add bounce effect on click
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create bounce animation
                Timer bounceTimer = new Timer(50, null);
                final int[] bounceCount = {0};

                bounceTimer.addActionListener(bounceEvent -> {
                    if (bounceCount[0] < 6) {
                        int offset = (bounceCount[0] % 2 == 0) ? -3 : 3;
                        button.setLocation(button.getX(), button.getY() + offset);
                        bounceCount[0]++;
                    } else {
                        bounceTimer.stop();
                        button.setLocation(button.getX(), button.getY()); // Reset position

                        // Launch the visualizer
                        try {
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
                bounceTimer.start();
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
