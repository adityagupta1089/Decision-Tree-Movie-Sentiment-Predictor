package tree.node;

import java.util.ArrayList;
import java.util.List;

import data.Review;
import tree.rule.Rule;

public class InternalNode extends Node {

    private final List<Node> children;

    public InternalNode() {
	super();
	this.children = new ArrayList<>();
    }

    public void addChild(final Node pChild) {
	this.children.add(pChild);
    }

    public List<Node> getChildren() {
	return this.children;
    }

    @Override
    public boolean getLabel(final Review review) {
	final int val = review.getValue(this.wordID);
	/*
	 * The ranges are sorted hence we can sweep from and left and recurse on
	 * appropriate node
	 */
	for (final Node child : this.children) {
	    if ((child.getMinVal() <= val) && (val < child.getMaxVal())) {
		return child.getLabel(review);
	    }
	}
	throw new RuntimeException("No Child Matched!");
    }

    @Override
    public List<Rule> getRules() {
	final List<Rule> rules = new ArrayList<>();
	for (final Node child : this.children) {
	    for (final Rule rule : child.getRules()) {
		rule.addAntedecent(this);
		rules.add(rule);
	    }
	}
	return rules;
    }

    @Override
    public String toString(final String prefix, final boolean last, final boolean root) {
	final StringBuilder sb = new StringBuilder();
	sb.append(prefix + (last ? "└──" : "├──") + "InternalNode: [" + this.minVal + ", " + this.maxVal
		+ "], compare #" + this.wordID + "\n");
	for (int i = 0; i < this.children.size(); i++) {
	    final boolean lastChild = i == (this.children.size() - 1);
	    sb.append(this.children.get(i).toString(prefix + (root ? "    " : "│    "), lastChild, false)
		    + (lastChild ? "" : "\n"));
	}
	return sb.toString();
    }

}