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
    public String toString(String prefix, boolean last, boolean root) {
	StringBuilder sb = new StringBuilder();
	sb.append(prefix + (last ? "└──" : "├──") + "InternalNode: [" + minVal + ", " + maxVal + "], compare #" + wordID
		+ "\n");
	for (int i = 0; i < this.children.size(); i++) {
	    boolean lastChild = i == this.children.size() - 1;
	    sb.append(this.children.get(i).toString(prefix + (root ? "    " : "│    "), lastChild, false)
		    + (lastChild ? "" : "\n"));
	}
	return sb.toString();
    }

}