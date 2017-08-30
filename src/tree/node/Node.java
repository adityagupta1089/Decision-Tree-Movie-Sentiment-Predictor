package tree.node;

public class Node {

    protected double minVal;
    protected double maxVal;

    protected int wordID;

    public Node() {
	this.minVal = Double.NEGATIVE_INFINITY;
	this.maxVal = Double.POSITIVE_INFINITY;
    }

    public double getMaxVal() {
	return maxVal;
    }

    public double getMinVal() {
	return minVal;
    }

    public int getWordID() {
	return wordID;
    }

    public void setMaxVal(double maxVal) {
	this.maxVal = maxVal;
    }

    public void setMinVal(double minVal) {
	this.minVal = minVal;
    }

    public void setWordID(int wordID) {
	this.wordID = wordID;
    }

    public String toString(String prefix, boolean last, boolean root) {
	return this.toString();
    }

}
