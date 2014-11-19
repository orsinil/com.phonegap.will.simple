package com.wacom.ink.utils;

import java.io.IOException;
import java.io.InputStream;

public class BitUtils {
	
	public static boolean compare(byte[] arr1, byte[] arr2){
		if (arr1==null || arr2==null){
			return false;
		} else if (arr1.length!=arr2.length){
			return false;
		} else {
			for (int i=0;i<arr1.length;i++){
				if (arr1[i]!=arr2[i]){
					return false;
				}
			}
		}
		return true;
	}
	
	public static int uint32LittleEndian(int value) {
		value = (int)((long)value & 0xFFFFFFFF);
		return swapBytes(value);
	}
	
	public static short swapBytes(short value) {
		int b1 = value & 0xff;
		int b2 = (value >> 8) & 0xff;

		return (short) (b1 << 8 | b2 << 0);
	}


	public static int swapBytes(int value) {
		int b1 = (value >>  0) & 0xff;
		int b2 = (value >>  8) & 0xff;
		int b3 = (value >> 16) & 0xff;
		int b4 = (value >> 24) & 0xff;

		return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
	}

	public static long swapBytes(long value) {
		long b1 = (value >>  0) & 0xff;
		long b2 = (value >>  8) & 0xff;
		long b3 = (value >> 16) & 0xff;
		long b4 = (value >> 24) & 0xff;
		long b5 = (value >> 32) & 0xff;
		long b6 = (value >> 40) & 0xff;
		long b7 = (value >> 48) & 0xff;
		long b8 = (value >> 56) & 0xff;

		return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 | b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
	}
	
	
	public static boolean findBytePattern(byte[] pattern, InputStream in) throws IOException {
		int read = -1;
		int index = 0;
		do {
			if (index == pattern.length) {
				return true;
			}
			read = in.read();
			if (read == pattern[index]) {
				index++;
			} else {
				index = 0;
			}
		}
		while(read != -1);
		return false;
	}
}
