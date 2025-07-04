import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * A single-file iPod Touch simulator using Java Swing.
 * This version uses panel switching for apps, features global top and bottom
 * bars,
 * and is sized for the 1st generation iPod Touch. It includes an advanced,
 * interactive Maps app
 * and a functional Music app using a Binary Search Tree.
 *
 * @author Gemini
 */
public class iPodSimulator extends JFrame {

    private static final int SCREEN_WIDTH = 320;
    private static final int SCREEN_HEIGHT = 480;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JSlider unlockSlider;
    private final Stack<String> navigationHistory = new Stack<>();
    private String currentCard = "lockScreen";
    // Define the exclusive list of apps, "Notes" has been removed.
    private final String[] appNames = { "Maps", "Mail", "Photos", "Music" };

    // Global UI components
    private JPanel globalTopMenu;
    private JPanel globalBottomNav;

    /**
     * Main constructor to set up the iPod simulator window.
     */
    public iPodSimulator() {
        setTitle("iPod Touch Simulator");
        setSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false);

        getContentPane().setLayout(new BorderLayout());

        // Create the global bars
        globalTopMenu = createGlobalTopMenu();
        globalBottomNav = createGlobalBottomNav();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Create and add all content panels ---
        mainPanel.add(createLockScreen(), "lockScreen");
        mainPanel.add(createHomeScreen(), "homeScreen");

        // Add all functional and placeholder app panels
        mainPanel.add(new MapsAppPanel(), "Maps");
        mainPanel.add(new MusicAppPanel(), "Music");

        for (String appName : appNames) {
            if (!"Maps".equals(appName) && !"Music".equals(appName)) {
                mainPanel.add(createAppPanel(appName), appName);
            }
        }

        getContentPane().add(globalTopMenu, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(globalBottomNav, BorderLayout.SOUTH);

        globalTopMenu.setVisible(false);
        globalBottomNav.setVisible(false);

        cardLayout.show(mainPanel, "lockScreen");
    }

    private JPanel createLockScreen() {
        JPanel lockScreenPanel = new JPanel(new BorderLayout(10, 10));
        lockScreenPanel.setBackground(new Color(20, 20, 60));
        lockScreenPanel.setBorder(new EmptyBorder(40, 20, 40, 20));

        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setOpaque(false);

        JLabel timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 60));
        timeLabel.setForeground(Color.WHITE);

        JLabel dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        dateLabel.setForeground(Color.WHITE);

        Timer lockScreenTimer = new Timer(1000, e -> {
            timeLabel.setText(new SimpleDateFormat("h:mm").format(new Date()));
            dateLabel.setText(new SimpleDateFormat("EEEE, MMMM d").format(new Date()));
        });
        lockScreenTimer.start();

        timePanel.add(timeLabel, BorderLayout.NORTH);
        timePanel.add(dateLabel, BorderLayout.CENTER);

        unlockSlider = new JSlider(0, 100, 0);
        unlockSlider.addChangeListener(e -> {
            if (!unlockSlider.getValueIsAdjusting()) {
                if (unlockSlider.getValue() > 95) {
                    navigateTo("homeScreen");
                }
                unlockSlider.setValue(0);
            }
        });

        JLabel unlockLabel = new JLabel("slide to unlock", SwingConstants.CENTER);
        unlockLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        unlockLabel.setForeground(Color.LIGHT_GRAY);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setOpaque(false);
        sliderPanel.add(unlockLabel, BorderLayout.CENTER);
        sliderPanel.add(unlockSlider, BorderLayout.SOUTH);

        lockScreenPanel.add(timePanel, BorderLayout.CENTER);
        lockScreenPanel.add(sliderPanel, BorderLayout.SOUTH);

        return lockScreenPanel;
    }

    private JPanel createHomeScreen() {
        JPanel homeScreenPanel = new JPanel(new GridLayout(4, 3, 15, 25));
        homeScreenPanel.setBackground(new Color(45, 45, 45));
        homeScreenPanel.setBorder(new EmptyBorder(30, 15, 30, 15));

        for (String appName : appNames) {
            JButton appButton = createAppButton(appName);
            homeScreenPanel.add(appButton);
        }

        return homeScreenPanel;
    }

    private JPanel createAppPanel(String appName) {
        JPanel appPanel = new JPanel(new BorderLayout());
        appPanel.setBackground(Color.WHITE);
        JLabel appLabel = new JLabel(appName, SwingConstants.CENTER);
        appLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        appPanel.add(appLabel, BorderLayout.CENTER);
        return appPanel;
    }

    private JPanel createGlobalTopMenu() {
        JPanel topMenu = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topMenu.setBackground(Color.BLACK);

        JLabel timeLabel = new JLabel("");
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        Timer clockTimer = new Timer(1000, e -> {
            timeLabel.setText(new SimpleDateFormat("h:mm:ss a").format(new Date()));
        });
        clockTimer.start();

        topMenu.add(timeLabel);
        return topMenu;
    }

    private JPanel createGlobalBottomNav() {
        JPanel bottomNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 5));
        bottomNav.setBackground(Color.DARK_GRAY);

        JButton backButton = new JButton("< Back");
        backButton.addActionListener(e -> navigateBack());
        JButton lockButton = new JButton("Lock");
        lockButton.addActionListener(e -> lockDevice());

        bottomNav.add(backButton);
        bottomNav.add(lockButton);
        return bottomNav;
    }

    private JButton createAppButton(String appName) {
        JButton button = new JButton(appName);
        button.setForeground(Color.BLACK);
        button.setBackground(getAppColor(appName));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.addActionListener(e -> navigateTo(appName));
        return button;
    }

    private void navigateTo(String cardName) {
        if (!currentCard.equals(cardName) && !"lockScreen".equals(currentCard)) {
            if ("homeScreen".equals(cardName)) {
                navigationHistory.clear();
            } else {
                navigationHistory.push(currentCard);
            }
        }
        currentCard = cardName;
        cardLayout.show(mainPanel, cardName);

        boolean isLockScreen = "lockScreen".equals(cardName);
        globalTopMenu.setVisible(!isLockScreen);
        globalBottomNav.setVisible(!isLockScreen);
    }

    private void navigateBack() {
        if (!navigationHistory.isEmpty()) {
            navigateTo(navigationHistory.pop());
        } else {
            navigateTo("homeScreen");
        }
    }

    private void lockDevice() {
        navigationHistory.clear();
        navigateTo("lockScreen");
    }

    private Color getAppColor(String appName) {
        switch (appName) {
            case "Music":
                return new Color(250, 140, 180);
            case "Photos":
                return new Color(140, 220, 250);
            case "Mail":
                return new Color(170, 200, 255);
            case "Maps":
                return new Color(160, 230, 170);
            default:
                return Color.LIGHT_GRAY;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Look and Feel not set.");
            }
            new iPodSimulator().setVisible(true);
        });
    }
}

// =================================================================================
// Music App Implementation (Binary Search Tree)
// =================================================================================

/**
 * Represents a single song with a title and artist.
 * Implements Comparable to allow sorting by title.
 */
class Song implements Comparable<Song> {
    String title;
    String artist;

    Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }

    @Override
    public int compareTo(Song other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Song song = (Song) obj;
        return title.equalsIgnoreCase(song.title);
    }

    @Override
    public int hashCode() {
        return title.toLowerCase().hashCode();
    }
}

/**
 * Implements a Binary Search Tree to store and manage a library of songs.
 */
class MusicLibrary {
    private Node root;

    private class Node {
        Song song;
        Node left, right;

        Node(Song song) {
            this.song = song;
        }
    }

    public void addSong(Song song) {
        root = addRecursive(root, song);
    }

    private Node addRecursive(Node current, Song song) {
        if (current == null) {
            return new Node(song);
        }
        if (song.compareTo(current.song) < 0) {
            current.left = addRecursive(current.left, song);
        } else if (song.compareTo(current.song) > 0) {
            current.right = addRecursive(current.right, song);
        }
        return current; // Value already exists
    }

    public List<Song> getSortedSongs() {
        List<Song> songs = new ArrayList<>();
        inOrderTraversal(root, songs);
        return songs;
    }

    private void inOrderTraversal(Node node, List<Song> songs) {
        if (node != null) {
            inOrderTraversal(node.left, songs);
            songs.add(node.song);
            inOrderTraversal(node.right, songs);
        }
    }

    public void deleteSong(String title) {
        root = deleteRecursive(root, title);
    }

    private Node deleteRecursive(Node current, String title) {
        if (current == null)
            return null;

        int comparison = title.compareToIgnoreCase(current.song.title);
        if (comparison < 0) {
            current.left = deleteRecursive(current.left, title);
        } else if (comparison > 0) {
            current.right = deleteRecursive(current.right, title);
        } else {
            // Node to delete found
            if (current.left == null)
                return current.right;
            if (current.right == null)
                return current.left;

            current.song = findSmallestSong(current.right);
            current.right = deleteRecursive(current.right, current.song.title);
        }
        return current;
    }

    private Song findSmallestSong(Node root) {
        return root.left == null ? root.song : findSmallestSong(root.left);
    }

    /**
     * Searches for a song by its title.
     * 
     * @param title The title of the song to search for.
     * @return The Song object if found, otherwise null.
     */
    public Song searchSong(String title) {
        return searchRecursive(root, title);
    }

    private Song searchRecursive(Node current, String title) {
        if (current == null) {
            return null; // Song not found
        }

        int comparison = title.compareToIgnoreCase(current.song.title);

        if (comparison == 0) {
            return current.song; // Song found
        }

        return comparison < 0
                ? searchRecursive(current.left, title)
                : searchRecursive(current.right, title);
    }
}

/**
 * The user interface panel for the Music application.
 */
class MusicAppPanel extends JPanel {
    private final MusicLibrary library;
    private final DefaultListModel<Song> listModel;
    private final JList<Song> songList;

    public MusicAppPanel() {
        super(new BorderLayout(5, 5));
        this.library = new MusicLibrary();
        addSampleSongs();

        // --- Controls ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Tambah");
        JButton deleteButton = new JButton("Hapus");
        JButton searchButton = new JButton("Cari"); // New search button
        controlPanel.add(addButton);
        controlPanel.add(deleteButton);
        controlPanel.add(searchButton);

        // --- Song Display ---
        listModel = new DefaultListModel<>();
        songList = new JList<>(listModel);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateSongList();

        JScrollPane scrollPane = new JScrollPane(songList);

        add(new JLabel("Perpustakaan Musik", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addButton.addActionListener(e -> addSong());
        deleteButton.addActionListener(e -> deleteSong());
        searchButton.addActionListener(e -> searchSong());
    }

    private void addSampleSongs() {
        library.addSong(new Song("Bohemian Rhapsody", "Queen"));
        library.addSong(new Song("Stairway to Heaven", "Led Zeppelin"));
        library.addSong(new Song("Hotel California", "Eagles"));
        library.addSong(new Song("Smells Like Teen Spirit", "Nirvana"));
        library.addSong(new Song("Imagine", "John Lennon"));
    }

    private void updateSongList() {
        listModel.clear();
        for (Song song : library.getSortedSongs()) {
            listModel.addElement(song);
        }
    }

    private void addSong() {
        String title = JOptionPane.showInputDialog(this, "Masukkan Judul Lagu:", "Tambah Lagu",
                JOptionPane.PLAIN_MESSAGE);
        if (title != null && !title.trim().isEmpty()) {
            String artist = JOptionPane.showInputDialog(this, "Masukkan Nama Artis:", "Tambah Lagu",
                    JOptionPane.PLAIN_MESSAGE);
            if (artist != null && !artist.trim().isEmpty()) {
                library.addSong(new Song(title, artist));
                updateSongList();
            }
        }
    }

    private void deleteSong() {
        Song selected = songList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin ingin menghapus lagu \"" + selected.title + "\"?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                library.deleteSong(selected.title);
                updateSongList();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih lagu yang ingin dihapus.", "Tidak Ada Pilihan",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void searchSong() {
        String searchTerm = JOptionPane.showInputDialog(this, "Masukkan judul lagu yang ingin dicari:", "Cari Lagu",
                JOptionPane.PLAIN_MESSAGE);
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            Song foundSong = library.searchSong(searchTerm);
            if (foundSong != null) {
                // Find the song in the list model and select it
                for (int i = 0; i < listModel.getSize(); i++) {
                    if (listModel.getElementAt(i).equals(foundSong)) {
                        songList.setSelectedIndex(i);
                        songList.ensureIndexIsVisible(i); // Scroll to the item
                        JOptionPane.showMessageDialog(this, "Lagu \"" + foundSong.title + "\" ditemukan!",
                                "Hasil Pencarian", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lagu dengan judul \"" + searchTerm + "\" tidak ditemukan.",
                        "Hasil Pencarian", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}

// =================================================================================
// Maps App Implementation (Graph, Dijkstra, Prim)
// =================================================================================
/**
 * A sophisticated Maps application panel with interactive graph visualization.
 */
class MapsAppPanel extends JPanel {
    private final MapPanel mapPanel;
    private final MapGraph mapGraph;
    private final JComboBox<String> startCityCombo;
    private final JComboBox<String> endCityCombo;
    private final JLabel resultLabel;

    public MapsAppPanel() {
        super(new BorderLayout());
        this.mapGraph = createPredefinedMap();
        this.mapPanel = new MapPanel(mapGraph);

        // --- Controls Panel ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Create dedicated panels for each row of controls for better layout management
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        startPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        startCityCombo = new JComboBox<>(mapGraph.getCityNames());
        startPanel.add(new JLabel("Start:"));
        startPanel.add(startCityCombo);

        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        endPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        endCityCombo = new JComboBox<>(mapGraph.getCityNames());
        endPanel.add(new JLabel("  End:"));
        endPanel.add(endCityCombo);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton findPathButton = new JButton("Find Shortest Path");
        findPathButton.addActionListener(e -> findShortestPath());

        JButton showMstButton = new JButton("Show MST");
        showMstButton.addActionListener(e -> showMst());

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetMap());

        buttonPanel.add(findPathButton);
        buttonPanel.add(showMstButton);
        buttonPanel.add(resetButton);

        // Result display
        resultLabel = new JLabel("Select cities and an action.");
        resultLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        controlPanel.add(startPanel);
        controlPanel.add(endPanel);
        controlPanel.add(buttonPanel);
        controlPanel.add(resultLabel);

        add(controlPanel, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
    }

    private void findShortestPath() {
        String startCity = (String) startCityCombo.getSelectedItem();
        String endCity = (String) endCityCombo.getSelectedItem();
        if (startCity == null || endCity == null || startCity.equals(endCity)) {
            resultLabel.setText("Please select two different cities.");
            return;
        }

        MapGraph.DijkstraResult result = mapGraph.findShortestPathDijkstra(startCity, endCity);
        List<MapGraph.Edge> path = result.path;
        double distance = result.distance;

        if (path.isEmpty() && !startCity.equals(endCity)) {
            resultLabel.setText("No path found between " + startCity + " and " + endCity);
        } else {
            resultLabel.setText(String.format("Shortest distance: %.1f", distance));
        }
        mapPanel.highlightPath(path);
        mapPanel.setMstEdges(null); // Clear MST view
    }

    private void showMst() {
        MapGraph.MstResult result = mapGraph.findMstPrim();
        List<MapGraph.Edge> mstEdges = result.edges;
        double totalWeight = result.totalWeight;

        resultLabel.setText(String.format("MST Total Weight: %.1f", totalWeight));
        mapPanel.setMstEdges(mstEdges);
        mapPanel.highlightPath(null); // Clear path view
    }

    private void resetMap() {
        mapPanel.highlightPath(null);
        mapPanel.setMstEdges(null);
        resultLabel.setText("Select cities and an action.");
        startCityCombo.setSelectedIndex(0);
        endCityCombo.setSelectedIndex(0);
        mapPanel.resetView();
    }

    private static MapGraph createPredefinedMap() {
        MapGraph graph = new MapGraph();
        // Add nodes with random-like positions
        graph.addNode("Cupertino", 150, 250);
        graph.addNode("San Francisco", 120, 100);
        graph.addNode("San Jose", 180, 300);
        graph.addNode("Oakland", 150, 110);
        graph.addNode("Palo Alto", 150, 200);
        graph.addNode("Mountain View", 160, 220);
        graph.addNode("Sunnyvale", 170, 240);
        graph.addNode("Santa Clara", 175, 270);
        graph.addNode("Fremont", 220, 250);
        graph.addNode("Hayward", 190, 160);
        graph.addNode("Berkeley", 140, 90);
        graph.addNode("Walnut Creek", 210, 100);
        graph.addNode("San Mateo", 130, 150);
        graph.addNode("Redwood City", 140, 175);
        graph.addNode("Daly City", 110, 120);
        graph.addNode("Sacramento", 350, 50);
        graph.addNode("Los Angeles", 400, 500);
        graph.addNode("San Diego", 450, 600);
        graph.addNode("Las Vegas", 600, 400);
        graph.addNode("Phoenix", 800, 550);

        // Add edges
        graph.addEdge("Cupertino", "Sunnyvale");
        graph.addEdge("Cupertino", "Palo Alto");
        graph.addEdge("Cupertino", "San Jose");
        graph.addEdge("Sunnyvale", "Mountain View");
        graph.addEdge("Mountain View", "Palo Alto");
        graph.addEdge("Palo Alto", "Redwood City");
        graph.addEdge("Redwood City", "San Mateo");
        graph.addEdge("San Mateo", "San Francisco");
        graph.addEdge("San Mateo", "Daly City");
        graph.addEdge("Daly City", "San Francisco");
        graph.addEdge("San Francisco", "Berkeley");
        graph.addEdge("Berkeley", "Oakland");
        graph.addEdge("Oakland", "Hayward");
        graph.addEdge("Oakland", "Walnut Creek");
        graph.addEdge("Hayward", "Fremont");
        graph.addEdge("Fremont", "San Jose");
        graph.addEdge("Santa Clara", "San Jose");
        graph.addEdge("Santa Clara", "Sunnyvale");
        graph.addEdge("San Francisco", "Sacramento");
        graph.addEdge("Sacramento", "Las Vegas");
        graph.addEdge("San Jose", "Los Angeles");
        graph.addEdge("Los Angeles", "San Diego");
        graph.addEdge("Los Angeles", "Las Vegas");
        graph.addEdge("Las Vegas", "Phoenix");
        graph.addEdge("San Diego", "Phoenix");

        return graph;
    }
}

/**
 * The custom panel for drawing the map. Handles rendering, pan, and zoom.
 */
class MapPanel extends JPanel {
    private final MapGraph graph;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private Point lastPanPoint;

    private List<MapGraph.Edge> highlightedPath = null;
    private List<MapGraph.Edge> mstEdges = null;

    public MapPanel(MapGraph graph) {
        this.graph = graph;
        this.setBackground(new Color(210, 235, 255)); // A light blue, like a map

        // --- Mouse Listeners for Pan and Zoom ---
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPanPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPanPoint != null) {
                    int dx = e.getX() - lastPanPoint.x;
                    int dy = e.getY() - lastPanPoint.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastPanPoint = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastPanPoint = null;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = 1.1;
                Point2D p1 = e.getPoint();
                if (e.getWheelRotation() > 0) { // Zoom out
                    scale /= zoomFactor;
                } else { // Zoom in
                    scale *= zoomFactor;
                }
                Point2D p2 = e.getPoint();
                offsetX += (p2.getX() - p1.getX());
                offsetY += (p2.getY() - p1.getY());
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    public void highlightPath(List<MapGraph.Edge> path) {
        this.highlightedPath = path;
        repaint();
    }

    public void setMstEdges(List<MapGraph.Edge> edges) {
        this.mstEdges = edges;
        repaint();
    }

    public void resetView() {
        this.scale = 1.0;
        this.offsetX = 0;
        this.offsetY = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply pan and zoom transform
        AffineTransform at = new AffineTransform();
        at.translate(offsetX, offsetY);
        at.scale(scale, scale);
        g2d.transform(at);

        // Draw all edges first
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(Color.GRAY);
        for (MapGraph.Edge edge : graph.getAllEdges()) {
            g2d.drawLine(edge.u.x, edge.u.y, edge.v.x, edge.v.y);
        }

        // Draw MST edges if available
        if (mstEdges != null) {
            g2d.setColor(new Color(34, 139, 34)); // Forest Green
            g2d.setStroke(new BasicStroke(4.0f));
            for (MapGraph.Edge edge : mstEdges) {
                g2d.drawLine(edge.u.x, edge.u.y, edge.v.x, edge.v.y);
            }
        }

        // Draw highlighted path on top
        if (highlightedPath != null) {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(4.0f));
            for (MapGraph.Edge edge : highlightedPath) {
                g2d.drawLine(edge.u.x, edge.u.y, edge.v.x, edge.v.y);
            }
        }

        // Draw nodes and labels
        int nodeDiameter = 12;
        FontMetrics fm = g2d.getFontMetrics();
        for (MapGraph.Node node : graph.getAllNodes()) {
            g2d.setColor(Color.RED);
            g2d.fillOval(node.x - nodeDiameter / 2, node.y - nodeDiameter / 2, nodeDiameter, nodeDiameter);

            g2d.setColor(Color.BLACK);
            g2d.drawString(node.name, node.x - fm.stringWidth(node.name) / 2, node.y - nodeDiameter / 2 - 5);
        }
    }
}

/**
 * A graph data structure to represent the map, with nodes (cities) and edges
 * (roads).
 * Includes implementations of Dijkstra's and Prim's algorithms.
 */
class MapGraph {
    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, List<Edge>> adjList = new HashMap<>();

    static class Node {
        String name;
        int x, y;

        Node(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    static class Edge {
        Node u, v;
        double weight;

        Edge(Node u, Node v) {
            this.u = u;
            this.v = v;
            this.weight = Point2D.distance(u.x, u.y, v.x, v.y);
        }
    }

    // Custom result classes to avoid unchecked casts
    static class DijkstraResult {
        final List<Edge> path;
        final double distance;

        DijkstraResult(List<Edge> path, double distance) {
            this.path = path;
            this.distance = distance;
        }
    }

    static class MstResult {
        final List<Edge> edges;
        final double totalWeight;

        MstResult(List<Edge> edges, double totalWeight) {
            this.edges = edges;
            this.totalWeight = totalWeight;
        }
    }

    public void addNode(String name, int x, int y) {
        if (!nodes.containsKey(name)) {
            nodes.put(name, new Node(name, x, y));
            adjList.put(name, new ArrayList<>());
        }
    }

    public void addEdge(String uName, String vName) {
        Node u = nodes.get(uName);
        Node v = nodes.get(vName);
        if (u != null && v != null) {
            Edge edge = new Edge(u, v);
            adjList.get(uName).add(edge);
            adjList.get(vName).add(new Edge(v, u)); // Undirected graph
        }
    }

    public String[] getCityNames() {
        return nodes.keySet().stream().sorted().toArray(String[]::new);
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    public Collection<Edge> getAllEdges() {
        Set<Edge> allEdges = new HashSet<>();
        adjList.values().forEach(allEdges::addAll);
        return allEdges;
    }

    // Dijkstra's Algorithm for Shortest Path
    public DijkstraResult findShortestPathDijkstra(String startName, String endName) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, Edge> previousEdges = new HashMap<>();
        // FIX: Corrected comparator to prevent NullPointerException
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(node -> distances.get(node.name)));
        Set<String> visited = new HashSet<>();

        for (String name : nodes.keySet()) {
            distances.put(name, Double.MAX_VALUE);
        }
        distances.put(startName, 0.0);
        pq.add(nodes.get(startName));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current == null || visited.contains(current.name))
                continue;
            visited.add(current.name);

            if (current.name.equals(endName))
                break;

            for (Edge edge : adjList.get(current.name)) {
                Node neighbor = edge.v;
                if (!visited.contains(neighbor.name)) {
                    double newDist = distances.get(current.name) + edge.weight;
                    if (newDist < distances.get(neighbor.name)) {
                        distances.put(neighbor.name, newDist);
                        previousEdges.put(neighbor.name, edge);
                        pq.add(neighbor);
                    }
                }
            }
        }

        List<Edge> path = new ArrayList<>();
        String at = endName;
        while (previousEdges.containsKey(at)) {
            Edge edge = previousEdges.get(at);
            path.add(edge);
            at = edge.u.name;
        }
        Collections.reverse(path);

        return new DijkstraResult(path, distances.getOrDefault(endName, 0.0));
    }

    // Prim's Algorithm for Minimum Spanning Tree
    public MstResult findMstPrim() {
        List<Edge> mstEdges = new ArrayList<>();
        double totalWeight = 0;
        Set<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weight));

        if (nodes.isEmpty()) {
            return new MstResult(mstEdges, totalWeight);
        }

        String startNodeName = nodes.keySet().iterator().next();
        visited.add(startNodeName);
        pq.addAll(adjList.get(startNodeName));

        while (!pq.isEmpty() && visited.size() < nodes.size()) {
            Edge edge = pq.poll();
            Node neighbor = edge.v;
            if (visited.contains(neighbor.name))
                continue;

            visited.add(neighbor.name);
            mstEdges.add(edge);
            totalWeight += edge.weight;

            for (Edge neighborEdge : adjList.get(neighbor.name)) {
                if (!visited.contains(neighborEdge.v.name)) {
                    pq.add(neighborEdge);
                }
            }
        }

        return new MstResult(mstEdges, totalWeight);
    }
}
