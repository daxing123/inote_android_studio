package com.fsck.k9.controller;

/**
 * @author chiron
 * 
 */
public interface AppendMessagesListener {

    public void messageFinished(String uid, String messageID, int number, int total,
            long startedTime);
}
