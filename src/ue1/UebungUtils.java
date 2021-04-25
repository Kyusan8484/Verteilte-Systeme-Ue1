package ue1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UebungUtils {

	public static void main(String[] args) {
		System.out.println(System.getProperty("java.version"));		
		
		if(args.length == 0) {
			System.out.println("no command");
			System.exit(0);
		}
		
		String command = args[0];
		if("delete".equals(command)) {
			final Path deleteFilePath = Paths.get(args[1]);
			if (!Files.isReadable(deleteFilePath)) throw new IllegalArgumentException(deleteFilePath.toString());						
			deleteFile(deleteFilePath);			
		}
		
	}
	
	private static void deleteFile(Path path) {
		try {
			Files.delete(path);
			System.out.println("Deleted " + path.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
