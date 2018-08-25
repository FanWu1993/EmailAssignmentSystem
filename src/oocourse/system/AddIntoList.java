package oocourse.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class AddIntoList {
	
	public static void add(String number, String address, String path) throws IOException {
		
		File outputFile = new File(path + "in.txt");
		if(!outputFile.exists()) {
			outputFile.createNewFile();
		}
		
		Scanner scanner = new Scanner(outputFile);
		
		while(scanner.hasNext()) {
			if(scanner.next().equals(number)) {
				scanner.close();
				return;
			}
			if(scanner.hasNext()) {
				scanner.nextLine();
			}
		}
		
		scanner.close();

		FileOutputStream fileOutputStream = new FileOutputStream(new File(path + "in.txt"), true);
		
		fileOutputStream.write((number + " " + address + "\r\n").getBytes());
		
		fileOutputStream.close();
	}
}
