package tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.DataReader;
import data.Review;
import tree.node.InternalNode;
import tree.node.LeafNode;
import tree.node.Node;

public class DecisionTree {

    private Node root;

    public DecisionTree(List<Review> reviews, Set<Integer> attributes) {
	this.root = iterativeDichotomiser3(reviews, attributes);
    }

    private Node iterativeDichotomiser3(List<Review> reviews, Set<Integer> attributes) {
	assert (reviews.size() > 0);
	boolean prevLabel = reviews.get(0).isPositiveLabel();
	boolean allSame = true;
	for (int i = 1; i < reviews.size(); i++) {
	    boolean nextLabel = reviews.get(i).isPositiveLabel();
	    if (nextLabel != prevLabel) {
		allSame = false;
		break;
	    }
	}
	if (allSame) {
	    return new LeafNode(prevLabel);
	}
	InternalNode root = new InternalNode();
	/* Finding the best attribute */
	int bestAttribute = -1;
	double maxInformationGain = Double.NEGATIVE_INFINITY;
	for (int attribute : attributes) {
	    double informationGain = getInformationGain(reviews, attribute);
	    if (informationGain > maxInformationGain) {
		bestAttribute = attribute;
		maxInformationGain = informationGain;
	    }
	}
	final Integer fBestAttribute = bestAttribute;
	attributes.remove(fBestAttribute);
	Collections.sort(reviews,
		(r1, r2) -> Integer.compare(r1.getValue(fBestAttribute), r2.getValue(fBestAttribute)));
	List<Review> currentClass = new ArrayList<>();
	double minVal = Double.NEGATIVE_INFINITY;
	for (int i = 0; i < reviews.size(); i++) {
	    Review curr = reviews.get(i);
	    int currVal = curr.getValue(fBestAttribute);
	    boolean currLabel = curr.isPositiveLabel();
	    if (i + 1 < reviews.size()) {
		Review next = reviews.get(i + 1);
		int nextVal = next.getValue(fBestAttribute);
		boolean nextLabel = next.isPositiveLabel();
		currentClass.add(curr);
		if (nextVal != currVal || (nextVal != currVal && nextLabel != currLabel)) {
		    Node childNode = iterativeDichotomiser3(currentClass, attributes);
		    root.setWordID(bestAttribute);
		    childNode.setMinVal(minVal);
		    minVal = (currVal + nextVal) / 2.0;
		    childNode.setMaxVal(minVal);
		    root.addChild(childNode);
		    currentClass = new ArrayList<>();
		}
	    } else if (i == reviews.size() - 1) {
		currentClass.add(curr);
		Node childNode = iterativeDichotomiser3(currentClass, attributes);
		childNode.setWordID(bestAttribute);
		childNode.setMinVal(minVal);
		childNode.setMaxVal(Double.POSITIVE_INFINITY);
		root.addChild(childNode);
	    }
	}
	return root;
    }

    private double getInformationGain(List<Review> reviews, int attribute) {
	assert (reviews.size() > 0);
	Collections.sort(reviews, (r1, r2) -> Integer.compare(r1.getValue(attribute), r2.getValue(attribute)));
	/*
	 * If a particular value for an attribute is common for many reviews
	 * with different labels, we cannot split them, rather we take them in
	 * the same class, also we put all the reviews with same label in the
	 * same class
	 */
	int positive = 0;
	int negative = 0;
	int totalPositive = 0;
	int totalNegative = 0;
	double informationGain = 0;
	for (int i = 0; i < reviews.size(); i++) {
	    Review curr = reviews.get(i);
	    int currVal = curr.getValue(attribute);
	    boolean currLabel = curr.isPositiveLabel();
	    if (currLabel) {
		positive++;
	    } else {
		negative++;
	    }
	    if (i + 1 < reviews.size()) {
		int nextVal = reviews.get(i + 1).getValue(attribute);
		boolean nextLabel = reviews.get(i + 1).isPositiveLabel();
		if (nextVal != currVal || (nextVal != currVal && nextLabel != currLabel)) {
		    totalPositive += positive;
		    totalNegative += negative;
		    /*
		     * information Gain = entropy(reviews) -
		     * size(class)/size(reviews) * entropy(class) We will add
		     * entropy(reviews) at the end and normalize by
		     * size(reviews) at the end. Note, class size =
		     * postive+negative
		     */
		    informationGain -= entropy(positive, negative) * (positive + negative);
		    positive = 0;
		    negative = 0;
		}
	    } else if (i == reviews.size() - 1) {
		informationGain -= entropy(positive, negative) * (positive + negative);
		/* normalizing by size(reviews) & adding the positive term */
		informationGain /= reviews.size();
		informationGain += entropy(totalPositive, totalNegative);
	    }
	}
	assert (informationGain != Double.NaN);
	return informationGain;
    }

    private double entropy(int pos, int neg) {
	double pp = pos / ((double) (pos + neg)); // p-plus
	double pm = 1 - pp; // p-minus
	double entropy = 0;
	if (pp != 0)
	    entropy -= pp * Math.log(pp) / Math.log(2);
	if (pm != 0)
	    entropy -= pm * Math.log(pm) / Math.log(2);
	return entropy;
    }

    @Override
    public String toString() {
	return root.toString();
    }

    private void testReviews(List<Review> reviews) {
	int correct = 0;
	for (Review review : reviews) {
	    if (getLabelUsingTree(review) == review.isPositiveLabel()) {
		correct++;
	    }
	}
	System.out.println("Accuracy is " + (100.0 * correct / ((double) reviews.size())) + "%");
    }

    private boolean getLabelUsingTree(Review review) {
	return getLabelUsingTree(review, root);
    }

    private boolean getLabelUsingTree(Review review, Node node) {
	if (node instanceof InternalNode) {
	    InternalNode inode = (InternalNode) node;
	    int val = review.getValue(node.getWordID());
	    for (Node child : inode.getChildren()) {
		if (child.getMinVal() <= val && val <= child.getMaxVal()) {
		    return getLabelUsingTree(review, child);
		}
	    }
	    throw new RuntimeException("No Child Matched!");
	} else {
	    return ((LeafNode) node).getLabel();
	}
    }

    public static void main(String[] args) {
	System.out.println("Processing Training Data");
	List<Review> trainReviews = DataReader.obtainReviews("data/train/labeledBow.feat", 1000);
	System.out.println("Processing Testing Data");
	List<Review> testReviews = DataReader.obtainReviews("data/test/labeledBow.feat", 1000);
	System.out.println("Processing Training Attributes");
	Set<Integer> trainAttributes = new HashSet<>();
	trainReviews.stream().map(Review::getAttributes).forEach(trainAttributes::addAll);
	System.out.println("Building Tree");
	DecisionTree tree = new DecisionTree(trainReviews, trainAttributes);
	System.out.print("Training Data: ");
	tree.testReviews(trainReviews);
	System.out.print("Testing Data: ");
	tree.testReviews(testReviews);
	System.out.println();
    }

}
