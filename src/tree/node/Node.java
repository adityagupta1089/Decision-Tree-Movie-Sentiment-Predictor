package tree.node;

public class Node {

    protected double minVal;
    protected double maxVal;

    protected int wordID;

    public double getMinVal() {
	return minVal;
    }

    public void setMinVal(double minVal) {
	this.minVal = minVal;
    }

    public double getMaxVal() {
	return maxVal;
    }

    public void setMaxVal(double maxVal) {
	this.maxVal = maxVal;
    }

    public int getWordID() {
	return wordID;
    }

    public void setWordID(int wordID) {
	this.wordID = wordID;
    }

    public Node() {
	this.minVal = Double.NEGATIVE_INFINITY;
	this.maxVal = Double.POSITIVE_INFINITY;
    }

    public String toString(String prefix, boolean last, boolean root) {
	return this.toString();
    }

}
