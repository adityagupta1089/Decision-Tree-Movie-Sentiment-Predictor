package tree.node;

public class LeafNode extends Node {

    private boolean label;

    public LeafNode(boolean label) {
	super();
	this.label = label;
    }

    public boolean getLabel() {
	return label;
    }

    @Override
    public String toString(String prefix, boolean last, boolean root) {
	return prefix + (last ? "└──" : "├──") + "LeafNode: [" + minVal + ", " + maxVal + "], " + label;
    }

}