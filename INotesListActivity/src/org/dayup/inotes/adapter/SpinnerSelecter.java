package org.dayup.inotes.adapter;

/**
 * @author Nicky
 * 
 */
public class SpinnerSelecter {
    public static final int TYPE_FOLDER = 0;
    public static final int TYPE_ACCOUNT = 1;
    public static final int LABEL_TYPE_HEADER = 2;
    public static final int LABEL_TYPE_MIDDLE = 3;
    public static final int NO_ID = -1;

    public int type = TYPE_ACCOUNT;
    public long id = NO_ID;
    public String displayName = "";
}
