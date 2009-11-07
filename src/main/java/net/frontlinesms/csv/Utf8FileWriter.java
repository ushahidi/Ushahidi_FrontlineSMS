/**
 * 
 */
package net.frontlinesms.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Writer for writing UTF-8 text to a file.
 * @author Alex
 */
class Utf8FileWriter extends Writer {	
	/** Stream to the file. */
	private FileOutputStream fileOutputStream;
	/** Writer to {@link #fileOutputStream} */
	private OutputStreamWriter outputStreamWriter;
	/** Buffer wrapping the {@link #outputStreamWriter} */
	private BufferedWriter bufferedWriter;

//> CONSTRUCTORS
	/**
	 * Creates a new UTF-8 {@link Writer} to a specific {@link File}.
	 * @param file The file to write to.
	 * @throws IOException if there was an I/O error
	 */
	public Utf8FileWriter(File file) throws IOException {
		this.fileOutputStream = new FileOutputStream(file);
		try {
			this.outputStreamWriter = new OutputStreamWriter(this.fileOutputStream, CsvUtils.ENCODING_UTF8);
		} catch(UnsupportedEncodingException ex) {
			/*
			 * http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html
			 * "Every implementation of the Java platform is required to support [UTF-8]."
			 */
			throw new IllegalStateException("JVM lacks support for the required characterset: '" + CsvUtils.ENCODING_UTF8 + "'.", ex);
		}
		this.bufferedWriter = new BufferedWriter(this.outputStreamWriter);
	}

//> WRITER METHODS
	/** Closes all {@link OutputStream}s and {@link Writer}s that this class wraps. */
	public void close() {
		// Close the streams in the reverse order from that which they were opened in
		CsvUtils.close(this.bufferedWriter);
		CsvUtils.close(this.outputStreamWriter);
		CsvUtils.close(this.fileOutputStream);
	}

	/** @see BufferedWriter#write(String) */
	public void write(String content) throws IOException {
		this.bufferedWriter.write(content);
	}

	/** @see BufferedWriter#flush() */
	@Override
	public void flush() throws IOException {
		this.bufferedWriter.flush();
	}

	/** @see BufferedWriter#write(char[], int, int) */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.bufferedWriter.write(cbuf, off, len);
	}
}