package org.archive.format.cdx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.archive.streamcontext.Stream;
import org.archive.streamcontext.StreamWrappedInputStream;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.binsearch.impl.RandomAccessFileSeekableLineReaderFactory;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.zip.OpenJDK7GZIPInputStream;

public class CDXFile extends SortedTextFile implements CDXInputSource {

	public CDXFile(String uri) throws IOException {
		super(getUriFactory(uri));
	}

	public CDXFile(SeekableLineReaderFactory factory) {
		super(factory);
	}

	public CloseableIterator<String> getCDXLineIterator(String key) throws IOException {
		return getRecordIteratorLT(key);
	}
	
	public static SeekableLineReaderFactory getUriFactory(String uri) throws IOException
	{
		if (uri.endsWith(".gz")) {
			return new RandomAccessFileSeekableLineReaderFactory(decodeGZToTemp(uri));
		}
		
		return GeneralURIStreamFactory.createSeekableStreamFactory(uri);
	}
	
	// Decode gzipped cdx to a temporary file	
	public static File decodeGZToTemp(String uriGZ) throws IOException {
		final int BUFFER_SIZE = 8192;

		Stream stream = null;

		try {
			stream = GeneralURIStreamFactory.createStream(uriGZ);
			InputStream input = new StreamWrappedInputStream(stream);
			input = new OpenJDK7GZIPInputStream(input);

			File uncompressedCdx = File.createTempFile(uriGZ, ".cdx");
			FileOutputStream out = new FileOutputStream(uncompressedCdx, false);

			byte buff[] = new byte[BUFFER_SIZE];
			int numRead = 0;
			
			while ((numRead = input.read(buff)) > 0) {
				out.write(buff, 0, numRead);
			}
			
			out.flush();
			out.close();
			
			return uncompressedCdx;
			
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}