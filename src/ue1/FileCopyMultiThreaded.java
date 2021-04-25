package ue1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCopyMultiThreaded {

	static public void main (final String[] args) throws IOException {

		// Nach wie vor die Pfade sauber aus den args lesen
		final Path sourcePath = Paths.get(args[0]);
		if (!Files.isReadable(sourcePath)) throw new IllegalArgumentException(sourcePath.toString());

		final Path sinkPath = Paths.get(args[1]);
		if (sinkPath.getParent() != null && !Files.isDirectory(sinkPath.getParent())) throw new IllegalArgumentException(sinkPath.toString());

		// Initialisieren der PipedStreams
		final PipedOutputStream readingPipedStream = new PipedOutputStream();
		final PipedInputStream writingPipedStream = new PipedInputStream(readingPipedStream);
		
		// reading thread
		Thread readingThread = new Thread(new Runnable() {
			@Override
            public void run() {
				try (InputStream fis = Files.newInputStream(sourcePath)) {
					final byte[] buffer = new byte[0x10000];
					for (int bytesRead = fis.read(buffer); bytesRead != -1; bytesRead = fis.read(buffer)) {
						readingPipedStream.write(buffer, 0, bytesRead);
					}
					readingPipedStream.close();
				} catch(IOException ioe) {
					System.out.println(ioe);
				}
			}
		});

		// writing thread
		Thread writingThread = new Thread(new Runnable() {
			@Override
            public void run() {
				try (OutputStream fos = Files.newOutputStream(sinkPath)) {
					final byte[] buffer = new byte[0x10000];
					for (int bytesRead = writingPipedStream.read(buffer); 
							bytesRead != -1; bytesRead = writingPipedStream.read(buffer)) {
						
						fos.write(buffer, 0, bytesRead);
					}
					writingPipedStream.close();	
				} catch(IOException ioe) {
					System.out.println("write");
					System.out.println(ioe);
				}
			}
		});

		readingThread.start();
		writingThread.start();
		
		System.out.println("done.");
	}
}
