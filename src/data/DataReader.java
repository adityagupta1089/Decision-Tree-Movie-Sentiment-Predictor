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
    public static List<Review> obtainReviews(String filename, int reviewCount) {
	BufferedReader br = null;
	/* List of all reviews */
	List<Review> reviews = new ArrayList<>();
	try {
	    br = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = br.readLine()) != null) {
		reviews.add(new Review(line));
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		br.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	/*
	 * We need to take random subset, so suffling all reviews and taking the
	 * required ones from the top seems reasonable
	 */
	Collections.shuffle(reviews);
	List<Review> selected = new ArrayList<>();
	for (int i = 0; i < reviewCount; i++) {
	    selected.add(reviews.get(i));
	}
	return selected;
    }

}
