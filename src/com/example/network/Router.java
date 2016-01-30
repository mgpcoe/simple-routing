package com.example.network;

import java.util.HashSet;

public class Router {
	private final String hostname;
	private HashSet<Router> links;

	public Router(final String hostname) {
		this.hostname = hostname;
		this.links = new HashSet<>();
	}
	
	public String getHostname() {
		return this.hostname;
	}
	
	public void addLink(final Router router) {
		links.add(router);
		for (Router link : links) {
			if (link == router) {
				continue;
			}

//			link.receiveRoutingMap(this, links);
		}		
	}
	
	void receiveRoutingMap(Router sender, HashSet<Router> incomingLinks) {
		throw new UnsupportedOperationException("Implementation left as an exercise to the reader.");
	}

	/**
	 * Determine if the destination is routable from this Router
	 * 
	 * After checking to see if this Router is, or is directly connected to,
	 * the destination, performs a naive depth-first search for the destination
	 * 
	 * @param destination The destination router
	 * @return Whether or not the destination is routable
	 */
	public boolean canRouteTo(final String destination) {
		if (destination.equals(hostname)) {
			return true;
		}
		
		for (Router link : links) {
			if (link.getHostname().equals(destination)) {
				return true;
			}
		}
		
		for (Router link : links) {
			if (link.canRouteTo(destination)) {
				return true;
			}
		}
		
		return false;
	}
}
