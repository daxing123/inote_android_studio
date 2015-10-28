package org.dayup.tasks;

public interface Task<T> {
    void preExecute();

    T doInbackground();

    void postExecute(T result);

    boolean isQuiet();

    void onCancel(Boolean canceled);
}
