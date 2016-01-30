package com.example.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class CommandLine {
	private static final String LINKS_FILE = "stations.csv";
	
	private final HashMap<String,Router> networkNodes = new HashMap<>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CommandLine();
	}

	private CommandLine() {
		populateNetwork();

		System.err.println("Network has " + networkNodes.size() + " nodes.");
		for (String hostname : networkNodes.keySet()) {
			System.err.println("Network contains host " + hostname);
		}
		
		byte[] source = new byte[255];
		byte[] dest   = new byte[255];
		
		System.out.print("enter start station>");
		try {
			System.in.read(source);
			System.out.print("enter end station>");
			System.in.read(dest);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		

		final String sourceHostname = new String(source).trim();
		final String destinationHostname = new String(dest).trim();

		validateRouteNodes(sourceHostname, destinationHostname);		
		validateRouteExists(sourceHostname, destinationHostname);
		
		System.out.println("Found a route from " + sourceHostname + " to " + destinationHostname);
	}

	private void validateRouteExists(final String sourceHostname, final String destinationHostname) {
		if (!networkNodes.get(sourceHostname).canRouteTo(destinationHostname)) {
			System.out.println("No route to host");
			throw new RuntimeException();
		}
	}

	private void validateRouteNodes(final String sourceHostname, final String destinationHostname) {
		if (!networkNodes.containsKey(sourceHostname)) {
			System.out.println("Unrecognised host " + sourceHostname);
			throw new RuntimeException();
		}
		
		if (!networkNodes.containsKey(destinationHostname)) {
			System.out.println("Unrecognised host " + destinationHostname);
			throw new RuntimeException();
		}
	}

	private void populateNetwork() {
		try {
			FileReader linkReader = new FileReader(LINKS_FILE);
			BufferedReader br;
			br = new BufferedReader(linkReader);
			
			String line;
			while ((line = br.readLine()) != null) {
				String[] nodes = line.split(",");
				for (String node : nodes) {
					if (!networkNodes.containsKey(node.trim())) {
						System.err.println("Adding " + node.trim() + " to network...");
						Router newRouter = new Router(node.trim());
						networkNodes.put(node.trim(), newRouter);
					}
				}
				networkNodes.get(nodes[0].trim()).addLink(networkNodes.get(nodes[1].trim()));
			}
			
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
