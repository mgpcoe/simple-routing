package com.example.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Router {
	private final String hostname;
	private HashSet<Router> links;
	private HashMap<String, Set<Router>> knownNodesAndGateways;
	private HashMap<Router, Set<Route>> knownRoutes;

	public Router(final String hostname) {
		this.hostname = hostname;
		this.links = new HashSet<>();
		this.knownNodesAndGateways = new HashMap<>();
		this.knownRoutes = new HashMap<>();
	}
	
	public String getHostname() {
		return this.hostname;
	}
	
	HashMap<Router, Set<Route>> getRoutes() {
		// Not exactly safe!
		return knownRoutes; 
	}
	
	public void addLink(final Router router) {
		System.err.println(hostname + " adding " + router.getHostname() + " to my list of active links");
		links.add(router);
		
		addUnknownNodesFromRoute(router, new Route(router, null));

		for (Router link : links) {
			link.updateRoutingTableFrom(this);
		}
	}
	
	void updateRoutingTableFrom(final Router router) {
		if (!links.contains(router)) {
			System.err.println(hostname + " Adding " + router.getHostname() + " to my list of active links");
			links.add(router);
		}
		
		// Update local routing table with the routes from the new link
		for (Set<Route> routes : router.knownRoutes.values()) {
			for (Route route : routes) {
				addUnknownNodesFromRoute(router, new Route(router, route));
			}
		}
		
		// Propagate routing table changes through the network
		for (Router gateway : links) {
			if (!gateway.equals(router)) {
				gateway.updateRoutingTableFrom(this);
			}
		}
	}
	
	void addUnknownNodesFromRoute(final Router gateway, final Route route) {
		System.err.println(hostname + ": processing route " + route.toString());

		if (route.nextHop == this) {
			System.err.println(hostname + ": next hop is myself, skipping...");
			// terminate early so as not to store routing loops.
			return;
		}

		if (!knownNodesAndGateways.keySet().contains(route.nextHop.getHostname())) {
			System.err.println(hostname + ": next hop " + route.nextHop.getHostname() + " is unknown, adding nodes and gateway to my cache");
			Set<Router> gateways = knownNodesAndGateways.get(route.nextHop.getHostname());
			if (gateways == null) {
				gateways = new HashSet<>();
			}
			gateways.add(gateway);
			knownNodesAndGateways.put(route.nextHop.getHostname(), gateways);
		}

		// recur until we reach the end of the route
		if (route.route != null) {
			System.err.println(hostname + ": route has more nodes, navigating down route...");
			addUnknownNodesFromRoute(gateway, route.route);
		}
		
		if (knownRoutes.get(route.nextHop) == null) {
			knownRoutes.put(route.nextHop, new HashSet<Route>());
		}
		
		if (route.getEndOfRoute() != this) {
			knownRoutes.get(route.nextHop).add(route);
		} else {
			System.err.println(hostname + ": route ends with me, skipping...");
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
		
		System.err.println("I have " + links.size() + " active links.");
		for (Router link : links) {
			System.err.println("Have a link to " + link.getHostname());
			if (link.getHostname().equals(destination)) {
				return true;
			}
		}
		
		return (knownNodesAndGateways.keySet().contains(destination));
		
//		for (Router link : links) {
//			if (link.canRouteTo(destination)) {
//				return true;
//			}
//		}
//		
//		return false;
	}
	
	public Set<Route> getRoutesToHost(final String destination) {
		if (!canRouteTo(destination)) {
			System.err.println("Wait a second, I can't route to " + destination);
			// Fail fast!
			return Collections.emptySet();
		}
		
		final Set<Route> routesToHost = new HashSet<>();
		
		System.err.println("My network includes " + knownNodesAndGateways.keySet().size() + " other routers:");
		for (String routerName : knownNodesAndGateways.keySet()) {
			System.err.println(" - " + routerName);
		}
		final Set<Router> gatewaysToDestination = knownNodesAndGateways.get(destination);
		System.err.println("I have " + gatewaysToDestination.size() + " route(s) to reach " + destination);
		for (Router gateway : gatewaysToDestination) {
			System.err.println("I know of " + knownRoutes.get(gateway).size() + " route(s) starting with " + gateway.getHostname());
			for (Route route : knownRoutes.get(gateway)) {
				final Router endOfRoute = route.getEndOfRoute();
				if (endOfRoute.getHostname().equals(destination)) {
					routesToHost.add(new Route(this, route));
				}
			}
		}
		
		return routesToHost;
	}
	
	/**
	 * Nodes with duplicate hostnames are considered to be identical
	 * 
	 * Autogenerated by Eclipse 3.8
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostname == null) ? 0 : hostname.hashCode());
		return result;
	}
	
	/**
	 * Nodes with duplicate hostnames are considered to be identical
	 * 
	 * Autogenerated by Eclipse 3.8
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Router other = (Router) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		return true;
	}

	class Route {
		private final Router nextHop;
		private final Route route;
		
		Route(final Router nextHop, final Route route) {
			this.nextHop = nextHop;
			this.route = route;
		}
		
		Router getEndOfRoute() {
			if (route != null) {
				return route.getEndOfRoute();
			}
			
			return nextHop;
		}
		
		@Override
		public String toString() {
			String routePath = nextHop.getHostname();
			if (route != null) {
				routePath += "->" + route.toString();
			}
			
			return routePath;
		}

		/**
		 * Autogenerated by Eclipse 3.8
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((nextHop == null) ? 0 : nextHop.hashCode());
			result = prime * result + ((route == null) ? 0 : route.hashCode());
			return result;
		}

		/**
		 * Autogenerated by Eclipse 3.8
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Route other = (Route) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (nextHop == null) {
				if (other.nextHop != null)
					return false;
			} else if (!nextHop.equals(other.nextHop))
				return false;
			if (route == null) {
				if (other.route != null)
					return false;
			} else if (!route.equals(other.route))
				return false;
			return true;
		}

		private Router getOuterType() {
			return Router.this;
		}
		
		
	}
}
