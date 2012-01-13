package com.plexobject.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class IOHelper {
	public static Object deepCopy(java.io.Serializable source)
			throws java.io.IOException {
		try {
			return toObject(toBytes(source));
		} catch (ClassNotFoundException e) {
			throw new java.io.IOException(e.getMessage());
		}
	}

	public static byte[] toBytes(java.io.Serializable source)
			throws java.io.IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(byteStream));
		os.writeObject(source);
		os.flush();
		byte[] data = byteStream.toByteArray();
		byteStream.close();
		os.close();
		return data;
	}

	public static byte[] load(File inputFile) throws java.io.IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				inputFile));
		byte[] data = load(in);
		in.close();
		return data;
	}

	public static String loadString(File inputFile) throws java.io.IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				inputFile));
		String s = loadString(in);
		in.close();
		return s;
	}

	public static String loadString(InputStream in) throws java.io.IOException {
		// BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringWriter out = new StringWriter();
		int c;
		while ((c = in.read()) != -1) {
			out.write((byte) c);
		}
		out.close();
		return out.toString();
	}

	public static byte[] load(InputStream in) throws java.io.IOException {
		// BufferedReader in = new BufferedReader(new InputStreamReader(is));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c;
		while ((c = in.read()) != -1) {
			out.write((byte) c);
		}
		out.close();
		return out.toByteArray();
	}

	public static byte[] loadUntilFrom(InputStream is)
			throws java.io.IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line = null;
		boolean anotherMessage = false;
		StringBuffer anotherBuffer = new StringBuffer();
		StringBuffer sb = new StringBuffer();

		int i = 0;
		while ((line = in.readLine()) != null) {
			if (i > 5 && line.startsWith("From ") && line.indexOf('@') != -1) {
				anotherMessage = true;
			}
			if (anotherMessage) {
				anotherBuffer.append(line + '\n');
			} else {
				sb.append(line + '\n');
			}
			i++;
		}
		if (anotherMessage) {
			String buf2 = anotherBuffer.toString();
			if (buf2.indexOf("From: ") != -1 && buf2.indexOf("To: ") != -1) {
				return sb.toString().getBytes(); // return first message, ignore
													// second
			}
			sb.append(buf2);
		}
		return sb.toString().getBytes();
	}

	public static void save(byte[] input, File outFile)
			throws java.io.IOException {
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(outFile));
		save(input, out);
	}

	public static void save(byte[] input, OutputStream out)
			throws java.io.IOException {
		out.write(input);
	}

	public static Object toObject(byte[] data) throws java.io.IOException,
			ClassNotFoundException {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				byteStream));
		Object object = is.readObject();
		is.close();
		byteStream.close();
		return object;
	}

	public static String fileToString(String filename) throws IOException {
		return fileToString(new File(filename));
	}

	public static String fileToString(File file) throws IOException {
		return fileToString(new FileReader(file));
	}

	public static String fileToString(InputStream in) throws IOException {
		return fileToString(new InputStreamReader(in));
	}

	public static String fileToString(Reader in) throws IOException {
		StringBuilder buffer = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(in);
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + "\n");
			}
		} catch (IOException e) {
			throw e;
		}
		return buffer.toString();
	}

	public static String[] fileToStrings(String filename) throws IOException {
		return fileToStrings(new File(filename));
	}

	public static String[] fileToStrings(File file) throws IOException {
		return fileToStrings(new FileInputStream(file));
	}

	public static String[] fileToStrings(InputStream in) throws IOException {
		List<String> list = new ArrayList<String>();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		while ((line = reader.readLine()) != null) {
			list.add(line);
		}
		reader.close();
		return list.toArray(new String[list.size()]);
	}

	public static void stringToFile(String filename, String buffer)
			throws IOException {
		stringToFile(new File(filename), buffer);
	}

	public static void stringToFile(File file, String buffer)
			throws IOException {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(file)));
			writer.println(buffer);
			writer.flush();
		} catch (IOException e) {
			throw e;
		}
	}

	public static void copy(String inFile, String outFile) throws IOException {
		copy(new File(inFile), new File(outFile));
	}

	public static void copy(File inFile, File outFile) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				inFile));
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(outFile));
		copy(in, out);
		in.close();
		out.close();
	}

	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		if (in instanceof BufferedInputStream == false)
			in = new BufferedInputStream(in);
		if (out instanceof BufferedOutputStream == false)
			out = new BufferedOutputStream(out);
		byte[] buffer = new byte[8192];
		int len = 0;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
		out.flush();
	}

	public static boolean deleteDirectory(File file, boolean recurse)
			throws IOException {
		File[] list = file.listFiles();
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory() && recurse)
				deleteDirectory(list[i], recurse);
			list[i].delete();
		}
		return file.delete();
	}

	public static void safecopy(File inFile, File outFile) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				inFile));
		safecopy(in, outFile);
	}

	public static void safecopy(InputStream in, File outFile)
			throws IOException {
		File temp = File.createTempFile(outFile.getName(), "REN");
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(temp));
		int c;
		while ((c = in.read()) != -1) {
			out.write(c);
		}
		in.close();
		out.close();
		if (outFile.exists())
			outFile.delete();
		temp.renameTo(outFile);
	}
}
