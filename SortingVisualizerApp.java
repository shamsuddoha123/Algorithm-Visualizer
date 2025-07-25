import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SortingVisualizerApp extends JFrame {
    int[] array;
    int arraySize;
    JPanel arrayPanel, explanationPanel;
    JButton startButton, resetButton, customInputButton;
    JSlider speedSlider;
    JLabel speedLabel, statusLabel, stepLabel;
    JTextArea explanationArea;
    JTabbedPane algorithmTabs;
    Timer animationTimer;

    // Algorithm selection
    String currentAlgorithm = "Insertion Sort";

    // Animation variables - Insertion Sort
    int insertionCurrentIndex = 1;
    int insertionCompareIndex = 0;
    int insertionKeyValue;
    boolean insertionIsComparing = false;
    boolean insertionIsShifting = false;

    // Animation variables - Selection Sort
    int selectionCurrentIndex = 0;
    int selectionMinIndex = 0;
    int selectionCompareIndex = 0;
    boolean selectionFindingMin = false;
    boolean selectionSwapping = false;

    // Common animation variables
    boolean isAnimating = false;
    int animationStep = 0;
    int totalSteps = 0;
    int currentStep = 0;

    // Enhanced colors
    Color defaultColor = new Color(240, 248, 255);
    Color currentColor = new Color(46, 204, 113);
    Color compareColor = new Color(231, 76, 60);
    Color sortedColor = new Color(52, 152, 219);
    Color minColor = new Color(155, 89, 182);
    Color borderColor = new Color(44, 62, 80);
    Color backgroundGradient1 = new Color(74, 144, 226);
    Color backgroundGradient2 = new Color(46, 204, 113);

    public SortingVisualizerApp() {
        initializeGUI();
        generateRandomArray(8);
    }

    void initializeGUI() {
        setTitle("Sorting Algorithms Visualizer - Interactive Learning Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(new Color(236, 240, 241));

        // Header panel with gradient background
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, backgroundGradient1, getWidth(), 0, backgroundGradient2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));

        JLabel titleLabel = new JLabel("SORTING ALGORITHMS VISUALIZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Algorithm selection tabs
        algorithmTabs = new JTabbedPane();
        algorithmTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        algorithmTabs.setBackground(new Color(236, 240, 241));

        JPanel insertionPanel = new JPanel();
        insertionPanel.setBackground(new Color(236, 240, 241));
        insertionPanel.add(new JLabel("Insertion Sort Algorithm"));

        JPanel selectionPanel = new JPanel();
        selectionPanel.setBackground(new Color(236, 240, 241));
        selectionPanel.add(new JLabel("Selection Sort Algorithm"));

        algorithmTabs.addTab("Insertion Sort", insertionPanel);
        algorithmTabs.addTab("Selection Sort", selectionPanel);

        algorithmTabs.addChangeListener(e -> {
            int selectedIndex = algorithmTabs.getSelectedIndex();
            currentAlgorithm = selectedIndex == 0 ? "Insertion Sort" : "Selection Sort";
            resetArray();
            updateAlgorithmExplanation();
        });

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(236, 240, 241));

        startButton = createStyledButton("Start Sort", new Color(46, 204, 113));
        resetButton = createStyledButton("Reset", new Color(52, 152, 219));
        customInputButton = createStyledButton("Custom Input", new Color(155, 89, 182));

        speedLabel = new JLabel("Animation Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        speedLabel.setForeground(new Color(44, 62, 80));

        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(2);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(new Color(236, 240, 241));
        speedSlider.setForeground(new Color(44, 62, 80));

        stepLabel = new JLabel("Step: 0 / 0");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        stepLabel.setForeground(new Color(44, 62, 80));

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(customInputButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(stepLabel);

        // Array panel
        arrayPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(248, 249, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                drawArray(g2d);
            }
        };
        arrayPanel.setPreferredSize(new Dimension(900, 300));
        arrayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Status and explanation panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(236, 240, 241));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Ready to sort - Select an algorithm and click 'Start Sort'!", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(44, 62, 80));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        explanationPanel = new JPanel(new BorderLayout());
        explanationPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Step-by-Step Explanation",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(52, 152, 219)
        ));
        explanationPanel.setBackground(Color.WHITE);

        explanationArea = new JTextArea(4, 50);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        explanationArea.setBackground(Color.WHITE);
        explanationArea.setForeground(new Color(44, 62, 80));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(explanationArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        explanationPanel.add(scrollPane, BorderLayout.CENTER);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(explanationPanel, BorderLayout.CENTER);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(algorithmTabs, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(arrayPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.EAST);

        // Event listeners
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startSorting();
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
                if (currentAlgorithm.equals("Insertion Sort")) {
                    performInsertionSortStep();
                } else {
                    performSelectionSortStep();
                }
            }
        });

        // FIX: Add change listener to speed slider to update timer delay in real-time
        speedSlider.addChangeListener(e -> {
            if (animationTimer != null) {
                animationTimer.setDelay(getAnimationDelay());
            }
        });

        updateAlgorithmExplanation();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // FIX: Add method to calculate animation delay based on slider value
    private int getAnimationDelay() {
        // Convert slider value (1-10) to delay (1000ms-100ms)
        // Higher slider value = faster animation = lower delay
        int sliderValue = speedSlider.getValue();
        return 1100 - (sliderValue * 100); // Range: 1000ms (slow) to 100ms (fast)
    }

    JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
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
        calculateTotalSteps();
        resetSortingVariables();
        repaint();
    }

    void calculateTotalSteps() {
        if (currentAlgorithm.equals("Insertion Sort")) {
            totalSteps = 0;
            for (int i = 1; i < arraySize; i++) {
                totalSteps += i;
            }
            totalSteps = totalSteps / 2;
        } else {
            // Selection sort: n-1 passes, each with n-i comparisons
            totalSteps = 0;
            for (int i = 0; i < arraySize - 1; i++) {
                totalSteps += (arraySize - i - 1) + 1; // comparisons + swap
            }
        }
    }

    void resetSortingVariables() {
        // Reset insertion sort variables
        insertionCurrentIndex = 1;
        insertionCompareIndex = 0;
        insertionKeyValue = 0;
        insertionIsComparing = false;
        insertionIsShifting = false;

        // Reset selection sort variables
        selectionCurrentIndex = 0;
        selectionMinIndex = 0;
        selectionCompareIndex = 0;
        selectionFindingMin = false;
        selectionSwapping = false;

        // Reset common variables
        isAnimating = false;
        animationStep = 0;
        currentStep = 0;

        statusLabel.setText("Ready to sort - Select an algorithm and click 'Start Sort'!");
        stepLabel.setText("Step: 0 / " + totalSteps);
    }

    void resetArray() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        generateRandomArray(arraySize);
        startButton.setText("Start Sort");
        startButton.setEnabled(true);
        startButton.setBackground(new Color(46, 204, 113));
    }

    void showCustomInputDialog() {
        if (isAnimating) return;

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sizeLabel = new JLabel("Array Size (3-12):");
        JTextField sizeField = new JTextField("8");
        JLabel elementsLabel = new JLabel("Elements (space-separated):");
        JTextField elementsField = new JTextField("4 3 2 10 12 1 5 6");

        inputPanel.add(sizeLabel);
        inputPanel.add(sizeField);
        inputPanel.add(elementsLabel);
        inputPanel.add(elementsField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Custom Array Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int size = Integer.parseInt(sizeField.getText().trim());
                if (size < 3 || size > 12) {
                    JOptionPane.showMessageDialog(this, "Size must be between 3 and 12");
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

                calculateTotalSteps();
                resetSortingVariables();
                repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers");
            }
        }
    }

    void startSorting() {
        if (isAnimating) return;

        isAnimating = true;
        startButton.setText("⏸ Sorting...");
        startButton.setEnabled(false);
        startButton.setBackground(new Color(231, 76, 60));

        // FIX: Update timer delay when starting animation
        animationTimer.setDelay(getAnimationDelay());
        animationTimer.start();

        statusLabel.setText("Starting " + currentAlgorithm + " algorithm...");

        if (currentAlgorithm.equals("Insertion Sort")) {
            updateExplanation("INSERTION SORT STARTED!\n\n" +
                    "We begin with the second element (index 1) as the first element is considered already sorted. " +
                    "The algorithm will pick each element and find its correct position in the sorted portion.");
        } else {
            updateExplanation("SELECTION SORT STARTED!\n\n" +
                    "Selection sort works by finding the minimum element in the unsorted portion and swapping it " +
                    "with the first element of the unsorted portion. We'll repeat this process for each position.");
        }
    }

    void performInsertionSortStep() {
        currentStep++;
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);

        if (insertionCurrentIndex >= arraySize) {
            completeSorting();
            return;
        }

        if (!insertionIsComparing && !insertionIsShifting) {
            insertionKeyValue = array[insertionCurrentIndex];
            insertionCompareIndex = insertionCurrentIndex - 1;
            insertionIsComparing = true;
            statusLabel.setText("Selecting element " + insertionKeyValue + " at position " + insertionCurrentIndex);
            updateExplanation("SELECTING KEY ELEMENT\n\n" +
                    "Current element: " + insertionKeyValue + " (at index " + insertionCurrentIndex + ")\n" +
                    "This element needs to be inserted into the correct position in the sorted portion " +
                    "(indices 0 to " + (insertionCurrentIndex-1) + "). We'll compare it with elements from right to left.");
        }

        if (insertionIsComparing) {
            if (insertionCompareIndex >= 0 && array[insertionCompareIndex] > insertionKeyValue) {
                insertionIsComparing = false;
                insertionIsShifting = true;
                statusLabel.setText("Comparing: " + array[insertionCompareIndex] + " > " + insertionKeyValue + " → Shift right");
                updateExplanation("COMPARISON & SHIFTING\n\n" +
                        "Comparing: " + array[insertionCompareIndex] + " with key " + insertionKeyValue + "\n" +
                        "Since " + array[insertionCompareIndex] + " > " + insertionKeyValue + ", we shift " +
                        array[insertionCompareIndex] + " one position right to make space.");
            } else {
                array[insertionCompareIndex + 1] = insertionKeyValue;
                insertionCurrentIndex++;
                insertionIsComparing = false;
                statusLabel.setText("Inserted " + insertionKeyValue + " at position " + (insertionCompareIndex + 1));
                updateExplanation("INSERTION COMPLETED\n\n" +
                        "Element " + insertionKeyValue + " inserted at index " + (insertionCompareIndex + 1) + ".\n" +
                        "The sorted portion now extends from index 0 to " + (insertionCurrentIndex-1) + ".");
            }
        }

        if (insertionIsShifting) {
            array[insertionCompareIndex + 1] = array[insertionCompareIndex];
            insertionCompareIndex--;
            insertionIsShifting = false;
            insertionIsComparing = true;
        }

        repaint();
    }

    void performSelectionSortStep() {
        currentStep++;
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);

        if (selectionCurrentIndex >= arraySize - 1) {
            completeSorting();
            return;
        }

        if (!selectionFindingMin && !selectionSwapping) {
            selectionMinIndex = selectionCurrentIndex;
            selectionCompareIndex = selectionCurrentIndex + 1;
            selectionFindingMin = true;
            statusLabel.setText("Finding minimum in unsorted portion starting from index " + selectionCurrentIndex);
            updateExplanation("FINDING MINIMUM ELEMENT\n\n" +
                    "Pass " + (selectionCurrentIndex + 1) + ": Looking for the minimum element in the unsorted portion " +
                    "(indices " + selectionCurrentIndex + " to " + (arraySize-1) + ").\n" +
                    "Current minimum candidate: " + array[selectionMinIndex] + " at index " + selectionMinIndex);
        }

        if (selectionFindingMin) {
            if (selectionCompareIndex < arraySize) {
                if (array[selectionCompareIndex] < array[selectionMinIndex]) {
                    selectionMinIndex = selectionCompareIndex;
                    statusLabel.setText("New minimum found: " + array[selectionMinIndex] + " at index " + selectionMinIndex);
                    updateExplanation("NEW MINIMUM FOUND!\n\n" +
                            "Comparing " + array[selectionCompareIndex] + " with current minimum " + array[selectionMinIndex] + "\n" +
                            "Since " + array[selectionCompareIndex] + " < " + array[selectionMinIndex] + ", we update our minimum to " +
                            array[selectionMinIndex] + " at index " + selectionMinIndex);
                } else {
                    statusLabel.setText("Comparing: " + array[selectionCompareIndex] + " >= " + array[selectionMinIndex] + " → Continue");
                    updateExplanation("COMPARISON\n\n" +
                            "Comparing " + array[selectionCompareIndex] + " with current minimum " + array[selectionMinIndex] + "\n" +
                            "Since " + array[selectionCompareIndex] + " >= " + array[selectionMinIndex] + ", the minimum remains " +
                            array[selectionMinIndex] + " at index " + selectionMinIndex);
                }
                selectionCompareIndex++;
            } else {
                selectionFindingMin = false;
                selectionSwapping = true;
                statusLabel.setText("Swapping minimum " + array[selectionMinIndex] + " with " + array[selectionCurrentIndex]);
                updateExplanation("SWAPPING ELEMENTS\n\n" +
                        "Minimum element found: " + array[selectionMinIndex] + " at index " + selectionMinIndex + "\n" +
                        "Swapping it with element " + array[selectionCurrentIndex] + " at index " + selectionCurrentIndex +
                        " to place the minimum in its correct sorted position.");
            }
        }

        if (selectionSwapping) {
            // Perform swap
            int temp = array[selectionCurrentIndex];
            array[selectionCurrentIndex] = array[selectionMinIndex];
            array[selectionMinIndex] = temp;

            selectionCurrentIndex++;
            selectionSwapping = false;
            statusLabel.setText("Swap completed. Position " + (selectionCurrentIndex-1) + " is now sorted.");
            updateExplanation("SWAP COMPLETED\n\n" +
                    "Elements successfully swapped! Position " + (selectionCurrentIndex-1) + " now contains the " +
                    (selectionCurrentIndex) + getOrdinalSuffix(selectionCurrentIndex) + " smallest element.\n" +
                    "Sorted portion: indices 0 to " + (selectionCurrentIndex-1) + "\n" +
                    "Moving to next position...");
        }

        repaint();
    }

    String getOrdinalSuffix(int number) {
        if (number >= 11 && number <= 13) return "th";
        switch (number % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    void completeSorting() {
        animationTimer.stop();
        isAnimating = false;
        startButton.setText("✅ Completed");
        startButton.setBackground(new Color(46, 204, 113));
        statusLabel.setText(currentAlgorithm + " completed successfully!");
        updateExplanation("SORTING COMPLETED!\n\n" +
                "Congratulations! The " + currentAlgorithm.toLowerCase() + " algorithm has successfully sorted the array. " +
                "Every element is now in its correct position. " +
                (currentAlgorithm.equals("Insertion Sort") ?
                        "We built the sorted portion one element at a time." :
                        "We found the minimum element for each position."));
        repaint();
    }

    void updateAlgorithmExplanation() {
        if (currentAlgorithm.equals("Insertion Sort")) {
            updateExplanation("INSERTION SORT ALGORITHM\n\n" +
                    "Insertion Sort builds a sorted portion one element at a time. It takes each element from " +
                    "the unsorted portion and inserts it into the correct position in the sorted portion.\n\n" +
                    "Time Complexity: O(n²)\nSpace Complexity: O(1)\nStable: Yes\n\n" +
                    "Click 'Start Sort' to see it in action!");
        } else {
            updateExplanation("SELECTION SORT ALGORITHM\n\n" +
                    "Selection Sort works by repeatedly finding the minimum element from the unsorted portion " +
                    "and swapping it with the first element of the unsorted portion.\n\n" +
                    "Time Complexity: O(n²)\nSpace Complexity: O(1)\nStable: No\n\n" +
                    "Click 'Start Sort' to see it in action!");
        }
    }

    void updateExplanation(String text) {
        explanationArea.setText(text);
        explanationArea.setCaretPosition(0);
    }

    void drawArray(Graphics2D g2d) {
        if (array == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = arrayPanel.getWidth();
        int panelHeight = arrayPanel.getHeight();
        int boxWidth = Math.min(80, (panelWidth - 60) / arraySize);
        int boxHeight = 60;
        int startX = (panelWidth - (boxWidth * arraySize + 15 * (arraySize - 1))) / 2;
        int startY = (panelHeight - boxHeight) / 2 - 20;

        // Draw title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        String title = currentAlgorithm + " Step-by-Step Visualization";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (panelWidth - fm.stringWidth(title)) / 2;

        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.drawString(title, titleX + 2, 35 + 2);
        g2d.setColor(new Color(44, 62, 80));
        g2d.drawString(title, titleX, 35);

        // Draw legend
        drawLegend(g2d, startX, startY - 40);

        // Draw array elements
        for (int i = 0; i < arraySize; i++) {
            int x = startX + i * (boxWidth + 15);
            int y = startY;

            Color boxColor = defaultColor;
            Color textColor = new Color(44, 62, 80);
            boolean isHighlighted = false;

            if (isAnimating) {
                if (currentAlgorithm.equals("Insertion Sort")) {
                    if (i < insertionCurrentIndex - 1) {
                        boxColor = sortedColor;
                        textColor = Color.WHITE;
                    } else if (i == insertionCurrentIndex) {
                        boxColor = currentColor;
                        textColor = Color.WHITE;
                        isHighlighted = true;
                    } else if (insertionIsComparing && i == insertionCompareIndex) {
                        boxColor = compareColor;
                        textColor = Color.WHITE;
                        isHighlighted = true;
                    } else if (insertionIsShifting && i == insertionCompareIndex + 1) {
                        boxColor = compareColor;
                        textColor = Color.WHITE;
                        isHighlighted = true;
                    }
                } else { // Selection Sort
                    if (i < selectionCurrentIndex) {
                        boxColor = sortedColor;
                        textColor = Color.WHITE;
                    } else if (i == selectionMinIndex && selectionFindingMin) {
                        boxColor = minColor;
                        textColor = Color.WHITE;
                        isHighlighted = true;
                    } else if (i == selectionCompareIndex && selectionFindingMin) {
                        boxColor = compareColor;
                        textColor = Color.WHITE;
                        isHighlighted = true;
                    } else if ((i == selectionCurrentIndex || i == selectionMinIndex) && selectionSwapping) {
                        boxColor = currentColor;
                        textColor = Color.WHITE;
                        isHighlighted = true;
                    }
                }
            }

            // Draw shadow for highlighted elements
            if (isHighlighted) {
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(x + 3, y + 3, boxWidth, boxHeight, 15, 15);
            }

            // Draw main box with gradient
            GradientPaint gradient = new GradientPaint(x, y, boxColor, x, y + boxHeight, boxColor.darker());
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);

            // Draw border
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);

            // Draw number
            g2d.setColor(textColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String text = String.valueOf(array[i]);
            FontMetrics textFm = g2d.getFontMetrics();
            int textX = x + (boxWidth - textFm.stringWidth(text)) / 2;
            int textY = y + (boxHeight + textFm.getAscent()) / 2 - 2;
            g2d.drawString(text, textX, textY);

            // Draw index
            g2d.setColor(new Color(127, 140, 141));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String indexText = "idx: " + i;
            FontMetrics indexFm = g2d.getFontMetrics();
            int indexX = x + (boxWidth - indexFm.stringWidth(indexText)) / 2;
            g2d.drawString(indexText, indexX, y + boxHeight + 15);
        }

        // Draw arrows and indicators
        if (isAnimating) {
            if (currentAlgorithm.equals("Insertion Sort") && insertionIsShifting && insertionCompareIndex >= 0) {
                drawShiftArrow(g2d, startX, startY, boxWidth, boxHeight);
            } else if (currentAlgorithm.equals("Selection Sort") && selectionSwapping) {
                drawSwapArrow(g2d, startX, startY, boxWidth, boxHeight);
            }

            drawSortedIndicator(g2d, startX, startY, boxWidth, boxHeight);
        }
    }

    void drawLegend(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        String[] labels;
        Color[] colors;

        if (currentAlgorithm.equals("Insertion Sort")) {
            labels = new String[]{"Sorted", "Current", "Comparing", "Unsorted"};
            colors = new Color[]{sortedColor, currentColor, compareColor, defaultColor};
        } else {
            labels = new String[]{"Sorted", "Minimum", "Comparing", "Swapping", "Unsorted"};
            colors = new Color[]{sortedColor, minColor, compareColor, currentColor, defaultColor};
        }

        int legendX = x;
        for (int i = 0; i < labels.length; i++) {
            g2d.setColor(colors[i]);
            g2d.fillRoundRect(legendX, y, 15, 10, 3, 3);
            g2d.setColor(borderColor);
            g2d.drawRoundRect(legendX, y, 15, 10, 3, 3);

            g2d.setColor(new Color(44, 62, 80));
            g2d.drawString(labels[i], legendX + 20, y + 8);

            legendX += g2d.getFontMetrics().stringWidth(labels[i]) + 40;
        }
    }

    void drawSortedIndicator(Graphics2D g2d, int startX, int startY, int boxWidth, int boxHeight) {
        int sortedEnd = currentAlgorithm.equals("Insertion Sort") ? insertionCurrentIndex - 1 : selectionCurrentIndex;
        if (sortedEnd >= 0) {
            int sortedEndX = startX + sortedEnd * (boxWidth + 15) + boxWidth;
            int indicatorY = startY + boxHeight + 25;

            g2d.setColor(new Color(46, 204, 113, 100));
            g2d.fillRect(startX, indicatorY, sortedEndX - startX, 4);
            g2d.setColor(new Color(46, 204, 113));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.drawString("SORTED PORTION", startX, indicatorY + 15);

            if (sortedEnd < arraySize - 1) {
                int unsortedStartX = startX + (sortedEnd + 1) * (boxWidth + 15);
                int unsortedEndX = startX + arraySize * (boxWidth + 15) - 15;
                g2d.setColor(new Color(231, 76, 60, 100));
                g2d.fillRect(unsortedStartX, indicatorY, unsortedEndX - unsortedStartX, 4);
                g2d.setColor(new Color(231, 76, 60));
                g2d.drawString("UNSORTED PORTION", unsortedStartX, indicatorY + 15);
            }
        }
    }

    void drawShiftArrow(Graphics2D g2d, int startX, int startY, int boxWidth, int boxHeight) {
        int fromX = startX + insertionCompareIndex * (boxWidth + 15) + boxWidth / 2;
        int toX = startX + (insertionCompareIndex + 1) * (boxWidth + 15) + boxWidth / 2;
        int arrowY = startY + boxHeight + 35;

        g2d.setColor(new Color(52, 152, 219));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int controlX = (fromX + toX) / 2;
        int controlY = arrowY - 15;
        g2d.drawLine(fromX, arrowY, controlX, controlY);
        g2d.drawLine(controlX, controlY, toX, arrowY);

        int[] xPoints = {toX, toX - 8, toX - 8};
        int[] yPoints = {arrowY, arrowY - 6, arrowY + 6};
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g2d.drawString("SHIFT", controlX - 10, controlY - 5);
    }

    void drawSwapArrow(Graphics2D g2d, int startX, int startY, int boxWidth, int boxHeight) {
        int fromX = startX + selectionCurrentIndex * (boxWidth + 15) + boxWidth / 2;
        int toX = startX + selectionMinIndex * (boxWidth + 15) + boxWidth / 2;
        int arrowY = startY + boxHeight + 35;

        g2d.setColor(new Color(155, 89, 182));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw double-headed arrow
        g2d.drawLine(fromX, arrowY, toX, arrowY);

        // Arrow heads
        int[] xPoints1 = {fromX, fromX + 8, fromX + 8};
        int[] yPoints1 = {arrowY, arrowY - 6, arrowY + 6};
        g2d.fillPolygon(xPoints1, yPoints1, 3);

        int[] xPoints2 = {toX, toX - 8, toX - 8};
        int[] yPoints2 = {arrowY, arrowY - 6, arrowY + 6};
        g2d.fillPolygon(xPoints2, yPoints2, 3);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
        int labelX = (fromX + toX) / 2 - 10;
        g2d.drawString("SWAP", labelX, arrowY - 10);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SortingVisualizerApp();
            }
        });
    }
}
