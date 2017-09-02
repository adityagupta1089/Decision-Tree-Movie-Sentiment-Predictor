package tree.node;

import java.util.ArrayList;
import java.util.List;

import data.Review;
import tree.rule.Rule;

public class LeafNode extends Node {

    private static int LEAF_NODE_COUNT = 0;

    public static int getLeafNodeCount() {
	return LEAF_NODE_COUNT;
    }

    public static void resetLeafNodeCount() {
	LEAF_NODE_COUNT = 0;
    }

    private final boolean label;

    public LeafNode(final boolean label) {
	super();
	this.label = label;
	LEAF_NODE_COUNT++;
    }

    public boolean getLabel() {
	return this.label;
    }

    @Override
    public boolean getLabel(final Review review) {
	return this.label;
    }

    @Override
    public List<Rule> getRules() {
	final List<Rule> rules = new ArrayList<>();
	rules.add(new Rule(this));
	return rules;
    }

    @Override
    public String toString(final String prefix, final boolean last, final boolean root) {
	return prefix + (last ? "└──" : "├──") + "LeafNode: [" + this.minVal + ", " + this.maxVal + "], " + this.label;
    }

}