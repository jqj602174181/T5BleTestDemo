package com.centerm.util;

/**
 * 字符格式化工具
 */
public class Formater {	
	/**
     * 
     * 将字节数组指定的范围内的字节转换为整数，转换最多读取数组的4位
     * 
     * @param bytes  待转换的字节数组
     * @param start  待转换的开始位置
     * @param length 最多转换的字节数组的长度
     * @return
     */
    public static int byteArrayToInteger(byte[] bytes, int start, int length) {
        int num = bytes[start];
        for (int i = start + 1; i < start + 4 && i < start + length
                && i < bytes.length; i++) {
            num = (num << 8) | (bytes[i] & 255);
        }
        return num;
    }

    /**
     * 将整型数字转换为长度为4的字节数组
     * 
     * @param num 待转换的数字
     * @return 获得的结果
     */
    public static byte[] IntegerToByteArray(int num) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) (num >>> ((3 - i) * 8));
        }
        return bytes;
    }
}
