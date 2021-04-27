package ue1.a12;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCopyReceive {

	static public void main (final String[] args) throws IOException {

		final Path sinkPath = Paths.get(args[0]);
		if (sinkPath.getParent() != null && !Files.isDirectory(sinkPath.getParent())) throw new IllegalArgumentException(sinkPath.toString());

		final String ipAndPortAsString = args[1];
		
		String ip;
		int port;
		
		String[] splitResult = ipAndPortAsString.split(":");
		
		if(splitResult.length != 2) throw new IllegalArgumentException(ipAndPortAsString);
		
		ip = splitResult[0];
		try {			
			port = Integer.parseInt(splitResult[1]);
		} catch(NumberFormatException nfe) {
			throw new IllegalArgumentException(ipAndPortAsString);
		}

		Socket socket = new Socket(ip, port);
		
		try (OutputStream fos = Files.newOutputStream(sinkPath)) {
			try(InputStream in = socket.getInputStream()) {
				final byte[] buffer = new byte[0x10000];
				for (int bytesRead = in.read(buffer); bytesRead != -1; bytesRead = in.read(buffer)) {					
					fos.write(buffer, 0, bytesRead);
				}
			}
		} catch(IOException ioe) {		
			System.out.println(ioe);
		}
		
		socket.close();
		
		System.out.println("Receiving done");
	}
}
