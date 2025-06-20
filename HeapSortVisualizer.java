import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class HeapSortVisualizer extends JFrame {
    int[] array;
    int arraySize;
    int heapSize;
    JPanel treePanel, arrayPanel, explanationPanel;
    JButton startButton, resetButton, customInputButton;
    JSlider speedSlider;
    JLabel speedLabel, statusLabel, stepLabel;
    JTextArea explanationArea;
    Timer animationTimer;

    // Animation variables
    boolean isAnimating = false;
    boolean isBuildingHeap = true;
    boolean isExtracting = false;
    int currentBuildIndex;
    int currentExtractIndex;
    int heapifyIndex = -1;
    int leftChild = -1;
    int rightChild = -1;
    int largestIndex = -1;
    boolean isSwapping = false;
    int swapIndex1 = -1;
    int swapIndex2 = -1;
    int animationPhase = 0; // 0: building heap, 1: extracting elements
    int currentStep = 0;
    int totalSteps = 0;

    // Tree visualization variables
    List<TreeNode> treeNodes;

    // Colors
    Color defaultColor = new Color(240, 248, 255);
    Color heapifyColor = new Color(231, 76, 60);
    Color compareColor = new Color(255, 193, 7);
    Color swapColor = new Color(46, 204, 113);
    Color sortedColor = new Color(52, 152, 219);
    Color maxColor = new Color(155, 89, 182);
    Color borderColor = new Color(44, 62, 80);
    Color treeLineColor = new Color(108, 117, 125);

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

        JLabel titleLabel = new JLabel("🌳 HEAP SORT VISUALIZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(new Color(248, 249, 250));

        startButton = createStyledButton("▶ Start Heap Sort", new Color(46, 204, 113));
        resetButton = createStyledButton("🔄 Reset", new Color(52, 152, 219));
        customInputButton = createStyledButton("⚙ Custom Input", new Color(155, 89, 182));

        speedLabel = new JLabel("⚡ Animation Speed:");
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
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(stepLabel);

        // Main visualization panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(248, 249, 250));

        // Tree panel
        treePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(),
                        new Color(248, 249, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                drawHeapTree(g2d);
            }
        };
        treePanel.setPreferredSize(new Dimension(800, 400));
        treePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(106, 90, 205), 2),
                "🌳 Max Heap Tree Structure",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(106, 90, 205)
        ));

        // Array panel
        arrayPanel = new JPanel() {
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
        };
        arrayPanel.setPreferredSize(new Dimension(800, 150));
        arrayPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "📊 Array Representation",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 152, 219)
        ));

        mainPanel.add(treePanel, BorderLayout.CENTER);
        mainPanel.add(arrayPanel, BorderLayout.SOUTH);

        // Status and explanation panel
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.setPreferredSize(new Dimension(400, 0));

        statusLabel = new JLabel("<html><center>🎯 Ready to sort!<br>Click 'Start Heap Sort' to begin</center></html>", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(44, 62, 80));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.WHITE);

        explanationPanel = new JPanel(new BorderLayout());
        explanationPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "📚 Algorithm Explanation",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(46, 204, 113)
        ));
        explanationPanel.setBackground(Color.WHITE);

        explanationArea = new JTextArea(20, 30);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        explanationArea.setBackground(Color.WHITE);
        explanationArea.setForeground(new Color(44, 62, 80));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setEditable(false);
        explanationArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(explanationArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        explanationPanel.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(statusLabel, BorderLayout.NORTH);
        rightPanel.add(explanationPanel, BorderLayout.CENTER);

        // Layout
        add(headerPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

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

        animationTimer = new Timer(800, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performHeapSortStep();
            }
        });

        updateInitialExplanation();
        pack();
        setLocationRelativeTo(null);
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
        repaint();
    }

    void calculateTotalSteps() {
        // Estimate steps: build heap + extract elements
        totalSteps = arraySize + (arraySize - 1) * 3; // Rough estimate
    }

    void resetSortingVariables() {
        isAnimating = false;
        isBuildingHeap = true;
        isExtracting = false;
        currentBuildIndex = arraySize / 2 - 1;
        currentExtractIndex = arraySize - 1;
        heapifyIndex = -1;
        leftChild = -1;
        rightChild = -1;
        largestIndex = -1;
        isSwapping = false;
        swapIndex1 = -1;
        swapIndex2 = -1;
        animationPhase = 0;
        currentStep = 0;
        heapSize = arraySize;

        statusLabel.setText("<html><center>🎯 Ready to sort!<br>Click 'Start Heap Sort' to begin</center></html>");
        stepLabel.setText("Step: 0 / " + totalSteps);
    }

    void resetArray() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        generateRandomArray(arraySize);
        startButton.setText("▶ Start Heap Sort");
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
                    if (array[i] < 1 || array[i] > 999) {
                        JOptionPane.showMessageDialog(this, "❌ Numbers must be between 1 and 999");
                        return;
                    }
                }

                calculateTotalSteps();
                resetSortingVariables();
                buildTreeNodes();
                repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "❌ Please enter valid numbers");
            }
        }
    }

    void startHeapSort() {
        if (isAnimating) return;

        isAnimating = true;
        startButton.setText("⏸ Sorting...");
        startButton.setEnabled(false);
        startButton.setBackground(new Color(231, 76, 60));

        int delay = 1200 - (speedSlider.getValue() * 100);
        animationTimer.setDelay(delay);
        animationTimer.start();

        statusLabel.setText("<html><center>🚀 Building Max Heap...<br>Phase 1 of 2</center></html>");
        updateExplanation("🎬 HEAP SORT STARTED!\n\n" +
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

        buildTreeNodes();
        repaint();
    }

    void performBuildHeapStep() {
        if (currentBuildIndex < 0) {
            // Finished building heap
            isBuildingHeap = false;
            isExtracting = true;
            currentExtractIndex = arraySize - 1;
            statusLabel.setText("<html><center>✅ Max Heap Built!<br>🔄 Extracting Elements...</center></html>");
            updateExplanation("✅ MAX HEAP CONSTRUCTION COMPLETED!\n\n" +
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

        // Heapify current node
        heapifyIndex = currentBuildIndex;
        maxHeapify(heapifyIndex);
        currentBuildIndex--;

        statusLabel.setText("<html><center>🔧 Heapifying node " + heapifyIndex +
                "<br>Value: " + array[heapifyIndex] + "</center></html>");
        updateExplanation("🔧 HEAPIFYING NODE " + heapifyIndex + "\n\n" +
                "Current node value: " + array[heapifyIndex] + "\n" +
                "Checking if this node satisfies the max heap property (parent ≥ children).\n\n" +
                "We compare with left child" + (leftChild < heapSize ? " (" + array[leftChild] + ")" : " (none)") +
                " and right child" + (rightChild < heapSize ? " (" + array[rightChild] + ")" : " (none)") + ".\n\n" +
                (largestIndex != heapifyIndex ?
                        "Heap property violated! Swapping " + array[heapifyIndex] + " with " + array[largestIndex] + "." :
                        "Heap property satisfied! No swap needed.") +
                "\n\nRemaining nodes to heapify: " + (currentBuildIndex + 1));
    }

    void performExtractStep() {
        if (currentExtractIndex <= 0) {
            // Sorting complete
            animationTimer.stop();
            isAnimating = false;
            startButton.setText("✅ Completed");
            startButton.setBackground(new Color(46, 204, 113));
            statusLabel.setText("<html><center>🎉 Heap Sort Complete!<br>Array is now sorted!</center></html>");
            updateExplanation("🎉 HEAP SORT COMPLETED!\n\n" +
                    "Congratulations! The heap sort algorithm has successfully sorted the array.\n\n" +
                    "SUMMARY:\n" +
                    "• Phase 1: Built a max heap from the unsorted array\n" +
                    "• Phase 2: Repeatedly extracted the maximum element\n" +
                    "• Result: Array sorted in ascending order\n\n" +
                    "Time Complexity: O(n log n)\n" +
                    "Space Complexity: O(1)\n" +
                    "Heap sort is an in-place, comparison-based sorting algorithm that guarantees " +
                    "O(n log n) performance in all cases!");
            return;
        }

        // Swap root with last element
        swapIndex1 = 0;
        swapIndex2 = currentExtractIndex;
        int temp = array[0];
        array[0] = array[currentExtractIndex];
        array[currentExtractIndex] = temp;

        statusLabel.setText("<html><center>🔄 Extracted: " + array[currentExtractIndex] +
                "<br>Swapped with root</center></html>");
        updateExplanation("🔄 EXTRACTING MAXIMUM ELEMENT\n\n" +
                "Extracted maximum: " + array[currentExtractIndex] + "\n" +
                "This element is now in its final sorted position at index " + currentExtractIndex + ".\n\n" +
                "PROCESS:\n" +
                "1. Swapped root (" + array[currentExtractIndex] + ") with last heap element (" + array[0] + ")\n" +
                "2. Reduced heap size from " + heapSize + " to " + (heapSize - 1) + "\n" +
                "3. Now heapifying the new root to restore max heap property\n\n" +
                "Remaining elements to sort: " + currentExtractIndex);

        // Reduce heap size and heapify root
        heapSize--;
        currentExtractIndex--;
        heapifyIndex = 0;
        maxHeapify(0);
    }

    void maxHeapify(int index) {
        leftChild = 2 * index + 1;
        rightChild = 2 * index + 2;
        largestIndex = index;

        // Find largest among node and its children
        if (leftChild < heapSize && array[leftChild] > array[largestIndex]) {
            largestIndex = leftChild;
        }

        if (rightChild < heapSize && array[rightChild] > array[largestIndex]) {
            largestIndex = rightChild;
        }

        // If largest is not the current node, swap and continue heapifying
        if (largestIndex != index) {
            int temp = array[index];
            array[index] = array[largestIndex];
            array[largestIndex] = temp;
        }
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

            // Fixed the casting issue here
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
        g2d.drawString("Legend:", legendX, legendY);

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

        int panelWidth = arrayPanel.getWidth();
        int panelHeight = arrayPanel.getHeight();
        int boxWidth = Math.min(60, (panelWidth - 40) / arraySize);
        int boxHeight = 50;
        int startX = (panelWidth - (boxWidth * arraySize + 10 * (arraySize - 1))) / 2;
        int startY = (panelHeight - boxHeight) / 2;

        // Draw array elements
        for (int i = 0; i < arraySize; i++) {
            int x = startX + i * (boxWidth + 10);
            int y = startY;

            Color boxColor = defaultColor;
            if (i >= heapSize) {
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

        // Draw heap size indicator
        if (heapSize < arraySize) {
            int heapEndX = startX + heapSize * (boxWidth + 10) - 5;
            g2d.setColor(new Color(231, 76, 60));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(heapEndX, startY - 10, heapEndX, startY + boxHeight + 10);

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.drawString("Heap Size: " + heapSize, heapEndX + 5, startY + boxHeight / 2);
        }
    }

    void updateInitialExplanation() {
        updateExplanation("🌳 HEAP SORT ALGORITHM\n\n" +
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