package org.dayup.inotes.utils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dayup.common.Log;

import android.text.TextUtils;

public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean equals(String str1, String str2) {
        if (str1 != null) {
            return str1.equals(str2);
        } else {
            return str2 == null;
        }
    }

    /**
     * ÂäüËÉΩÔºöÂà§Êñ≠‰∏Ä‰∏™Â≠óÁ¨¶‰∏≤ÊòØÂê¶ÂåÖÂê´ÁâπÊÆäÂ≠óÁ¨¶
     * 
     * @param string
     *            Ë¶ÅÂà§Êñ≠ÁöÑÂ≠óÁ¨¶‰∏≤
     * @return true Êèê‰æõÁöÑÂèÇÊï∞string‰∏çÂåÖÂê´ÁâπÊÆäÂ≠óÁ¨¶
     * @return false Êèê‰æõÁöÑÂèÇÊï∞stringÂåÖÂê´ÁâπÊÆäÂ≠óÁ¨¶
     */
    public static boolean isConSpeCharacters(String string) {
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~ÔºÅ@#Ôø•%‚Ä¶‚Ä¶&*ÔºàÔºâ‚Äî‚Äî+|{}„Äê„Äë‚ÄòÔºõÔºö‚Äù‚Äú‚Äô„ÄÇÔºå„ÄÅÔºü]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(string);
        return m.find();
    }

    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        String text = str;
        String repl = "'";
        String with = "''";
        int max = -1;

        StringBuffer buf = new StringBuffer(text.length());
        int start = 0, end = 0;
        while ((end = text.indexOf(repl, start)) != -1) {
            buf.append(text.substring(start, end)).append(with);
            start = end + repl.length();

            if (--max == 0) {
                break;
            }
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static String escapeHtml(String str) {
        if (str == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        char[] chars = str.toCharArray();
        for (char c : chars) {
            if (c == ' ') {
                sb.append("&ensp;");
            } else if (c == '　') {
                sb.append("&emsp;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '©') {
                sb.append("&copy;");
            } else if (c == '®') {
                sb.append("&reg;");
            } else if (c == '™') {
                sb.append("™");
            } else if (c == '×') {
                sb.append("&times;");
            } else if (c == '÷') {
                sb.append("&divide;");
            } else if (c == '\n') {
                sb.append("<br>");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String escapeHtmlToString(String str) {
        return str.replace("&ensp;", " ").replace("&emsp;", "„ÄÄ").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&amp;", "&").replace("&quot;", "\"")
                .replace("&copy;", "¬©").replace("&reg;", "¬Æ").replace("‚Ñ¢", "‚Ñ¢")
                .replace("&times;", "√ó").replace("&divide;", "√∑").replace("<br>", "\n");
    }

    public static String toUtf8(String str) {
        try {
            return new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("toUtf8", e.getMessage(), e);
            return str;
        }
    }

    public static String uuId2MessageId(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return uuid;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("<").append(uuid).append("@email.android.com>");
        return sb.toString();
    }

    public static String messageId2UUId(String massageId) {
        if (TextUtils.isEmpty(massageId)) {
            return massageId;
        }
        return massageId.replace("<", "").replace("@email.android.com>", "");
    }

    public static int parseInt(String str, int defaultValue) {
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }

    }

}
