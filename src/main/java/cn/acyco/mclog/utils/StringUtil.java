package cn.acyco.mclog.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Acyco
 * @create 2022-01-03 11:33
 * @url https://acyco.cn
 */
public class StringUtil {
    public static String StartStringTrim(String string, String trim) {
        if (string == null || string.length() == 0 || trim == null || trim.length() == 0) {
            return string;
        }
        String regex = "[" + trim + "]*+";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(string);
        if (matcher.lookingAt()) {
            string = string.substring(matcher.end());
        }
        return string;
    }
}
