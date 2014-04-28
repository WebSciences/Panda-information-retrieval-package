package uk.ac.ucl.panda.retrieval.models;

import java.util.ArrayList;

/**
 * SArray is a utility class that makes ArrayList methods consistent
 * with those in Vector.
 */
public class SArray extends ArrayList<Object> {

	private static final long serialVersionUID = -5673263599080704778L;
	private int last_index = 0;

	public int length() {
		return (size());
	}

	public Object elementAt(int index) {
		last_index = index;
		return (get(index));
	}

	public void addElement(Object o) {
		add(o);
	}

	/**
	 * Sets the last index requested to the supplied value.
	 */

	public void setLastIndex(int value) {
		last_index = value;
	}

	/**
	 * Returns the last index requested by the elementAt method.
	 */
	public int getLastIndex() {
		return (last_index);
	}

	/**
	 * Make compatible with Vector.
	 */
	public void removeElementAt(int index) {
		remove(index);
	}

	/**
	 * Make compatible with Vector.
	 */
	public void removeElement(Object obj) {
		int index = indexOf(obj);

		if (index > -1)
			remove(index);
	}
}
