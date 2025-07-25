import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; // Added for Arrays.toString() in explanation

public class MergeSortVisualizer extends JFrame {
    int[] array;
    int[] tempArray;
    int arraySize;
    JPanel arrayPanel, explanationPanel;
    JScrollPane arrayScrollPane, explanationScrollPane;
    JButton startButton, resetButton, customInputButton;
    JSlider speedSlider;
    JLabel speedLabel, statusLabel, stepLabel, phaseLabel;
    JTextArea explanationArea;
    Timer animationTimer;

    // Animation variables
    boolean isAnimating = false;
    int currentStep = 0;
    int totalSteps = 0;

    // Merge operation variables
    int mergeLeft = -1;
    int mergeRight = -1;
    int mergeMid = -1;
    int leftPointer = -1;
    int rightPointer = -1;
    int mergePointer = -1;
    boolean isComparing = false;
    boolean isCopying = false;
    int compareIndex1 = -1;
    int compareIndex2 = -1;

    // Merge operations queue
    List<MergeOperation> mergeOperations;
    int currentOperationIndex = 0;

    // Colors
    Color defaultColor = new Color(240, 248, 255);
    Color leftArrayColor = new Color(52, 152, 219);
    Color rightArrayColor = new Color(231, 76, 60);
    Color compareColor = new Color(255, 193, 7);
    Color mergeColor = new Color(46, 204, 113);
    Color sortedColor = new Color(155, 89, 182);
    Color activeColor = new Color(255, 165, 0);
    Color borderColor = new Color(44, 62, 80);

    class MergeOperation {
        int left, mid, right;
        int step;
        String description;
        boolean completed;

        MergeOperation(int left, int mid, int right, int step, String description) {
            this.left = left;
            this.mid = mid;
            this.right = right;
            this.step = step;
            this.description = description;
            this.completed = false;
        }
    }

    public MergeSortVisualizer() {
        initializeGUI();
        generateRandomArray(8);
    }

    void initializeGUI() {
        setTitle("🔀 Merge Sort Visualizer - Single Array View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(new Color(248, 249, 250));

        // Header panel
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 152, 219),
                        getWidth(), 0, new Color(46, 204, 113));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));

        JLabel titleLabel = new JLabel("MERGE SORT VISUALIZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(new Color(248, 249, 250)); // FIX: Changed from 750 to 250
        controlPanel.setPreferredSize(new Dimension(0, 90));

        startButton = createStyledButton("Start Merge Sort", new Color(46, 204, 113));
        resetButton = createStyledButton("Reset Array", new Color(52, 152, 219));
        customInputButton = createStyledButton("Custom Input", new Color(155, 89, 182));

        speedLabel = new JLabel("Animation Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        speedLabel.setForeground(new Color(44, 62, 80));

        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(new Color(248, 249, 250));
        speedSlider.setPreferredSize(new Dimension(200, 50));

        stepLabel = new JLabel("Step: 0 / 0");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stepLabel.setForeground(new Color(44, 62, 80));

        phaseLabel = new JLabel("Phase: Ready");
        phaseLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phaseLabel.setForeground(new Color(155, 89, 182));

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(customInputButton);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(stepLabel);
        controlPanel.add(phaseLabel);

        // Create main content panel
        JPanel mainContentPanel = new JPanel(new BorderLayout(15, 15));
        mainContentPanel.setBackground(new Color(248, 249, 250)); // FIX: Changed from 750 to 250

        // Array visualization panel with scrolling
        arrayPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(),
                        new Color(248, 249, 250)); // FIX: Changed from 750 to 250
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                drawArray(g2d);
            }

            public Dimension getPreferredSize() {
                int width = Math.max(1000, arraySize * 80 + 200);
                int height = 400;
                return new Dimension(width, height);
            }
        };
        arrayPanel.setBackground(Color.WHITE);

        arrayScrollPane = new JScrollPane(arrayPanel);
        arrayScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        arrayScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        arrayScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Array Visualization - Merge Sort Process",
                0, 0, new Font("Segoe UI", Font.BOLD, 16), new Color(52, 152, 219)
        ));

        // Right panel for status and explanation
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(new Color(248, 249, 250)); // FIX: Changed from 750 to 250
        rightPanel.setPreferredSize(new Dimension(450, 600));

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Current Status",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 152, 219)
        ));
        statusPanel.setPreferredSize(new Dimension(450, 120));

        statusLabel = new JLabel("<html><center>🎯 Ready to sort!<br>Click 'Start Merge Sort' to begin</center></html>", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(44, 62, 80));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Explanation panel with scrolling
        explanationPanel = new JPanel(new BorderLayout());
        explanationPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "Algorithm Explanation & Step Details",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(46, 204, 113)
        ));
        explanationPanel.setBackground(Color.WHITE);

        explanationArea = new JTextArea(25, 35);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        explanationArea.setBackground(Color.WHITE);
        explanationArea.setForeground(new Color(44, 62, 80));
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
                startMergeSort();
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
                performMergeSortStep();
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
        // Convert slider value (1-10) to delay (1500ms-150ms)
        // Higher slider value = faster animation = lower delay
        int sliderValue = speedSlider.getValue();
        return 1650 - (sliderValue * 150); // Range: 1500ms (slow) to 150ms (fast)
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
        tempArray = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            array[i] = (int)(Math.random() * 99) + 1;
        }
        resetSortingVariables();
        buildMergeOperations();
        updatePanelSizes();
        repaint();
    }

    void resetSortingVariables() {
        isAnimating = false;
        currentStep = 0;
        currentOperationIndex = 0;

        mergeLeft = -1;
        mergeRight = -1;
        mergeMid = -1;
        leftPointer = -1;
        rightPointer = -1;
        mergePointer = -1;
        isComparing = false;
        isCopying = false;
        compareIndex1 = -1;
        compareIndex2 = -1;

        statusLabel.setText("<html><center>🎯 Ready to sort!<br>Click 'Start Merge Sort' to begin</center></html>");
        stepLabel.setText("Step: 0 / " + (mergeOperations != null ? mergeOperations.size() : 0));
        phaseLabel.setText("Phase: Ready");
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
        startButton.setText("Start Merge Sort");
        startButton.setEnabled(true);
        startButton.setBackground(new Color(46, 204, 113));
    }

    void showCustomInputDialog() {
        if (isAnimating) return;

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sizeLabel = new JLabel("Array Size (4-32):");
        sizeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField sizeField = new JTextField("8");

        JLabel elementsLabel = new JLabel("Elements (space-separated, -999 to 999):");
        elementsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField elementsField = new JTextField("38 27 43 3 9 82 10 1");

        inputPanel.add(sizeLabel);
        inputPanel.add(sizeField);
        inputPanel.add(elementsLabel);
        inputPanel.add(elementsField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "🔀 Custom Merge Sort Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int size = Integer.parseInt(sizeField.getText().trim());
                if (size < 4 || size > 32) {
                    JOptionPane.showMessageDialog(this, "❌ Size must be between 4 and 32");
                    return;
                }

                String[] elements = elementsField.getText().trim().split("\\s+");
                if (elements.length != size) {
                    JOptionPane.showMessageDialog(this, "❌ Please enter exactly " + size + " numbers");
                    return;
                }

                arraySize = size;
                array = new int[arraySize];
                tempArray = new int[arraySize];
                for (int i = 0; i < arraySize; i++) {
                    array[i] = Integer.parseInt(elements[i]);
                    if (array[i] < -999 || array[i] > 999) {
                        JOptionPane.showMessageDialog(this, "❌ Numbers must be between -999 and 999");
                        return;
                    }
                }

                resetSortingVariables();
                buildMergeOperations();
                updatePanelSizes();
                repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "❌ Please enter valid numbers");
            }
        }
    }

    void startMergeSort() {
        if (isAnimating) return;

        isAnimating = true;
        startButton.setText("Sorting...");
        startButton.setEnabled(false);
        startButton.setBackground(new Color(231, 76, 60));

        // FIX: Update timer delay when starting animation
        animationTimer.setDelay(getAnimationDelay());
        animationTimer.start();

        statusLabel.setText("<html><center>🚀 Starting Merge Sort...<br>Merging sorted sub-arrays</center></html>");
        phaseLabel.setText("Phase: Merging");
        updateExplanation("MERGE SORT STARTED!\n\n" +
                "Merge Sort uses a divide-and-conquer approach.\n" +
                "We'll simulate the merging process step by step.\n\n" +
                "The algorithm works by:\n" +
                "1. Dividing the array into smaller sub-arrays (conceptually)\n" +
                "2. Merging these sub-arrays back together in sorted order\n\n" +
                "We'll show each merge operation on the single array,\n" +
                "highlighting the sections being merged.\n\n" +
                "Array size: " + arraySize + " elements\n" +
                "Total merge operations: " + mergeOperations.size() + "\n\n" +
                "Starting merge operations...");
    }

    void performMergeSortStep() {
        if (currentOperationIndex >= mergeOperations.size()) {
            // Sorting complete
            animationTimer.stop();
            isAnimating = false;
            startButton.setText("Completed");
            startButton.setBackground(new Color(46, 204, 113));
            statusLabel.setText("<html><center>🎉 Merge Sort Complete!<br>Array is now perfectly sorted!</center></html>");
            phaseLabel.setText("Phase: Complete");
            updateExplanation("MERGE SORT COMPLETED!\n\n" +
                    "Congratulations! The merge sort algorithm has successfully sorted the array.\n\n" +
                    "FINAL RESULT:\n" +
                    "The array is now in perfect ascending order!\n" + Arrays.toString(array) + "\n\n" +
                    "ALGORITHM SUMMARY:\n" +
                    "• Total merge operations: " + mergeOperations.size() + "\n" +
                    "• All sub-arrays merged successfully\n" +
                    "• Array is now completely sorted\n\n" +
                    "PERFORMANCE CHARACTERISTICS:\n" +
                    "• Time Complexity: O(n log n) - guaranteed!\n" +
                    "• Space Complexity: O(n) - requires temporary array\n" +
                    "• Stable: Yes - maintains relative order of equal elements\n" +
                    "• Predictable: Always O(n log n), regardless of input\n\n" +
                    "KEY ADVANTAGES:\n" +
                    "• Guaranteed O(n log n) performance\n" +
                    "• Stable sorting algorithm\n" +
                    "• Works excellently with large datasets\n" +
                    "• Parallelizable for even better performance\n\n" +
                    "Merge sort is widely used in practice due to its\n" +
                    "predictable performance and stability guarantees!");

            // Reset visual indicators
            mergeLeft = -1;
            mergeRight = -1;
            mergeMid = -1;
            leftPointer = -1;
            rightPointer = -1;
            compareIndex1 = -1;
            compareIndex2 = -1;
            repaint();
            return;
        }

        MergeOperation op = mergeOperations.get(currentOperationIndex);

        if (!op.completed) {
            currentStep++;
            stepLabel.setText("Step: " + currentStep + " / " + mergeOperations.size());

            // Set up merge operation
            mergeLeft = op.left;
            mergeRight = op.right;
            mergeMid = op.mid;

            statusLabel.setText("<html><center>🔄 " + op.description + "<br>Merging [" + mergeLeft + "," + mergeMid + "] with [" + (mergeMid + 1) + "," + mergeRight + "]</center></html>");
            updateExplanation("MERGE OPERATION " + (currentOperationIndex + 1) + " / " + mergeOperations.size() + "\n\n" +
                    op.description + "\n\n" +
                    "CURRENT OPERATION:\n" +
                    "• Left sub-array: indices [" + mergeLeft + ", " + mergeMid + "]\n" +
                    "• Right sub-array: indices [" + (mergeMid + 1) + ", " + mergeRight + "]\n" +
                    "• Target range: indices [" + mergeLeft + ", " + mergeRight + "]\n\n" +
                    "MERGE PROCESS:\n" +
                    "1. Compare elements from both sorted sub-arrays\n" +
                    "2. Copy the smaller element to temporary array\n" +
                    "3. Advance the pointer in the array we copied from\n" +
                    "4. Repeat until one sub-array is exhausted\n" +
                    "5. Copy remaining elements from the other sub-array\n" +
                    "6. Copy merged result back to original array\n\n" +
                    "This merge combines two already-sorted segments\n" +
                    "into one larger sorted segment.\n\n" +
                    "Performing merge operation...");

            // Perform the actual merge
            performMerge(op.left, op.mid, op.right);
            op.completed = true;
        }

        currentOperationIndex++;
        repaint();
    }

    void performMerge(int left, int mid, int right) {
        // Copy elements to temporary array
        for (int i = left; i <= right; i++) {
            tempArray[i] = array[i];
        }

        int leftIndex = left;
        int rightIndex = mid + 1;
        int mergeIndex = left;

        // Merge the two sorted sub-arrays
        while (leftIndex <= mid && rightIndex <= right) {
            if (tempArray[leftIndex] <= tempArray[rightIndex]) {
                array[mergeIndex] = tempArray[leftIndex];
                leftIndex++;
            } else {
                array[mergeIndex] = tempArray[rightIndex];
                rightIndex++;
            }
            mergeIndex++;
        }

        // Copy remaining elements from left sub-array
        while (leftIndex <= mid) {
            array[mergeIndex] = tempArray[leftIndex];
            leftIndex++;
            mergeIndex++;
        }

        // Copy remaining elements from right sub-array
        while (rightIndex <= right) {
            array[mergeIndex] = tempArray[rightIndex];
            rightIndex++;
            mergeIndex++;
        }
    }

    void buildMergeOperations() {
        mergeOperations = new ArrayList<>();
        totalSteps = 0;
        buildMergeOperationsRecursive(0, arraySize - 1, 1);
    }

    void buildMergeOperationsRecursive(int left, int right, int level) {
        if (left < right) {
            int mid = (left + right) / 2;

            // Recursively build operations for left and right halves
            buildMergeOperationsRecursive(left, mid, level + 1);
            buildMergeOperationsRecursive(mid + 1, right, level + 1);

            // Add merge operation for this level
            totalSteps++;
            String description = "Level " + level + " merge: combining sorted segments";
            mergeOperations.add(new MergeOperation(left, mid, right, totalSteps, description));
        }
    }

    void drawArray(Graphics2D g2d) {
        if (array == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = arrayPanel.getWidth();
        int panelHeight = arrayPanel.getHeight();
        int boxWidth = Math.min(60, (panelWidth - 100) / arraySize);
        int boxHeight = 60;
        int totalWidth = arraySize * boxWidth + (arraySize - 1) * 8;
        int startX = (panelWidth - totalWidth) / 2;
        int startY = (panelHeight - boxHeight) / 2;

        // Draw title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.setColor(new Color(44, 62, 80));
        String title = "Merge Sort - Single Array Visualization";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (panelWidth - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 30);

        // Draw merge range background
        if (isAnimating && mergeLeft >= 0 && mergeRight >= 0) {
            int rangeStartX = startX + mergeLeft * (boxWidth + 8);
            int rangeWidth = (mergeRight - mergeLeft + 1) * (boxWidth + 8) - 8;
            g2d.setColor(new Color(46, 204, 113, 60));
            g2d.fillRoundRect(rangeStartX - 5, startY - 10, rangeWidth + 10, boxHeight + 20, 12, 12);

            // Draw merge operation label
            g2d.setColor(new Color(46, 204, 113));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String mergeLabel = "Merging Range [" + mergeLeft + ", " + mergeRight + "]";
            FontMetrics mergeFm = g2d.getFontMetrics();
            int mergeLabelX = rangeStartX + (rangeWidth - mergeFm.stringWidth(mergeLabel)) / 2;
            g2d.drawString(mergeLabel, mergeLabelX, startY - 15);
        }

        // Draw array elements
        for (int i = 0; i < arraySize; i++) {
            int x = startX + i * (boxWidth + 8);
            int y = startY;

            // Determine color based on current merge operation
            Color boxColor = defaultColor;
            Color textColor = new Color(44, 62, 80);
            boolean isHighlighted = false;

            if (isAnimating && mergeLeft >= 0 && mergeRight >= 0) {
                if (i >= mergeLeft && i <= mergeRight) {
                    if (i <= mergeMid) {
                        boxColor = leftArrayColor;
                        textColor = Color.WHITE;
                    } else {
                        boxColor = rightArrayColor;
                        textColor = Color.WHITE;
                    }
                    isHighlighted = true;
                } else if (i < mergeLeft || i > mergeRight) {
                    // Elements outside current merge range - already sorted
                    boxColor = sortedColor;
                    textColor = Color.WHITE;
                }
            }

            // Draw shadow for highlighted elements
            if (isHighlighted) {
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(x + 3, y + 3, boxWidth, boxHeight, 10, 10);
            }

            // Draw main box with gradient
            GradientPaint gradient = new GradientPaint(x, y, boxColor, x, y + boxHeight, boxColor.darker());
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, 10, 10);

            // Draw border
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, 10, 10);

            // Draw number
            g2d.setColor(textColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String text = String.valueOf(array[i]);
            FontMetrics textFm = g2d.getFontMetrics();
            int textX = x + (boxWidth - textFm.stringWidth(text)) / 2;
            int textY = y + (boxHeight + textFm.getAscent()) / 2 - 2;
            g2d.drawString(text, textX, textY);

            // Draw index
            g2d.setColor(new Color(108, 117, 125));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String indexText = String.valueOf(i);
            FontMetrics indexFm = g2d.getFontMetrics();
            int indexX = x + (boxWidth - indexFm.stringWidth(indexText)) / 2;
            g2d.drawString(indexText, indexX, y + boxHeight + 15);

            // Draw array section labels
            if (isAnimating && mergeLeft >= 0 && mergeRight >= 0 && i >= mergeLeft && i <= mergeRight) {
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2d.setColor(Color.WHITE);
                String sectionLabel = i <= mergeMid ? "LEFT" : "RIGHT";
                FontMetrics sectionFm = g2d.getFontMetrics();
                int sectionX = x + (boxWidth - sectionFm.stringWidth(sectionLabel)) / 2;
                g2d.drawString(sectionLabel, sectionX, y + 15);
            }
        }

        // Draw legend
        drawLegend(g2d);
    }

    void drawLegend(Graphics2D g2d) {
        String[] labels = {"Unsorted", "Left Sub-array", "Right Sub-array", "Sorted"};
        Color[] colors = {defaultColor, leftArrayColor, rightArrayColor, sortedColor};

        int legendX = 20;
        int legendY = 60;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.setColor(new Color(44, 62, 80));

        legendY += 18;
        for (int i = 0; i < labels.length; i++) {
            // Draw color box
            g2d.setColor(colors[i]);
            g2d.fillRoundRect(legendX, legendY - 8, 15, 12, 3, 3);
            g2d.setColor(borderColor);
            g2d.drawRoundRect(legendX, legendY - 8, 15, 12, 3, 3);

            // Draw label
            g2d.setColor(new Color(44, 62, 80));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.drawString(labels[i], legendX + 20, legendY);

            legendY += 16;
        }
    }

    void updateInitialExplanation() {
        updateExplanation("MERGE SORT ALGORITHM\n\n" +
                "Merge Sort is a divide-and-conquer algorithm that guarantees O(n log n) performance.\n\n" +
                "HOW IT WORKS:\n" +
                "1. Conceptually divide the array into smaller sub-arrays\n" +
                "2. Merge these sub-arrays back together in sorted order\n" +
                "3. Each merge combines two sorted segments into one larger sorted segment\n\n" +
                "VISUALIZATION APPROACH:\n" +
                "This visualizer shows the merge operations on a single array.\n" +
                "We'll highlight the sections being merged at each step.\n\n" +
                "COLOR CODING:\n" +
                "• Blue: Left sub-array being merged\n" +
                "• Red: Right sub-array being merged\n" +
                "• Purple: Already sorted sections\n" +
                "• Light Blue: Unsorted sections\n\n" +
                "KEY CHARACTERISTICS:\n" +
                "• Time Complexity: O(n log n) - guaranteed!\n" +
                "• Space Complexity: O(n) - requires temporary array\n" +
                "• Stable: Maintains relative order of equal elements\n" +
                "• Predictable: Performance doesn't depend on input\n\n" +
                "ADVANTAGES:\n" +
                "• Guaranteed O(n log n) performance\n" +
                "• Stable sorting algorithm\n" +
                "• Excellent for large datasets\n" +
                "• Can be parallelized effectively\n\n" +
                "Click 'Start Merge Sort' to see the algorithm in action!\n" +
                "Watch how sorted sub-arrays are merged together step by step.");
    }

    void updateExplanation(String text) {
        explanationArea.setText(text);
        explanationArea.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MergeSortVisualizer();
            }
        });
    }
}
