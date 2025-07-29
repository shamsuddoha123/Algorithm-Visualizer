import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class QuickSortVisualizer extends JFrame {
    int[] array;
    int arraySize;
    JPanel arrayPanel, explanationPanel, pivotPanel;
    JButton startButton, resetButton, customInputButton, pauseButton;
    JSlider speedSlider;
    JLabel speedLabel, statusLabel, stepLabel, recursionLabel;
    JTextArea explanationArea;
    JComboBox<String> pivotSelector;
    Timer animationTimer;

    // Animation variables
    boolean isAnimating = false;
    boolean isPaused = false;
    int currentLow = 0;
    int currentHigh = 0;
    int pivotIndex = -1;
    int pivotValue = -1;
    int leftPointer = -1;
    int rightPointer = -1;
    boolean isPartitioning = false;
    boolean isSwapping = false;
    int swapIndex1 = -1;
    int swapIndex2 = -1;
    int currentStep = 0;
    int totalSteps = 0;
    int recursionDepth = 0;

    // Stack for managing recursive calls
    Stack<PartitionCall> callStack;

    // Pivot selection modes
    String[] pivotModes = {"First Element", "Last Element", "Middle Element", "Random Element", "Custom Index"};
    String currentPivotMode = "Last Element";
    int customPivotIndex = -1;

    // Colors
    Color defaultColor = new Color(240, 248, 255);
    Color pivotColor = new Color(231, 76, 60);
    Color leftPointerColor = new Color(46, 204, 113);
    Color rightPointerColor = new Color(52, 152, 219);
    Color swapColor = new Color(255, 193, 7);
    Color sortedColor = new Color(155, 89, 182);
    Color partitionColor = new Color(108, 117, 125, 100);
    Color borderColor = new Color(44, 62, 80);

    class PartitionCall {
        int low, high;
        boolean isProcessed;

        PartitionCall(int low, int high) {
            this.low = low;
            this.high = high;
            this.isProcessed = false;
        }
    }

    public QuickSortVisualizer() {
        initializeGUI();
        generateRandomArray(12);
    }

    void initializeGUI() {
        setTitle("Quick Sort Visualizer - Divide and Conquer Algorithm");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(new Color(248, 249, 250));

        // Header panel
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 94, 77),
                        getWidth(), 0, new Color(255, 154, 0));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 90));

        JLabel titleLabel = new JLabel("QUICK SORT VISUALIZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 15, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Pivot selection panel
        pivotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pivotPanel.setBackground(new Color(248, 249, 250)); // FIX: Changed from 750 to 250
        pivotPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 94, 77), 2),
                "Pivot Selection Strategy",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(255, 94, 77)
        ));

        JLabel pivotLabel = new JLabel("Choose Pivot:");
        pivotLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pivotLabel.setForeground(new Color(44, 62, 80));

        pivotSelector = new JComboBox<>(pivotModes);
        pivotSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pivotSelector.setPreferredSize(new Dimension(150, 30));
        pivotSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentPivotMode = (String) pivotSelector.getSelectedItem();
                if (currentPivotMode.equals("Custom Index") && !isAnimating) {
                    showCustomPivotDialog();
                }
            }
        });

        pivotPanel.add(pivotLabel);
        pivotPanel.add(pivotSelector);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(new Color(248, 249, 250)); // FIX: Changed from 750 to 250

        startButton = createStyledButton("Start Quick Sort", new Color(46, 204, 113));
        resetButton = createStyledButton("Reset", new Color(52, 152, 219));
        customInputButton = createStyledButton("Custom Input", new Color(155, 89, 182));
        pauseButton = createStyledButton("Pause", new Color(255, 165, 0));
        pauseButton.setEnabled(false);

        // Add back button after customInputButton
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
        controlPanel.add(backButton);
        controlPanel.add(pauseButton);
        controlPanel.add(Box.createHorizontalStrut(30));

        speedLabel = new JLabel("Animation Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        speedLabel.setForeground(new Color(44, 62, 80));

        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(2);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(new Color(248, 249, 250)); // FIX: Changed from 750 to 250
        speedSlider.setPreferredSize(new Dimension(200, 50));

        stepLabel = new JLabel("Step: 0 / 0");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stepLabel.setForeground(new Color(44, 62, 80));

        recursionLabel = new JLabel("Recursion Depth: 0");
        recursionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        recursionLabel.setForeground(new Color(155, 89, 182));

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(customInputButton);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(stepLabel);
        controlPanel.add(recursionLabel);

        // Array panel
        arrayPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(),
                        new Color(248, 249, 250)); // FIX: Changed from 750 to 250
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                drawArray(g2d);
            }
        };
        arrayPanel.setPreferredSize(new Dimension(1000, 250));
        arrayPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Quick Sort Partitioning Process",
                0, 0, new Font("Segoe UI", Font.BOLD, 16), new Color(52, 152, 219)
        ));

        // Status and explanation panel
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(new Color(248, 249, 250)); // FIX: Changed from 750 to 250
        rightPanel.setPreferredSize(new Dimension(450, 0));

        statusLabel = new JLabel("<html><center>Ready to sort!<br>Select pivot strategy and click 'Start'</center></html>", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(44, 62, 80));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.WHITE);

        explanationPanel = new JPanel(new BorderLayout());
        explanationPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "Algorithm Explanation",
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

        JScrollPane scrollPane = new JScrollPane(explanationArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        explanationPanel.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(statusLabel, BorderLayout.NORTH);
        rightPanel.add(explanationPanel, BorderLayout.CENTER);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(pivotPanel, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(arrayPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Event listeners
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startQuickSort();
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

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pauseQuickSort();
            }
        });

        // FIX: Create timer with dynamic delay based on speed slider
        animationTimer = new Timer(getAnimationDelay(), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performQuickSortStep();
            }
        });

        // FIX: Add change listener to speed slider to update timer delay
        speedSlider.addChangeListener(e -> {
            if (animationTimer != null) {
                animationTimer.setDelay(getAnimationDelay());
            }
        });

        updateInitialExplanation();
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
        for (int i = 0; i < arraySize; i++) {
            array[i] = (int)(Math.random() * 99) + 1;
        }
        calculateTotalSteps();
        resetSortingVariables();
        repaint();
    }

    void calculateTotalSteps() {
        // Rough estimate for quick sort steps
        totalSteps = arraySize * (int)(Math.log(arraySize) / Math.log(2)) * 2;
    }

    void resetSortingVariables() {
        isAnimating = false;
        isPaused = false;
        currentLow = 0;
        currentHigh = arraySize - 1;
        pivotIndex = -1;
        pivotValue = -1;
        leftPointer = -1;
        rightPointer = -1;
        isPartitioning = false;
        isSwapping = false;
        swapIndex1 = -1;
        swapIndex2 = -1;
        currentStep = 0;
        recursionDepth = 0;

        callStack = new Stack<>();
        callStack.push(new PartitionCall(0, arraySize - 1));

        statusLabel.setText("<html><center>Ready to sort!<br>Select pivot strategy and click 'Start'</center></html>");
        stepLabel.setText("Step: 0 / " + totalSteps);
        recursionLabel.setText("Recursion Depth: 0");
    }

    void resetArray() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        generateRandomArray(arraySize);
        startButton.setText("Start Quick Sort");
        startButton.setEnabled(true);
        startButton.setBackground(new Color(46, 204, 113));
        pauseButton.setText("Pause");
        pauseButton.setEnabled(false);
        pauseButton.setBackground(new Color(255, 165, 0));
    }

    void showCustomInputDialog() {
        if (isAnimating) return;

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sizeLabel = new JLabel("Array Size (5-20):");
        sizeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField sizeField = new JTextField("12");

        JLabel elementsLabel = new JLabel("Elements (space-separated):");
        elementsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField elementsField = new JTextField("64 34 25 12 22 11 90 88 76 50 42 30");

        inputPanel.add(sizeLabel);
        inputPanel.add(sizeField);
        inputPanel.add(elementsLabel);
        inputPanel.add(elementsField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Custom Quick Sort Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int size = Integer.parseInt(sizeField.getText().trim());
                if (size < 5 || size > 20) {
                    JOptionPane.showMessageDialog(this, "Size must be between 5 and 20");
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

    void showCustomPivotDialog() {
        if (isAnimating) return;

        String input = JOptionPane.showInputDialog(this,
                "Enter custom pivot index (0 to " + (arraySize - 1) + "):",
                String.valueOf(arraySize - 1));

        if (input != null) {
            try {
                int index = Integer.parseInt(input.trim());
                if (index >= 0 && index < arraySize) {
                    customPivotIndex = index;
                } else {
                    JOptionPane.showMessageDialog(this, "Index must be between 0 and " + (arraySize - 1));
                    pivotSelector.setSelectedItem("Last Element");
                    currentPivotMode = "Last Element";
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number");
                pivotSelector.setSelectedItem("Last Element");
                currentPivotMode = "Last Element";
            }
        } else {
            pivotSelector.setSelectedItem("Last Element");
            currentPivotMode = "Last Element";
        }
    }

    void startQuickSort() {
        if (isAnimating) return;

        isAnimating = true;
        isPaused = false;
        startButton.setText("⏸ Sorting...");
        startButton.setEnabled(false);
        startButton.setBackground(new Color(231, 76, 60));
        pauseButton.setEnabled(true);

        // FIX: Update timer delay when starting animation
        animationTimer.setDelay(getAnimationDelay());
        animationTimer.start();

        statusLabel.setText("<html><center>Starting Quick Sort...<br>Using " + currentPivotMode + " strategy</center></html>");
        updateExplanation("QUICK SORT STARTED!\n\n" +
                "Quick Sort is a divide-and-conquer algorithm that works by selecting a 'pivot' element " +
                "and partitioning the array around it.\n\n" +
                "PIVOT STRATEGY: " + currentPivotMode + "\n\n" +
                "ALGORITHM STEPS:\n" +
                "1. Choose a pivot element\n" +
                "2. Partition: rearrange array so elements smaller than pivot come before it, " +
                "and elements greater come after\n" +
                "3. Recursively apply the same process to sub-arrays\n\n" +
                "Starting with the entire array...");
    }

    void pauseQuickSort() {
        if (isAnimating) {
            if (isPaused) {
                isPaused = false;
                animationTimer.start();
                pauseButton.setText("Pause");
                statusLabel.setText("<html><center>Resumed Quick Sort...<br>Using " + currentPivotMode + " strategy</center></html>");
            } else {
                isPaused = true;
                animationTimer.stop();
                pauseButton.setText("Resume");
                statusLabel.setText("<html><center>Paused Quick Sort...<br>Click Resume to continue</center></html>");
            }
        }
    }

    void performQuickSortStep() {
        if (isPaused) return;

        currentStep++;
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);

        if (callStack.isEmpty()) {
            // Sorting complete
            animationTimer.stop();
            isAnimating = false;
            startButton.setText("Completed");
            startButton.setBackground(new Color(46, 204, 113));
            pauseButton.setEnabled(false);
            statusLabel.setText("<html><center>Quick Sort Complete!<br>Array is now sorted!</center></html>");
            updateExplanation("QUICK SORT COMPLETED!\n\n" +
                    "Congratulations! The quick sort algorithm has successfully sorted the array.\n\n" +
                    "SUMMARY:\n" +
                    "• Used " + currentPivotMode + " pivot selection strategy\n" +
                    "• Divided the problem into smaller sub-problems\n" +
                    "• Conquered each sub-problem recursively\n" +
                    "• Combined results to get the final sorted array\n\n" +
                    "PERFORMANCE:\n" +
                    "• Average Time Complexity: O(n log n)\n" +
                    "• Worst Case: O(n²) - when pivot is always smallest/largest\n" +
                    "• Best Case: O(n log n) - when pivot divides array evenly\n" +
                    "• Space Complexity: O(log n) - for recursion stack\n\n" +
                    "Quick sort is widely used due to its excellent average-case performance!");

            // Reset visual indicators
            pivotIndex = -1;
            leftPointer = -1;
            rightPointer = -1;
            currentLow = -1;
            currentHigh = -1;
            repaint();
            return;
        }

        PartitionCall currentCall = callStack.peek();

        if (!currentCall.isProcessed) {
            // Start new partition
            currentLow = currentCall.low;
            currentHigh = currentCall.high;

            if (currentLow < currentHigh) {
                // Choose pivot
                pivotIndex = choosePivot(currentLow, currentHigh);
                pivotValue = array[pivotIndex];

                // Move pivot to end for partitioning
                if (pivotIndex != currentHigh) {
                    swap(pivotIndex, currentHigh);
                    pivotIndex = currentHigh;
                }

                // Initialize pointers
                leftPointer = currentLow;
                rightPointer = currentHigh - 1;
                isPartitioning = true;

                recursionDepth = callStack.size();
                recursionLabel.setText("Recursion Depth: " + recursionDepth);

                statusLabel.setText("<html><center>Partitioning [" + currentLow + ", " + currentHigh + "]<br>" +
                        "Pivot: " + pivotValue + " at index " + pivotIndex + "</center></html>");
                updateExplanation("NEW PARTITION CALL\n\n" +
                        "Range: [" + currentLow + ", " + currentHigh + "]\n" +
                        "Pivot selected: " + pivotValue + " (index " + pivotIndex + ")\n" +
                        "Strategy: " + currentPivotMode + "\n\n" +
                        "PARTITIONING PROCESS:\n" +
                        "We'll use two pointers - left pointer starts at " + leftPointer +
                        ", right pointer starts at " + rightPointer + ".\n\n" +
                        "Goal: Move all elements ≤ " + pivotValue + " to the left side, " +
                        "and all elements > " + pivotValue + " to the right side.\n\n" +
                        "Current recursion depth: " + recursionDepth);
            } else {
                // Base case - single element or empty
                callStack.pop();
                statusLabel.setText("<html><center>Base case reached<br>Range [" + currentLow + ", " + currentHigh + "] complete</center></html>");
            }

            currentCall.isProcessed = true;
        } else {
            // Continue partitioning
            if (isPartitioning) {
                performPartitionStep();
            }
        }

        repaint();
    }

    void performPartitionStep() {
        // Find element from left that should be on right
        while (leftPointer <= rightPointer && array[leftPointer] <= pivotValue) {
            leftPointer++;
        }

        // Find element from right that should be on left
        while (leftPointer <= rightPointer && array[rightPointer] > pivotValue) {
            rightPointer--;
        }

        if (leftPointer < rightPointer) {
            // Swap elements
            swapIndex1 = leftPointer;
            swapIndex2 = rightPointer;
            swap(leftPointer, rightPointer);

            statusLabel.setText("<html><center>Swapping elements<br>" +
                    array[swapIndex2] + " ↔ " + array[swapIndex1] + "</center></html>");
            updateExplanation("SWAPPING ELEMENTS\n\n" +
                    "Found " + array[swapIndex2] + " at index " + swapIndex1 + " (should be on right)\n" +
                    "Found " + array[swapIndex1] + " at index " + swapIndex2 + " (should be on left)\n\n" +
                    "Swapping these elements to maintain partition property:\n" +
                    "• Left side: elements ≤ " + pivotValue + "\n" +
                    "• Right side: elements > " + pivotValue + "\n\n" +
                    "Continuing partition process...");

            leftPointer++;
            rightPointer--;
        } else {
            // Partitioning complete - place pivot in correct position
            int finalPivotPos = leftPointer;
            swap(pivotIndex, finalPivotPos);

            statusLabel.setText("<html><center>Partition complete!<br>" +
                    "Pivot " + pivotValue + " placed at index " + finalPivotPos + "</center></html>");
            updateExplanation("PARTITION COMPLETED!\n\n" +
                    "Pivot " + pivotValue + " is now in its final sorted position at index " + finalPivotPos + ".\n\n" +
                    "PARTITION RESULT:\n" +
                    "• Elements at indices [" + currentLow + ", " + (finalPivotPos-1) + "] are ≤ " + pivotValue + "\n" +
                    "• Elements at indices [" + (finalPivotPos+1) + ", " + currentHigh + "] are > " + pivotValue + "\n\n" +
                    "RECURSIVE CALLS:\n" +
                    "Now we'll recursively sort the two sub-arrays:\n" +
                    "1. Left sub-array: [" + currentLow + ", " + (finalPivotPos-1) + "]\n" +
                    "2. Right sub-array: [" + (finalPivotPos+1) + ", " + currentHigh + "]\n\n" +
                    "Adding these calls to the stack...");

            // Remove current call and add sub-problems
            callStack.pop();

            // Add right sub-array first (will be processed after left)
            if (finalPivotPos + 1 < currentHigh) {
                callStack.push(new PartitionCall(finalPivotPos + 1, currentHigh));
            }

            // Add left sub-array
            if (currentLow < finalPivotPos - 1) {
                callStack.push(new PartitionCall(currentLow, finalPivotPos - 1));
            }

            isPartitioning = false;
            pivotIndex = -1;
            leftPointer = -1;
            rightPointer = -1;
            swapIndex1 = -1;
            swapIndex2 = -1;
        }
    }

    int choosePivot(int low, int high) {
        switch (currentPivotMode) {
            case "First Element":
                return low;
            case "Last Element":
                return high;
            case "Middle Element":
                return (low + high) / 2;
            case "Random Element":
                return low + (int)(Math.random() * (high - low + 1));
            case "Custom Index":
                if (customPivotIndex >= low && customPivotIndex <= high) {
                    return customPivotIndex;
                } else {
                    return high; // Fallback to last element
                }
            default:
                return high;
        }
    }

    void swap(int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    void drawArray(Graphics2D g2d) {
        if (array == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = arrayPanel.getWidth();
        int panelHeight = arrayPanel.getHeight();
        int boxWidth = Math.min(70, (panelWidth - 60) / arraySize);
        int boxHeight = 60;
        int startX = (panelWidth - (boxWidth * arraySize + 12 * (arraySize - 1))) / 2;
        int startY = (panelHeight - boxHeight) / 2;

        // Draw partition background
        if (isAnimating && currentLow >= 0 && currentHigh >= 0) {
            int partitionStartX = startX + currentLow * (boxWidth + 12);
            int partitionWidth = (currentHigh - currentLow + 1) * (boxWidth + 12) - 12;
            g2d.setColor(partitionColor);
            g2d.fillRoundRect(partitionStartX - 5, startY - 10, partitionWidth + 10, boxHeight + 20, 10, 10);

            // Draw partition label
            g2d.setColor(new Color(44, 62, 80));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.drawString("Current Partition [" + currentLow + ", " + currentHigh + "]",
                    partitionStartX, startY - 15);
        }

        // Draw array elements
        for (int i = 0; i < arraySize; i++) {
            int x = startX + i * (boxWidth + 12);
            int y = startY;

            // Determine color
            Color boxColor = defaultColor;
            Color textColor = new Color(44, 62, 80);
            boolean isHighlighted = false;

            if (isAnimating) {
                if (i == pivotIndex) {
                    boxColor = pivotColor;
                    textColor = Color.WHITE;
                    isHighlighted = true;
                } else if (i == leftPointer) {
                    boxColor = leftPointerColor;
                    textColor = Color.WHITE;
                    isHighlighted = true;
                } else if (i == rightPointer) {
                    boxColor = rightPointerColor;
                    textColor = Color.WHITE;
                    isHighlighted = true;
                } else if (i == swapIndex1 || i == swapIndex2) {
                    boxColor = swapColor;
                    textColor = new Color(44, 62, 80);
                    isHighlighted = true;
                } else if (isSorted(i)) {
                    boxColor = sortedColor;
                    textColor = Color.WHITE;
                }
            }

            // Draw shadow for highlighted elements
            if (isHighlighted) {
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(x + 3, y + 3, boxWidth, boxHeight, 12, 12);
            }

            // Draw main box with gradient
            GradientPaint gradient = new GradientPaint(x, y, boxColor, x, y + boxHeight, boxColor.darker());
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, 12, 12);

            // Draw border
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, 12, 12);

            // Draw number
            g2d.setColor(textColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String text = String.valueOf(array[i]);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (boxWidth - fm.stringWidth(text)) / 2;
            int textY = y + (boxHeight + fm.getAscent()) / 2 - 2;
            g2d.drawString(text, textX, textY);

            // Draw index
            g2d.setColor(new Color(108, 117, 125));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String indexText = String.valueOf(i);
            FontMetrics indexFm = g2d.getFontMetrics();
            int indexX = x + (boxWidth - indexFm.stringWidth(indexText)) / 2;
            g2d.drawString(indexText, indexX, y + boxHeight + 15);

            // Draw pointer labels
            if (isAnimating) {
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                if (i == leftPointer) {
                    g2d.setColor(leftPointerColor);
                    g2d.drawString("LEFT", x + (boxWidth - g2d.getFontMetrics().stringWidth("LEFT")) / 2, y - 5);
                } else if (i == rightPointer) {
                    g2d.setColor(rightPointerColor);
                    g2d.drawString("RIGHT", x + (boxWidth - g2d.getFontMetrics().stringWidth("RIGHT")) / 2, y - 5);
                } else if (i == pivotIndex) {
                    g2d.setColor(pivotColor);
                    g2d.drawString("PIVOT", x + (boxWidth - g2d.getFontMetrics().stringWidth("PIVOT")) / 2, y - 5);
                }
            }
        }

        // Draw legend
        drawLegend(g2d);
    }

    boolean isSorted(int index) {
        // Simple heuristic - if element is not in current partition range, consider it sorted
        return isAnimating && (index < currentLow || index > currentHigh);
    }

    void drawLegend(Graphics2D g2d) {
        String[] labels = {"Unsorted", "Pivot", "Left Pointer", "Right Pointer", "Swapping", "Sorted"};
        Color[] colors = {defaultColor, pivotColor, leftPointerColor, rightPointerColor, swapColor, sortedColor};

        int legendX = 20;
        int legendY = 20;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.setColor(new Color(44, 62, 80));

        legendY += 20;
        for (int i = 0; i < labels.length; i++) {
            // Draw color box
            g2d.setColor(colors[i]);
            g2d.fillRoundRect(legendX, legendY - 10, 15, 12, 3, 3);
            g2d.setColor(borderColor);
            g2d.drawRoundRect(legendX, legendY - 10, 15, 12, 3, 3);

            // Draw label
            g2d.setColor(new Color(44, 62, 80));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.drawString(labels[i], legendX + 20, legendY);

            legendY += 18;
        }
    }

    void updateInitialExplanation() {
        updateExplanation("QUICK SORT ALGORITHM\n\n" +
                "Quick Sort is a highly efficient divide-and-conquer sorting algorithm. " +
                "It works by selecting a 'pivot' element and partitioning the array around it.\n\n" +
                "PIVOT SELECTION STRATEGIES:\n" +
                "• First Element: Simple but can be inefficient for sorted arrays\n" +
                "• Last Element: Most common implementation\n" +
                "• Middle Element: Often provides better balance\n" +
                "• Random Element: Helps avoid worst-case scenarios\n" +
                "• Custom Index: Choose your own pivot position\n\n" +
                "ALGORITHM STEPS:\n" +
                "1. Choose a pivot element from the array\n" +
                "2. Partition: rearrange array so elements ≤ pivot come before it, " +
                "elements > pivot come after it\n" +
                "3. Recursively apply quick sort to the sub-arrays\n\n" +
                "PERFORMANCE:\n" +
                "• Average Case: O(n log n)\n" +
                "• Best Case: O(n log n)\n" +
                "• Worst Case: O(n²)\n" +
                "• Space: O(log n) for recursion\n\n" +
                "Select a pivot strategy and click 'Start Quick Sort' to begin!");
    }

    void updateExplanation(String text) {
        explanationArea.setText(text);
        explanationArea.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new QuickSortVisualizer();
            }
        });
    }
}
