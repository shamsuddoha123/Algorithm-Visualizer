import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BubbleSortVisualizer extends JFrame {
    int[] array;
    int arraySize;
    JPanel arrayPanel, explanationPanel;
    JScrollPane arrayScrollPane, explanationScrollPane;
    JButton startButton, resetButton, customInputButton;
    JSlider speedSlider;
    JLabel speedLabel, statusLabel, stepLabel, passLabel;
    JTextArea explanationArea;
    Timer animationTimer;

    // Animation variables
    boolean isAnimating = false;
    int currentPass = 0;
    int currentIndex = 0;
    int totalPasses = 0;
    int currentStep = 0;
    int totalSteps = 0;
    int comparisons = 0;
    int swaps = 0;

    // Bubble animation variables
    boolean isComparing = false;
    boolean isSwapping = false;
    int compareIndex1 = -1;
    int compareIndex2 = -1;
    int swapIndex1 = -1;
    int swapIndex2 = -1;

    // Animation states
    int animationPhase = 0; // 0: moving to compare, 1: comparing, 2: swapping, 3: moving back
    double bubble1X = 0, bubble1Y = 0;
    double bubble2X = 0, bubble2Y = 0;
    double targetBubble1X = 0, targetBubble1Y = 0;
    double targetBubble2X = 0, targetBubble2Y = 0;
    double animationProgress = 0.0;
    boolean needsSwap = false;

    // Colors
    Color defaultColor = new Color(135, 206, 250); // Light sky blue
    Color compareColor = new Color(255, 215, 0); // Gold
    Color swapColor = new Color(255, 69, 0); // Red orange
    Color sortedColor = new Color(50, 205, 50); // Lime green
    Color bubbleColor = new Color(173, 216, 230); // Light blue
    Color borderColor = new Color(25, 25, 112); // Midnight blue
    Color shadowColor = new Color(0, 0, 0, 50);

    public BubbleSortVisualizer() {
        initializeGUI();
        generateRandomArray(10);
    }

    void initializeGUI() {
        setTitle("Bubble Sort Visualizer - Animated Bubbles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(new Color(240, 248, 255));

        // Header panel with bubble theme
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(135, 206, 250),
                        getWidth(), 0, new Color(70, 130, 180));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw decorative bubbles
                g2d.setColor(new Color(255, 255, 255, 100));
                for (int i = 0; i < 15; i++) {
                    int x = (i * 80 + 50) % getWidth();
                    int y = 10 + (i % 3) * 15;
                    int size = 8 + (i % 3) * 4;
                    g2d.fillOval(x, y, size, size);
                    g2d.setColor(new Color(255, 255, 255, 150));
                    g2d.fillOval(x + 2, y + 2, size/3, size/3);
                    g2d.setColor(new Color(255, 255, 255, 100));
                }
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 90));

        JLabel titleLabel = new JLabel("BUBBLE SORT VISUALIZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 15, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(new Color(240, 248, 255));
        controlPanel.setPreferredSize(new Dimension(0, 100));

        startButton = createStyledButton("Start Bubble Sort", new Color(50, 205, 50));
        resetButton = createStyledButton("Reset Array", new Color(70, 130, 180));
        customInputButton = createStyledButton("Custom Input", new Color(138, 43, 226));

        // Add back button after resetButton
        JButton backButton = createStyledButton("Back to Hub", new Color(100, 149, 237));
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (animationTimer != null) animationTimer.stop();
                dispose(); // Close this window
            }
        });

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(customInputButton);
        controlPanel.add(backButton); // Add this line
        controlPanel.add(Box.createHorizontalStrut(30));

        speedLabel = new JLabel("Animation Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        speedLabel.setForeground(new Color(25, 25, 112));

        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(new Color(240, 248, 255));
        speedSlider.setPreferredSize(new Dimension(200, 50));

        stepLabel = new JLabel("Step: 0 / 0");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stepLabel.setForeground(new Color(25, 25, 112));

        passLabel = new JLabel("Pass: 0 / 0");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(new Color(138, 43, 226));

        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(stepLabel);
        controlPanel.add(passLabel);

        // Create main content panel
        JPanel mainContentPanel = new JPanel(new BorderLayout(15, 15));
        mainContentPanel.setBackground(new Color(240, 248, 255));

        // Array visualization panel with scrolling
        arrayPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Underwater background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(176, 224, 230),
                        0, getHeight(), new Color(135, 206, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw floating bubbles in background
                drawBackgroundBubbles(g2d);

                // Draw array bubbles
                drawBubbleArray(g2d);
            }

            public Dimension getPreferredSize() {
                int width = Math.max(1000, arraySize * 90 + 200);
                int height = 500;
                return new Dimension(width, height);
            }
        };
        arrayPanel.setBackground(new Color(176, 224, 230));

        arrayScrollPane = new JScrollPane(arrayPanel);
        arrayScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        arrayScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        arrayScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
                "Bubble Array - Watch the Bubbles Float and Swap!",
                0, 0, new Font("Segoe UI", Font.BOLD, 16), new Color(70, 130, 180)
        ));

        // Right panel for status and explanation
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(new Color(240, 248, 255));
        rightPanel.setPreferredSize(new Dimension(450, 600));

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Bubble Status",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(70, 130, 180)
        ));
        statusPanel.setPreferredSize(new Dimension(450, 140));

        statusLabel = new JLabel("<html><center>Ready to bubble!<br>Click 'Start Bubble Sort' to begin<br><br>Comparisons: 0 | Swaps: 0</center></html>", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(25, 25, 112));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Explanation panel with scrolling
        explanationPanel = new JPanel(new BorderLayout());
        explanationPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 205, 50), 2),
                "Bubble Sort Algorithm Explanation",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(50, 205, 50)
        ));
        explanationPanel.setBackground(Color.WHITE);

        explanationArea = new JTextArea(25, 35);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        explanationArea.setBackground(Color.WHITE);
        explanationArea.setForeground(new Color(25, 25, 112));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setEditable(false);
        explanationArea.setMargin(new Insets(15, 15, 15, 15));

        explanationScrollPane = new JScrollPane(explanationArea);
        explanationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        explanationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        explanationPanel.add(explanationScrollPane, BorderLayout.CENTER);

        rightPanel.add(statusPanel, BorderLayout.NORTH);
        rightPanel.add(explanationPanel, BorderLayout.CENTER);

        mainContentPanel.add(arrayScrollPane, BorderLayout.CENTER);
        mainContentPanel.add(rightPanel, BorderLayout.EAST);

        // Add everything to main frame
        add(headerPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.SOUTH);

        // Event listeners
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startBubbleSort();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetArray();
            }
        });

        customInputButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCustomInputDialog();
            }
        });

        // FIX: Create timer with dynamic delay based on speed slider
        animationTimer = new Timer(getAnimationDelay(), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performBubbleSortStep();
            }
        });

        // FIX: Add change listener to speed slider to update timer delay
        speedSlider.addChangeListener(e -> {
            if (animationTimer != null) {
                animationTimer.setDelay(getAnimationDelay());
            }
        });

        updateInitialExplanation();
        setVisible(true);
    }

    // FIX: Add method to calculate animation delay based on slider value
    private int getAnimationDelay() {
        // Convert slider value (1-10) to delay (200ms-20ms)
        // Higher slider value = faster animation = lower delay
        int sliderValue = speedSlider.getValue();
        return 220 - (sliderValue * 20); // Range: 200ms (slow) to 20ms (fast)
    }

    JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add bubble effect on hover
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    void generateRandomArray(int size) {
        arraySize = size;
        array = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            array[i] = (int)(Math.random() * 99) + 1;
        }
        resetSortingVariables();
        updatePanelSizes();
        repaint();
    }

    void resetSortingVariables() {
        isAnimating = false;
        currentPass = 0;
        currentIndex = 0;
        totalPasses = arraySize - 1;
        currentStep = 0;
        totalSteps = (arraySize * (arraySize - 1)) / 2;
        comparisons = 0;
        swaps = 0;

        isComparing = false;
        isSwapping = false;
        compareIndex1 = -1;
        compareIndex2 = -1;
        swapIndex1 = -1;
        swapIndex2 = -1;
        animationPhase = 0;
        animationProgress = 0.0;
        needsSwap = false;

        statusLabel.setText("<html><center>Ready to bubble!<br>Click 'Start Bubble Sort' to begin<br><br>Comparisons: 0 | Swaps: 0</center></html>");
        stepLabel.setText("Step: 0 / " + totalSteps);
        passLabel.setText("Pass: 0 / " + totalPasses);
    }

    void updatePanelSizes() {
        arrayPanel.revalidate();
        arrayScrollPane.revalidate();
    }

    void resetArray() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        generateRandomArray(arraySize);
        startButton.setText("Start Bubble Sort");
        startButton.setEnabled(true);
        startButton.setBackground(new Color(50, 205, 50));
    }

    void showCustomInputDialog() {
        if (isAnimating) return;

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sizeLabel = new JLabel("Array Size (4-20):");
        sizeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField sizeField = new JTextField("10");

        JLabel elementsLabel = new JLabel("Elements (space-separated):");
        elementsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField elementsField = new JTextField("64 34 25 12 22 11 90 88 76 50");

        inputPanel.add(sizeLabel);
        inputPanel.add(sizeField);
        inputPanel.add(elementsLabel);
        inputPanel.add(elementsField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Custom Bubble Sort Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int size = Integer.parseInt(sizeField.getText().trim());
                if (size < 4 || size > 20) {
                    JOptionPane.showMessageDialog(this, "Size must be between 4 and 20");
                    return;
                }

                String[] elements = elementsField.getText().trim().split("\\s+");
                if (elements.length != size) {
                    JOptionPane.showMessageDialog(this, "Please enter exactly " + size + " numbers");
                    return;
                }

                arraySize = size;
                array = new int[arraySize];
                for (int i = 0; i < arraySize; i++) {
                    array[i] = Integer.parseInt(elements[i]);
                    // FIX: Allow 0 and negative numbers, adjust range
                    if (array[i] < -999 || array[i] > 999) {
                        JOptionPane.showMessageDialog(this, "❌ Numbers must be between -999 and 999");
                        return;
                    }
                }

                resetSortingVariables();
                updatePanelSizes();
                repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers");
            }
        }
    }

    void startBubbleSort() {
        if (isAnimating) return;

        isAnimating = true;
        startButton.setText("Bubbling...");
        startButton.setEnabled(false);
        startButton.setBackground(new Color(255, 69, 0));

        // FIX: Update timer delay when starting animation
        animationTimer.setDelay(getAnimationDelay());
        animationTimer.start();

        statusLabel.setText("<html><center>Bubble Sort Started!<br>Bubbles are floating up...<br><br>Comparisons: 0 | Swaps: 0</center></html>");
        updateExplanation("BUBBLE SORT STARTED!\n\n" +
                "Bubble Sort works like bubbles floating to the surface!\n" +
                "Larger bubbles (bigger numbers) will slowly float up to the top,\n" +
                "while smaller bubbles sink down.\n\n" +
                "ALGORITHM PROCESS:\n" +
                "1. Compare adjacent bubbles (elements)\n" +
                "2. If left bubble is bigger than right bubble, swap them\n" +
                "3. Continue through the array\n" +
                "4. Repeat until no more swaps are needed\n\n" +
                "ANIMATION FEATURES:\n" +
                "• Watch bubbles bounce when comparing\n" +
                "• See bubbles swap positions with smooth animation\n" +
                "• Sorted bubbles turn green and float to the top\n\n" +
                "Array size: " + arraySize + " bubbles\n" +
                "Total passes needed: " + totalPasses + "\n" +
                "Maximum comparisons: " + totalSteps + "\n\n" +
                "Let the bubbling begin!");
    }

    void performBubbleSortStep() {
        if (!isAnimating) return;

        if (animationPhase == 0) {
            // Start new comparison
            if (currentIndex < arraySize - 1 - currentPass) {
                compareIndex1 = currentIndex;
                compareIndex2 = currentIndex + 1;
                isComparing = true;
                needsSwap = array[compareIndex1] > array[compareIndex2];

                // Set up animation positions
                setupBubbleAnimation();
                animationPhase = 1;
                animationProgress = 0.0;

                comparisons++;
                currentStep++;
                stepLabel.setText("Step: " + currentStep + " / " + totalSteps);

                statusLabel.setText("<html><center>Comparing bubbles " + array[compareIndex1] +
                        " and " + array[compareIndex2] + "<br>" +
                        (needsSwap ? "💫 Bubbles will swap!" : "✅ Bubbles stay in place") +
                        "<br><br>Comparisons: " + comparisons + " | Swaps: " + swaps + "</center></html>");

                updateExplanation("COMPARING BUBBLES\n\n" +
                        "Pass " + (currentPass + 1) + " of " + totalPasses + "\n" +
                        "Comparing position " + compareIndex1 + " and " + compareIndex2 + "\n\n" +
                        "Bubble values:\n" +
                        "• Left bubble: " + array[compareIndex1] + "\n" +
                        "• Right bubble: " + array[compareIndex2] + "\n\n" +
                        "COMPARISON RESULT:\n" +
                        (needsSwap ?
                                "Left bubble (" + array[compareIndex1] + ") > Right bubble (" + array[compareIndex2] + ")\n" +
                                        "SWAP NEEDED! The bigger bubble will float up!" :
                                "Left bubble (" + array[compareIndex1] + ") ≤ Right bubble (" + array[compareIndex2] + ")\n" +
                                        "NO SWAP NEEDED! Bubbles are in correct order.") + "\n\n" +
                        "Watch the bubble animation...\n\n" +
                        "Statistics:\n" +
                        "• Comparisons so far: " + comparisons + "\n" +
                        "• Swaps so far: " + swaps + "\n" +
                        "• Progress: " + String.format("%.1f", (currentStep * 100.0 / totalSteps)) + "%");
            } else {
                // End of current pass
                currentPass++;
                currentIndex = 0;
                passLabel.setText("Pass: " + currentPass + " / " + totalPasses);

                if (currentPass >= totalPasses) {
                    // Sorting complete
                    animationTimer.stop();
                    isAnimating = false;
                    startButton.setText("Completed");
                    startButton.setBackground(new Color(50, 205, 50));
                    statusLabel.setText("<html><center>Bubble Sort Complete!<br>All bubbles are sorted!<br><br>Comparisons: " +
                            comparisons + " | Swaps: " + swaps + "</center></html>");
                    updateExplanation("BUBBLE SORT COMPLETED!\n\n" +
                            "Congratulations! All bubbles have found their correct positions!\n\n" +
                            "FINAL STATISTICS:\n" +
                            "• Total comparisons: " + comparisons + "\n" +
                            "• Total swaps: " + swaps + "\n" +
                            "• Total passes: " + currentPass + "\n" +
                            "• Array size: " + arraySize + " elements\n\n" +
                            "PERFORMANCE ANALYSIS:\n" +
                            "• Time Complexity: O(n²) - quadratic time\n" +
                            "• Space Complexity: O(1) - constant space\n" +
                            "• Stable: Yes - equal elements maintain relative order\n" +
                            "• In-place: Yes - sorts within the original array\n\n" +
                            "BUBBLE SORT CHARACTERISTICS:\n" +
                            "• Simple to understand and implement\n" +
                            "• Good for educational purposes\n" +
                            "• Inefficient for large datasets\n" +
                            "• Best case: O(n) when array is already sorted\n" +
                            "• Worst case: O(n²) when array is reverse sorted\n\n" +
                            "The bubbles have successfully floated to their correct positions!\n" +
                            "Larger values (bigger bubbles) are now at the top,\n" +
                            "and smaller values (smaller bubbles) are at the bottom.");

                    // Reset animation variables
                    isComparing = false;
                    isSwapping = false;
                    compareIndex1 = -1;
                    compareIndex2 = -1;
                    repaint();
                    return;
                } else {
                    statusLabel.setText("<html><center>Pass " + currentPass + " completed!<br>Starting pass " +
                            (currentPass + 1) + "...<br><br>Comparisons: " + comparisons + " | Swaps: " + swaps + "</center></html>");
                }
            }
        } else if (animationPhase == 1) {
            // Animate comparison (bubbles moving up)
            animationProgress += 0.1;
            if (animationProgress >= 1.0) {
                animationProgress = 1.0;
                if (needsSwap) {
                    animationPhase = 2; // Move to swap phase
                    animationProgress = 0.0;
                } else {
                    animationPhase = 3; // Move to return phase
                    animationProgress = 0.0;
                }
            }
        } else if (animationPhase == 2) {
            // Animate swap (bubbles crossing over)
            animationProgress += 0.08;
            if (animationProgress >= 1.0) {
                animationProgress = 1.0;
                // Perform actual swap
                int temp = array[compareIndex1];
                array[compareIndex1] = array[compareIndex2];
                array[compareIndex2] = temp;
                swaps++;
                animationPhase = 3; // Move to return phase
                animationProgress = 0.0;
            }
        } else if (animationPhase == 3) {
            // Animate return to normal position
            animationProgress += 0.12;
            if (animationProgress >= 1.0) {
                animationProgress = 1.0;
                // Reset animation state
                isComparing = false;
                isSwapping = false;
                compareIndex1 = -1;
                compareIndex2 = -1;
                animationPhase = 0;
                currentIndex++;
            }
        }

        repaint();
    }

    void setupBubbleAnimation() {
        int panelWidth = arrayPanel.getWidth();
        int bubbleSize = Math.min(70, (panelWidth - 100) / arraySize);
        int totalWidth = arraySize * bubbleSize + (arraySize - 1) * 10;
        int startX = (panelWidth - totalWidth) / 2;
        int baseY = arrayPanel.getHeight() / 2;

        // Calculate base positions
        bubble1X = startX + compareIndex1 * (bubbleSize + 10) + bubbleSize / 2;
        bubble1Y = baseY;
        bubble2X = startX + compareIndex2 * (bubbleSize + 10) + bubbleSize / 2;
        bubble2Y = baseY;

        // Set target positions for animation
        targetBubble1X = bubble1X;
        targetBubble1Y = baseY - 30; // Float up for comparison
        targetBubble2X = bubble2X;
        targetBubble2Y = baseY - 30; // Float up for comparison
    }

    void drawBackgroundBubbles(Graphics2D g2d) {
        // Draw floating background bubbles for atmosphere
        g2d.setColor(new Color(255, 255, 255, 80));
        for (int i = 0; i < 20; i++) {
            int x = (i * 67 + 30) % arrayPanel.getWidth();
            int y = (i * 43 + 50) % arrayPanel.getHeight();
            int size = 5 + (i % 4) * 3;

            // Bubble body
            g2d.fillOval(x, y, size, size);

            // Bubble highlight
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillOval(x + 1, y + 1, size/3, size/3);
            g2d.setColor(new Color(255, 255, 255, 80));
        }
    }

    void drawBubbleArray(Graphics2D g2d) {
        if (array == null) return;

        int panelWidth = arrayPanel.getWidth();
        int panelHeight = arrayPanel.getHeight();
        int bubbleSize = Math.min(70, (panelWidth - 100) / arraySize);
        int totalWidth = arraySize * bubbleSize + (arraySize - 1) * 10;
        int startX = (panelWidth - totalWidth) / 2;
        int baseY = panelHeight / 2;

        // Draw title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2d.setColor(new Color(25, 25, 112));
        String title = "Bubble Sort - Watch the Bubbles Float and Swap!";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (panelWidth - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 40);

        // Draw array bubbles
        for (int i = 0; i < arraySize; i++) {
            int x = startX + i * (bubbleSize + 10);
            int y = baseY;

            // Determine if this bubble is sorted (in final position)
            boolean isSorted = isAnimating && (i >= arraySize - currentPass);

            // Skip drawing if this bubble is being animated
            if ((i == compareIndex1 || i == compareIndex2) && (isComparing || isSwapping)) {
                continue;
            }

            drawBubble(g2d, x, y, bubbleSize, array[i], isSorted, false, false);
        }

        // Draw animated bubbles
        if ((isComparing || isSwapping) && compareIndex1 >= 0 && compareIndex2 >= 0) {
            drawAnimatedBubbles(g2d, bubbleSize);
        }

        // Draw legend
        drawBubbleLegend(g2d);
    }

    void drawAnimatedBubbles(Graphics2D g2d, int bubbleSize) {
        // Calculate current positions based on animation phase and progress
        double currentBubble1X = bubble1X;
        double currentBubble1Y = bubble1Y;
        double currentBubble2X = bubble2X;
        double currentBubble2Y = bubble2Y;

        if (animationPhase == 1) {
            // Moving up for comparison
            double easeProgress = easeInOutQuad(animationProgress);
            currentBubble1Y = bubble1Y + (targetBubble1Y - bubble1Y) * easeProgress;
            currentBubble2Y = bubble2Y + (targetBubble2Y - bubble2Y) * easeProgress;
        } else if (animationPhase == 2 && needsSwap) {
            // Swapping positions
            double easeProgress = easeInOutQuad(animationProgress);
            currentBubble1X = bubble1X + (bubble2X - bubble1X) * easeProgress;
            currentBubble2X = bubble2X + (bubble1X - bubble2X) * easeProgress;
            currentBubble1Y = targetBubble1Y;
            currentBubble2Y = targetBubble2Y;
        } else if (animationPhase == 3) {
            // Moving back to base position
            double easeProgress = easeInOutQuad(animationProgress);
            if (needsSwap) {
                // Return to swapped positions
                currentBubble1X = bubble2X;
                currentBubble2X = bubble1X;
            }
            currentBubble1Y = targetBubble1Y + (bubble1Y - targetBubble1Y) * easeProgress;
            currentBubble2Y = targetBubble2Y + (bubble2Y - targetBubble2Y) * easeProgress;
        }

        // Draw the animated bubbles
        boolean isSorted1 = compareIndex1 >= arraySize - currentPass;
        boolean isSorted2 = compareIndex2 >= arraySize - currentPass;

        drawBubble(g2d, (int)currentBubble1X - bubbleSize/2, (int)currentBubble1Y - bubbleSize/2,
                bubbleSize, array[compareIndex1], isSorted1, true, needsSwap);
        drawBubble(g2d, (int)currentBubble2X - bubbleSize/2, (int)currentBubble2Y - bubbleSize/2,
                bubbleSize, array[compareIndex2], isSorted2, true, needsSwap);
    }

    void drawBubble(Graphics2D g2d, int x, int y, int size, int value, boolean isSorted, boolean isAnimated, boolean willSwap) {
        // Determine bubble color
        Color bubbleColor;
        if (isSorted) {
            bubbleColor = sortedColor;
        } else if (isAnimated) {
            bubbleColor = willSwap ? swapColor : compareColor;
        } else {
            bubbleColor = defaultColor;
        }

        // Draw shadow
        g2d.setColor(shadowColor);
        g2d.fillOval(x + 3, y + 3, size, size);

        // Draw bubble body with gradient
        GradientPaint gradient = new GradientPaint(x, y, bubbleColor.brighter(),
                x + size, y + size, bubbleColor.darker());
        g2d.setPaint(gradient);
        g2d.fillOval(x, y, size, size);

        // Draw bubble border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y, size, size);

        // Draw bubble highlight (makes it look more like a bubble)
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillOval(x + size/4, y + size/6, size/4, size/4);

        // Draw smaller highlight
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.fillOval(x + size/6, y + size/4, size/8, size/8);

        // Draw value in the bubble
        g2d.setColor(isSorted || isAnimated ? Color.WHITE : new Color(25, 25, 112));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, size/4)));
        String text = String.valueOf(value);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (size - fm.stringWidth(text)) / 2;
        int textY = y + (size + fm.getAscent()) / 2 - 2;
        g2d.drawString(text, textX, textY);

        // Draw floating effect for animated bubbles
        if (isAnimated) {
            g2d.setColor(new Color(255, 255, 255, 100));
            for (int i = 0; i < 3; i++) {
                int bubbleX = x + size + 5 + i * 8;
                int bubbleY = y + i * 10;
                int bubbleSize = 4 - i;
                g2d.fillOval(bubbleX, bubbleY, bubbleSize, bubbleSize);
            }
        }
    }

    double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    void drawBubbleLegend(Graphics2D g2d) {
        String[] labels = {"Unsorted Bubble", "Comparing", "Swapping", "Sorted Bubble"};
        Color[] colors = {defaultColor, compareColor, swapColor, sortedColor};

        int legendX = 20;
        int legendY = 80;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.setColor(new Color(25, 25, 112));

        legendY += 20;
        for (int i = 0; i < labels.length; i++) {
            // Draw mini bubble
            drawBubble(g2d, legendX, legendY - 10, 20, 0, false, false, false);
            g2d.setColor(colors[i]);
            g2d.fillOval(legendX + 2, legendY - 8, 16, 16);

            // Draw label
            g2d.setColor(new Color(25, 25, 112));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.drawString(labels[i], legendX + 30, legendY);

            legendY += 25;
        }
    }

    void updateInitialExplanation() {
        updateExplanation("BUBBLE SORT ALGORITHM\n\n" +
                "Bubble Sort is like watching bubbles float to the surface of water!\n" +
                "It's one of the simplest sorting algorithms to understand.\n\n" +
                "HOW IT WORKS:\n" +
                "Just like real bubbles, larger elements (bigger bubbles) gradually\n" +
                "float up to the top, while smaller elements sink down.\n\n" +
                "ALGORITHM STEPS:\n" +
                "1. Compare adjacent elements (bubbles)\n" +
                "2. If the left element is larger than the right, swap them\n" +
                "3. Continue through the entire array\n" +
                "4. Repeat until no more swaps are needed\n\n" +
                "BUBBLE ANIMATION:\n" +
                "• Bubbles float up when being compared\n" +
                "• Bubbles bounce and swap positions when needed\n" +
                "• Sorted bubbles turn green and stay at the top\n" +
                "• Background bubbles create underwater atmosphere\n\n" +
                "PERFORMANCE:\n" +
                "• Time Complexity: O(n²) - quadratic time\n" +
                "• Space Complexity: O(1) - constant space\n" +
                "• Stable: Yes - maintains order of equal elements\n" +
                "• In-place: Yes - sorts within original array\n\n" +
                "CHARACTERISTICS:\n" +
                "• Simple and intuitive\n" +
                "• Good for learning sorting concepts\n" +
                "• Inefficient for large datasets\n" +
                "• Best for small arrays or educational purposes\n\n" +
                "INTERESTING FACTS:\n" +
                "• Named 'Bubble Sort' because elements bubble up\n" +
                "• Can detect if array is already sorted\n" +
                "• Each pass guarantees one element in final position\n\n" +
                "Click 'Start Bubble Sort' to watch the bubbles dance!\n" +
                "Enjoy the smooth animations as bubbles find their way home.");
    }

    void updateExplanation(String text) {
        explanationArea.setText(text);
        explanationArea.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BubbleSortVisualizer();
            }
        });
    }
}
