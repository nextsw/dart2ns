package classes;

import d3e.core.ListExt;
import d3e.core.StringExt;

public class ParserUtil {
    public static boolean isDouble(String lit) {
        return (lit.indexOf(".", 0) >= 0l || (lit.indexOf("e", 0) >= 0l || lit.indexOf("E", 0) >= 0l))
                && !(ListExt.asList("0x", "0X", "0o", "0O", "0b", "0B").contains(StringExt.substring(lit, 0l, 2l)));
    }

    public static boolean isSpace(String s) {
        return Character.isWhitespace(s.charAt(0));
    }

    public static boolean isLetter(String c) {
        return Character.isLetter(c.charAt(0));
    }

    public static boolean isNameChar(String c) {
        return Character.isLetter(c.charAt(0)) || c.charAt(0) == '_';
    }

    public static boolean isFuncChar(String c) {
        return Character.isLetterOrDigit(c.charAt(0)) || c.charAt(0) == '_';
    }

    public static boolean containsCapital(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDigit(String c) {
        if(c.isEmpty()){
            return false;
        }
        return Character.isDigit(c.charAt(0));
    }

    public static boolean isBinDigit(String str) {
        if (!isDigit(str)) {
            return false;
        }
        char c = str.charAt(0);
        return c == '0' || c == '1';
    }

    public static boolean isHexDigit(String str) {
        if (str == null || str.length() != 1) {
            return false;
        }
        char c = str.charAt(0);
        return isDigit(str) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    public static boolean isOctDigit(String str) {
        if (str == null || str.length() != 1) {
            return false;
        }
        char c = str.charAt(0);
        return c >= '0' && c <= '7';
    }

    public static boolean isUpperCase(String str) {
        return str.charAt(0) == '_' || Character.isUpperCase(str.charAt(0));
    }

	public static boolean isTypeName(String lit) {
		if(lit.charAt(0) == '_' && lit.length() > 1) {
			return Character.isUpperCase(lit.charAt(1));
		} else {
			return Character.isUpperCase(lit.charAt(0));
		}
	}
}
