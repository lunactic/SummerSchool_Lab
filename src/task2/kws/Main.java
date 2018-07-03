package kws;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/*
 * IAPR TC10/TC11 Summer School on Document Analysis, 2018 La Rochelle
 *
 * Lab: Structural Methods for Handwriting Analysis
 * Andreas Fischer - andreas.fischer@unifr.ch
 * Marcel Würsch - marcel.wuersch@unifr.ch
 *
 * Task 2: Use the word graphs from Task 1 for keyword spotting.
 * - The keyword graph is matched against all word graphs.
 * - The binary images of the top-N word results are displayed.
 * - Your task is to implement the method match(KGraph g1, KGraph g2) in the HED class, which computes the Hausdorff
 *   edit distance between two keypoint graphs g1 and g2.
 *
 * Reference paper:
 * A. Fischer, C. Y. Suen, V. Frinken, K. Riesen, and H. Bunke.
 * Approximation of graph edit distance based on Hausdorff matching.
 * Pattern Recognition, 48(2):331–343, 2015.
 *
 */
public class Main {

    /*
     * Defaults from Task 1
     */

    private static final String DIR_GXL = "graphs_binary";
    private static final String TYPE_GXL = "xml";
    private static final String DTD_GXL = "http://www.gupro.de/GXL/gxl-1.0.dtd";

    private static final String DIR_IMG = "words_binary";
    private static final String TYPE_IMG = "png";

    public static void main(String[] args) {

        /*
         * Settings
         *
         * ==========================================================================
         * =====> CHANGE pathOut to the the output directory of the first task <=====
         * ==========================================================================
         *
         * the cost for node deletion/insertion and edge deletion/insertion is set to reasonable defaults
         * (assuming that the node distance on the keypoint graphs is around 25.0)
         */

        String pathOut = "C:\\Users\\marce\\DEV\\SummerSchool_Lab\\out";

        String keywordId = "270-01-05";
        String[] pageIds = {"270", "271", "272", "273", "274", "275", "276", "277", "278", "279", "300", "301", "302", "303", "304"};

        double nodeCost = 25.0;
        double edgeCost = 50.0;

        int topN = 10;
        boolean displayTopN = true; // turn off the (Swing-based) visualization if needed

        /*
         * Read Graphs
         */

        System.out.println("Reading keyword graph and page graphs ...");
        double startTime = System.currentTimeMillis();

        KGraph keyword = getKeyword(pathOut, keywordId);
        List<KGraph> words = getWords(pathOut, pageIds);

        System.out.println("... done. Read "
                + (1 + words.size()) + " graphs in "
                + (System.currentTimeMillis() - startTime) + " ms.\n");

        /*
         * Match Graphs
         */

        System.out.println("Matching keyword graph with page graphs ...");
        startTime = System.currentTimeMillis();

        List<Result> results = new ArrayList<Result>();
        KCost cost = new KCost(nodeCost, edgeCost);
        HED hed = new HED(cost);
        for (KGraph word : words) {
            double distance = hed.match(keyword, word); // you have to implement this method
            Result result = new Result(word.getGraphId(), distance);
            results.add(result);
        }

        System.out.println("... done. Matched "
                + results.size() + " graphs in "
                + (System.currentTimeMillis() - startTime) + " ms.\n");

        /*
         * Print Top-N
         */

        System.out.println("Top " + topN);
        Collections.sort(results);
        for (int i = 0; i < topN && i < results.size(); i++) {
            Result result = results.get(i);
            System.out.println((i + 1) + ". " + result.graphId + " (" + Math.round(result.distance * 1000.0) / 1000.0 + ")");
        }

        /*
         * Display Top-N
         */

        if (displayTopN) {
            JFrame frame = new JFrame("Top " + topN);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            // add keyword image
            String pageId = keywordId.substring(0, keywordId.indexOf("-"));
            JLabel label = new JLabel();
            label.setIcon(new ImageIcon(Paths.get(pathOut, pageId, DIR_IMG, keywordId + "." + TYPE_IMG).toString()));
            int width = label.getIcon().getIconWidth();
            panel.add(label);

            // add top-N word images
            for (int i = 0; i < topN && i < results.size(); i++) {
                Result result = results.get(i);
                String wordId = result.graphId;
                pageId = wordId.substring(0, wordId.indexOf("-"));
                label = new JLabel();
                label.setIcon(new ImageIcon(Paths.get(pathOut, pageId, DIR_IMG, wordId + "." + TYPE_IMG).toString()));
                panel.add(label);
            }

            // show
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(dim.width/2-width/2, 0);
            frame.pack();
            frame.setVisible(true);
        }

    }

    /*
     * Helper class to store the distance results.
     */
    private static class Result implements Comparable<Result> {

        private String graphId;
        private Double distance;

        public Result(String graphId, double distance) {
            this.graphId = graphId;
            this.distance = distance;
        }

        @Override
        public int compareTo(Result o) {
            return distance.compareTo(o.distance);
        }
    }

    private static KGraph getKeyword(String pathOut, String keywordId) {
        String pageID = keywordId.substring(0, keywordId.indexOf("-"));
        File gxl = Paths.get(pathOut, pageID, DIR_GXL, keywordId + "." + TYPE_GXL).toFile();
        KGraph keyword = readGraph(gxl, keywordId);
        return keyword;
    }

    private static List<KGraph> getWords(String pathOut, String[] pageIds) {
        List<File> gxlFiles = new ArrayList<File>();
        try {
            for (String pageID : pageIds) {
                Files.newDirectoryStream(Paths.get(pathOut, pageID, DIR_GXL)
                        , path -> path.toString().endsWith(TYPE_GXL))
                        .forEach(filePath -> gxlFiles.add(filePath.toFile()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<KGraph> words = new ArrayList<KGraph>();
        for (File gxl : gxlFiles) {
            String gxlName = gxl.getName();
            String graphId = gxlName.substring(0, gxlName.indexOf(TYPE_GXL) - 1);
            KGraph word = readGraph(gxl, graphId);
            words.add(word);
        }
        return words;
    }

    /*
     * Read the GXL and build the keypoint graph (KGraph).
     * We consider unirected graphs and add an inverse edge (b,a) for each edge (a,b) in the GXL.
     */
    private static KGraph readGraph(File gxl, String graphId) {
        Map<String, KNode> nodes = new HashMap<String, KNode>();
        try {

            // parse GXL
            // skip DTD to speed up XML parsing
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId.contains(DTD_GXL)) {
                        return new InputSource(new StringReader(""));
                    } else {
                        return null;
                    }
                }
            });

            // read graph nodes
            Document doc = dBuilder.parse(gxl);
            NodeList gxlNodes = doc.getElementsByTagName("node");
            for (int i = 0; i < gxlNodes.getLength(); i++) {
                Element gxlNode = (Element) gxlNodes.item(i);
                String id = gxlNode.getAttribute("id");
                String x = readAttribute(gxlNode, "x");
                String y = readAttribute(gxlNode, "y");
                if (x != null && y != null) {
                    KNode node = new KNode(Double.parseDouble(x), Double.parseDouble(y));
                    nodes.put(id, node);
                }
            }

            // read graph edges and add them to the corresponding nodes
            NodeList gxlEdges = doc.getElementsByTagName("edge");
            for (int i = 0; i < gxlEdges.getLength(); i++) {
                Element gxlEdge = (Element) gxlEdges.item(i);
                String id1 = gxlEdge.getAttribute("from");
                String id2 = gxlEdge.getAttribute("to");
                KNode node1 = nodes.get(id1);
                KNode node2 = nodes.get(id2);
                node1.addEdge(node2); // for each edge (a,b) ...
                node2.addEdge(node1); // ... add an inverse edge (b,a)
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        // create a graph and add the nodes
        KGraph graph = new KGraph(graphId);
        for (KNode node : nodes.values()) {
            graph.addNode(node);
        }

        // normalize the graph and return the result
        // normalization centers the node labels, such that they have zero mean (0,0) afterwards
        graph.normalize();
        return graph;
    }

    private static String readAttribute(Element gxlNode, String attributeName) {
        NodeList gxlAttributes = gxlNode.getElementsByTagName("attr");
        for (int i = 0; i < gxlAttributes.getLength(); i++) {
            Element gxlAttribute = (Element) gxlAttributes.item(i);
            if (gxlAttribute.getAttribute("name").equals(attributeName)) {
                return gxlAttribute.getTextContent();
            }
        }
        return null;
    }

}
