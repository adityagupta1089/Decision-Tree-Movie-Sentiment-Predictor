package tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import data.DataReader;
import data.Review;
import tree.node.InternalNode;
import tree.node.LeafNode;
import tree.node.Node;

public class DecisionTree {

    private static final int TOTAL_REVIEWS = 1000;
    private static final int MAX_HEIGHT = 10;

    private static final String TRAINING_FILENAME = "data/train/labeledBow.feat";

    private Node root;

    public DecisionTree(List<Review> reviews, Set<Integer> attributes, boolean earlyStopping,
	    boolean attributePrinting) {
	Map<Integer, Integer> attributeCount = new HashMap<>();

	this.root = iterativeDichotomiser3(reviews, attributes, earlyStopping, attributeCount, 0);

	if (attributePrinting) {
	    System.out.println("Attribute Counts");
	    for (Entry<Integer, Integer> entry : attributeCount.entrySet()) {
		System.out.printf("%6d %2d\n", entry.getKey(), entry.getValue());
	    }
	    System.out.println("Total Leaf Nodes (Terminal Nodes) are " + LeafNode.getLeafNodeCount());
	}
    }

    // TODO Q.4 Pruning, post-pruning
    // TODO Q.4 Change in prediction accuracy as a function of pruning
    private Node iterativeDichotomiser3(List<Review> reviews, Set<Integer> attributes, boolean earlyStopping,
	    Map<Integer, Integer> attributeCount, int height) {
	/*
	 * Check if all labels are same or not, where return a single leaf node
	 * in case of the former
	 */
	boolean firstLabel = reviews.get(0).isPositiveLabel();
	boolean allSame = true;

	for (int i = 1; i < reviews.size(); i++) {
	    boolean currLabel = reviews.get(i).isPositiveLabel();
	    if (currLabel != firstLabel) {
		allSame = false;
		break;
	    }
	}
	if (allSame) {
	    return new LeafNode(firstLabel);
	}

	int totalSize = reviews.size();

	int totalPositive = (int) reviews.stream().filter(Review::isPositiveLabel).count();
	int totalNegative = totalSize - totalPositive;

	if (earlyStopping && height >= MAX_HEIGHT) {
	    return new LeafNode(totalPositive > totalNegative);
	}

	InternalNode root = new InternalNode();
	/* Finding the best attribute */

	int bestAttribute = -1;

	double maxInformationGain = 0;
	double maxSplitPosition = 0;

	double totalEntropy = entropy(totalPositive, totalNegative);

	for (int attribute : attributes) {

	    Collections.sort(reviews, (r1, r2) -> Integer.compare(r1.getValue(attribute), r2.getValue(attribute)));

	    int positive = 0;
	    int negative = 0;

	    for (int i = 0; i < totalSize - 1; i++) {
		if (reviews.get(i).isPositiveLabel()) {
		    positive++;
		} else {
		    negative++;
		}
		/*
		 * If we can split between this element and the next element we
		 * calculate information gain of this binary split
		 */
		int currVal = reviews.get(i).getValue(attribute);
		int nextVal = reviews.get(i + 1).getValue(attribute);

		if (nextVal != currVal) {

		    int remainingPositive = totalPositive - positive;
		    int remainingNegative = totalNegative - negative;

		    int size1 = positive + negative;
		    int size2 = remainingPositive + remainingNegative;

		    double informationGain = totalEntropy - (size1 * entropy(positive, negative)
			    + size2 * entropy(remainingPositive, remainingNegative)) / totalSize;

		    if (informationGain > maxInformationGain) {
			bestAttribute = attribute;
			maxInformationGain = informationGain;
			maxSplitPosition = (nextVal + currVal) / 2.0;
		    }
		}
	    }
	}
	if (maxInformationGain == 0) {
	    /*
	     * If no attribute makes any information gain then return a lead
	     * node with majority of the label. NOTE this occurs when we have
	     * duplicate attribute value pairs, or in case where we take a
	     * subset of them and this subset of pairs may collide for some
	     * reviews.
	     */
	    return new LeafNode(totalPositive > totalNegative);
	} else {

	    root.setWordID(bestAttribute);
	    if (!attributeCount.containsKey(bestAttribute)) {
		attributeCount.put(bestAttribute, 0);
	    }
	    attributeCount.put(bestAttribute, attributeCount.get(bestAttribute) + 1);

	    List<Review> class1 = new ArrayList<>();
	    List<Review> class2 = new ArrayList<>();

	    for (int i = 0; i < totalSize; i++) {
		if (reviews.get(i).getValue(bestAttribute) < maxSplitPosition) {
		    class1.add(reviews.get(i));
		} else {
		    class2.add(reviews.get(i));
		}
	    }

	    Node child1 = iterativeDichotomiser3(class1, attributes, earlyStopping, attributeCount, height + 1);
	    Node child2 = iterativeDichotomiser3(class2, attributes, earlyStopping, attributeCount, height + 1);

	    child1.setMinVal(Double.NEGATIVE_INFINITY);
	    child1.setMaxVal(maxSplitPosition);

	    child2.setMinVal(maxSplitPosition);
	    child2.setMaxVal(Double.POSITIVE_INFINITY);

	    root.addChild(child1);
	    root.addChild(child2);
	}
	return root;
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
	return root.toString("", true, true);
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
	    /*
	     * The ranges are sorted hence we can sweep from and left and
	     * recurse on appropriate node
	     */
	    for (Node child : inode.getChildren()) {
		if (child.getMinVal() <= val && val < child.getMaxVal()) {
		    return getLabelUsingTree(review, child);
		}
	    }
	    throw new RuntimeException("No Child Matched!");
	} else {
	    /* Reached a terminal node, return the label of this node */
	    return ((LeafNode) node).getLabel();
	}
    }

    // TODO Q.5 feature bagging
    // TODO Q.5 effect of number of trees in the forest on prediction accuracy
    /**
     * arguments : filename experiment-number
     */
    public static void main(String[] args) {
	if (args.length != 2) {
	    System.err.println("Two arguments expected, recieved " + args.length);
	} else {
	    String testFileName = args[0];
	    int exptNo = Integer.parseInt(args[1]);
	    if (exptNo < 2 || exptNo > 5) {
		System.err.println("Experiment Number should be 2-5, recieved " + exptNo);
	    } else {

		System.out.println("Processing Training Data");
		boolean noise = exptNo == 3;
		List<Review> trainReviews = DataReader.obtainReviews(TRAINING_FILENAME, TOTAL_REVIEWS, noise);

		System.out.println("Processing Testing Data");
		List<Review> testReviews = DataReader.obtainReviews(testFileName, TOTAL_REVIEWS, false);

		System.out.println("Processing Training Attributes");
		Set<Integer> trainAttributes = new HashSet<>();

		trainReviews.stream().map(Review::getAttributes).forEach(trainAttributes::addAll);

		System.out.println("Building Tree");
		DecisionTree tree = null;
		List<DecisionTree> forests = null;
		switch (exptNo) {
		/*
		 * Decision Tree arguments: reviews attributes earlyStopping
		 * attributePrinting
		 */
		case 2:
		    tree = new DecisionTree(trainReviews, trainAttributes, true, true);
		    break;
		case 3:
		case 4:
		    tree = new DecisionTree(trainReviews, trainAttributes, false, false);
		    break;
		case 5:
		    // TODO forests
		    break;
		}

		if (exptNo == 4) {
		    // TODO pruning
		}

		System.out.print("Training Data: ");
		tree.testReviews(trainReviews);

		System.out.print("Testing Data: ");
		tree.testReviews(testReviews);
	    }
	}
    }

}
