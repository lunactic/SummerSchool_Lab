package kws;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/*
 * Keypoint node with an (x,y) label.
 */
public class KNode {

    private Point2D point;
    private List<KEdge> edges;

    public KNode(double x, double y) {
        point = new Point2D.Double(x, y);
        edges = new ArrayList<KEdge>();
    }

    public void addEdge(KNode goal) {
        KEdge edge = new KEdge(goal);
        edges.add(edge);
    }

    public double distance(KNode other) {
        return point.distance(other.point);
    }

    public List<KEdge> getEdges() {
        return edges;
    }

    public double getX() {
        return point.getX();
    }

    public double getY() {
        return point.getY();
    }

    public void setX(double x) {
        point.setLocation(x, point.getY());
    }

    public void setY(double y) {
        point.setLocation(point.getX(), y);
    }

}
