package org.dayup.inotes.sync;

import java.util.Date;

import org.dayup.common.Log;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.utils.ConvertUtils;
import org.dayup.inotes.utils.CustomHtmlConvertUtils;
import org.dayup.inotes.utils.StringUtils;

import android.text.TextUtils;

import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Part;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.internet.MimeUtility;
import com.fsck.k9.mail.internet.TextBody;

public class MessageCompose {

    public static Note notesFromMessage(Message message) throws MessagingException {
        Part textPart = MimeUtility.findFirstPartByMimeType(message, "text/plain");
        Note note = new Note();
        note.sid = message.getUid();
        String[] messageId = message.getHeader("X-Universally-Unique-Identifier");
        String[] createTime = message.getHeader("X-Mail-Created-Date");
        String[] modifyTime = message.getHeader("X-Modified-Date");

        if (messageId != null) {
            note.id = StringUtils.messageId2UUId(messageId[0]);
        }

        if (createTime != null) {
            note.createdTime = ConvertUtils.remoteTime2LocalTime(createTime[0]);
        } else {
            note.createdTime = message.getSentDate() == null ? System.currentTimeMillis() : message
                    .getSentDate().getTime();
        }

        if (modifyTime != null) {
            note.modifiedTime = ConvertUtils.remoteTime2LocalTime(modifyTime[0]);
        } else {
            note.modifiedTime = message.getSentDate() == null ? System.currentTimeMillis()
                    : message.getSentDate().getTime();
        }

        String subject = message.getSubject().trim();
        boolean htmlContent = false;
        if (textPart == null) {
            textPart = MimeUtility.findFirstPartByMimeType(message, "text/html");
            htmlContent = true;
        }

        if (textPart != null) {
            String content = MimeUtility.getTextFromPart(textPart).replace("\r", "")
                    .replace("&nbsp;", " ");
            if (htmlContent) {
                content = CustomHtmlConvertUtils.fromHtml(content);
            }
            note.content = getTextFromMessage(subject, content.trim());
        } else {
            note.content = subject;
        }

        return note;
    }

    private static String getTextFromMessage(String subject, String content) {
        if (TextUtils.isEmpty(subject)) {
            return content;
        }
        if (TextUtils.isEmpty(content)) {
            return subject;
        }
        if (TextUtils.equals(subject, content)) {
            return content;
        }
        
        int index = content.indexOf("\n");
        if (index > 0) {
            String tempS = content.substring(0, index).trim();
            if (tempS.startsWith(subject) || tempS.startsWith("- " + subject)) {
                content = " " + content;
            }
            return content;
        } else {
            if (content.startsWith("- " + subject)) {
                return content = " " + content;
            }
            return new StringBuffer().append(subject).append("\n").append(content).toString();
        }
    }

    public static MimeMessage messageFromNotes(Note note, INotesApplication application)
            throws MessagingException {
        MimeMessage msg = new MimeMessage();
        try {
            msg.addHeader("X-Mail-Created-Date", String.valueOf(note.createdTime));
            msg.addHeader("X-Universally-Unique-Identifier", note.id);
            msg.addHeader("X-Uniform-Type-Identifier", "com.apple.mail-note");
            msg.addHeader("Content-Type", "text/html;charset=utf-8");

            String content = "";
            if (!StringUtils.isEmpty(note.content)) {
                content = note.content;
            }
            msg.setSubject(Note.getNoteSubject(content));
            TextBody body = new TextBody(CustomHtmlConvertUtils.toHtml(content));
            msg.setUid(note.sid);
            msg.setMessageId(note.messageId);
            msg.setBody(body);
            msg.setInternalSentDate(new Date(note.modifiedTime));
            msg.setSentDate(new Date(note.modifiedTime));
            msg.setInternalDate(new Date(note.modifiedTime));
            msg.setFlag(Flag.SEEN, true);
        } catch (MessagingException e) {
            throw new MessagingException(e.getLocalizedMessage());
        }
        return msg;
    }

    public static MimeMessage messageFromDeletedNotes(Note note) throws MessagingException {
        MimeMessage msg = new MimeMessage();
        msg.setMessageId(note.messageId);
        msg.setUid(note.sid);
        return msg;
    }
}
