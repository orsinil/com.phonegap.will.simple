package com.wacom.ink.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
	private ByteBuffer buffer;
	
	public ByteBufferInputStream(ByteBuffer byteBuffer) throws IOException {
		if (byteBuffer == null) throw new IOException("Invalid ByteBuffer");
		this.buffer = byteBuffer;
	}

	@Override
	public int read() throws IOException {
		if (buffer.remaining() == 0) return -1;
		return buffer.get();
	}
	
	@Override
	public int read(byte[] buff, int byteOffset, int byteCount) throws IOException {
		int count = Math.min(byteCount, buffer.remaining());
		if (count == 0) return -1;
		buffer.get(buff, byteOffset, count);
		return count;
	}
}