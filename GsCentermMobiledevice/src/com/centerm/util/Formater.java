package com.centerm.util;

/**
 * �ַ���ʽ������
 */
public class Formater {	
	/**
     * 
     * ���ֽ�����ָ���ķ�Χ�ڵ��ֽ�ת��Ϊ������ת������ȡ�����4λ
     * 
     * @param bytes  ��ת�����ֽ�����
     * @param start  ��ת���Ŀ�ʼλ��
     * @param length ���ת�����ֽ�����ĳ���
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
     * ����������ת��Ϊ����Ϊ4���ֽ�����
     * 
     * @param num ��ת��������
     * @return ��õĽ��
     */
    public static byte[] IntegerToByteArray(int num) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) (num >>> ((3 - i) * 8));
        }
        return bytes;
    }
}
