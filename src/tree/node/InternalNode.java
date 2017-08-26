package tree.node;

import java.util.ArrayList;
import java.util.List;

public class InternalNode extends Node {

    private List<Node> children;

    public List<Node> getChildren() {
	return children;
    }

    public InternalNode() {
	super();
	this.children = new ArrayList<>();
    }

    public void addChild(Node pChild) {
	this.children.add(pChild);
    }

    @Override
    public String toString() {
	return "(InternalNode: [" + minVal + ", " + maxVal + "], " + wordID + ", " + children + ")";
    }

}