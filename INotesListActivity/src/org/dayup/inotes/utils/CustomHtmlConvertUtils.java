package org.dayup.inotes.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dayup.common.Log;

import android.text.TextUtils;
import android.widget.Toast;

/**
 * 
 * @author Jack
 */
public class CustomHtmlConvertUtils {

    public static String toHtml(String text) {

        if (TextUtils.isEmpty(text)) {
            return text;
        }
        String[] subs = text.split("\n");
        StringBuffer sb = new StringBuffer();
        sb.append("<html>").append("<head>").append("</head>").append("<body>");

        boolean liTagStarted = false;

        for (int i = 0, size = subs.length; i < size; i++) {
            String sub = subs[i];
            Pattern p = Pattern.compile("\\s?- ");
            Matcher m = p.matcher(sub);
            if (m.find()) {
                String prefix = m.group(0);
                if (!liTagStarted) {
                    sb.append("<div><ul class=\"Apple-dash-list\">");
                    liTagStarted = true;
                }
                String content = sub.substring(prefix.length());
                sb.append("<li>").append(TextUtils.isEmpty(content) ? "<br/>" : content)
                        .append("</li>");
            } else {
                if (liTagStarted) {
                    sb.append("</ul></div>");
                    liTagStarted = false;
                }
                sb.append("<div>").append(TextUtils.isEmpty(sub) ? "<br/>" : sub).append("</div>");
            }
        }
        if (liTagStarted) {
            sb.append("</ul></div>");
        }
        sb.append("</body>").append("</html>");
        return sb.toString();
    }

    public static String fromHtml(String html) {
        if (TextUtils.isEmpty(html)) {
            return html;
        }
        html = html.replace("<div><br></div>", "<br>");
        html = html.replace("<span><br></span>", "<span></span>");
        html = html.replace("\n", "").replaceAll("\\</.*?>", "");
        html = html.replace("<html><head></head><body>", "");
        String[] htmlDivStrs = html.split("<div>");
        StringBuffer sb = new StringBuffer();
        sb = replaceLiTag(replaceDivTag(htmlDivStrs));
        String text = sb.toString().replace("<br>", "\n");
        text = text.replace("\n<ul class=\"Apple-dash-list\">", "\n");
        text = text.replace("<ul class=\"Apple-dash-list\">", "\n");
        return text.replaceAll("\\<.*?>", "");
    }

    public static StringBuffer replaceLiTag(StringBuffer sb) {
        String[] htmlLiStrs = sb.toString().split("<li>");
        if (htmlLiStrs == null || htmlLiStrs.length == 0) {
            return sb;
        }
        StringBuffer liSb = new StringBuffer();
        for (int i = 0, size = htmlLiStrs.length; i < size; i++) {
            String liStr = htmlLiStrs[i];
            // <li>标签下为空时带了一个<br>
            liStr = liStr.replace("<br></li>", "");
            if (i > 0) {
                String liPre = htmlLiStrs[i - 1];
                if (TextUtils.isEmpty(liPre) || !liPre.endsWith("<ul class=\"Apple-dash-list\">")) {
                    liSb.append("\n");
                }
                liSb.append(" - ").append(liStr);
            } else {
                liSb.append(liStr);
            }
        }
        return liSb;
    }

    public static StringBuffer replaceDivTag(String[] htmlDivStrs) {
        StringBuffer divSb = new StringBuffer();
        for (int i = 0, size = htmlDivStrs.length; i < size; i++) {
            String htmlSplit = htmlDivStrs[i];
            if (i == 0 || TextUtils.isEmpty(htmlSplit)) {
                divSb.append(htmlSplit);
            } else {
                divSb.append("\n").append(htmlSplit);
            }
        }
        return divSb;
    }
}
