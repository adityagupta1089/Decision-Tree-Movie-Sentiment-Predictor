package tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import data.DataReader;
import data.Review;
import tree.node.InternalNode;
import tree.node.LeafNode;
import tree.node.Node;
import tree.rule.Rule;

public class DecisionTree {

    private static final int TOTAL_REVIEWS = 1000;

    private static final int[] MAX_HEIGHTS = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 50 };
    private static int MAX_HEIGHT = Integer.MAX_VALUE;

    private static final double[] NOISE_THRESHOLDS = new double[] { 0f, 0.5f, 1f, 5f, 10f };

    private static final int MAX_ATTRIBUTES_PRINT = 10;

    private static final String TRAINING_FILENAME = "data/train/labeledBow.feat";

    private static final int MAX_TREES_IN_FORESTS = 50;
    private static final int FOREST_STEP_SIZE = 5;

    private static double getAccuracy(final LinkedList<Rule> sortedRules, final List<Review> reviews) {
	int correct = 0;
	for (final Review review : reviews) {
	    for (final Rule rule : sortedRules) {
		if (rule.satisfiedBy(review)) {
		    if (rule.getLabel() == review.getLabel()) {
			correct++;
		    }
		    break;
		}
	    }
	}
	return (100.0 * correct) / (reviews.size());
    }

    private static int getCorrect(final List<DecisionTree> forest, final List<Review> reviews) {
	int correct = 0;
	for (final Review r : reviews) {
	    int positiveVote = 0;
	    int negativeVote = 0;
	    for (final DecisionTree tree : forest) {
		if (tree.getLabelUsingTree(r)) {
		    positiveVote++;
		} else {
		    negativeVote++;
		}
	    }
	    final boolean majorityVote = positiveVote > negativeVote;
	    if (majorityVote == r.getLabel()) {
		correct++;
	    }
	}
	return correct;
    }

    /**
     * @param args
     *            Exactly two arguments, i.e. filename and experiment-number
     */
    public static void main(final String[] args) {
	if (args.length != 2) {
	    System.err.println("Two arguments expected, recieved " + args.length);
	} else {
	    final String testFileName = args[0];
	    final int exptNo = Integer.parseInt(args[1]);
	    if ((exptNo < 2) || (exptNo > 5)) {
		System.err.println("Experiment Number should be 2-5, recieved " + exptNo);
	    } else {
		System.out.println("Processing Training/Validation Data");

		final List<Review> trainingAndValidationReviews = DataReader.obtainReviews(TRAINING_FILENAME,
			TOTAL_REVIEWS * 2);

		final List<Review> trainReviews = new ArrayList<>();
		final List<Review> validateReviews = new ArrayList<>();

		for (int i = 0; i < TOTAL_REVIEWS; i++) {
		    trainReviews.add(trainingAndValidationReviews.get(i));
		}

		for (int i = TOTAL_REVIEWS; i < (2 * TOTAL_REVIEWS); i++) {
		    validateReviews.add(trainingAndValidationReviews.get(i));
		}

		System.out.println("Processing Testing Data");
		final List<Review> testReviews = DataReader.obtainReviews(testFileName, TOTAL_REVIEWS);

		System.out.println("Processing Training Attributes");
		final Set<Integer> trainAttributes = new HashSet<>();

		trainReviews.stream().map(Review::getAttributes).forEach(trainAttributes::addAll);

		switch (exptNo) {
		case 2:
		    for (final int height : MAX_HEIGHTS) {
			MAX_HEIGHT = height;
			System.out.println("Current Maximum Height is " + height);
			System.out.println("Building Tree");
			final DecisionTree tree = new DecisionTree(trainReviews, trainAttributes, true, true);

			System.out.print("Training Data: ");
			tree.testReviews(trainReviews);

			System.out.print("Testing Data: ");
			tree.testReviews(testReviews);
		    }
		    break;
		case 3:
		    final Random rand = new Random();
		    rand.setSeed(System.currentTimeMillis());
		    for (final double noiseThreshold : NOISE_THRESHOLDS) {
			for (final Review r : trainReviews) {
			    r.resetLabel();
			    if ((rand.nextDouble() * 100) < noiseThreshold) {
				r.switchLabel();
			    }
			}

			System.out.println("Current Noise Threshold is " + noiseThreshold);
			System.out.println("Building Tree");
			final DecisionTree tree = new DecisionTree(trainReviews, trainAttributes, false, false);

			System.out.print("Training Data: ");
			tree.testReviews(trainReviews);

			System.out.print("Testing Data: ");
			tree.testReviews(testReviews);
		    }
		    break;
		case 4:

		    System.out.println("Building Tree");
		    final DecisionTree tree = new DecisionTree(trainReviews, trainAttributes, false, false);

		    int rulesPruned = 0;

		    System.out.println("Generating Rules");
		    final List<Rule> rules = tree.getRules();

		    final LinkedList<Rule> sortedRules = new LinkedList<>();
		    for (final Rule rule : rules) {
			sortedRules.add(rule);
		    }

		    System.out.println("Accuracy using tree: " + tree.getAccuracy(testReviews));
		    System.out.println("Accuracy using unsorted rules: " + getAccuracy(sortedRules, testReviews));
		    Collections.sort(sortedRules, (r1, r2) -> Double.compare(r2.getEstimatedAccuracy(validateReviews),
			    r1.getEstimatedAccuracy(validateReviews)));
		    System.out.println("Accuracy using sorted rules: " + getAccuracy(sortedRules, testReviews));
		    System.out.println("Pruning Rules on Validation Data");

		    System.out.println("+------------+----------------+");
		    System.out.println("|Rules Pruned|Testing Accuracy|");
		    System.out.println("+------------+----------------+");

		    for (final Rule rule : rules) {
			rule.prune(validateReviews);
			Collections.sort(sortedRules,
				(r1, r2) -> Double.compare(r2.getEstimatedAccuracy(validateReviews),
					r1.getEstimatedAccuracy(validateReviews)));
			rulesPruned++;
			System.out.printf("|%12d|%16.2f|\n", rulesPruned, getAccuracy(sortedRules, testReviews));
		    }
		    System.out.println("+------------+----------------+");
		    break;
		case 5:
		    final List<DecisionTree> forest = new ArrayList<>();
		    final List<Integer> trainAttributesList = new ArrayList<>(trainAttributes);

		    final int subsetAttributeSize = (int) Math.round(Math.sqrt(trainAttributesList.size()));

		    System.out.println("+-----------+--------------------+--------------------+");
		    System.out.printf("|%11s|%20s|%20s|\n", "Total Trees", "Training Accuracy", "Testing Accuracy");
		    System.out.println("+-----------+--------------------+--------------------+");

		    for (int i = 0; i < (MAX_TREES_IN_FORESTS / FOREST_STEP_SIZE); i++) {
			for (int k = 0; k < FOREST_STEP_SIZE; k++) {
			    Collections.shuffle(trainAttributesList);

			    final Set<Integer> attributeSubset = new HashSet<>();

			    for (int j = 0; j < subsetAttributeSize; j++) {
				attributeSubset.add(trainAttributesList.get(j));
			    }

			    forest.add(new DecisionTree(trainReviews, attributeSubset, false, false));
			}

			final int trainCorrect = getCorrect(forest, trainReviews);
			final int testCorrect = getCorrect(forest, testReviews);

			final double trainingAccuracy = (100.0 * trainCorrect) / (trainReviews.size());
			final double testingAccuracy = (100.0 * testCorrect) / (testReviews.size());

			System.out.printf("|%11d|%20f|%20f|\n", forest.size(), trainingAccuracy, testingAccuracy);
		    }

		    System.out.println("+-----------+--------------------+--------------------+");
		    break;
		}
	    }
	}
    }

    private final Node root;

    /**
     * @param reviews
     *            The set of reviews to build the tree upon
     * @param attributes
     *            The attributes to use to build the tree
     * @param earlyStopping
     *            Whether to stop building the tree after some threshold
     * @param attributePrinting
     *            Whether to print the Attribute Usage Statistics
     */
    public DecisionTree(final List<Review> reviews, final Set<Integer> attributes, final boolean earlyStopping,
	    final boolean attributePrinting) {
	final Map<Integer, Integer> attributeCount = new HashMap<>();
	LeafNode.resetLeafNodeCount();

	this.root = this.iterativeDichotomiser3(reviews, attributes, earlyStopping, attributeCount, 0);

	if (attributePrinting) {
	    System.out.println("Top " + MAX_ATTRIBUTES_PRINT + " Attribute Counts");
	    System.out.println("+---------+-----+");
	    System.out.printf("|%9s|%5s|\n", "Attribute", "Count");
	    System.out.println("+---------+-----+");
	    attributeCount.entrySet().stream().sorted((k1, k2) -> Integer.compare(k2.getValue(), k1.getValue()))
		    .limit(MAX_ATTRIBUTES_PRINT).forEach(entry -> {
			System.out.printf("|%9d|%5d|\n", entry.getKey(), entry.getValue());
		    });
	    System.out.println("+---------+-----+");
	    System.out.println("Total Leaf Nodes (Terminal Nodes) are " + LeafNode.getLeafNodeCount());
	}

    }

    /**
     * @param pos
     *            positive labeled reviews
     * @param neg
     *            negative labeled reviews
     */
    private double entropy(final int pos, final int neg) {

	final double pp = pos / ((double) (pos + neg)); // p-plus
	final double pm = 1 - pp; // p-minus

	double entropy = 0;

	if (pp != 0) {
	    entropy -= (pp * Math.log(pp)) / Math.log(2);
	}
	if (pm != 0) {
	    entropy -= (pm * Math.log(pm)) / Math.log(2);
	}
	return entropy;
    }

    private double getAccuracy(final List<Review> reviews) {
	int correct = 0;
	for (final Review review : reviews) {
	    if (this.getLabelUsingTree(review) == review.getLabel()) {
		correct++;
	    }
	}
	return (100.0 * correct) / (reviews.size());
    }

    private boolean getLabelUsingTree(final Review review) {
	return this.root.getLabel(review);
    }

    private List<Rule> getRules() {
	return this.root.getRules();
    }

    private Node iterativeDichotomiser3(final List<Review> reviews, final Set<Integer> attributes,
	    final boolean earlyStopping, final Map<Integer, Integer> attributeCount, final int height) {
	/*
	 * Check if all labels are same or not, where return a single leaf node
	 * in case of the former
	 */
	final boolean firstLabel = reviews.get(0).getLabel();
	boolean allSame = true;

	for (int i = 1; i < reviews.size(); i++) {
	    final boolean currLabel = reviews.get(i).getLabel();
	    if (currLabel != firstLabel) {
		allSame = false;
		break;
	    }
	}
	if (allSame) {
	    return new LeafNode(firstLabel);
	}

	final int totalSize = reviews.size();

	final int totalPositive = (int) reviews.stream().filter(Review::getLabel).count();
	final int totalNegative = totalSize - totalPositive;

	if (earlyStopping && (height >= MAX_HEIGHT)) {
	    return new LeafNode(totalPositive > totalNegative);
	}

	final InternalNode root = new InternalNode();
	/* Finding the best attribute */

	int bestAttribute = -1;

	double maxInformationGain = 0;
	double maxSplitPosition = 0;

	final double totalEntropy = this.entropy(totalPositive, totalNegative);

	for (final int attribute : attributes) {

	    Collections.sort(reviews, (r1, r2) -> Integer.compare(r1.getValue(attribute), r2.getValue(attribute)));

	    int positive = 0;
	    int negative = 0;

	    for (int i = 0; i < (totalSize - 1); i++) {
		if (reviews.get(i).getLabel()) {
		    positive++;
		} else {
		    negative++;
		}
		/*
		 * If we can split between this element and the next element we
		 * calculate information gain of this binary split
		 */
		final int currVal = reviews.get(i).getValue(attribute);
		final int nextVal = reviews.get(i + 1).getValue(attribute);

		if (nextVal != currVal) {

		    final int remainingPositive = totalPositive - positive;
		    final int remainingNegative = totalNegative - negative;

		    final int size1 = positive + negative;
		    final int size2 = remainingPositive + remainingNegative;

		    final double informationGain = totalEntropy - (((size1 * this.entropy(positive, negative))
			    + (size2 * this.entropy(remainingPositive, remainingNegative))) / totalSize);

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

	    final List<Review> class1 = new ArrayList<>();
	    final List<Review> class2 = new ArrayList<>();

	    for (int i = 0; i < totalSize; i++) {
		if (reviews.get(i).getValue(bestAttribute) < maxSplitPosition) {
		    class1.add(reviews.get(i));
		} else {
		    class2.add(reviews.get(i));
		}
	    }

	    final Node child1 = this.iterativeDichotomiser3(class1, attributes, earlyStopping, attributeCount,
		    height + 1);
	    final Node child2 = this.iterativeDichotomiser3(class2, attributes, earlyStopping, attributeCount,
		    height + 1);

	    child1.setParentWordID(bestAttribute);
	    child1.setMinVal(Double.NEGATIVE_INFINITY);
	    child1.setMaxVal(maxSplitPosition);

	    child2.setParentWordID(bestAttribute);
	    child2.setMinVal(maxSplitPosition);
	    child2.setMaxVal(Double.POSITIVE_INFINITY);

	    root.addChild(child1);
	    root.addChild(child2);
	}
	return root;
    }

    private void testReviews(final List<Review> reviews) {
	System.out.println("Accuracy is " + this.getAccuracy(reviews) + "%");
    }

    @Override
    public String toString() {
	return this.root.toString("", true, true);
    }

}
