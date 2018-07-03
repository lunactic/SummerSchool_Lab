package kws;

import java.util.ArrayList;
import java.util.List;

/*
 * Keypoint graph with a list of nodes.
 */
public class KGraph {

    private String graphId;
    private List<KNode> nodes;

    public KGraph(String graphId) {
        this.graphId = graphId;
        nodes = new ArrayList<KNode>();
    }

    public void addNode(KNode node) {
        nodes.add(node);
    }

    public String getGraphId() {
        return graphId;
    }

    public List<KNode> getNodes() {
        return nodes;
    }

    public int size() {
        return nodes.size();
    }

    public KNode get(int i) {
        return nodes.get(i);
    }

    /*
     * Center the node labels around (0,0).
     */
    public void normalize() {
        double meanX = 0;
        double meanY = 0;
        for (KNode node : nodes) {
            meanX += node.getX();
            meanY += node.getY();
        }
        meanX /= nodes.size();
        meanY /= nodes.size();
        for (KNode node : nodes) {
            node.setX(node.getX() - meanX);
            node.setY(node.getY() - meanY);
        }
    }

}
