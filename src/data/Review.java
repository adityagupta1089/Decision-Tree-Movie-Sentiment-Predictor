package data;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Review {

    private boolean label;
    private HashMap<Integer, Integer> entries;

    private static final double NOISE_THRESHOLD = 0.0;
    private static final Random rand = new Random();

    public Review(String line, boolean noise) {
	String[] words = line.split(" ");
	int rating = Integer.parseInt(words[0]);
	assert (rating >= 7 || rating <= 4);
	if (rating >= 7) {
	    this.label = true;
	} else if (rating <= 4) {
	    this.label = false;
	}
	if (noise && rand.nextDouble() * 100 < NOISE_THRESHOLD) {
	    this.label = !this.label;
	}
	this.entries = new HashMap<>();
	for (int i = 1; i < words.length; i++) {
	    String[] idcount = words[i].split(":");
	    int id = Integer.parseInt(idcount[0]);
	    int cnt = Integer.parseInt(idcount[1]);
	    entries.put(id, cnt);
	}
    }

    public boolean isPositiveLabel() {
	return this.label;
    }

    public Set<Integer> getAttributes() {
	return this.entries.keySet();
    }

    public int getValue(int attribute) {
	return this.entries.containsKey(attribute) ? this.entries.get(attribute) : 0;
    }

    @Override
    public String toString() {
	return "(" + label + ", " + entries + ")";
    }

}
