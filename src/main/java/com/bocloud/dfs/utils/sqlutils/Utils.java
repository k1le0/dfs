package com.bocloud.dfs.utils.sqlutils;

class Utils {
    static String toPropertyName(String name) {
        StringBuilder sb = new StringBuilder(name.length() + 10);
        int status = 0;
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);
            if (ch == '_') {
                status = 1;
            } else {
                if (status == 1) {
                    status = 0;
                    sb.append(Character.toUpperCase(ch));
                } else {
                    sb.append(ch);
                }
            }
        }
        return sb.toString();
    }

}
