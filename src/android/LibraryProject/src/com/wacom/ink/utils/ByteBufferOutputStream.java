package com.wacom.ink.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
	private ByteBuffer buffer;
	
	public ByteBufferOutputStream(ByteBuffer byteBuffer) throws IOException {
		if (byteBuffer == null) throw new IOException("Invalid ByteBuffer");
		this.buffer = byteBuffer;
	}

	@Override
	public void write(int oneByte) throws IOException {
		buffer.put((byte) oneByte);
	}
	
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		this.buffer.put(buffer, offset, count);
	}
}