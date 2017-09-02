package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataReader {

    /**
     * Reads given quantity of reviews from the given file.
     */
    public static List<Review> obtainReviews(final String filename, final int reviewCount) {
	BufferedReader br = null;
	/* List of all reviews */
	final List<Review> reviews = new ArrayList<>();
	try {
	    br = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = br.readLine()) != null) {
		reviews.add(new Review(line));
	    }
	} catch (final Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		br.close();
	    } catch (final IOException e) {
		e.printStackTrace();
	    }
	}
	/*
	 * We need to take random subset, so shuffling all reviews and taking
	 * the required ones from the top seems reasonable keeping the positive
	 * and negative ones limited to reviewCount/2 each
	 */
	int positiveCount = 0;
	int negativeCount = 0;
	Collections.shuffle(reviews);
	final List<Review> selected = new ArrayList<>();
	for (int i = 0; selected.size() < reviewCount; i++) {
	    final Review curr = reviews.get(i);
	    if (curr.getLabel() && (positiveCount < (reviewCount / 2))) {
		positiveCount++;
		selected.add(curr);
	    } else if (!curr.getLabel() && (negativeCount < (reviewCount / 2))) {
		negativeCount++;
		selected.add(curr);
	    }
	}
	return selected;
    }

}
