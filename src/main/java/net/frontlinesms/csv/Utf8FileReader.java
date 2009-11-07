/**
 * 
 */
package net.frontlinesms.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * In this class we read all files as UTF-8.
 * @author Alex
 */
public final class Utf8FileReader extends Reader {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** {@link InputStream} wrapping the file we are reading from */
	private final FileInputStream fileInputStream;
	/** {@link Reader} wrapping {@link #fileInputStream} and enforcing {@link CsvUtils#ENCODING_UTF8} */
	private final InputStreamReader inputStreamReader;
	/** {@link Reader} wrapping {@link #inputStreamReader} */
	private final BufferedReader bufferedReader;

//> CONSTRUCTORS
	/**
	 * Create a new reader using the UTF-8 character encoding to the supplied file.
	 * @param file 
	 * @throws IOException 
	 */
	public Utf8FileReader(File file) throws IOException {
		this.fileInputStream = new FileInputStream(file);
		try {
			this.inputStreamReader = new InputStreamReader(this.fileInputStream, CsvUtils.ENCODING_UTF8);
		} catch(UnsupportedEncodingException ex) {
			/*
			 * http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html
			 * "Every implementation of the Java platform is required to support [UTF-8]."
			 */
			throw new IllegalStateException("JVM lacks support for the required characterset: '" + CsvUtils.ENCODING_UTF8 + "'.", ex);
		}
		this.bufferedReader = new BufferedReader(this.inputStreamReader);
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/** Close all underlying streams */
	@Override
	public void close() {
		CsvUtils.close(this.bufferedReader);
		CsvUtils.close(this.inputStreamReader);
		CsvUtils.close(this.fileInputStream);
	}

	/** @see BufferedReader#read(char[], int, int) */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return this.bufferedReader.read(cbuf, off, len);
	}
}
