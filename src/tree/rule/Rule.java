package tree.rule;

import java.util.ArrayList;
import java.util.List;

import data.Review;
import tree.node.LeafNode;
import tree.node.Node;

public class Rule {

    List<Node> antedecents;

    boolean label;
    public Rule(final LeafNode leafNode) {
	this.antedecents = new ArrayList<>();
	this.antedecents.add(leafNode);
	this.label = leafNode.getLabel();
    }

    public void addAntedecent(final Node internalNode) {
	this.antedecents.add(internalNode);
    }

    public double getEstimatedAccuracy(final List<Review> validateReviews) {
	int correct = 0;
	int total = 0;
	for (final Review review : validateReviews) {
	    boolean satisfies = true;
	    for (final Node antedecent : this.antedecents) {
		if (!antedecent.satisfies(review)) {
		    satisfies = false;
		    break;
		}
	    }
	    if (satisfies) {
		if (this.label == review.getLabel()) {
		    correct++;
		}
		total++;
	    }
	}
	return total == 0 ? 100.0 : (100.0 * correct) / (total);
    }

    public boolean getLabel() {
	return this.label;
    }

    public void prune(final List<Review> validateReviews) {
	double estimatedAccuracy = this.getEstimatedAccuracy(validateReviews);
	double maxEstimatedAccuracy = estimatedAccuracy;
	int removeIndex = 0;
	do {
	    for (int i = 0; i < this.antedecents.size(); i++) {
		final Node curr = this.antedecents.remove(i);
		final double newEstimatedAccuracy = this.getEstimatedAccuracy(validateReviews);
		if (newEstimatedAccuracy > maxEstimatedAccuracy) {
		    maxEstimatedAccuracy = newEstimatedAccuracy;
		    removeIndex = i;
		}
		this.antedecents.add(i, curr);
	    }
	    if (maxEstimatedAccuracy > estimatedAccuracy) {
		estimatedAccuracy = maxEstimatedAccuracy;
		this.antedecents.remove(removeIndex);
	    } else {
		break;
	    }
	} while (true);
    }

    public boolean satisfiedBy(final Review review) {
	return this.antedecents.stream().allMatch(antedecent -> antedecent.satisfies(review));
    }

    @Override
    public String toString() {
	return "(" + this.antedecents + ", " + this.label + ")";
    }

}
