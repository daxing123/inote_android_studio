package org.dayup.tasks;

public interface TwoPhaseTask<T> extends Task<T> {
	boolean doFirstPhaseInBackground();
}
