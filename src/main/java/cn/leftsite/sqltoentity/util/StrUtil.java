package cn.leftsite.sqltoentity.util;

public class StrUtil {
    private StrUtil() {
    }

    public static String toCamelCase(CharSequence charSequence) {
        if (null == charSequence) {
            return null;
        } else {
            String str = charSequence.toString();
            if (str.contains("_")) {
                int length = str.length();
                StringBuilder sb = new StringBuilder(length);
                boolean upperCase = false;

                for (int i = 0; i < length; ++i) {
                    char c = str.charAt(i);
                    if (c == '_') {
                        upperCase = true;
                    } else if (upperCase) {
                        sb.append(Character.toUpperCase(c));
                        upperCase = false;
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                }
                return sb.toString();
            } else {
                return str;
            }
        }
    }
}