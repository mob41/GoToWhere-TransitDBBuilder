package com.github.mob41.gotowhere.transitdb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import com.github.mob41.gotowhere.transitdb.build.DownloadedInfo;
import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.build.impl.ctbnwfb.CtbNwfbDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.build.impl.hktransit.HkTransitDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.build.impl.kmb.KmbDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.db.TransitDatabase;

public class Main {
	
	public static final String VERSION = "0.0.1";

	public static void main(String[] args) {
		//Put implementation instances here
		List<TransitDatabaseBuilder> builders = new ArrayList<TransitDatabaseBuilder>();
		
		builders.add(new KmbDatabaseBuilder());
		builders.add(new CtbNwfbDatabaseBuilder());
		
		try {
			builders.addAll(HkTransitDatabaseBuilder.getHkTransitBuilders());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not include HkTransit builders. Please make sure you have connected to the internet properly.\n");
		}
		
		System.out.println("GoToWhere TransitDBBuilder v" + VERSION);
		System.out.println("Licensed under MIT License." + "\n");
		
		System.out.println("All fetched data and generated databases are copyrighted to their respective owners.\n");
		
		System.out.println("There are total of " + builders.size() + " builders available.");
		System.out.println("Type \"list\" to show all of the available DB builders.");
		System.out.println("Type \"exit\" to terminate the application.\n");
		
		if (args.length > 0 && args[0].equals("--trust-all-ssl")) {
			System.out.println("Trusting all insecure SSL!\n");
			trustAllSsl();
		}
		
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
				System.out.println("\t\tUsage: build <{builderName}|{index}> [json|b64] [{fileName}]");
				System.out.println("\texit - Terminates the application.\n");
				System.out.println("<>: Required []: Optional |: Or {}: Replace with input");
			} else if (input.startsWith("list")) {
				System.out.println("Listing " + builders.size() + " available builders:");
				for (int i = 0; i < builders.size(); i++) {
					System.out.println((i + 1) + ": " + builders.get(i).getBuilderName());
				}
			} else if (input.startsWith("build")) {
				String[] splits = input.split(" ");
				
				if (splits.length < 2) {
					System.out.println("Error: The correct usage of \"build\" is:");
					System.out.println("\tbuild <{builderName}|{index}> [json|b64] [{fileName}]");
					continue;
				}
				
				String builderName = splits[1];
				int index = -1;
				try {
					index = Integer.parseInt(builderName);
				} catch (NumberFormatException ignore) {}
				
				TransitDatabaseBuilder builder = null;
				if (index == -1) {
					for (int i = 0; i < builders.size(); i++) {
						if (builderName.toLowerCase().equals(builders.get(i).getBuilderName().toLowerCase())) {
							builder = builders.get(i);
							break;
						}
					}
				} else {
					builder = builders.get(index - 1);
				}
				
				if (builder == null) {
					System.out.println("Error: Could not find specified builder. Type \"list\" to get a list of available builders.");
					continue;
				}
				
			    boolean encodeBase64 = false;
			    if (splits.length >= 3 && "b64".equals(splits[2])) {
		    		encodeBase64 = true;
			    }
			    
			    DownloadedInfo[] infos = builder.getAvailableDownloaded();
			    if (infos.length > 0) {
			    	System.out.println("There are existing database downloads. You can choose to use these downloads:\n");
			    	System.out.println("[0]\tDownload a new copy");
			    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			    	Date date;
			    	for (int i = 0; i < infos.length; i++) {
			    		date = new Date(infos[i].getTimeMs());
			    		System.out.println("[" + (i + 1) + "]\t" + simpleDateFormat.format(date));
			    	}
			    	int inputIndex;
			    	System.out.println("\nPlease input from 0 to " + infos.length + ".");
			    	while (true) {
			    		inputIndex = -1;
			    		
			    		System.out.print("[0-" + infos.length + "]>");
			    		input = scanner.nextLine();
			    		try {
			    			inputIndex = Integer.parseInt(input);
			    		} catch (NumberFormatException e) {}
			    		
			    		if (inputIndex == -1 || inputIndex < 0 || inputIndex > infos.length) {
			    			System.out.println("Invalid input. Please input a number from 0 to " + infos.length + ".");
			    		} else {
			    			break;
			    		}
			    	}
			    	
			    	if (inputIndex == 0) {
			    		builder.generateDownloadedKey();
			    		System.out.println("\nA new copy of the data will be downloaded.\n");
			    	} else {
			    		DownloadedInfo info = infos[inputIndex - 1];
			    		builder.setDownloadedKey(info.getDownloadedKey());
				    	System.out.println("\nThe copy at " + simpleDateFormat.format(new Date(info.getTimeMs())) + " will be used.\n");
			    	}
			    }
			    
				builder.deleteObservers();
				builder.addObserver(new ConsoleObserver(builder.getProviderName(), builder));
				
				builder.cleanUp();
				
				long startTime = System.currentTimeMillis();
			    boolean status = false;
				try {
					status = builder.build();
				} catch (Exception e) {
					System.out.println("");
					e.printStackTrace();
					System.err.println("Error: Error occurred. Aborting current operation");
					continue;
				}
				
				if (status) {
					System.out.println("\rDatabase build success. Used " + (System.currentTimeMillis() - startTime) + " ms\t");
					
					System.out.print("Generating structure... ");
					TransitDatabase db = builder.create();
					System.out.println("done");
					
					System.out.print("Generating Database JSON... ");
					String dbJson = db.getDatabaseJson();
					System.out.println("done");
					System.out.print("Generating Routes JSON... ");
					String routesJson = db.getRoutesJson();
					System.out.println("done");
					System.out.print("Generating Stops JSON... ");
					String stopsJson = db.getStopsJson();
					System.out.println("done");
					System.out.print("Generating Version JSON... ");
					String verJson = db.getVersionJson();
					System.out.println("done");
					
					String output;
					if (encodeBase64) {
						System.out.print("Encoding database JSON in Base64... ");
						output = Base64.encodeBase64String(dbJson.getBytes(StandardCharsets.UTF_8));
						System.out.println("done");
					} else {
						System.out.println("Keeping database as JSON format.");
						output = dbJson;
					}
					
					String routesOutput;
					if (encodeBase64) {
						System.out.print("Encoding routes JSON in Base64... ");
						routesOutput = Base64.encodeBase64String(routesJson.getBytes(StandardCharsets.UTF_8));
						System.out.println("done");
					} else {
						System.out.println("Keeping routes as JSON format.");
						routesOutput = routesJson;
					}
					
					String stopsOutput;
					if (encodeBase64) {
						System.out.print("Encoding stops JSON in Base64... ");
						stopsOutput = Base64.encodeBase64String(stopsJson.getBytes(StandardCharsets.UTF_8));
						System.out.println("done");
					} else {
						System.out.println("Keeping stops as JSON format.");
						stopsOutput = stopsJson;
					}
					
					String fileName;
					if (splits.length >= 4) {
						fileName = splits[3];
					} else {
						fileName = builder.getBuilderName();// + "-" + System.currentTimeMillis();
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
					String versionFileName = fileName.substring(0, dotIndex) + "-version.json";
					String routesFileName = fileName.substring(0, dotIndex) + "-routes.json";
					String stopsFileName = fileName.substring(0, dotIndex) + "-stops.json";

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
						PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
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
					
					System.out.print("Outputting routes file as \"" + routesFileName + "\"... ");
					try {
						File file = new File(routesFileName);
						if (file.exists()) {
							file.delete();
						}
						if (!file.exists()) {
							file.createNewFile();
						}
						
						FileOutputStream out = new FileOutputStream(file);
						PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
						writer.println(routesOutput);
						writer.flush();
						writer.close();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Error: Error occurred. Aborting current operation");
						continue;
					}
					System.out.println("done");
					
					System.out.print("Outputting stops file as \"" + stopsFileName + "\"... ");
					try {
						File file = new File(stopsFileName);
						if (file.exists()) {
							file.delete();
						}
						if (!file.exists()) {
							file.createNewFile();
						}
						
						FileOutputStream out = new FileOutputStream(file);
						PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
						writer.println(stopsOutput);
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
	
	private static void trustAllSsl() {
		try {

			/* Start of Fix */
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {}
				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {}

	        } };

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) { return true; }
	        };
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        /* End of the fix*/
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: Trust All SSL Failed!");
		}
	}

}
