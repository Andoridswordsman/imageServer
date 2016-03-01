

package com.imageServer.util;

public class ObjectUtil {
    /**
     * 判断字符串是否为空
     *
     * @param str
     *            字符串
     * @return 是否为空
     */
    public static boolean isNull(String str) {
        return (str == null || "".equals(str.trim()) || "null".equals(str.trim()) || "undefined".equals(str.trim()));
    }

}
