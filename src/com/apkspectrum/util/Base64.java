package com.apkspectrum.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class Base64 {

	private Base64() {}

	public static interface Decoder {
		public byte[] decode(byte[] src);
		public byte[] decode(String src);
		public int decode(byte[] src, byte[] dst);
		public ByteBuffer decode(ByteBuffer buffer);
		public InputStream wrap(InputStream is);
	}

	public static interface Encoder {
		public byte[] encode(byte[] src);
		public int encode(byte[] src,
                byte[] dst);
		public String encodeToString(byte[] src);
		public ByteBuffer encode(ByteBuffer buffer);
		public OutputStream wrap(OutputStream os) throws UnsupportedEncodingException;
		public Base64.Encoder withoutPadding() throws UnsupportedEncodingException;
	}

	private static class Base64Decoder implements Decoder {
		Class<?> clazz;
		Object instance;

		private Base64Decoder() throws ClassNotFoundException {
			this("getDecoder");
		}

		private Base64Decoder(String createInstanceMethod) throws ClassNotFoundException {
			clazz = Class.forName("java.util.Base64$Decoder");
			try {
				instance = Class.forName("java.util.Base64").getMethod(createInstanceMethod).invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				ClassNotFoundException t = new ClassNotFoundException();
				t.addSuppressed(e);
				throw t;
			}
		}

		public byte[] decode(byte[] src) {
			try {
				return (byte[]) clazz.getMethod("decode", byte[].class).invoke(instance, src);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		public byte[] decode(String src) {
			try {
				return (byte[]) clazz.getMethod("decode", String.class).invoke(instance, src);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		public int decode(byte[] src, byte[] dst) {
			try {
				return (int) clazz.getMethod("decode", byte[].class, byte[].class).invoke(instance, src, dst);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return 0;
		}

		public ByteBuffer decode(ByteBuffer buffer) {
			try {
				return (ByteBuffer) clazz.getMethod("decode", ByteBuffer.class).invoke(instance, buffer);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		public InputStream wrap(InputStream is) {
			try {
				return (InputStream) clazz.getMethod("wrap", InputStream.class).invoke(instance, is);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static class Base64Encoder implements Encoder {

		Class<?> clazz;
		Object instance;

		private Base64Encoder() throws ClassNotFoundException {
			this("getEncoder");
		}

		private Base64Encoder(String createInstanceMethod) throws ClassNotFoundException {
			clazz = Class.forName("java.util.Base64$Encoder");
			try {
				instance = Class.forName("java.util.Base64").getMethod(createInstanceMethod).invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				ClassNotFoundException t = new ClassNotFoundException();
				t.addSuppressed(e);
				throw t;
			}
		}

		private Base64Encoder(Object instance) throws ClassNotFoundException {
			clazz = Class.forName("java.util.Base64$Encoder");
			this.instance = instance;
		}

		@Override
		public byte[] encode(byte[] src) {
			try {
				return (byte[]) clazz.getMethod("encode", byte[].class).invoke(instance, src);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public int encode(byte[] src, byte[] dst) {
			try {
				return (int) clazz.getMethod("encode", byte[].class, byte[].class).invoke(instance, src, dst);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return 0;
		}

		@Override
		public String encodeToString(byte[] src) {
			try {
				return (String) clazz.getMethod("encodeToString", byte[].class).invoke(instance, src);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public ByteBuffer encode(ByteBuffer buffer) {
			try {
				return (ByteBuffer) clazz.getMethod("encode", ByteBuffer.class).invoke(instance, buffer);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public OutputStream wrap(OutputStream os) {
			try {
				return (OutputStream) clazz.getMethod("wrap", OutputStream.class).invoke(instance, os);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Encoder withoutPadding() {
			try {
				return new Base64Encoder(clazz.getMethod("withoutPadding").invoke(instance));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static class LegacyDecoder implements Decoder {
		Class<?> clazz;
		Object instance;

		private LegacyDecoder()  throws ClassNotFoundException {
			clazz = Class.forName("sun.misc.BASE64Decoder");
			try {
				instance = clazz.getConstructor().newInstance();
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
				ClassNotFoundException t = new ClassNotFoundException();
				t.addSuppressed(e);
				throw t;
			}
		}

		@Override
		public byte[] decode(byte[] src) {
			return decode(new String(src));
		}

		@Override
		public byte[] decode(String src) {
			try {
				return (byte[]) clazz.getMethod("decodeBuffer", String.class).invoke(instance, src);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public int decode(byte[] src, byte[] dst) {
			byte[] data = decode(src);
			for(int i = 0; i < data.length && i < dst.length; i++) {
				dst[i] = data[i];
			}
			return data.length <= dst.length ? data.length : dst.length;
		}

		@Override
		public ByteBuffer decode(ByteBuffer buffer) {
			return ByteBuffer.wrap(decode(buffer.array()));
		}

		@Override
		public InputStream wrap(InputStream is) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			try {
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new ByteArrayInputStream(decode(buffer.toByteArray()));
		}
	}

	public static class LegacyEncoder implements Encoder {
		Class<?> clazz;
		Object instance;

		private LegacyEncoder()  throws ClassNotFoundException {
			clazz = Class.forName("sun.misc.BASE64Encoder");
			try {
				instance = clazz.getConstructor().newInstance();
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
				ClassNotFoundException t = new ClassNotFoundException();
				t.addSuppressed(e);
				throw t;
			}
		}

		@Override
		public byte[] encode(byte[] src) {
			return encodeToString(src).getBytes();
		}

		@Override
		public int encode(byte[] src, byte[] dst) {
			byte[] data = encode(src);
			for(int i = 0; i < data.length && i < dst.length; i++) {
				dst[i] = data[i];
			}
			return data.length <= dst.length ? data.length : dst.length;
		}

		@Override
		public String encodeToString(byte[] src) {
			try {
				return (String) clazz.getMethod("encodeBuffer", byte[].class).invoke(instance, src);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public ByteBuffer encode(ByteBuffer buffer) {
			return ByteBuffer.wrap(encode(buffer.array()));
		}

		@Override
		public OutputStream wrap(OutputStream os) throws UnsupportedEncodingException {
			throw new UnsupportedEncodingException();
		}

		@Override
		public Encoder withoutPadding() throws UnsupportedEncodingException {
			throw new UnsupportedEncodingException();
		}
	}

	public static Decoder getDecorder() {
		try {
			return new Base64Decoder();
		} catch (ClassNotFoundException e) { }
		try {
			return new LegacyDecoder();
		} catch (ClassNotFoundException e) { }
		return null;
	}

	public static Decoder getMimeDecoder() {
		try {
			return new Base64Decoder("getMimeDecoder");
		} catch (ClassNotFoundException e) { }
		try {
			return new LegacyDecoder();
		} catch (ClassNotFoundException e) { }
		return null;
	}

	public static Decoder getUrlDecoder() {
		try {
			return new Base64Decoder("getUrlDecoder");
		} catch (ClassNotFoundException e) { }
		try {
			return new LegacyDecoder();
		} catch (ClassNotFoundException e) { }
		return null;
	}

	public static Encoder getEncoder() {
		try {
			return new Base64Encoder();
		} catch (ClassNotFoundException e) { }
		try {
			return new LegacyEncoder();
		} catch (ClassNotFoundException e) { }
		return null;
	}

	public static Encoder getMimeEncoder() {
		try {
			return new Base64Encoder("getMimeEncoder");
		} catch (ClassNotFoundException e) { }
		try {
			return new LegacyEncoder();
		} catch (ClassNotFoundException e) { }
		return null;
	}

	public static Encoder getUrlEncoder() {
		try {
			return new Base64Encoder("getUrlEncoder");
		} catch (ClassNotFoundException e) { }
		try {
			return new LegacyEncoder();
		} catch (ClassNotFoundException e) { }
		return null;
	}
}
