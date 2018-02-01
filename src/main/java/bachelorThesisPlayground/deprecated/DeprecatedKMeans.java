package bachelorThesisPlayground.deprecated;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;
import bachelorThesisPlayground.graphBuilding.GraphBuilding;

public class DeprecatedKMeans {

	public static void setSensorsWithGraphKMeans(List<SimpleWeightedGraph<Vertex,Edge>> components, final int k) {
		int placeableVertexesCount = 0;
		for (SimpleWeightedGraph<Vertex,Edge> component : components)
			placeableVertexesCount += getComponentPlaceableCount(component);
		
		componentloop: 
		for (SimpleWeightedGraph<Vertex,Edge> component : components) {
			for (Vertex p : component.vertexSet())
				p.mainSensorPlaced = false;	

			FloydWarshallShortestPaths<Vertex, Edge> alg = new FloydWarshallShortestPaths<>(component);

			System.out.println("component size: " + component.vertexSet().size());
			List<Vertex> placeable = component.vertexSet().stream().filter(x->canBeCentroid(x, component)).collect(Collectors.toList());
			System.out.println("of them placeable: " + getComponentPlaceableCount(component) + "/" + placeableVertexesCount);
			double centroidsCountCoef = ((double)getComponentPlaceableCount(component))/(double)placeableVertexesCount;
			System.out.println("coef is: " + centroidsCountCoef);

			List<Vertex> result = null;
			double score = 999999999;
			
			for (int i = 0; i < 2; i++) {
				Collections.shuffle(placeable);
				
				List<Vertex> centroids = placeable.subList(0, (int)(k *centroidsCountCoef));
				//System.out.println("centroids count: " + centroids.size());
				
				if ((int)(k *centroidsCountCoef)==0) {
					System.out.println("ERROR : CANNOT DO KMEANS, NO PLACEABLES?");
					continue componentloop;
				}
				
				centroids = optimizeWithKMeans(component, centroids, alg);

				double localScore = calculateScore(component, centroids, alg);
				if (localScore < score) {
					System.out.println("GLOBAL IT: " + i + " SCORE changed: " + score + " ==> " + localScore);
					result = centroids;
					score = localScore;
				}				
			}
			
			for (Vertex centroid : result)
				centroid.mainSensorPlaced = true;
		}
	} 
	
	public static boolean canBeCentroid(Vertex node, SimpleWeightedGraph<Vertex,Edge> component){
		return node.placecode > -1;
	}
	
	public static List<Vertex> optimizeWithKMeans(SimpleWeightedGraph<Vertex,Edge> component, List<Vertex> centroids, FloydWarshallShortestPaths<Vertex, Edge> alg) {

		int it = 0;
		
		boolean centroidsChange = true;
		List<Vertex> oldCentroids = centroids;
		while (centroidsChange) {
			it++;
			//System.out.println("it: " + it);
			//System.out.println("centroids: " + centroids.stream().map(x->x.oldId).collect(Collectors.toList()));
			oldCentroids = centroids;
			centroids = kMeansIteration(component, oldCentroids, alg);
			centroidsChange = !(centroids.containsAll(oldCentroids) && oldCentroids.containsAll(centroids));
			/*
			if (!centroidsChange) {
				oldCentroids = centroids;
				System.out.println("trying to move stuck centroid");
				centroids = teleportStuckCentroid(component, oldCentroids, alg);
				centroidsChange = !(centroids.containsAll(oldCentroids) && oldCentroids.containsAll(centroids));
				if (centroidsChange) {
					System.out.println("it moved");
				} else {
					System.out.println("it didnt move");
				}
			}
			*/
			}
		//System.out.println("centroids for this component have converged at it: " + it);
				
		return centroids;
	}
	
	public static List<Vertex> kMeansIteration(SimpleWeightedGraph<Vertex,Edge> component, List<Vertex> centroids, FloydWarshallShortestPaths<Vertex, Edge> alg) {
		List<List<Vertex>> zones = getZonesAndMarkCentroids(component, centroids, alg);
		
		centroids = zones
				.stream()
				.map(zone -> findCentroid(zone, alg, component))
				.collect(Collectors.toList());
		
		//markClusters(component, centroids, alg);
		
		return centroids;		
	}

	public static void markClusters(SimpleWeightedGraph<Vertex,Edge> component, List<Vertex> centroids, FloydWarshallShortestPaths<Vertex, Edge> alg) {
		List<List<Vertex>> zones = getZonesAndMarkCentroids(component, centroids, alg);
		for (List<Vertex> zone : zones) {
			Color color = GraphBuilding.colors.get((int)(Math.random()*GraphBuilding.colors.size()));
			zone.forEach(v->{
				v.r = color.getRed();
				v.g = color.getGreen();
				v.b = color.getBlue();
			});
		}		
	}
	
	public static List<List<Vertex>> getZonesAndMarkCentroids(SimpleWeightedGraph<Vertex,Edge> component, List<Vertex> centroids, FloydWarshallShortestPaths<Vertex, Edge> alg) {
		for (Vertex p : component.vertexSet())
			p.zoneIndex = -1;
		
		List<List<Vertex>> zones = new ArrayList<>();
		for (int i = 0; i < centroids.size(); i++) {
			centroids.get(i).zoneIndex = i;
			zones.add(new ArrayList<>());
			zones.get(i).add(centroids.get(i));
		}
		
		for (Vertex p : component.vertexSet()){
			zones.get(
					centroids
						.stream()
						//близость к центроиде
						.min((a,b)->Double.compare(alg.getPathWeight(p, a), alg.getPathWeight(p, b)))
						.get()
						.zoneIndex
				).add(p);
		}
		return zones;
	}
	
	public static double calculateScore(SimpleWeightedGraph<Vertex,Edge> component, List<Vertex> centroids, FloydWarshallShortestPaths<Vertex, Edge> alg) {
		centroids = new ArrayList<>(centroids);		
		List<List<Vertex>> zones = getZonesAndMarkCentroids(component, centroids, alg);
		
		double sum = 0;		
		for (Vertex centroid : centroids) {
			double weight = getZoneWeight(centroid, zones.get(centroid.zoneIndex), alg)/10000;
			sum += weight*weight;
		}
		return sum;
	}
	
	public static List<Vertex> teleportStuckCentroid(SimpleWeightedGraph<Vertex,Edge> component, List<Vertex> centroids, FloydWarshallShortestPaths<Vertex, Edge> alg) {
		centroids = new ArrayList<>(centroids);		
		List<List<Vertex>> zones = getZonesAndMarkCentroids(component, centroids, alg);
		
		Map<Integer, Double> zoneWeights = new HashMap<>();
		
		for (Vertex centroid : centroids)
			zoneWeights.put(centroid.zoneIndex, getZoneWeight(centroid, zones.get(centroid.zoneIndex), alg));
		
		Vertex poorCentroid = centroids.stream()
				.min((a,b)->Double.compare(zoneWeights.get(a.zoneIndex), zoneWeights.get(b.zoneIndex)))
				.get();
		
		List<Vertex> sortedByWealth = centroids
				.stream()
				.sorted((a,b)->-Double.compare(zoneWeights.get(a.zoneIndex), zoneWeights.get(b.zoneIndex)))
				.collect(Collectors.toList());
		
		for (int i = 0; i < sortedByWealth.size(); i++) {
			Vertex richCentroid = sortedByWealth.get(i);
			if (zoneWeights.get(poorCentroid.zoneIndex)*4 < zoneWeights.get(richCentroid.zoneIndex)) {
				System.out.println("poorest centroid has zone sum of " + zoneWeights.get(poorCentroid.zoneIndex));
				System.out.println("richest centroid has zone sum of " + zoneWeights.get(richCentroid.zoneIndex));
				try{
					Vertex replacement = zones
							.get(richCentroid.zoneIndex)
							.stream()
							.filter(x-> x.zoneIndex==-1 && canBeCentroid(x,component))
							.findAny()
							.get();
					centroids.set(poorCentroid.zoneIndex, replacement);
					replacement.zoneIndex = poorCentroid.zoneIndex;
					poorCentroid.zoneIndex = -1;
					break;
				} catch (NoSuchElementException e) {
					continue;
				}
			} else {
				break;
			}
		}
		
		return centroids;		
	}
	
	public static Vertex findCentroid(List<Vertex> zone, FloydWarshallShortestPaths<Vertex, Edge> alg, SimpleWeightedGraph<Vertex,Edge> component){
		return zone.stream()
				.filter(a->canBeCentroid(a, component))
				.min(
						(a,b)->Double.compare(
							getDistanceFromEverybodyInTheZone(a,zone,alg),
							getDistanceFromEverybodyInTheZone(b,zone,alg)
						)
					).get();
	}
	
	public static double getDistanceFromEverybodyInTheZone(Vertex p, List<Vertex> zone, FloydWarshallShortestPaths<Vertex, Edge> alg){
		double sum = 0;
		for (Vertex a : zone) {
			sum+=alg.getPathWeight(a, p);
		}
		return sum;
	}
	
	public static double getZoneWeight(Vertex p, List<Vertex> zone, FloydWarshallShortestPaths<Vertex, Edge> alg){
		SimpleWeightedGraph<Vertex,Edge> union = new SimpleWeightedGraph<>(Edge.class);
		for (Vertex a : zone) {
			for (Vertex v : alg.getPath(a, p).getVertexList())
				union.addVertex(v);
			for (Edge e : alg.getPath(a, p).getEdgeList())
				union.addEdge(e.a, e.b, e);
		}
		double sum = 0;
		for (Edge e : union.edgeSet())
			sum += e.length;
		return sum;
	}
	
	public static int getComponentPlaceableCount(SimpleWeightedGraph<Vertex,Edge> component){
		return (int) component.vertexSet().stream().filter(x->canBeCentroid(x, component)).count();
	}
	
}
