import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque; // For Deque (stack-like behavior)
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class HeapSortVisualizer extends JFrame {
    int[] array;
    int arraySize;
    int heapSize;
    JPanel treePanel, arrayPanel, explanationPanel;
    JButton startButton, resetButton, customInputButton, backButton; // Added backButton
    JSlider speedSlider;
    JLabel speedLabel, statusLabel, stepLabel;
    JTextArea explanationArea;
    Timer animationTimer;

    // Animation variables
    boolean isAnimating = false;
    boolean isBuildingHeap = true;
    boolean isExtracting = false;
    int currentBuildIndex; // Index of the root of the subtree to heapify in build phase

    // Highlighting variables
    int heapifyIndex = -1; // Node currently being heapified (root of current heapify operation)
    int leftChild = -1;    // Left child being compared
    int rightChild = -1;   // Right child being compared
    int largestIndex = -1; // Index of the largest element found in current heapify step
    int swapIndex1 = -1;
    int swapIndex2 = -1;
    int currentStep = 0;
    int totalSteps = 0;

    // NEW: Stack for managing iterative heapify calls
    private Deque<HeapifyCall> heapifyStack;

    // Tree visualization variables
    List<TreeNode> treeNodes;

    // Colors
    Color defaultColor = new Color(240, 248, 255);
    Color heapifyColor = new Color(231, 76, 60); // Reddish for current heapify root
    Color compareColor = new Color(255, 193, 7); // Yellowish for elements being compared
    Color swapColor = new Color(46, 204, 113); // Greenish for elements being swapped
    Color sortedColor = new Color(52, 152, 219); // Bluish for sorted elements
    Color maxColor = new Color(155, 89, 182); // Purplish for the largest element found
    Color borderColor = new Color(44, 62, 80); // Dark gray for borders
    Color treeLineColor = new Color(108, 117, 125); // Gray for tree lines

    // NEW: Class to represent a single "call" to maxHeapify in the iterative process
    class HeapifyCall {
        int index; // The root of the current subtree being heapified
        int largest; // The index of the largest element found so far in this call
        int state; // Internal state for this specific heapify call
        int leftChildIdx;
        int rightChildIdx;

        // States for 'state' variable
        static final int INIT = 0;
        static final int COMPARE_LEFT = 1;
        static final int COMPARE_RIGHT = 2;
        static final int CHECK_SWAP = 3;
        static final int PERFORM_SWAP = 4;
        static final int RECURSE_CHILD = 5; // Indicates a child needs to be heapified
        static final int WAITING_FOR_CHILD = 6; // NEW STATE: Parent waiting for child heapify to finish
        static final int DONE = 7; // This heapify call is complete

        HeapifyCall(int index) {
            this.index = index;
            this.largest = index;
            this.state = INIT;
            this.leftChildIdx = 2 * index + 1;
            this.rightChildIdx = 2 * index + 2;
        }
    }

    // NEW: TreeNode class definition (was missing)
    class TreeNode {
        int value;
        int index;
        int x, y;
        Color color;

        TreeNode(int value, int index, int x, int y) {
            this.value = value;
            this.index = index;
            this.x = x;
            this.y = y;
            this.color = defaultColor;
        }
    }

    public HeapSortVisualizer() {
        initializeGUI();
        generateRandomArray(10);
    }

    void initializeGUI() {
        setTitle("🌳 Heap Sort Visualizer - Max Heap Tree Structure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize the window
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(new Color(248, 249, 250));

        // Header panel
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(106, 90, 205),
                        getWidth(), 0, new Color(72, 61, 139));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));

        JLabel titleLabel = new JLabel("HEAP SORT VISUALIZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Control panel (buttons and speed slider)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(new Color(248, 249, 250));

        startButton = createStyledButton("Start Heap Sort", new Color(46, 204, 113));
        resetButton = createStyledButton("Reset", new Color(52, 152, 219));
        customInputButton = createStyledButton("Custom Input", new Color(155, 89, 182));
        backButton = createStyledButton("Back to Hub", new Color(100, 149, 237));

        speedLabel = new JLabel("Animation Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        speedLabel.setForeground(new Color(44, 62, 80));

        speedSlider = new JSlider(1, 10, 4);
        speedSlider.setMajorTickSpacing(2);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(new Color(248, 249, 250));
        speedSlider.setPreferredSize(new Dimension(200, 50));

        stepLabel = new JLabel("Step: 0 / 0");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stepLabel.setForeground(new Color(44, 62, 80));

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(customInputButton);
        controlPanel.add(backButton);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(stepLabel);

        // Right panel for status and explanation
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.setPreferredSize(new Dimension(400, 0)); // Fixed width, flexible height

        statusLabel = new JLabel("<html><center>🎯 Ready to sort!<br>Click 'Start Heap Sort' to begin</center></html>", JLabel.CENTER);
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

        // MODIFIED: Reduced rows for explanationArea to make it smaller
        explanationArea = new JTextArea(8, 30);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        explanationArea.setBackground(Color.WHITE);
        explanationArea.setForeground(new Color(44, 62, 80));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setEditable(false);
        explanationArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane explanationScrollPane = new JScrollPane(explanationArea);
        explanationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        explanationPanel.add(explanationScrollPane, BorderLayout.CENTER);

        rightPanel.add(statusLabel, BorderLayout.NORTH);
        rightPanel.add(explanationPanel, BorderLayout.CENTER);

        // Left visuals panel (Tree and Array)
        JPanel leftVisualsPanel = new JPanel();
        leftVisualsPanel.setLayout(new BoxLayout(leftVisualsPanel, BoxLayout.Y_AXIS)); // Stack vertically
        leftVisualsPanel.setBackground(new Color(248, 249, 250));

        // Tree panel
        // MODIFIED: Increased preferred height for treePanel to make it bigger
        treePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(),
                        new Color(248, 249, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                drawHeapTree(g2d);
            }
        };
        treePanel.setPreferredSize(new Dimension(800, 450)); // Increased height for tree
        treePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(106, 90, 205), 2),
                "Max Heap Tree Structure",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(106, 90, 205)
        ));

        // Array panel
        arrayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(),
                        new Color(248, 249, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                drawArray(g2d);
            }

            @Override // Override getPreferredSize to calculate dynamic width
            public Dimension getPreferredSize() {
                int boxWidth = 60;
                int spacing = 10;
                int padding = 20; // Padding on both sides

                // Calculate total width needed for all boxes and spacing
                int totalContentWidth = arraySize * (boxWidth + spacing) - spacing;

                // Return the actual content width needed, without constraining it to 800px
                return new Dimension(totalContentWidth + 2 * padding, 150);
            }
        };
        arrayPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Array Representation",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 152, 219)
        ));

        // Wrap arrayPanel in a JScrollPane
        JScrollPane arrayScrollPane = new JScrollPane(arrayPanel);
        arrayScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // Only horizontal needed
        arrayScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        arrayScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        // MODIFIED: Removed fixed preferred width for arrayScrollPane to allow it to expand
        arrayScrollPane.setPreferredSize(new Dimension(0, 200)); // Set width to 0 to allow horizontal expansion

        leftVisualsPanel.add(treePanel);
        leftVisualsPanel.add(arrayScrollPane);

        // Create a JSplitPane for the main visualization area (left visuals vs right panel)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftVisualsPanel, rightPanel);
        // MODIFIED: Increased resizeWeight to give more space to the left visuals (tree + array)
        mainSplitPane.setResizeWeight(0.8); // Left side takes 80% of the space
        mainSplitPane.setDividerSize(10);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default split pane border

        // Add components to the JFrame
        add(headerPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH); // Control panel at the bottom
        add(mainSplitPane, BorderLayout.CENTER); // The split pane takes the center

        // Event listeners
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startHeapSort();
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

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (animationTimer != null) animationTimer.stop();
                dispose(); // Close this window
            }
        });

        speedSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int delay = 1200 - (speedSlider.getValue() * 100);
                if (animationTimer != null) {
                    animationTimer.setDelay(delay);
                }
            }
        });

        animationTimer = new Timer(800, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performHeapSortStep();
            }
        });

        updateInitialExplanation();
        pack(); // Pack the frame to its preferred size
        setLocationRelativeTo(null); // Center the frame on screen
        setVisible(true);
    }

    JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
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
        heapSize = arraySize;
        array = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            array[i] = (int)(Math.random() * 99) + 1;
        }
        calculateTotalSteps();
        resetSortingVariables();
        buildTreeNodes();
        arrayPanel.revalidate(); // Revalidate arrayPanel to update preferred size
        arrayPanel.repaint();
        repaint();
    }

    void calculateTotalSteps() {
        // A more generous estimate for granular steps in Heap Sort
        // This is an initial estimate for the progress bar. The final totalSteps will be
        // set to currentStep when the algorithm completes for perfect X/X display.
        totalSteps = arraySize * 25; // Increased from 15 to 25 per element for more buffer
    }

    void resetSortingVariables() {
        isAnimating = false;
        isBuildingHeap = true;
        isExtracting = false;
        currentBuildIndex = arraySize / 2 - 1; // Start from the last non-leaf node
        heapSize = arraySize; // Reset heap size to full array size

        // Clear highlighting variables
        heapifyIndex = -1;
        leftChild = -1;
        rightChild = -1;
        largestIndex = -1;
        swapIndex1 = -1;
        swapIndex2 = -1;
        currentStep = 0;

        // NEW: Initialize heapify stack
        heapifyStack = new ArrayDeque<>();

        statusLabel.setText("<html><center>🎯 Ready to sort!<br>Click 'Start Heap Sort' to begin</center></html>");
        stepLabel.setText("Step: 0 / " + totalSteps);
    }

    void resetArray() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        generateRandomArray(arraySize);
        startButton.setText("Start Heap Sort");
        startButton.setEnabled(true);
        startButton.setBackground(new Color(46, 204, 113));
    }

    void showCustomInputDialog() {
        if (isAnimating) return;

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sizeLabel = new JLabel("Array Size (4-15):");
        sizeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField sizeField = new JTextField("10");

        JLabel elementsLabel = new JLabel("Elements (space-separated):");
        elementsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField elementsField = new JTextField("4 10 3 5 1 8 9 2 7 6");

        inputPanel.add(sizeLabel);
        inputPanel.add(sizeField);
        inputPanel.add(elementsLabel);
        inputPanel.add(elementsField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "🌳 Custom Heap Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int size = Integer.parseInt(sizeField.getText().trim());
                if (size < 4 || size > 15) {
                    JOptionPane.showMessageDialog(this, "❌ Size must be between 4 and 15");
                    return;
                }

                String[] elements = elementsField.getText().trim().split("\\s+");
                if (elements.length != size) {
                    JOptionPane.showMessageDialog(this, "❌ Please enter exactly " + size + " numbers");
                    return;
                }

                arraySize = size;
                heapSize = arraySize;
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
                buildTreeNodes();
                arrayPanel.revalidate(); // Revalidate arrayPanel to update preferred size
                arrayPanel.repaint();
                repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "❌ Please enter valid numbers");
            }
        }
    }

    void startHeapSort() {
        if (isAnimating) return;

        // Reset state to ensure a clean start if not already reset
        resetSortingVariables();

        isAnimating = true;
        startButton.setText("Sorting...");
        startButton.setEnabled(false);
        startButton.setBackground(new Color(231, 76, 60));

        int delay = 1200 - (speedSlider.getValue() * 100);
        animationTimer.setDelay(delay);
        animationTimer.start();

        statusLabel.setText("<html><center>🚀 Building Max Heap...<br>Phase 1 of 2</center></html>");
        updateExplanation("HEAP SORT STARTED!\n\n" +
                "Heap Sort works in two main phases:\n\n" +
                "PHASE 1: BUILD MAX HEAP\n" +
                "We'll convert the array into a max heap where each parent node is larger than its children. " +
                "Starting from the last non-leaf node and working backwards.\n\n" +
                "PHASE 2: EXTRACT ELEMENTS\n" +
                "We'll repeatedly extract the maximum element (root) and place it at the end, " +
                "then restore the heap property.\n\n" +
                "Starting with building the heap...");
    }

    void performHeapSortStep() {
        currentStep++;
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);

        if (isBuildingHeap) {
            performBuildHeapStep();
        } else if (isExtracting) {
            performExtractStep();
        }

        buildTreeNodes(); // Rebuild tree nodes to reflect current state for drawing
        repaint();
    }

    void performBuildHeapStep() {
        // If the heapify stack is empty, it means the previous heapify operation is done
        // or we are starting a new one for the next element in the build phase.
        if (heapifyStack.isEmpty()) {
            currentBuildIndex--; // Move to the next parent node to heapify
            if (currentBuildIndex < 0) {
                // Finished building heap
                isBuildingHeap = false;
                isExtracting = true;
                // currentExtractIndex is now implicitly heapSize - 1
                clearHighlights(); // Clear any remaining highlights
                statusLabel.setText("<html><center>✅ Max Heap Built!<br>🔄 Extracting Elements...</center></html>");
                updateExplanation("MAX HEAP CONSTRUCTION COMPLETED!\n\n" +
                        "The array has been successfully converted into a max heap! " +
                        "Notice how every parent node is now larger than its children.\n\n" +
                        "PHASE 2: EXTRACTING ELEMENTS\n" +
                        "Now we'll repeatedly:\n" +
                        "1. Swap the root (maximum) with the last element\n" +
                        "2. Reduce heap size\n" +
                        "3. Heapify the root to maintain heap property\n\n" +
                        "This will sort the array in ascending order.");
                return;
            }
            // Start a new heapify operation for the currentBuildIndex
            heapifyStack.push(new HeapifyCall(currentBuildIndex));
            updateExplanation("🔧 Starting to heapify node " + currentBuildIndex + " (value: " + array[currentBuildIndex] + ").");
        }

        // Process one step of the current heapify operation from the stack
        processHeapifyStepFromStack();
    }

    void performExtractStep() {
        // If the heapify stack is empty, it means the previous heapify operation is done
        // or we are starting a new extraction step.
        if (heapifyStack.isEmpty()) {
            if (heapSize <= 1) { // Sorting complete (heapSize 1 means only one element left, which is sorted)
                // Sorting complete
                completeAnalysis(); // Call completeAnalysis to finalize state
                return;
            }

            // Perform the swap (root with last element of current heap)
            swapIndex1 = 0;
            swapIndex2 = heapSize - 1; // Last element of current heap
            int temp = array[0];
            array[0] = array[heapSize - 1];
            array[heapSize - 1] = temp;

            statusLabel.setText("<html><center>🔄 Extracted: " + array[heapSize - 1] +
                    "<br>Swapped with root</center></html>");
            updateExplanation("🔄 EXTRACTING MAXIMUM ELEMENT\n\n" +
                    "Extracted maximum: " + array[heapSize - 1] + " (now at index " + (heapSize - 1) + ")\n" +
                    "This element is now in its final sorted position.\n\n" +
                    "PROCESS:\n" +
                    "1. Swapped root (" + array[heapSize - 1] + ") with last heap element (" + array[0] + ")\n" +
                    "2. Reduced heap size from " + heapSize + " to " + (heapSize - 1) + "\n" +
                    "3. Now preparing to heapify the new root to restore max heap property.\n\n" +
                    "Remaining elements to sort: " + (heapSize - 1));

            // Reduce heap size
            heapSize--;

            // Start heapify from root (index 0)
            heapifyStack.push(new HeapifyCall(0));
        }

        // Process one step of the current heapify operation from the stack
        processHeapifyStepFromStack();
    }

    // This method processes one step of the maxHeapify operation using the stack
    void processHeapifyStepFromStack() {
        if (heapifyStack.isEmpty()) {
            clearHighlights(); // Ensure no highlights if stack is empty
            return;
        }

        HeapifyCall currentCall = heapifyStack.peek(); // Get the current call without removing it
        heapifyIndex = currentCall.index; // Highlight the current root being heapified
        largestIndex = currentCall.largest; // Update largestIndex for highlighting

        int i = currentCall.index;
        int l = currentCall.leftChildIdx;
        int r = currentCall.rightChildIdx;

        // Clear comparison/swap highlights from previous step, unless we are in a swap state
        if (currentCall.state != HeapifyCall.PERFORM_SWAP) {
            leftChild = -1;
            rightChild = -1;
            swapIndex1 = -1;
            swapIndex2 = -1;
        }

        switch (currentCall.state) {
            case HeapifyCall.INIT:
                currentCall.largest = i;
                largestIndex = i; // Update largestIndex for highlighting
                currentCall.state = HeapifyCall.COMPARE_LEFT;
                updateExplanation("Heapifying node " + i + " (value: " + array[i] + "). Initializing comparison.");
                break;

            case HeapifyCall.COMPARE_LEFT:
                leftChild = l; // Highlight left child
                if (l < heapSize) {
                    updateExplanation("Comparing " + array[i] + " (parent) with " + array[l] + " (left child).");
                    if (array[l] > array[currentCall.largest]) {
                        currentCall.largest = l;
                        largestIndex = l; // Update largestIndex for highlighting
                        updateExplanation("Left child (" + array[l] + ") is larger than current largest (" + array[i] + ").");
                    }
                } else {
                    updateExplanation("No left child for node " + i + ".");
                }
                currentCall.state = HeapifyCall.COMPARE_RIGHT;
                break;

            case HeapifyCall.COMPARE_RIGHT:
                rightChild = r; // Highlight right child
                if (r < heapSize) {
                    updateExplanation("Comparing " + array[currentCall.largest] + " (current largest) with " + array[r] + " (right child).");
                    if (array[r] > array[currentCall.largest]) {
                        currentCall.largest = r;
                        largestIndex = r; // Update largestIndex for highlighting
                        updateExplanation("Right child (" + array[r] + ") is larger than current largest (" + array[currentCall.largest] + ").");
                    }
                } else {
                    updateExplanation("No right child for node " + i + ".");
                }
                currentCall.state = HeapifyCall.CHECK_SWAP;
                break;

            case HeapifyCall.CHECK_SWAP:
                if (currentCall.largest != i) {
                    // A swap is needed. Prepare for the swap animation.
                    swapIndex1 = i;
                    swapIndex2 = currentCall.largest;
                    updateExplanation("Heap property violated! Preparing to swap " + array[i] + " with " + array[currentCall.largest] + ".");
                    currentCall.state = HeapifyCall.PERFORM_SWAP;
                } else {
                    // No swap needed, this heapify operation is done.
                    updateExplanation("Node " + i + " is already a max heap. No swap needed.");
                    currentCall.state = HeapifyCall.DONE;
                    heapifyStack.pop(); // This call is done, remove from stack
                    clearHighlights(); // Clear highlights for this completed heapify
                }
                break;

            case HeapifyCall.PERFORM_SWAP:
                // Perform the actual swap
                int temp = array[i];
                array[i] = array[currentCall.largest];
                array[currentCall.largest] = temp;
                updateExplanation("Swapped " + array[swapIndex1] + " and " + array[swapIndex2] + ".");
                swapIndex1 = -1; // Clear swap highlights
                swapIndex2 = -1;
                currentCall.state = HeapifyCall.RECURSE_CHILD;
                break;

            case HeapifyCall.RECURSE_CHILD:
                // After a swap, we need to recursively heapify the subtree where the swapped element went.
                // Push a new HeapifyCall for the child onto the stack.
                // The current call (parent) remains on the stack, waiting for the child to complete.
                heapifyStack.push(new HeapifyCall(currentCall.largest));
                updateExplanation("Recursively heapifying subtree at index " + currentCall.largest + " (new value: " + array[currentCall.largest] + ").");
                currentCall.state = HeapifyCall.WAITING_FOR_CHILD; // Parent is now waiting for child
                break;

            case HeapifyCall.WAITING_FOR_CHILD: // NEW STATE
                // This state means the parent is waiting for its child's heapify to complete.
                // If the child's call is no longer on top of the stack (meaning it was popped),
                // then this parent call can now be marked as DONE.
                if (heapifyStack.peek() == currentCall) { // Child has completed and this parent call is now on top
                    currentCall.state = HeapifyCall.DONE;
                    heapifyStack.pop(); // Now this parent call is truly done
                    clearHighlights(); // Clear highlights for this completed heapify
                }
                // If heapifyStack.peek() is NOT currentCall, it means a child call is still on top,
                // so the parent should continue waiting. No action needed in this step.
                break;

            case HeapifyCall.DONE:
                // This state indicates that the current HeapifyCall has completed its work
                // and has been popped from the stack.
                // This case should ideally not be hit if the stack management is perfect.
                // If it's still on stack, pop it.
                if (!heapifyStack.isEmpty() && heapifyStack.peek() == currentCall) {
                    heapifyStack.pop();
                }
                clearHighlights(); // Clear highlights for this completed heapify
                break;
        }
    }

    void completeAnalysis() {
        animationTimer.stop();
        isAnimating = false;
        startButton.setText("Completed");
        startButton.setBackground(new Color(46, 204, 113));
        clearHighlights(); // Clear any remaining highlights

        heapSize = 0; // Set heapSize to 0 to ensure all elements are marked as sorted

        // Update totalSteps to match currentStep for accurate final display
        totalSteps = currentStep;
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);

        statusLabel.setText("<html><center>🎉 Heap Sort Complete!<br>Array is now sorted!</center></html>");
        updateExplanation("HEAP SORT COMPLETED!\n\n" +
                "Congratulations! The heap sort algorithm has successfully sorted the array.\n\n" +
                "SUMMARY:\n" +
                "• Phase 1: Built a max heap from the unsorted array\n" +
                "• Phase 2: Repeatedly extracted the maximum element\n" +
                "• Result: Array sorted in ascending order\n\n" +
                "Time Complexity: O(n log n)\n" +
                "Space Complexity: O(1)\n" +
                "Heap sort is an in-place, comparison-based sorting algorithm that guarantees " +
                "O(n log n) performance in all cases!");
        repaint(); // Repaint to show final sorted state
    }

    void clearHighlights() {
        heapifyIndex = -1;
        leftChild = -1;
        rightChild = -1;
        largestIndex = -1;
        swapIndex1 = -1;
        swapIndex2 = -1;
    }

    void buildTreeNodes() {
        treeNodes = new ArrayList<>();
        if (arraySize == 0) return;

        int panelWidth = 800;
        int panelHeight = 350;

        // Calculate positions for tree nodes
        for (int i = 0; i < arraySize; i++) {
            int level = (int)(Math.log(i + 1) / Math.log(2));
            int positionInLevel = i - ((int)Math.pow(2, level) - 1);
            int nodesInLevel = (int)Math.pow(2, level);

            double spacing = (double)panelWidth / (nodesInLevel + 1);
            int x = (int)(panelWidth / 2 + (positionInLevel - nodesInLevel / 2.0 + 0.5) * spacing);
            int y = 60 + level * 80;

            TreeNode node = new TreeNode(array[i], i, x, y);

            // Set colors based on current state
            if (i >= heapSize) {
                node.color = sortedColor;
            } else if (i == heapifyIndex) {
                node.color = heapifyColor;
            } else if (i == leftChild || i == rightChild) {
                node.color = compareColor;
            } else if (i == largestIndex && largestIndex != heapifyIndex) {
                node.color = maxColor;
            } else if (i == swapIndex1 || i == swapIndex2) {
                node.color = swapColor;
            } else {
                node.color = defaultColor;
            }

            treeNodes.add(node);
        }
    }

    void drawHeapTree(Graphics2D g2d) {
        if (treeNodes == null || treeNodes.isEmpty()) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw connections first
        g2d.setColor(treeLineColor);
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i < treeNodes.size(); i++) {
            TreeNode parent = treeNodes.get(i);
            int leftChildIndex = 2 * i + 1;
            int rightChildIndex = 2 * i + 2;

            if (leftChildIndex < treeNodes.size()) {
                TreeNode leftChild = treeNodes.get(leftChildIndex);
                g2d.drawLine(parent.x, parent.y, leftChild.x, leftChild.y);
            }

            if (rightChildIndex < treeNodes.size()) {
                TreeNode rightChild = treeNodes.get(rightChildIndex);
                g2d.drawLine(parent.x, parent.y, rightChild.x, rightChild.y);
            }
        }

        // Draw nodes
        for (TreeNode node : treeNodes) {
            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillOval(node.x - 22, node.y - 22, 44, 44);

            // Draw node circle with gradient
            GradientPaint gradient = new GradientPaint(node.x - 20, node.y - 20, node.color,
                    node.x + 20, node.y + 20, node.color.darker());
            g2d.setPaint(gradient);
            g2d.fillOval(node.x - 20, node.y - 20, 40, 40);

            // Draw border
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(node.x - 20, node.y - 20, 40, 40);

            // Draw value
            g2d.setColor(node.color == defaultColor || node.color == sortedColor ?
                    new Color(44, 62, 80) : Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String text = String.valueOf(node.value);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = node.x - fm.stringWidth(text) / 2;
            int textY = node.y + fm.getAscent() / 2 - 2;
            g2d.drawString(text, textX, textY);

            // Draw index below node
            g2d.setColor(new Color(108, 117, 125));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String indexText = String.valueOf(node.index);
            FontMetrics indexFm = g2d.getFontMetrics();
            int indexX = node.x - indexFm.stringWidth(indexText) / 2;
            g2d.drawString(indexText, indexX, node.y + 35);
        }

        // Draw legend
        drawTreeLegend(g2d);
    }

    void drawTreeLegend(Graphics2D g2d) {
        String[] labels = {"Heap Element", "Heapifying", "Comparing", "Largest", "Swapping", "Sorted"};
        Color[] colors = {defaultColor, heapifyColor, compareColor, maxColor, swapColor, sortedColor};

        int legendX = 20;
        int legendY = 20;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.setColor(new Color(44, 62, 80));

        legendY += 20;
        for (int i = 0; i < labels.length; i++) {
            // Draw color circle
            g2d.setColor(colors[i]);
            g2d.fillOval(legendX, legendY - 8, 12, 12);
            g2d.setColor(borderColor);
            g2d.drawOval(legendX, legendY - 8, 12, 12);

            // Draw label
            g2d.setColor(new Color(44, 62, 80));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.drawString(labels[i], legendX + 18, legendY);

            legendY += 16;
        }
    }

    void drawArray(Graphics2D g2d) {
        if (array == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fixed width for each box to ensure readability
        int boxWidth = 60;
        int boxHeight = 50;
        int spacing = 10; // Spacing between boxes

        // Fixed left padding for the first element.
        int startX = 20;
        int startY = (arrayPanel.getHeight() - boxHeight) / 2; // Center vertically

        // Draw array elements
        for (int i = 0; i < arraySize; i++) {
            int x = startX + i * (boxWidth + spacing);
            int y = startY;

            Color boxColor = defaultColor;
            if (i >= heapSize) { // This condition now correctly colors all elements as sorted when heapSize is 0
                boxColor = sortedColor;
            } else if (i == heapifyIndex) {
                boxColor = heapifyColor;
            } else if (i == leftChild || i == rightChild) {
                boxColor = compareColor;
            } else if (i == largestIndex && largestIndex != heapifyIndex) {
                boxColor = maxColor;
            } else if (i == swapIndex1 || i == swapIndex2) {
                boxColor = swapColor;
            }

            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 20));
            g2d.fillRoundRect(x + 2, y + 2, boxWidth, boxHeight, 8, 8);

            // Draw box with gradient
            GradientPaint gradient = new GradientPaint(x, y, boxColor, x, y + boxHeight, boxColor.darker());
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, 8, 8);

            // Draw border
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, 8, 8);

            // Draw number
            Color textColor = (boxColor == defaultColor || boxColor == sortedColor) ?
                    new Color(44, 62, 80) : Color.WHITE;
            g2d.setColor(textColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String text = String.valueOf(array[i]);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (boxWidth - fm.stringWidth(text)) / 2;
            int textY = y + (boxHeight + fm.getAscent()) / 2 - 2;
            g2d.drawString(text, textX, textY);

            // Draw index
            g2d.setColor(new Color(108, 117, 125));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String indexText = String.valueOf(i);
            FontMetrics indexFm = g2d.getFontMetrics();
            int indexX = x + (boxWidth - indexFm.stringWidth(indexText)) / 2;
            g2d.drawString(indexText, indexX, y + boxHeight + 12);
        }

        // Draw heap size indicator ONLY if animating and heap is not fully sorted
        if (isAnimating && heapSize < arraySize) {
            int heapEndX = startX + heapSize * (boxWidth + spacing) - (spacing / 2); // Position the line between boxes
            g2d.setColor(new Color(231, 76, 60));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(heapEndX, startY - 10, heapEndX, startY + boxHeight + 10);

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.drawString("Heap Size: " + heapSize, heapEndX + 5, startY + boxHeight / 2);
        }
    }

    void updateInitialExplanation() {
        updateExplanation("HEAP SORT ALGORITHM\n\n" +
                "Heap Sort is a comparison-based sorting algorithm that uses a binary heap data structure. " +
                "It's an in-place algorithm with guaranteed O(n log n) time complexity.\n\n" +
                "HOW IT WORKS:\n" +
                "1. BUILD MAX HEAP: Convert the array into a max heap where each parent ≥ children\n" +
                "2. EXTRACT ELEMENTS: Repeatedly remove the maximum (root) and place it at the end\n\n" +
                "HEAP PROPERTIES:\n" +
                "• Complete binary tree (filled left to right)\n" +
                "• Max heap: parent ≥ children\n" +
                "• Array representation: parent at i, children at 2i+1 and 2i+2\n\n" +
                "ADVANTAGES:\n" +
                "• Guaranteed O(n log n) performance\n" +
                "• In-place sorting (O(1) space)\n" +
                "• Not affected by input distribution\n\n" +
                "Click 'Start Heap Sort' to see the algorithm in action!");
    }

    void updateExplanation(String text) {
        explanationArea.setText(text);
        explanationArea.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HeapSortVisualizer();
            }
        });
    }
}
