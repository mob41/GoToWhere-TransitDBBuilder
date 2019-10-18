package com.github.mob41.gotowhere.transitdb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.build.impl.kmb.KmbDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.db.TransitDatabase;

public class Main {
	
	public static final String VERSION = "0.0.1";

	public static void main(String[] args) {
		//Put implementation instances here
		TransitDatabaseBuilder[] builders = {
			new KmbDatabaseBuilder()
		};
		
		System.out.println("GoToWhere TransitDBBuilder v" + VERSION);
		System.out.println("Licensed under MIT License." + "\n");
		
		System.out.println("All fetched data and generated databases are copyrighted to their respective owners.\n");
		
		System.out.println("There are total of " + builders.length + " builders available.");
		System.out.println("Type \"list\" to show all of the available DB builders.");
		System.out.println("Type \"exit\" to terminate the application.\n");
		
		String input;
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print(">");
			input = scanner.nextLine();
			if (input.startsWith("exit")) {
				scanner.close();
				System.exit(0);
				return;
			} else if (input.startsWith("help")) {
				System.out.println("The CLI has 4 commands only:");
				System.out.println("\thelp - Shows this help message.");
				System.out.println("\tlist - Lists all the available builders.");
				System.out.println("\tbuild - Uses the specified builder to build a transit database.");
				System.out.println("\t\tUsage: build <{builderName}|{index}> [{fileName}] [b64|json]");
				System.out.println("\texit - Terminates the application.\n");
				System.out.println("<>: Required []: Optional |: Or {}: Replace with input");
			} else if (input.startsWith("list")) {
				System.out.println("Listing " + builders.length + " available builders:");
				for (int i = 0; i < builders.length; i++) {
					System.out.println((i + 1) + ": " + builders[i].getProviderName());
				}
			} else if (input.startsWith("build")) {
				String[] splits = input.split(" ");
				
				if (splits.length < 2) {
					System.out.println("Error: The correct usage of \"build\" is:");
					System.out.println("\tbuild <{builderName}|{index}> [{fileName}] [b64|json]");
					continue;
				}
				
				String builderName = splits[1];
				int index = -1;
				try {
					index = Integer.parseInt(builderName);
				} catch (NumberFormatException ignore) {}
				
				TransitDatabaseBuilder builder = null;
				if (index == -1) {
					for (int i = 0; i < builders.length; i++) {
						if (builders[i].getProviderName().equals(builderName)) {
							builder = builders[i];
							break;
						}
					}
				} else {
					builder = builders[index - 1];
				}
				
				if (builder == null) {
					System.out.println("Error: Could not find specified builder. Type \"list\" to get a list of available builders.");
					continue;
				}
				
			    boolean encodeBase64 = true;
			    if (splits.length >= 4 && "json".equals(splits[3])) {
		    		encodeBase64 = false;
			    }
			    
				builder.deleteObservers();
				builder.addObserver(new ConsoleObserver(builder.getProviderName(), builder));
				
				long startTime = System.currentTimeMillis();
			    boolean status = false;
				try {
					status = builder.build();
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Error: Error occurred. Aborting current operation");
					continue;
				}
				
				if (status) {
					System.out.println("Database build success. Used " + (System.currentTimeMillis() - startTime) + " ms");
					
					System.out.print("Generating structure... ");
					TransitDatabase db = builder.create();
					System.out.println("done");
					
					System.out.print("Generating Database JSON... ");
					String dbJson = db.getDatabaseJson();
					System.out.println("done");
					System.out.print("Generating Version JSON... ");
					String verJson = db.getVersionJson();
					System.out.println("done");
					
					String output;
					if (encodeBase64) {
						System.out.print("Encoding database JSON in Base64... ");
						output = Base64.encodeBase64String(dbJson.getBytes(Charsets.UTF_8));
						System.out.println("done");
					} else {
						System.out.println("Keeping it as JSON format.");
						output = dbJson;
					}
					
					String fileName;
					if (splits.length >= 3) {
						fileName = splits[2];
					} else {
						fileName = builder.getProviderName() + "-" + System.currentTimeMillis();
					}
					
					if (encodeBase64) {
						if (!fileName.endsWith(".b64")) {
							fileName += ".b64";
						}
					} else {
						if (!fileName.endsWith(".json")) {
							fileName += ".json";
						}
					}
					
					int dotIndex = fileName.lastIndexOf('.');
					String versionFileName = fileName.substring(dotIndex + 1) + "-version.json";

					System.out.print("Outputting database file as \"" + fileName + "\"... ");
					try {
						File file = new File(fileName);
						if (file.exists()) {
							file.delete();
						}
						if (!file.exists()) {
							file.createNewFile();
						}
						
						FileOutputStream out = new FileOutputStream(file);
						PrintWriter writer = new PrintWriter(out, true);
						writer.println(output);
						writer.flush();
						writer.close();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Error: Error occurred. Aborting current operation");
						continue;
					}
					System.out.println("done");
					
					System.out.print("Outputting version file as \"" + versionFileName + "\"... ");
					try {
						File file = new File(versionFileName);
						if (file.exists()) {
							file.delete();
						}
						if (!file.exists()) {
							file.createNewFile();
						}
						
						FileOutputStream out = new FileOutputStream(file);
						PrintWriter writer = new PrintWriter(out, true);
						writer.println(verJson);
						writer.flush();
						writer.close();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Error: Error occurred. Aborting current operation");
						continue;
					}
					System.out.println("done");
					
					System.out.println("Success: Database build and output was successful. Used " + (System.currentTimeMillis() - startTime) + " ms.");
				} else {
					System.err.println("Error: Database build was not successful.");
				}
			} else {
				System.out.println("Unknown command. Type \"help\" for a list of available commands.");
			}
		}
	}

}
