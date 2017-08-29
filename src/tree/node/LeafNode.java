package tree.node;

public class LeafNode extends Node {

    private boolean label;
    private static int LEAF_NODE_COUNT = 0;

    public LeafNode(boolean label) {
	super();
	this.label = label;
	LEAF_NODE_COUNT++;
    }

    public boolean getLabel() {
	return label;
    }

    @Override
    public String toString(String prefix, boolean last, boolean root) {
	return prefix + (last ? "└──" : "├──") + "LeafNode: [" + minVal + ", " + maxVal + "], " + label;
    }

    public static int getLeafNodeCount() {
	return LEAF_NODE_COUNT;
    }

}