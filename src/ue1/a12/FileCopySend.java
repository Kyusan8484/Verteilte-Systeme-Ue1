package ue1.a12;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCopySend {

	static public void main (final String[] args) throws IOException {

		final Path sourcePath = Paths.get(args[0]);
		if (!Files.isReadable(sourcePath)) throw new IllegalArgumentException(sourcePath.toString());

		final String portAsString = args[1];
		int port;
		try {			
			port = Integer.parseInt(portAsString);
		} catch(NumberFormatException nfe) {
			throw new IllegalArgumentException(portAsString);
		}
		
		ServerSocket service = new ServerSocket(port);
		
		System.out.println("Waiting for connections...");
        Socket connection = service.accept();
		System.out.println("Connection established");

		try (InputStream fis = Files.newInputStream(sourcePath)) {			
			final byte[] buffer = new byte[0x10000];
			OutputStream outputStream = connection.getOutputStream();
			for (int bytesRead = fis.read(buffer); bytesRead != -1; bytesRead = fis.read(buffer)) {
				outputStream.write(buffer, 0, bytesRead);
			}			
			outputStream.close();
		} catch(IOException ioe) {
			System.out.println(ioe);
		}

		service.close();
		
		System.out.println("Sending done");
	}
}
