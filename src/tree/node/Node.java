package tree.node;

import java.util.List;

import data.Review;
import tree.rule.Rule;

public abstract class Node {

    protected double minVal;
    protected double maxVal;

    protected int wordID;
    protected int parentWordID;

    public Node() {
	this.minVal = Double.NEGATIVE_INFINITY;
	this.maxVal = Double.POSITIVE_INFINITY;
    }

    public abstract boolean getLabel(Review review);

    public double getMaxVal() {
	return this.maxVal;
    }

    public double getMinVal() {
	return this.minVal;
    }

    public int getParentWordID() {
	return this.parentWordID;
    }

    public abstract List<Rule> getRules();

    public int getWordID() {
	return this.wordID;
    }

    public boolean satisfies(final Review review) {
	final int val = review.getValue(this.parentWordID);
	return (this.minVal <= val) && (val <= this.maxVal);
    }

    public void setMaxVal(final double maxVal) {
	this.maxVal = maxVal;
    }

    public void setMinVal(final double minVal) {
	this.minVal = minVal;
    }

    public void setParentWordID(final int parentWordID) {
	this.parentWordID = parentWordID;
    }

    public void setWordID(final int wordID) {
	this.wordID = wordID;
    }

    @Override
    public String toString() {
	if (Double.isFinite(this.minVal)) {
	    return "(" + this.parentWordID + ">" + this.minVal + ")";
	} else if (Double.isFinite(this.maxVal)) {
	    return "(" + this.parentWordID + "<" + this.maxVal + ")";
	} else {
	    return "Root";
	}
    }

    public abstract String toString(String prefix, boolean last, boolean root);
}
