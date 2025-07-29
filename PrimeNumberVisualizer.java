import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PrimeNumberVisualizer extends JFrame {
    // GUI Components
    JPanel mainPanel, controlPanel, visualizationPanel, resultsPanel;
    JScrollPane visualizationScrollPane, resultsScrollPane;
    JButton startButton, resetButton, switchModeButton, pauseButton;
    boolean isPaused = false;
    JTextField lowerBoundField, upperBoundField, singleNumberField;
    JLabel modeLabel, statusLabel, performanceLabel;
    JLabel rangeFromLabel, rangeToLabel, singleNumberLabel;
    JTextArea resultsArea, stepsArea;
    JComboBox<String> algorithmComboBox;
    JSlider speedSlider;
    javax.swing.Timer animationTimer;

    // Algorithm and Mode Variables
    boolean isMode1 = true; // true = Range Mode, false = Single Number Mode
    String currentAlgorithm = "Iterative";
    boolean isAnimating = false;

    // Range Mode Variables
    int lowerBound = 2;
    int upperBound = 50;
    int[] numbers;
    boolean[] isPrime;
    boolean[] isMarked;
    int currentNumber = 2;
    int currentMultiple = 4;
    int animationStep = 0;
    List<Integer> foundPrimes;
    long startTime, endTime;

    // Single Number Mode Variables
    int targetNumber = 17;
    int currentDivisor = 2;
    boolean isPrimeResult = true;

    // Sieve Variables
    int sieveCurrentPrime = 2;
    int sieveStep = 0; // 0: finding next prime, 1: marking multiples

    // Animation and Visual Variables
    int highlightedIndex = -1;
    int markingIndex = -1;
    Color primeColor = new Color(46, 204, 113);
    Color compositeColor = new Color(231, 76, 60);
    Color currentColor = new Color(52, 152, 219);
    Color markingColor = new Color(255, 193, 7);
    Color neutralColor = new Color(236, 240, 241);
    Color backgroundColor = new Color(248, 249, 250);

    public PrimeNumberVisualizer() {
        initializeGUI();
        initializeMode1();
    }

    void initializeGUI() {
        setTitle("Prime Number Visualizer - Interactive Learning Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(backgroundColor);

        createHeaderPanel();
        createMainLayout();

        // FIX: Create animation timer with dynamic delay based on speed slider
        animationTimer = new javax.swing.Timer(getAnimationDelay(), e -> performAnimationStep());

        // FIX: Add change listener to speed slider to update timer delay
        speedSlider.addChangeListener(e -> {
            if (animationTimer != null) {
                animationTimer.setDelay(getAnimationDelay());
            }
        });

        setVisible(true);
    }

    // FIX: Add method to calculate animation delay based on slider value
    private int getAnimationDelay() {
        // Convert slider value (1-10) to delay (1000ms-100ms)
        // Higher slider value = faster animation = lower delay
        int sliderValue = speedSlider.getValue();
        return 1100 - (sliderValue * 100); // Range: 1000ms (slow) to 100ms (fast)
    }

    void createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(74, 144, 226),
                        getWidth(), 0, new Color(46, 204, 113));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw mathematical symbols
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.setFont(new Font("Serif", Font.BOLD, 16));
                String[] symbols = {"π", "∑", "∞", "√", "∫", "∆"};
                for (int i = 0; i < symbols.length; i++) {
                    int x = (i * 120 + 40) % getWidth();
                    int y = 15 + (i % 2) * 20;
                    g2d.drawString(symbols[i], x, y);
                }
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 60)); // Reduced from 100 to 60

        JLabel titleLabel = new JLabel("PRIME NUMBER VISUALIZER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Reduced from 36 to 24
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); // Reduced padding
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
    }

    void createMainLayout() {
        // Create main container with BorderLayout
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(backgroundColor);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create left panel for controls (compact)
        createControlPanel();

        // Create center panel for visualization (large)
        createVisualizationPanel();

        // Create right panel for results and info
        createResultsPanel();

        // Add panels to main container
        mainContainer.add(controlPanel, BorderLayout.WEST);
        mainContainer.add(visualizationScrollPane, BorderLayout.CENTER);
        mainContainer.add(resultsPanel, BorderLayout.EAST);

        add(mainContainer, BorderLayout.CENTER);
    }

    void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(backgroundColor);
        controlPanel.setPreferredSize(new Dimension(280, 0)); // Fixed width, flexible height
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                        "Controls",
                        0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(52, 152, 219)
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Mode Section
        JPanel modeSection = createSection("Mode & Algorithm");

        switchModeButton = createCompactButton("Switch Mode", new Color(155, 89, 182));
        switchModeButton.addActionListener(e -> switchMode());

        modeLabel = new JLabel("Range Analysis");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        modeLabel.setForeground(new Color(44, 62, 80));

        JLabel algorithmLabel = new JLabel("Algorithm:");
        algorithmLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        algorithmComboBox = new JComboBox<>(new String[]{"Iterative", "Sieve of Eratosthenes"});
        algorithmComboBox.setPreferredSize(new Dimension(200, 25));
        algorithmComboBox.setMaximumSize(new Dimension(200, 25));
        algorithmComboBox.addActionListener(e -> {
            currentAlgorithm = (String) algorithmComboBox.getSelectedItem();
            updateControlsForAlgorithm();
        });

        modeSection.add(switchModeButton);
        modeSection.add(Box.createVerticalStrut(5));
        modeSection.add(modeLabel);
        modeSection.add(Box.createVerticalStrut(5));
        modeSection.add(algorithmLabel);
        modeSection.add(algorithmComboBox);

        // Input Section
        JPanel inputSection = createSection("Input Parameters");

        // Range input components
        JLabel rangeLabel = new JLabel("Range:");
        rangeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        rangePanel.setBackground(backgroundColor);
        rangeFromLabel = new JLabel("From:");
        rangeFromLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lowerBoundField = new JTextField("2", 4);
        lowerBoundField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeToLabel = new JLabel("To:");
        rangeToLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        upperBoundField = new JTextField("50", 4);
        upperBoundField.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        rangePanel.add(rangeFromLabel);
        rangePanel.add(lowerBoundField);
        rangePanel.add(rangeToLabel);
        rangePanel.add(upperBoundField);

        // Single number input components
        singleNumberLabel = new JLabel("Number to Check:");
        singleNumberLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        singleNumberField = new JTextField("17", 8);
        singleNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        singleNumberField.setMaximumSize(new Dimension(100, 25));

        inputSection.add(rangeLabel);
        inputSection.add(rangePanel);
        inputSection.add(singleNumberLabel);
        inputSection.add(singleNumberField);

        // Hide single number components initially
        singleNumberLabel.setVisible(false);
        singleNumberField.setVisible(false);

        // Speed Section
        JPanel speedSection = createSection("Animation Speed");

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setPreferredSize(new Dimension(200, 30));
        speedSlider.setMaximumSize(new Dimension(200, 30));
        speedSlider.setBackground(backgroundColor);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setFont(new Font("Segoe UI", Font.PLAIN, 9));

        speedSection.add(speedLabel);
        speedSection.add(speedSlider);

        // Action Buttons Section
        JPanel buttonSection = createSection("Actions");

        startButton = createCompactButton("Start Analysis", new Color(46, 204, 113));
        resetButton = createCompactButton("Reset", new Color(52, 152, 219));
        pauseButton = createCompactButton("Pause", new Color(255, 165, 0));
        pauseButton.setEnabled(false);

        buttonSection.add(startButton);
        buttonSection.add(Box.createVerticalStrut(5));
        buttonSection.add(resetButton);
        buttonSection.add(Box.createVerticalStrut(5));
        buttonSection.add(pauseButton);
        buttonSection.add(Box.createVerticalStrut(5));
        JButton backButton = createCompactButton("Back to Hub", new Color(100, 149, 237));
        backButton.addActionListener(e -> {
            if (animationTimer != null) animationTimer.stop();
            dispose(); // Close this window
        });

        startButton.addActionListener(e -> startAnalysis());
        resetButton.addActionListener(e -> resetVisualization());
        pauseButton.addActionListener(e -> togglePause());

        buttonSection.add(backButton); // Add this line

        // Add all sections to control panel
        controlPanel.add(modeSection);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(inputSection);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(speedSection);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(buttonSection);
        controlPanel.add(Box.createVerticalGlue()); // Push everything to top
    }

    JPanel createSection(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(backgroundColor);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        title,
                        0, 0, new Font("Segoe UI", Font.BOLD, 10), new Color(100, 100, 100)
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }

    JButton createCompactButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 30));
        button.setMaximumSize(new Dimension(200, 30));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
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

    void createVisualizationPanel() {
        // Visualization panel
        visualizationPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (isMode1) {
                    drawRangeVisualization(g2d);
                } else {
                    drawSingleNumberVisualization(g2d);
                }
            }

            public Dimension getPreferredSize() {
                if (isMode1 && numbers != null) {
                    int cols = Math.min(10, numbers.length); // Changed from 20 to 10
                    int rows = (numbers.length + cols - 1) / cols;
                    return new Dimension(cols * 80 + 100, rows * 80 + 200); // Adjusted for larger cells
                }
                return new Dimension(1000, 700); // Larger default size
            }
        };
        visualizationPanel.setBackground(Color.WHITE);

        visualizationScrollPane = new JScrollPane(visualizationPanel);
        visualizationScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Prime Number Visualization",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 152, 219)
        ));
        visualizationScrollPane.setPreferredSize(new Dimension(800, 600)); // Large visualization area
    }

    void createResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout(5, 5));
        resultsPanel.setBackground(backgroundColor);
        resultsPanel.setPreferredSize(new Dimension(350, 0)); // Fixed width, flexible height
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Top section: Status and Performance
        JPanel topSection = new JPanel(new BorderLayout(5, 5));
        topSection.setBackground(backgroundColor);

        // Status panel (smaller)
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "Status",
                0, 0, new Font("Segoe UI", Font.BOLD, 11), new Color(46, 204, 113)
        ));
        statusPanel.setPreferredSize(new Dimension(350, 70)); // Smaller height

        statusLabel = new JLabel("<html><center>Ready to analyze!<br>Click Start to begin</center></html>", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setForeground(new Color(44, 62, 80));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Performance panel (bigger with scroll)
        JPanel performancePanel = new JPanel(new BorderLayout());
        performancePanel.setBackground(Color.WHITE);
        performancePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
                "Performance Metrics",
                0, 0, new Font("Segoe UI", Font.BOLD, 11), new Color(155, 89, 182)
        ));
        performancePanel.setPreferredSize(new Dimension(350, 150)); // Bigger height

        performanceLabel = new JLabel("<html><center>Execution Time: --<br>Numbers Checked: --<br>Primes Found: --<br>Progress: --<br>Algorithm: " + currentAlgorithm + "</center></html>", JLabel.CENTER);
        performanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        performanceLabel.setForeground(new Color(44, 62, 80));

        JScrollPane performanceScrollPane = new JScrollPane(performanceLabel);
        performanceScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        performanceScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        performancePanel.add(performanceScrollPane, BorderLayout.CENTER);

        topSection.add(statusPanel, BorderLayout.NORTH);
        topSection.add(performancePanel, BorderLayout.CENTER);

        // Bottom section: Steps and Results
        JPanel bottomSection = new JPanel(new BorderLayout(5, 5));
        bottomSection.setBackground(backgroundColor);

        // Steps explanation panel (bigger)
        JPanel stepsPanel = new JPanel(new BorderLayout());
        stepsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
                "Step-by-Step Process",
                0, 0, new Font("Segoe UI", Font.BOLD, 11), new Color(231, 76, 60)
        ));
        stepsPanel.setPreferredSize(new Dimension(350, 200)); // Bigger height

        stepsArea = new JTextArea(15, 25); // More rows
        stepsArea.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        stepsArea.setBackground(Color.WHITE);
        stepsArea.setForeground(new Color(44, 62, 80));
        stepsArea.setLineWrap(true);
        stepsArea.setWrapStyleWord(true);
        stepsArea.setEditable(false);
        stepsArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane stepsScrollPane = new JScrollPane(stepsArea);
        stepsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        stepsPanel.add(stepsScrollPane, BorderLayout.CENTER);

        // Results panel
        JPanel resultsSubPanel = new JPanel(new BorderLayout());
        resultsSubPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "Results",
                0, 0, new Font("Segoe UI", Font.BOLD, 11), new Color(46, 204, 113)
        ));

        resultsArea = new JTextArea(8, 25);
        resultsArea.setFont(new Font("Segoe UI", Font.BOLD, 10));
        resultsArea.setBackground(new Color(248, 249, 250));
        resultsArea.setForeground(new Color(46, 204, 113));
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setEditable(false);
        resultsArea.setMargin(new Insets(8, 8, 8, 8));

        resultsScrollPane = new JScrollPane(resultsArea);
        resultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        resultsSubPanel.add(resultsScrollPane, BorderLayout.CENTER);

        bottomSection.add(stepsPanel, BorderLayout.CENTER);
        bottomSection.add(resultsSubPanel, BorderLayout.SOUTH);

        resultsPanel.add(topSection, BorderLayout.NORTH);
        resultsPanel.add(bottomSection, BorderLayout.CENTER);
    }

    void switchMode() {
        if (isAnimating) return;

        isMode1 = !isMode1;

        if (isMode1) {
            // Switch to Range Mode
            switchModeButton.setText("Switch Mode");
            modeLabel.setText("Range Analysis");

            // Show range components
            rangeFromLabel.setVisible(true);
            lowerBoundField.setVisible(true);
            rangeToLabel.setVisible(true);
            upperBoundField.setVisible(true);
            rangeFromLabel.getParent().setVisible(true);

            // Hide single number components
            singleNumberLabel.setVisible(false);
            singleNumberField.setVisible(false);

            initializeMode1();
        } else {
            // Switch to Single Number Mode
            switchModeButton.setText("Switch Mode");
            modeLabel.setText("Single Number Check");

            // Hide range components
            rangeFromLabel.setVisible(false);
            lowerBoundField.setVisible(false);
            rangeToLabel.setVisible(false);
            upperBoundField.setVisible(false);
            rangeFromLabel.getParent().setVisible(false);

            // Show single number components
            singleNumberLabel.setVisible(true);
            singleNumberField.setVisible(true);

            initializeMode2();
        }

        controlPanel.revalidate();
        controlPanel.repaint();
        visualizationPanel.revalidate();
        visualizationPanel.repaint();
        resetVisualization();
    }

    void updateControlsForAlgorithm() {
        if (!isMode1 && currentAlgorithm.equals("Sieve of Eratosthenes")) {
            JOptionPane.showMessageDialog(this,
                    "Sieve of Eratosthenes is designed for finding all primes in a range.\n" +
                            "Switching to Range Mode for this algorithm.",
                    "Algorithm Notice", JOptionPane.INFORMATION_MESSAGE);
            switchMode();
        }
    }

    void initializeMode1() {
        try {
            lowerBound = Math.max(2, Integer.parseInt(lowerBoundField.getText().trim()));
            upperBound = Math.max(lowerBound, Integer.parseInt(upperBoundField.getText().trim()));

            // Update fields with validated values
            lowerBoundField.setText(String.valueOf(lowerBound));
            upperBoundField.setText(String.valueOf(upperBound));
        } catch (NumberFormatException e) {
            lowerBound = 2;
            upperBound = 50;
            lowerBoundField.setText("2");
            upperBoundField.setText("50");
        }

        int size = upperBound - lowerBound + 1;
        numbers = new int[size];
        isPrime = new boolean[size];
        isMarked = new boolean[size];
        foundPrimes = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            numbers[i] = lowerBound + i;
            isPrime[i] = true;
            isMarked[i] = false;
        }

        // 1 is not prime, and if our range includes it
        if (lowerBound == 1) {
            isPrime[0] = false;
        }

        resetAnimationVariables();
        updateStepsArea("PRIME NUMBER RANGE ANALYSIS\n\n" +
                "Ready to analyze numbers from " + lowerBound + " to " + upperBound + "\n" +
                "Algorithm: " + currentAlgorithm + "\n\n" +
                "Click 'Start Analysis' to begin the visualization!");
    }

    void initializeMode2() {
        try {
            targetNumber = Math.max(2, Integer.parseInt(singleNumberField.getText().trim()));
            singleNumberField.setText(String.valueOf(targetNumber));
        } catch (NumberFormatException e) {
            targetNumber = 17;
            singleNumberField.setText("17");
        }

        currentDivisor = 2;
        isPrimeResult = true;
        resetAnimationVariables();
        updateStepsArea("SINGLE NUMBER PRIME CHECK\n\n" +
                "Ready to check if " + targetNumber + " is prime\n" +
                "Algorithm: " + currentAlgorithm + "\n\n" +
                "Enter a number in the field above and click 'Start Analysis' to begin!");
    }

    void resetAnimationVariables() {
        currentNumber = lowerBound;
        currentMultiple = currentNumber * 2;
        animationStep = 0;
        sieveCurrentPrime = 2;
        sieveStep = 0;
        highlightedIndex = -1;
        markingIndex = -1;
        isAnimating = false;

        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    void startAnalysis() {
        if (isAnimating) return;

        if (isMode1) {
            initializeMode1();
        } else {
            initializeMode2();
        }

        isAnimating = true;
        isPaused = false;
        startButton.setText("Analyzing...");
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        pauseButton.setText("Pause");
        pauseButton.setBackground(new Color(255, 165, 0));

        startTime = System.currentTimeMillis();

        // FIX: Update timer delay when starting animation
        animationTimer.setDelay(getAnimationDelay());
        animationTimer.start();

        if (isMode1) {
            statusLabel.setText("<html><center>Analyzing range<br>" + lowerBound + " to " + upperBound + "</center></html>");
        } else {
            statusLabel.setText("<html><center>Checking number<br>" + targetNumber + "</center></html>");
        }

        resultsArea.setText("");
    }

    void resetVisualization() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }

        isAnimating = false;
        isPaused = false;
        startButton.setText("Start Analysis");
        startButton.setEnabled(true);
        startButton.setBackground(new Color(46, 204, 113));
        pauseButton.setEnabled(false);
        pauseButton.setText("Pause");
        pauseButton.setBackground(new Color(255, 165, 0));

        if (isMode1) {
            initializeMode1();
        } else {
            initializeMode2();
        }

        statusLabel.setText("<html><center>Ready!<br>Click Start</center></html>");
        performanceLabel.setText("<html><center>Execution Time: --<br>Numbers Checked: --<br>Primes Found: --<br>Progress: --<br>Algorithm: " + currentAlgorithm + "</center></html>");
        resultsArea.setText("");

        visualizationPanel.repaint();
    }

    void performAnimationStep() {
        if (!isAnimating) return;

        if (isMode1) {
            if (currentAlgorithm.equals("Iterative")) {
                performIterativeRangeStep();
            } else {
                performSieveStep();
            }
        } else {
            performSingleNumberStep();
        }

        visualizationPanel.repaint();
    }

    void performIterativeRangeStep() {
        if (currentNumber > upperBound) {
            completeAnalysis();
            return;
        }

        int index = currentNumber - lowerBound;
        highlightedIndex = index;

        if (currentNumber < 2) {
            isPrime[index] = false;
            updateStepsArea("CHECKING NUMBER: " + currentNumber + "\n\n" +
                    "Numbers less than 2 are not prime by definition.\n" +
                    currentNumber + " is NOT PRIME.\n\n" +
                    "Moving to next number...");
        } else {
            boolean isPrimeNumber = checkIfPrime(currentNumber);
            isPrime[index] = isPrimeNumber;

            if (isPrimeNumber) {
                foundPrimes.add(currentNumber);
                updateStepsArea("PRIME FOUND: " + currentNumber + "\n\n" +
                        "Checked all divisors from 2 to √" + currentNumber + "\n" +
                        "No divisors found - " + currentNumber + " is PRIME!\n\n" +
                        "Total primes found so far: " + foundPrimes.size());
            } else {
                updateStepsArea("COMPOSITE NUMBER: " + currentNumber + "\n\n" +
                        "Found a divisor - " + currentNumber + " is NOT PRIME.\n\n" +
                        "Moving to next number...");
            }
        }

        currentNumber++;

        // Update performance metrics
        int checked = currentNumber - lowerBound;
        int progress = (checked * 100) / (upperBound - lowerBound + 1);
        performanceLabel.setText("<html><center>Time: " +
                (System.currentTimeMillis() - startTime) + "ms<br>" +
                "Numbers Checked: " + checked + "/" + (upperBound - lowerBound + 1) + "<br>" +
                "Primes Found: " + foundPrimes.size() + "<br>" +
                "Progress: " + progress + "%<br>" +
                "Algorithm: " + currentAlgorithm + "<br>" +
                "🎯 Current: " + (currentNumber - 1) + "</center></html>");
    }

    void performSieveStep() {
        if (sieveCurrentPrime * sieveCurrentPrime > upperBound) {
            // Mark remaining unmarked numbers as prime
            for (int i = 0; i < numbers.length; i++) {
                if (!isMarked[i] && numbers[i] >= 2) {
                    isPrime[i] = true;
                    if (!foundPrimes.contains(numbers[i])) {
                        foundPrimes.add(numbers[i]);
                    }
                }
            }
            completeAnalysis();
            return;
        }

        if (sieveStep == 0) {
            // Find next prime
            int index = sieveCurrentPrime - lowerBound;
            if (index >= 0 && index < numbers.length && !isMarked[index]) {
                highlightedIndex = index;
                isPrime[index] = true;
                if (!foundPrimes.contains(sieveCurrentPrime)) {
                    foundPrimes.add(sieveCurrentPrime);
                }

                updateStepsArea("SIEVE STEP: Found Prime " + sieveCurrentPrime + "\n\n" +
                        "Now marking all multiples of " + sieveCurrentPrime + " as composite.\n" +
                        "Starting from " + sieveCurrentPrime + "² = " + (sieveCurrentPrime * sieveCurrentPrime) + "\n\n" +
                        "Multiples to mark: ");

                currentMultiple = sieveCurrentPrime * sieveCurrentPrime;
                sieveStep = 1;
            } else {
                sieveCurrentPrime++;
            }
        } else {
            // Mark multiples
            if (currentMultiple <= upperBound) {
                int index = currentMultiple - lowerBound;
                if (index >= 0 && index < numbers.length) {
                    isMarked[index] = true;
                    isPrime[index] = false;
                    markingIndex = index;

                    updateStepsArea("MARKING COMPOSITE: " + currentMultiple + "\n\n" +
                            "Marking " + currentMultiple + " as composite (multiple of " + sieveCurrentPrime + ")\n" +
                            "Next multiple: " + (currentMultiple + sieveCurrentPrime) + "\n\n" +
                            "Primes found so far: " + foundPrimes.size());
                }
                currentMultiple += sieveCurrentPrime;
            } else {
                // Move to next prime
                sieveCurrentPrime++;
                sieveStep = 0;
                markingIndex = -1;
            }
        }

        // Update performance metrics
        int totalProcessed = 0;
        for (boolean marked : isMarked) {
            if (marked) totalProcessed++;
        }
        totalProcessed += foundPrimes.size();

        performanceLabel.setText("<html><center>Time: " +
                (System.currentTimeMillis() - startTime) + "ms<br>" +
                "Numbers Checked: " + totalProcessed + "<br>" +
                "Primes Found: " + foundPrimes.size() + "<br>" +
                "Progress: Current Prime: " + sieveCurrentPrime + "<br>" +
                "Algorithm: " + currentAlgorithm + "<br>" +
                "🎯 Marking: " + (markingIndex >= 0 ? numbers[markingIndex] : "--") + "</center></html>");
    }

    void performSingleNumberStep() {
        if (currentDivisor * currentDivisor > targetNumber) {
            // Completed check - number is prime
            completeAnalysis();
            return;
        }

        if (targetNumber % currentDivisor == 0) {
            // Found a divisor - not prime
            isPrimeResult = false;
            updateStepsArea("DIVISOR FOUND!\n\n" +
                    "Checking: " + targetNumber + " ÷ " + currentDivisor + " = " + (targetNumber / currentDivisor) + "\n" +
                    "Remainder: " + (targetNumber % currentDivisor) + "\n\n" +
                    "Since " + targetNumber + " is divisible by " + currentDivisor + ",\n" +
                    targetNumber + " is NOT PRIME.\n\n" +
                    "A prime number should only be divisible by 1 and itself.");
            completeAnalysis();
            return;
        } else {
            updateStepsArea("CHECKING DIVISOR: " + currentDivisor + "\n\n" +
                    "Testing: " + targetNumber + " ÷ " + currentDivisor + "\n" +
                    "Result: " + (targetNumber / currentDivisor) + " remainder " + (targetNumber % currentDivisor) + "\n\n" +
                    "Since remainder ≠ 0, " + currentDivisor + " is not a divisor.\n" +
                    "Continuing to check next potential divisor...\n\n" +
                    "Progress: Checking divisors up to √" + targetNumber + " ≈ " +
                    String.format("%.2f", Math.sqrt(targetNumber)));
        }

        currentDivisor++;

        // Update performance
        int maxDivisor = (int) Math.sqrt(targetNumber) + 1;
        int progress = Math.min(100, (currentDivisor - 2) * 100 / (maxDivisor - 2));
        performanceLabel.setText("<html><center>Time: " +
                (System.currentTimeMillis() - startTime) + "ms<br>" +
                "Numbers Checked: Divisors Checked: " + (currentDivisor - 2) + "<br>" +
                "Primes Found: 🎯 Testing: " + targetNumber + "<br>" +
                "Progress: " + progress + "%<br>" +
                "Algorithm: " + currentAlgorithm + "<br>" +
                "🎯 Current Divisor: " + (currentDivisor - 1) + "</center></html>");
    }

    boolean checkIfPrime(int number) {
        if (number < 2) return false;
        if (number == 2) return true;
        if (number % 2 == 0) return false;

        for (int i = 3; i * i <= number; i += 2) {
            if (number % i == 0) return false;
        }
        return true;
    }

    void completeAnalysis() {
        animationTimer.stop();
        isAnimating = false;
        isPaused = false;
        endTime = System.currentTimeMillis();

        startButton.setText("Completed");
        startButton.setBackground(new Color(46, 204, 113));
        pauseButton.setEnabled(false);
        pauseButton.setText("Pause");
        pauseButton.setBackground(new Color(255, 165, 0));

        if (isMode1) {
            Collections.sort(foundPrimes);
            StringBuilder primesText = new StringBuilder();
            for (int i = 0; i < foundPrimes.size(); i++) {
                if (i > 0) primesText.append(", ");
                primesText.append(foundPrimes.get(i));
                if ((i + 1) % 10 == 0) primesText.append("\n");
            }

            resultsArea.setText("PRIME NUMBERS FOUND:\n\n" + primesText.toString() +
                    "\n\nSUMMARY:\n" +
                    "• Range: " + lowerBound + " to " + upperBound + "\n" +
                    "• Total numbers: " + (upperBound - lowerBound + 1) + "\n" +
                    "• Prime numbers: " + foundPrimes.size() + "\n" +
                    "• Algorithm: " + currentAlgorithm + "\n" +
                    "• Execution time: " + (endTime - startTime) + "ms");

            statusLabel.setText("<html><center>Complete!<br>" + foundPrimes.size() + " primes found</center></html>");

            updateStepsArea("ANALYSIS COMPLETED!\n\n" +
                    "Successfully analyzed all numbers from " + lowerBound + " to " + upperBound + "\n\n" +
                    "RESULTS SUMMARY:\n" +
                    "• Algorithm used: " + currentAlgorithm + "\n" +
                    "• Total numbers analyzed: " + (upperBound - lowerBound + 1) + "\n" +
                    "• Prime numbers found: " + foundPrimes.size() + "\n" +
                    "• Execution time: " + (endTime - startTime) + "ms\n\n" +
                    "ALGORITHM EFFICIENCY:\n" +
                    (currentAlgorithm.equals("Sieve of Eratosthenes") ?
                            "• Time Complexity: O(n log log n)\n• Very efficient for finding all primes in a range" :
                            "• Time Complexity: O(n√n)\n• Good for checking individual numbers") + "\n\n" +
                    "Check the results panel for the complete list of prime numbers!");
        } else {
            String result = isPrimeResult ? "PRIME" : "NOT PRIME";
            String emoji = isPrimeResult ? "✅" : "❌";

            resultsArea.setText("PRIME CHECK RESULT:\n\n" +
                    "Number tested: " + targetNumber + "\n" +
                    "Result: " + targetNumber + " is " + result + " " + emoji + "\n\n" +
                    "📊 ANALYSIS DETAILS:\n" +
                    "• Algorithm: " + currentAlgorithm + "\n" +
                    "• Divisors checked: " + (currentDivisor - 2) + "\n" +
                    "• Execution time: " + (endTime - startTime) + "ms\n\n" +
                    (isPrimeResult ?
                            "✅ No divisors found between 2 and √" + targetNumber + "\n" +
                                    "Therefore, " + targetNumber + " is PRIME!" :
                            "❌ Found divisor: " + (currentDivisor - 1) + "\n" +
                                    "Therefore, " + targetNumber + " is NOT PRIME."));

            statusLabel.setText("<html><center>" + result + "!<br>Check completed</center></html>");

            updateStepsArea("PRIME CHECK COMPLETED!\n\n" +
                    "Number: " + targetNumber + "\n" +
                    "Result: " + result + " " + emoji + "\n\n" +
                    "ANALYSIS SUMMARY:\n" +
                    "• Algorithm: " + currentAlgorithm + "\n" +
                    "• Divisors tested: " + (currentDivisor - 2) + "\n" +
                    "• Range tested: 2 to " + (currentDivisor - 1) + "\n" +
                    "• Execution time: " + (endTime - startTime) + "ms\n\n" +
                    "EXPLANATION:\n" +
                    (isPrimeResult ?
                            "A prime number has exactly two factors: 1 and itself.\n" +
                                    "Since no divisors were found, " + targetNumber + " is prime!" :
                            "A composite number has more than two factors.\n" +
                                    "Since we found a divisor, " + targetNumber + " is composite."));
        }

        performanceLabel.setText("<html><center>Total: " + (endTime - startTime) + "ms<br>" +
                "Numbers Checked: Analysis: Complete<br>" +
                "Primes Found: Status: " + (isMode1 ? foundPrimes.size() + " primes found" :
                (isPrimeResult ? "Prime" : "Not Prime")) + "<br>" +
                "Progress: 100%<br>" +
                "Algorithm: " + currentAlgorithm + "<br>" +
                "🎯 Final Result: Success</center></html>");

        highlightedIndex = -1;
        markingIndex = -1;
        visualizationPanel.repaint();
    }

    void togglePause() {
        if (!isAnimating) return;

        isPaused = !isPaused;

        if (isPaused) {
            animationTimer.stop();
            pauseButton.setText("Resume");
            pauseButton.setBackground(new Color(46, 204, 113));
            statusLabel.setText("<html><center>⏸️ Analysis Paused<br>Click Resume to continue</center></html>");
        } else {
            animationTimer.start();
            pauseButton.setText("Pause");
            pauseButton.setBackground(new Color(255, 165, 0));
            if (isMode1) {
                statusLabel.setText("<html><center>▶️ Analysis Resumed<br>Finding primes...</center></html>");
            } else {
                statusLabel.setText("<html><center>▶️ Analysis Resumed<br>Checking " + targetNumber + "</center></html>");
            }
        }
    }

    void drawRangeVisualization(Graphics2D g2d) {
        if (numbers == null) return;

        int panelWidth = visualizationPanel.getWidth();
        int panelHeight = visualizationPanel.getHeight();

        // Draw title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2d.setColor(new Color(44, 62, 80));
        String title = "" + currentAlgorithm + " - Range: " + lowerBound + " to " + upperBound;
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (panelWidth - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 40);

        // Calculate grid layout (10 numbers per row)
        int cols = Math.min(10, numbers.length); // Changed from 20 to 10
        int rows = (numbers.length + cols - 1) / cols;
        int cellSize = Math.min(70, (panelWidth - 100) / cols); // Slightly larger cells
        int startX = (panelWidth - (cols * cellSize + (cols - 1) * 10)) / 2; // Increased spacing
        int startY = 80;

        // Draw numbers grid
        for (int i = 0; i < numbers.length; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = startX + col * (cellSize + 10); // Increased spacing from 8 to 10
            int y = startY + row * (cellSize + 10); // Increased spacing from 8 to 10

            // Determine color
            Color cellColor = neutralColor;
            Color textColor = new Color(44, 62, 80);

            if (i == highlightedIndex) {
                cellColor = currentColor;
                textColor = Color.WHITE;
            } else if (i == markingIndex) {
                cellColor = markingColor;
                textColor = Color.WHITE;
            } else if (isMarked[i] || !isPrime[i]) {
                cellColor = compositeColor;
                textColor = Color.WHITE;
            } else if (isPrime[i] && numbers[i] >= 2) {
                cellColor = primeColor;
                textColor = Color.WHITE;
            }

            // Draw cell shadow
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillRoundRect(x + 2, y + 2, cellSize, cellSize, 10, 10);

            // Draw cell
            g2d.setColor(cellColor);
            g2d.fillRoundRect(x, y, cellSize, cellSize, 10, 10);

            // Draw border
            g2d.setColor(new Color(44, 62, 80));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, cellSize, cellSize, 10, 10);

            // Draw number
            g2d.setColor(textColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, Math.max(14, cellSize / 4)));
            String numberText = String.valueOf(numbers[i]);
            FontMetrics numberFm = g2d.getFontMetrics();
            int textX = x + (cellSize - numberFm.stringWidth(numberText)) / 2;
            int textY = y + (cellSize + numberFm.getAscent()) / 2 - 2;
            g2d.drawString(numberText, textX, textY);
        }

        // Draw legend
        drawRangeLegend(g2d, startY + rows * (cellSize + 10) + 40);
    }

    void drawSingleNumberVisualization(Graphics2D g2d) {
        int panelWidth = visualizationPanel.getWidth();
        int panelHeight = visualizationPanel.getHeight();

        // Draw title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g2d.setColor(new Color(44, 62, 80));
        String title = "Checking if " + targetNumber + " is Prime";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (panelWidth - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 60);

        // Draw target number prominently
        int centerX = panelWidth / 2;
        int centerY = 180;
        int numberSize = 150;

        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(centerX - numberSize/2 + 4, centerY - numberSize/2 + 4, numberSize, numberSize);

        // Draw main circle
        Color numberColor = isAnimating ? currentColor : neutralColor;
        if (!isAnimating && isPrimeResult) numberColor = primeColor;
        if (!isAnimating && !isPrimeResult) numberColor = compositeColor;

        g2d.setColor(numberColor);
        g2d.fillOval(centerX - numberSize/2, centerY - numberSize/2, numberSize, numberSize);

        // Draw border
        g2d.setColor(new Color(44, 62, 80));
        g2d.setStroke(new BasicStroke(5));
        g2d.drawOval(centerX - numberSize/2, centerY - numberSize/2, numberSize, numberSize);

        // Draw target number
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 42));
        String numberText = String.valueOf(targetNumber);
        FontMetrics numberFm = g2d.getFontMetrics();
        int numberX = centerX - numberFm.stringWidth(numberText) / 2;
        int numberY = centerY + numberFm.getAscent() / 2 - 5;
        g2d.drawString(numberText, numberX, numberY);

        // Draw divisor testing visualization
        if (isAnimating) {
            drawDivisorTesting(g2d, centerX, centerY + 250);
        }

        // Draw result if completed
        if (!isAnimating) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 32));
            g2d.setColor(isPrimeResult ? primeColor : compositeColor);
            String resultText = isPrimeResult ? "✅ PRIME" : "❌ NOT PRIME";
            FontMetrics resultFm = g2d.getFontMetrics();
            int resultX = centerX - resultFm.stringWidth(resultText) / 2;
            g2d.drawString(resultText, resultX, centerY + 250);
        }
    }

    void drawDivisorTesting(Graphics2D g2d, int centerX, int centerY) {
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2d.setColor(new Color(44, 62, 80));
        String testingText = "Testing divisor: " + currentDivisor;
        FontMetrics fm = g2d.getFontMetrics();
        int textX = centerX - fm.stringWidth(testingText) / 2;
        g2d.drawString(testingText, textX, centerY - 40);

        // Draw division visualization
        int divisionY = centerY;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        String divisionText = targetNumber + " ÷ " + currentDivisor + " = " +
                (targetNumber / currentDivisor) + " remainder " + (targetNumber % currentDivisor);
        FontMetrics divFm = g2d.getFontMetrics();
        int divX = centerX - divFm.stringWidth(divisionText) / 2;
        g2d.drawString(divisionText, divX, divisionY);

        // Draw progress bar
        int maxDivisor = (int) Math.sqrt(targetNumber) + 1;
        int progress = Math.min(100, (currentDivisor - 2) * 100 / (maxDivisor - 2));

        int barWidth = 400;
        int barHeight = 25;
        int barX = centerX - barWidth / 2;
        int barY = centerY + 50;

        // Progress bar background
        g2d.setColor(new Color(236, 240, 241));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);

        // Progress bar fill
        g2d.setColor(currentColor);
        g2d.fillRoundRect(barX, barY, (barWidth * progress) / 100, barHeight, 12, 12);

        // Progress bar border
        g2d.setColor(new Color(44, 62, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);

        // Progress text
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String progressText = "Progress: " + progress + "% (checking up to √" + targetNumber + ")";
        FontMetrics progFm = g2d.getFontMetrics();
        int progX = centerX - progFm.stringWidth(progressText) / 2;
        g2d.drawString(progressText, progX, barY + barHeight + 25);
    }

    void drawRangeLegend(Graphics2D g2d, int startY) {
        String[] labels = {"Unchecked", "Current", "Prime", "Composite", "Marking"};
        Color[] colors = {neutralColor, currentColor, primeColor, compositeColor, markingColor};

        int legendX = 60;
        int legendY = startY;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2d.setColor(new Color(44, 62, 80));

        legendY += 30;
        for (int i = 0; i < labels.length; i++) {
            // Draw color box
            g2d.setColor(colors[i]);
            g2d.fillRoundRect(legendX, legendY - 15, 25, 18, 6, 6);
            g2d.setColor(new Color(44, 62, 80));
            g2d.drawRoundRect(legendX, legendY - 15, 25, 18, 6, 6);

            // Draw label
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.drawString(labels[i], legendX + 35, legendY);

            legendY += 25;
        }
    }

    void updateStepsArea(String text) {
        stepsArea.setText(text);
        stepsArea.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrimeNumberVisualizer());
    }
}
