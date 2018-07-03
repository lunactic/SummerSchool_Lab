package kws;

import java.util.List;

/*
 * Hausdorff Edit Distance (HED)
 */
public class HED {

    KCost cost;

    public HED(KCost cost) {
        this.cost = cost;
    }

    /*
     * Compute the Hausdorff edit distance between two keypoint graphs g1 and g2.
     * Pseudocode: Algorithm 3 (HED) in the reference paper.
     *
     * For keyword spotting, there is an additional step at the end of the algorithm:
     * Before returning the HED, normalize it with the maximum graph edit distance between g1 and g2.
     */
    public double match(KGraph g1, KGraph g2) {
        //TODO: Compute Hausdorff Edit Distance
    }

    /*
     * Compute the Hausdorff edit cost between two sets of edges.
     * This method corresponds to Algorithm 2 (HEC) in the reference paper.
     *
     * You need this method for implementing match(KGraph g1, KGraph g2).
     */
    private double matchEdges(List<KEdge> edges1, List<KEdge> edges2) {

        // initialize cost A -> B with edge deletion costs
        double[] costAB = new double[edges1.size()];
        for (int i = 0; i < edges1.size(); i++) {
            costAB[i] = cost.edgeDelIns();
        }

        // initialize cost B -> A with edge insertion costs
        double[] costBA = new double[edges2.size()];
        for (int j = 0; j < edges2.size(); j++) {
            costBA[j] = cost.edgeDelIns();
        }

        // compute the cost of substituting edges
        for (int i = 0; i < edges1.size(); i++) {
            for (int j = 0; j < edges2.size(); j++) {
                double sub = 0.5 * cost.edgeSub(edges1.get(i), edges2.get(j));
                costAB[i] = Math.min(costAB[i], sub);
                costBA[j] = Math.min(costBA[j], sub);
            }
        }

        // sum of costs A -> B plus costs B -> A
        double distance = 0;
        for (int i = 0; i < edges1.size(); i++) {
            distance += costAB[i];
        }
        for (int j = 0; j < edges2.size(); j++) {
            distance += costBA[j];
        }
        return distance;
    }

}
