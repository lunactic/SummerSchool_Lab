package kws;

/*
 * Euclidean cost function for keypoint graphs:
 * - deleting/inserting nodes has a fixed cost (nodeCost)
 * - deleting/inserting edges has a fixed cost (edgeCost)
 * - the cost for substituting node labels is the Euclidean distance between the keypoints
 * - the cost for substituting the unlabeled edges is zero
 */
public class KCost {

    private double nodeCost;
    private double edgeCost;

    public KCost(double nodeCost, double edgeCost) {
        this.nodeCost = nodeCost;
        this.edgeCost = edgeCost;
    }

    public double nodeDelIns() {
        return nodeCost;
    }

    public double edgeDelIns() {
        return edgeCost;
    }

    public double nodeSub(KNode n1, KNode n2) {
        return n1.distance(n2);
    }

    public double edgeSub(KEdge e1, KEdge e2) {
        return 0;
    }

}
