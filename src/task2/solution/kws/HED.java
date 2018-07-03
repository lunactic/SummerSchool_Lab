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

        // initialize cost A -> B with node and edge deletion costs
        double[] costAB = new double[g1.size()];
        for (int i = 0; i < g1.size(); i++) {
            costAB[i] = cost.nodeDelIns() + 0.5 * g1.get(i).getEdges().size() * cost.edgeDelIns();
        }

        // initialize cost B -> A with node and edge insertion costs
        double[] costBA = new double[g2.size()];
        for (int j = 0; j < g2.size(); j++) {
            costBA[j] = cost.nodeDelIns() + 0.5 * g2.get(j).getEdges().size() * cost.edgeDelIns();
        }

        // compute the cost of substituting substructures (node plus adjacent edges)
        for (int i = 0; i < g1.size(); i++) {
            List<KEdge> edges1 = g1.get(i).getEdges();
            for (int j = 0; j < g2.size(); j++) {
                List<KEdge> edges2 = g2.get(j).getEdges();

                // node and edge matching cost
                double subNode = cost.nodeSub(g1.get(i), g2.get(j));
                double subEdges = matchEdges(edges1, edges2);

                // lower bound for edge matching
                double minEdges = Math.abs(edges1.size() - edges2.size()) * cost.edgeDelIns();
                if (minEdges > subEdges) {
                    subEdges = minEdges;
                }

                // substitution cost
                double substitution = 0.5 * (subNode + (0.5 * subEdges));
                costAB[i] = Math.min(costAB[i], substitution);
                costBA[j] = Math.min(costBA[j], substitution);
            }
        }

        // sum of costs A -> B plus costs B -> A
        double distance = 0;
        for (int i = 0; i < g1.size(); i++) {
            distance += costAB[i];
        }
        for (int j = 0; j < g2.size(); j++) {
            distance += costBA[j];
        }

        // lower bound for node matching
        double minNodes = Math.abs(g1.size() - g2.size()) * cost.nodeDelIns();
        if (minNodes > distance) {
            distance = minNodes;
        }

        // normalize with the maximum graph edit distance
        double maxNodes = (g1.size() + g2.size()) * cost.nodeDelIns();
        double maxEdges = 0;
        for (KNode node : g1.getNodes()) {
            maxEdges += 0.5 * node.getEdges().size() * cost.edgeDelIns();
        }
        for (KNode node : g2.getNodes()) {
            maxEdges += 0.5 * node.getEdges().size() * cost.edgeDelIns();
        }
        distance /= (maxNodes + maxEdges);

        return distance;
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
