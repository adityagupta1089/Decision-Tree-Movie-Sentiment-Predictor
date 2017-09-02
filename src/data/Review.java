package data;

import java.util.HashMap;
import java.util.Set;

public class Review {

    private final boolean originalLabel;
    private boolean label;
    private final HashMap<Integer, Integer> entries;

    public Review(final String line) {
	final String[] words = line.split(" ");
	final int rating = Integer.parseInt(words[0]);
	assert ((rating >= 7) || (rating <= 4));
	if (rating >= 7) {
	    this.label = true;
	} else if (rating <= 4) {
	    this.label = false;
	}
	this.originalLabel = this.label;
	this.entries = new HashMap<>();
	for (int i = 1; i < words.length; i++) {
	    final String[] idcount = words[i].split(":");
	    final int id = Integer.parseInt(idcount[0]);
	    final int cnt = Integer.parseInt(idcount[1]);
	    this.entries.put(id, cnt);
	}
    }

    public Set<Integer> getAttributes() {
	return this.entries.keySet();
    }

    public boolean getLabel() {
	return this.label;
    }

    public int getValue(final int attribute) {
	return this.entries.containsKey(attribute) ? this.entries.get(attribute) : 0;
    }

    public void resetLabel() {
	this.label = this.originalLabel;
    }

    public void switchLabel() {
	this.label = !this.originalLabel;
    }

    @Override
    public String toString() {
	return "(" + this.label + ", " + this.entries + ")";
    }
}
