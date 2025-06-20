import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.border.TitledBorder;

public class AlgorithmComplexityAnalyzer extends JFrame {
    // GUI Components
    JPanel mainPanel, headerPanel, tablePanel, chartPanel, comparisonPanel, controlPanel;
    JTable complexityTable;
    DefaultTableModel tableModel;
    JComboBox<String> categoryFilter, complexityFilter;
    JButton compareButton, resetButton, analyzeButton;
    JTextArea analysisArea, comparisonArea;
    JScrollPane tableScrollPane, analysisScrollPane, comparisonScrollPane;

    // Algorithm Data
    private final AlgorithmData[] algorithms = {
            // Prime Number Algorithms
            new AlgorithmData("Iterative Prime Check", "Prime Numbers", "O(√n)", "O(√n)", "O(√n)",
                    "Checks divisibility up to square root", Color.decode("#3498db")),
            new AlgorithmData("Sieve of Eratosthenes", "Prime Numbers", "O(n log log n)", "O(n log log n)", "O(n log log n)",
                    "Efficient for finding all primes in range", Color.decode("#2ecc71")),

            // Sorting Algorithms
            new AlgorithmData("Bubble Sort", "Sorting", "O(n²)", "O(n)", "O(n²)",
                    "Simple comparison-based sorting", Color.decode("#e74c3c")),
            new AlgorithmData("Quick Sort", "Sorting", "O(n log n)", "O(n log n)", "O(n²)",
                    "Divide-and-conquer with pivot", Color.decode("#f39c12")),
            new AlgorithmData("Merge Sort", "Sorting", "O(n log n)", "O(n log n)", "O(n log n)",
                    "Stable divide-and-conquer sorting", Color.decode("#9b59b6")),
            new AlgorithmData("Insertion Sort", "Sorting", "O(n²)", "O(n)", "O(n²)",
                    "Efficient for small datasets", Color.decode("#1abc9c")),
            new AlgorithmData("Selection Sort", "Sorting", "O(n²)", "O(n²)", "O(n²)",
                    "Finds minimum/maximum repeatedly", Color.decode("#34495e")),
            new AlgorithmData("Heap Sort", "Sorting", "O(n log n)", "O(n log n)", "O(n log n)",
                    "Uses binary heap data structure", Color.decode("#e67e22"))
    };

    private final Map<String, Integer> complexityRanking = new HashMap<>();
    private final Map<String, Color> complexityColors = new HashMap<>();

    public AlgorithmComplexityAnalyzer() {
        initializeComplexityMaps();
        initializeGUI();
        populateTable();
    }

    void initializeComplexityMaps() {
        // Ranking complexities from best to worst
        complexityRanking.put("O(1)", 1);
        complexityRanking.put("O(log n)", 2);
        complexityRanking.put("O(√n)", 3);
        complexityRanking.put("O(n)", 4);
        complexityRanking.put("O(n log n)", 5);
        complexityRanking.put("O(n log log n)", 6);
        complexityRanking.put("O(n²)", 7);
        complexityRanking.put("O(n³)", 8);
        complexityRanking.put("O(2ⁿ)", 9);
        complexityRanking.put("O(n!)", 10);

        // Color coding for complexities
        complexityColors.put("O(1)", new Color(46, 204, 113));        // Green - Excellent
        complexityColors.put("O(log n)", new Color(52, 152, 219));    // Blue - Very Good
        complexityColors.put("O(√n)", new Color(26, 188, 156));       // Teal - Good
        complexityColors.put("O(n)", new Color(155, 89, 182));        // Purple - Fair
        complexityColors.put("O(n log n)", new Color(241, 196, 15));  // Yellow - Acceptable
        complexityColors.put("O(n log log n)", new Color(230, 126, 34)); // Orange - Acceptable
        complexityColors.put("O(n²)", new Color(231, 76, 60));        // Red - Poor
        complexityColors.put("O(n³)", new Color(192, 57, 43));        // Dark Red - Very Poor
        complexityColors.put("O(2ⁿ)", new Color(142, 68, 173));       // Dark Purple - Terrible
        complexityColors.put("O(n!)", new Color(44, 62, 80));         // Dark Gray - Worst
    }

    void initializeGUI() {
        setTitle("🔬 Algorithm Time Complexity Analyzer & Comparator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(248, 249, 250));

        createHeaderPanel();
        createMainLayout();

        setVisible(true);
    }

    void createHeaderPanel() {
        headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(74, 144, 226),
                        getWidth(), 0, new Color(155, 89, 182));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw complexity symbols
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.setFont(new Font("Serif", Font.BOLD, 16));
                String[] symbols = {"O(1)", "O(n)", "O(log n)", "O(n²)", "O(n!)", "∞"};
                for (int i = 0; i < symbols.length; i++) {
                    int x = (i * 150 + 50) % getWidth();
                    int y = 20 + (i % 2) * 25;
                    g2d.drawString(symbols[i], x, y);
                }
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));

        JLabel titleLabel = new JLabel("🔬 ALGORITHM TIME COMPLEXITY ANALYZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
    }

    void createMainLayout() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(248, 249, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create control panel
        createControlPanel();

        // Create table panel
        createTablePanel();

        // Create analysis and comparison panels
        createAnalysisPanel();

        // Layout arrangement
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 249, 250));
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(tablePanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, comparisonPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.6);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    void createControlPanel() {
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Category filter
        JLabel categoryLabel = new JLabel("📂 Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoryFilter = new JComboBox<>(new String[]{"All Categories", "Prime Numbers", "Sorting"});
        categoryFilter.setPreferredSize(new Dimension(150, 30));
        categoryFilter.addActionListener(e -> filterTable());

        // Complexity filter
        JLabel complexityLabel = new JLabel("⚡ Max Complexity:");
        complexityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        complexityFilter = new JComboBox<>(new String[]{"All", "O(n log n) or better", "O(n²) or better", "Show worst only"});
        complexityFilter.setPreferredSize(new Dimension(180, 30));
        complexityFilter.addActionListener(e -> filterTable());

        // Buttons
        compareButton = createStyledButton("🔍 Compare Selected", new Color(52, 152, 219));
        compareButton.addActionListener(e -> compareSelectedAlgorithms());

        analyzeButton = createStyledButton("📊 Analyze All", new Color(46, 204, 113));
        analyzeButton.addActionListener(e -> analyzeAllAlgorithms());

        resetButton = createStyledButton("🔄 Reset", new Color(155, 89, 182));
        resetButton.addActionListener(e -> resetFilters());

        controlPanel.add(categoryLabel);
        controlPanel.add(categoryFilter);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(complexityLabel);
        controlPanel.add(complexityFilter);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(compareButton);
        controlPanel.add(analyzeButton);
        controlPanel.add(resetButton);
    }

    JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 32));
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

    void createTablePanel() {
        tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBackground(new Color(248, 249, 250));

        // Create table
        String[] columnNames = {"Algorithm", "Category", "Average Case", "Best Case", "Worst Case", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        complexityTable = new JTable(tableModel);
        complexityTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        complexityTable.setRowHeight(35);
        complexityTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        complexityTable.setGridColor(new Color(220, 220, 220));
        complexityTable.setSelectionBackground(new Color(52, 152, 219, 100));

        // Custom cell renderer for complexity coloring
        complexityTable.setDefaultRenderer(Object.class, new ComplexityTableCellRenderer());

        // Column widths
        TableColumnModel columnModel = complexityTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(180); // Algorithm
        columnModel.getColumn(1).setPreferredWidth(120); // Category
        columnModel.getColumn(2).setPreferredWidth(120); // Average
        columnModel.getColumn(3).setPreferredWidth(120); // Best
        columnModel.getColumn(4).setPreferredWidth(120); // Worst
        columnModel.getColumn(5).setPreferredWidth(300); // Description

        tableScrollPane = new JScrollPane(complexityTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "📊 Algorithm Complexity Comparison Table",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(52, 152, 219)
        ));

        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add complexity legend
        createComplexityLegend();
    }

    void createComplexityLegend() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 1),
                "🎨 Complexity Legend",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), new Color(155, 89, 182)
        ));

        String[] complexities = {"O(1)", "O(log n)", "O(√n)", "O(n)", "O(n log n)", "O(n²)"};
        String[] labels = {"Constant", "Logarithmic", "Square Root", "Linear", "Linearithmic", "Quadratic"};

        for (int i = 0; i < complexities.length; i++) {
            JPanel legendItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            legendItem.setBackground(Color.WHITE);

            JLabel colorBox = new JLabel("  ");
            colorBox.setOpaque(true);
            colorBox.setBackground(complexityColors.get(complexities[i]));
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            colorBox.setPreferredSize(new Dimension(20, 15));

            JLabel textLabel = new JLabel(complexities[i] + " (" + labels[i] + ")");
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            legendItem.add(colorBox);
            legendItem.add(textLabel);
            legendPanel.add(legendItem);
        }

        tablePanel.add(legendPanel, BorderLayout.SOUTH);
    }

    void createAnalysisPanel() {
        comparisonPanel = new JPanel(new BorderLayout(10, 10));
        comparisonPanel.setBackground(new Color(248, 249, 250));

        // Analysis area
        JPanel analysisPanel = new JPanel(new BorderLayout());
        analysisPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "📈 Complexity Analysis & Insights",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(46, 204, 113)
        ));

        analysisArea = new JTextArea(8, 40);
        analysisArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        analysisArea.setBackground(Color.WHITE);
        analysisArea.setForeground(new Color(44, 62, 80));
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        analysisArea.setEditable(false);
        analysisArea.setMargin(new Insets(10, 10, 10, 10));

        analysisScrollPane = new JScrollPane(analysisArea);
        analysisScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        analysisPanel.add(analysisScrollPane, BorderLayout.CENTER);

        // Comparison area
        JPanel comparisonSubPanel = new JPanel(new BorderLayout());
        comparisonSubPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
                "⚔️ Algorithm Comparison Results",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(231, 76, 60)
        ));

        comparisonArea = new JTextArea(8, 40);
        comparisonArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        comparisonArea.setBackground(new Color(248, 249, 250));
        comparisonArea.setForeground(new Color(44, 62, 80));
        comparisonArea.setLineWrap(true);
        comparisonArea.setWrapStyleWord(true);
        comparisonArea.setEditable(false);
        comparisonArea.setMargin(new Insets(10, 10, 10, 10));

        comparisonScrollPane = new JScrollPane(comparisonArea);
        comparisonScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        comparisonSubPanel.add(comparisonScrollPane, BorderLayout.CENTER);

        // Split the analysis panel
        JSplitPane analysisSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, analysisPanel, comparisonSubPanel);
        analysisSplitPane.setDividerLocation(500);
        analysisSplitPane.setResizeWeight(0.5);

        comparisonPanel.add(analysisSplitPane, BorderLayout.CENTER);

        // Initialize with default analysis
        showDefaultAnalysis();
    }

    void populateTable() {
        tableModel.setRowCount(0);
        for (AlgorithmData algo : algorithms) {
            Object[] row = {
                    algo.name,
                    algo.category,
                    algo.averageCase,
                    algo.bestCase,
                    algo.worstCase,
                    algo.description
            };
            tableModel.addRow(row);
        }
    }

    void filterTable() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String selectedComplexity = (String) complexityFilter.getSelectedItem();

        tableModel.setRowCount(0);

        for (AlgorithmData algo : algorithms) {
            boolean categoryMatch = selectedCategory.equals("All Categories") ||
                    algo.category.equals(selectedCategory);

            boolean complexityMatch = true;
            if (selectedComplexity.equals("O(n log n) or better")) {
                complexityMatch = isComplexityBetterOrEqual(algo.averageCase, "O(n log n)");
            } else if (selectedComplexity.equals("O(n²) or better")) {
                complexityMatch = isComplexityBetterOrEqual(algo.averageCase, "O(n²)");
            } else if (selectedComplexity.equals("Show worst only")) {
                complexityMatch = algo.averageCase.equals("O(n²)") || algo.averageCase.equals("O(n³)");
            }

            if (categoryMatch && complexityMatch) {
                Object[] row = {
                        algo.name,
                        algo.category,
                        algo.averageCase,
                        algo.bestCase,
                        algo.worstCase,
                        algo.description
                };
                tableModel.addRow(row);
            }
        }
    }

    boolean isComplexityBetterOrEqual(String complexity, String threshold) {
        Integer complexityRank = complexityRanking.get(complexity);
        Integer thresholdRank = complexityRanking.get(threshold);

        if (complexityRank == null || thresholdRank == null) return true;
        return complexityRank <= thresholdRank;
    }

    void compareSelectedAlgorithms() {
        int[] selectedRows = complexityTable.getSelectedRows();
        if (selectedRows.length < 2) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least 2 algorithms to compare!",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder comparison = new StringBuilder();
        comparison.append("🔍 ALGORITHM COMPARISON ANALYSIS\n");
        comparison.append("=" .repeat(50)).append("\n\n");

        List<AlgorithmData> selectedAlgos = new ArrayList<>();
        for (int row : selectedRows) {
            String algoName = (String) tableModel.getValueAt(row, 0);
            for (AlgorithmData algo : algorithms) {
                if (algo.name.equals(algoName)) {
                    selectedAlgos.add(algo);
                    break;
                }
            }
        }

        // Group by category
        Map<String, List<AlgorithmData>> categoryGroups = new HashMap<>();
        for (AlgorithmData algo : selectedAlgos) {
            categoryGroups.computeIfAbsent(algo.category, k -> new ArrayList<>()).add(algo);
        }

        for (Map.Entry<String, List<AlgorithmData>> entry : categoryGroups.entrySet()) {
            String category = entry.getKey();
            List<AlgorithmData> algos = entry.getValue();

            comparison.append("📂 ").append(category.toUpperCase()).append(" ALGORITHMS:\n");
            comparison.append("-".repeat(30)).append("\n");

            // Find best and worst in category
            AlgorithmData bestAvg = algos.stream()
                    .min((a, b) -> compareComplexity(a.averageCase, b.averageCase))
                    .orElse(null);
            AlgorithmData worstAvg = algos.stream()
                    .max((a, b) -> compareComplexity(a.averageCase, b.averageCase))
                    .orElse(null);

            for (AlgorithmData algo : algos) {
                comparison.append("• ").append(algo.name).append(":\n");
                comparison.append("  Average: ").append(algo.averageCase);
                if (algo == bestAvg) comparison.append(" ⭐ BEST");
                if (algo == worstAvg && algos.size() > 1) comparison.append(" ⚠️ WORST");
                comparison.append("\n");
                comparison.append("  Best: ").append(algo.bestCase);
                comparison.append(" | Worst: ").append(algo.worstCase).append("\n");
                comparison.append("  ").append(algo.description).append("\n\n");
            }

            // Category recommendations
            if (category.equals("Sorting")) {
                comparison.append("🎯 SORTING RECOMMENDATIONS:\n");
                comparison.append("• For small datasets (n < 50): Insertion Sort\n");
                comparison.append("• For general purpose: Merge Sort (stable) or Quick Sort (faster average)\n");
                comparison.append("• For guaranteed O(n log n): Merge Sort or Heap Sort\n");
                comparison.append("• Avoid: Bubble Sort, Selection Sort for large datasets\n\n");
            } else if (category.equals("Prime Numbers")) {
                comparison.append("🎯 PRIME NUMBER RECOMMENDATIONS:\n");
                comparison.append("• For single number check: Iterative method\n");
                comparison.append("• For finding all primes in range: Sieve of Eratosthenes\n");
                comparison.append("• Sieve is much more efficient for multiple prime queries\n\n");
            }
        }

        // Overall comparison
        comparison.append("🏆 OVERALL COMPARISON SUMMARY:\n");
        comparison.append("=" .repeat(35)).append("\n");

        AlgorithmData overallBest = selectedAlgos.stream()
                .min((a, b) -> compareComplexity(a.averageCase, b.averageCase))
                .orElse(null);
        AlgorithmData overallWorst = selectedAlgos.stream()
                .max((a, b) -> compareComplexity(a.averageCase, b.averageCase))
                .orElse(null);

        if (overallBest != null) {
            comparison.append("🥇 Best Average Case: ").append(overallBest.name)
                    .append(" (").append(overallBest.averageCase).append(")\n");
        }
        if (overallWorst != null) {
            comparison.append("🥉 Worst Average Case: ").append(overallWorst.name)
                    .append(" (").append(overallWorst.averageCase).append(")\n");
        }

        comparisonArea.setText(comparison.toString());
        comparisonArea.setCaretPosition(0);
    }

    int compareComplexity(String complexity1, String complexity2) {
        Integer rank1 = complexityRanking.get(complexity1);
        Integer rank2 = complexityRanking.get(complexity2);

        if (rank1 == null) rank1 = 999;
        if (rank2 == null) rank2 = 999;

        return Integer.compare(rank1, rank2);
    }

    void analyzeAllAlgorithms() {
        StringBuilder analysis = new StringBuilder();
        analysis.append("📊 COMPREHENSIVE ALGORITHM ANALYSIS\n");
        analysis.append("=" .repeat(45)).append("\n\n");

        // Complexity distribution
        Map<String, Integer> complexityCount = new HashMap<>();
        for (AlgorithmData algo : algorithms) {
            complexityCount.merge(algo.averageCase, 1, Integer::sum);
        }

        analysis.append("📈 COMPLEXITY DISTRIBUTION:\n");
        analysis.append("-".repeat(25)).append("\n");
        complexityCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByKey((a, b) -> compareComplexity(a, b)))
                .forEach(entry -> {
                    analysis.append("• ").append(entry.getKey()).append(": ")
                            .append(entry.getValue()).append(" algorithm(s)\n");
                });

        analysis.append("\n🏆 PERFORMANCE RANKINGS:\n");
        analysis.append("-".repeat(22)).append("\n");

        // Best performers by category
        Map<String, List<AlgorithmData>> categoryMap = new HashMap<>();
        for (AlgorithmData algo : algorithms) {
            categoryMap.computeIfAbsent(algo.category, k -> new ArrayList<>()).add(algo);
        }

        for (Map.Entry<String, List<AlgorithmData>> entry : categoryMap.entrySet()) {
            String category = entry.getKey();
            List<AlgorithmData> algos = entry.getValue();

            analysis.append("\n📂 ").append(category).append(":\n");
            algos.stream()
                    .sorted((a, b) -> compareComplexity(a.averageCase, b.averageCase))
                    .forEach(algo -> {
                        analysis.append("  ").append(getRankEmoji(algo.averageCase))
                                .append(" ").append(algo.name).append(" (").append(algo.averageCase).append(")\n");
                    });
        }

        analysis.append("\n💡 KEY INSIGHTS:\n");
        analysis.append("-".repeat(15)).append("\n");
        analysis.append("• Merge Sort offers the most consistent performance (always O(n log n))\n");
        analysis.append("• Quick Sort has best average case but worst case can be O(n²)\n");
        analysis.append("• Sieve of Eratosthenes is superior for multiple prime number queries\n");
        analysis.append("• Bubble Sort and Selection Sort should be avoided for large datasets\n");
        analysis.append("• Insertion Sort is surprisingly efficient for small datasets\n");

        analysis.append("\n🎯 ALGORITHM SELECTION GUIDE:\n");
        analysis.append("-".repeat(28)).append("\n");
        analysis.append("• Small datasets (n < 100): Insertion Sort\n");
        analysis.append("• Need stability: Merge Sort\n");
        analysis.append("• Best average performance: Quick Sort\n");
        analysis.append("• Guaranteed performance: Merge Sort or Heap Sort\n");
        analysis.append("• Memory constrained: Heap Sort (in-place)\n");
        analysis.append("• Educational purposes: Bubble Sort (simple to understand)\n");

        analysis.append("\n⚠️ COMPLEXITY WARNINGS:\n");
        analysis.append("-".repeat(20)).append("\n");
        analysis.append("• O(n²) algorithms become impractical for n > 10,000\n");
        analysis.append("• Quick Sort's worst case O(n²) occurs with poor pivot selection\n");
        analysis.append("• Always consider the nature of your input data\n");
        analysis.append("• Best case scenarios rarely occur in practice\n");

        analysisArea.setText(analysis.toString());
        analysisArea.setCaretPosition(0);
    }

    String getRankEmoji(String complexity) {
        Integer rank = complexityRanking.get(complexity);
        if (rank == null) return "❓";

        switch (rank) {
            case 1: case 2: return "🥇";
            case 3: case 4: return "🥈";
            case 5: case 6: return "🥉";
            default: return "⚠️";
        }
    }

    void showDefaultAnalysis() {
        String defaultText = "🔬 ALGORITHM COMPLEXITY ANALYZER\n\n" +
                "Welcome to the comprehensive algorithm analysis tool!\n\n" +
                "📊 FEATURES:\n" +
                "• Compare time complexities across different algorithms\n" +
                "• Filter by category (Prime Numbers, Sorting)\n" +
                "• Visual complexity ranking with color coding\n" +
                "• Detailed analysis and recommendations\n\n" +
                "🎯 HOW TO USE:\n" +
                "1. Browse the algorithm table above\n" +
                "2. Use filters to narrow down algorithms\n" +
                "3. Select multiple algorithms and click 'Compare Selected'\n" +
                "4. Click 'Analyze All' for comprehensive insights\n\n" +
                "💡 TIP: Algorithms are color-coded by complexity:\n" +
                "Green = Excellent, Blue = Very Good, Yellow = Acceptable, Red = Poor\n\n" +
                "Select algorithms from the table above to begin comparison!";

        analysisArea.setText(defaultText);

        String comparisonText = "⚔️ ALGORITHM COMPARISON\n\n" +
                "Select 2 or more algorithms from the table above and click\n" +
                "'Compare Selected' to see detailed comparisons.\n\n" +
                "🔍 COMPARISON FEATURES:\n" +
                "• Side-by-side complexity analysis\n" +
                "• Best and worst performers identification\n" +
                "• Category-specific recommendations\n" +
                "• Use case suggestions\n" +
                "• Performance trade-offs analysis\n\n" +
                "Ready to compare algorithms!";

        comparisonArea.setText(comparisonText);
    }

    void resetFilters() {
        categoryFilter.setSelectedIndex(0);
        complexityFilter.setSelectedIndex(0);
        complexityTable.clearSelection();
        populateTable();
        showDefaultAnalysis();
    }

    // Custom table cell renderer for complexity coloring
    class ComplexityTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // Color code complexity columns
                if (column >= 2 && column <= 4 && value != null) {
                    String complexity = value.toString();
                    Color bgColor = complexityColors.get(complexity);
                    if (bgColor != null) {
                        c.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 50));
                        setForeground(bgColor.darker());
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
            }

            return c;
        }
    }

    // Algorithm data class
    static class AlgorithmData {
        String name, category, averageCase, bestCase, worstCase, description;
        Color color;

        AlgorithmData(String name, String category, String averageCase, String bestCase,
                      String worstCase, String description, Color color) {
            this.name = name;
            this.category = category;
            this.averageCase = averageCase;
            this.bestCase = bestCase;
            this.worstCase = worstCase;
            this.description = description;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AlgorithmComplexityAnalyzer());
    }
}