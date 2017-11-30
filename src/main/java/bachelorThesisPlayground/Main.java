package bachelorThesisPlayground;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.educationalProject.surfacePathfinder.visualization.DisplayMode;
import org.educationalProject.surfacePathfinder.visualization.NetworkVisualizer;
import org.educationalProject.surfacePathfinder.visualization.Screenshooter;
import org.educationalProject.surfacePathfinder.visualization.SwingWindow;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.normalizers.Normalizer;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.readers.JsonGraphReader;
import bachelorThesisPlayground.writers.JSONWriter;

public class Main {

	public static void main(String[] args) {
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		
		drawGraph();
		
		//denormalizeCoordsAndWriteIntoFiles();
	}
	
	//������ �����, ����� � ����� ���� ��� d3 force v4
	static void writeJSONGraphForD3() {
		//����� ����� ����� � �������������� �� �����-������
		Map<Integer, Edge> edges = ExcelGraphReader.getEdgeSkeletons("C:\\Users\\test\\Desktop\\������\\�����_���_�_�����.xlsx");
		//����� �������������� ��������� ����� - �����, ������� � ��
		ExcelGraphReader.populateEdgeParameters(edges, "C:\\Users\\test\\Desktop\\������\\����.xlsx");
		//����� ���������� �����-������ �����
		CSVGraphReader.populateVertexCoordinates(edges, "C:\\Users\\test\\Desktop\\������\\����� � ������������.csv");
		//GraphReader.filterUnusedEdges(edges);
		
		ExcelGraphReader.populatePointsWithParameters(edges, "C:\\Users\\test\\Desktop\\������\\����.xlsx");
		
		//� ����� ���� ��������� ����������� �������� �����, ����������
		Iterator<Integer> it = edges.keySet().iterator();
		while(it.hasNext()){
			Integer id = it.next();
			if(edges.get(id).diameter == 0)
				it.remove();
		}		
		
		//������� ���������� ���������, ��� ��� �� ����� ������������� �����
		//������� ����� ������� ��������� ����������
		ConvergenceImprover.removeSmallConnectedComponents(edges, false, 50);
		
		//�������� �������������� ����� � ���� 0...n-1
		Normalizer.normalizeIds(edges);
		
		//�������� ���������� � ��������� [0,m_x/m_y],
		//�������� ��������������� ���������
		Normalizer.normalizeCoords(edges);
		
		//�������� ���������� d3 � ������� �������������� ����
		//<<��� ��������������� ����� ������ � ����������� ������� ���������������>>
		//�������� ������: ������ �������� fixed � ������
		ConvergenceImprover.assignCoordsOld(edges);
		
		/* �������� ��� �����, � ������� ����� ���������� ������ ���������
		int threshold = 3000;
		Iterator<Map.Entry<Integer, Edge>> iter = edges.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<Integer, Edge> entry = iter.next();
			if(entry.getValue().a.x > threshold || entry.getValue().b.x > threshold 
					|| entry.getValue().a.y > threshold || entry.getValue().b.y > threshold
					)		
				iter.remove();	
		}
		*/
		
		//����������� ������ �����, ��� �3
		JSONWriter.write(edges, "C:\\Users\\test\\Desktop\\������\\d3.json");
		//����� ������ ������ �����, ����� ������ ��� ��������
		JSONWriter.writeFull(edges, "C:\\Users\\test\\Desktop\\������\\d3full.json");
		
		System.out.println(edges.values().stream().mapToDouble(e->Math.max(e.a.x, e.b.x)).max().getAsDouble());
		System.out.println(edges.values().stream().mapToDouble(e->Math.max(e.a.y, e.b.y)).max().getAsDouble());
		
	}
	
	
	public static void drawGraph(){				
			ArrayList<Vertex> points = getPoints();
		
			//from here we get all needed data about edges
			ArrayList<Edge> edges = JsonGraphReader.readEdges("C:\\Users\\test\\Desktop\\������\\d3full.json");

			edges = filterUnneededEdges(edges);									
				
			Collections.sort(edges, (a,b)->Integer.compare(a.id, b.id));
				
			//there are some unneeded points after cleaning, lets remove them
			Set<Vertex> usedPoints = new HashSet<Vertex>(points.size());
			for (Edge e : edges){
				usedPoints.add(e.a);
				usedPoints.add(e.b);		
			}
			points.removeIf(v->!usedPoints.contains(v));
				
			//update valve statuses
			CSVGraphReader.updateStatusOfValves(points, "C:\\Users\\test\\Desktop\\������\\���������_������.csv");
			
			//now we want to normalize points coordinates (normalization can be ruined after layouting)
			double minX = points.stream().mapToDouble(p->p.x).min().getAsDouble();
			double minY = points.stream().mapToDouble(p->p.y).min().getAsDouble();
			   
			points.stream()
				.forEach(v->{
				    v.x -= minX;
				    v.y -= minY;
				});			
			    	  
			//construct graph 
			DefaultDirectedWeightedGraph<Vertex,Edge> graph = new DefaultDirectedWeightedGraph<Vertex,Edge>(Edge.class);
			
			boolean filterByCoords = false;
			//System.out.println(points.stream().max((a,b)->Double.compare(a.x, b.x)).get());
			//System.out.println(points.stream().max((a,b)->Double.compare(a.y, b.y)).get());
			
			double lowX = 3300;
			double lowY = 3300;
			double highX = 5000;
			double highY = 5000;
			
			
			HashMap<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(points.size());
			for (Vertex p : points){
				if (filterByCoords)
					if (p.x > highX || p.y > highY || p.x < lowX || p.y < lowY) {
						continue;						
					} else {
						p.x -= lowX;
						p.y -= lowY;
					}
				idToVertex.put(p.id, p);
				graph.addVertex(p);
			}
				
			for (Edge e: edges) {
				e.a = idToVertex.get(e.a.id);
				e.b = idToVertex.get(e.b.id);
				
				if(filterByCoords && (e.a==null || e.b == null))
					continue;
				
				if (e.a != e.b) {
					graph.addEdge(e.a, e.b, e);
					System.out.println("filling graph with edge: " + e);
				}
			}
			
			System.out.println(graph.vertexSet().size());
			
			double edgeDeletingThreshold = 1.01;
			
			Pair<Edge, Vertex> edgeAndPointToDelete = findEdgeAndPointToDelete(graph, edgeDeletingThreshold);
			while (edgeAndPointToDelete != null){
				Edge e = edgeAndPointToDelete.getFirst();
				Vertex pointToSave = (edgeAndPointToDelete.getSecond() == e.a) ? e.b : e.a;
				Vertex pointToDelete = (edgeAndPointToDelete.getSecond() == e.a) ? e.a : e.b;
				
				graph.removeEdge(e);
				Set<Edge> edgesToChange = new HashSet<>(graph.edgesOf(pointToDelete));
				graph.removeVertex(pointToDelete);		
				
				for (Edge edgeToChange : edgesToChange) {
					Edge edgeToChangeCopy = new Edge(edgeToChange.id);
					edgeToChangeCopy.diameter = edgeToChange.diameter;
					edgeToChangeCopy.material = edgeToChange.material;
					
					if (edgeToChange.a == pointToDelete) {
						edgeToChangeCopy.a = pointToSave;	
						edgeToChangeCopy.b = edgeToChange.b;
					} else {
						edgeToChangeCopy.a = edgeToChange.a;
						edgeToChangeCopy.b = pointToSave;						
					}	
					
					if (!graph.containsEdge(edgeToChangeCopy.a, edgeToChangeCopy.b)
							&&
						!graph.containsEdge(edgeToChangeCopy.b, edgeToChangeCopy.a)) {
						edgeToChangeCopy.length = edgeToChangeCopy.a.distance(edgeToChangeCopy.b);
						if(edgeToChangeCopy.a != edgeToChangeCopy.b){
							graph.addEdge(edgeToChangeCopy.a, edgeToChangeCopy.b, edgeToChangeCopy);
						}
					}		
				}				
							
				edgeAndPointToDelete = findEdgeAndPointToDelete(graph, edgeDeletingThreshold);
			}

			System.out.println(graph.vertexSet().size());
			//display 
				
			//save as png file
			DisplayMode.setMode("screenshot");
				
			NetworkVisualizer visScreenshot = new NetworkVisualizer()
					.setData(graph)
					.setDefaultWidth(10000)
					.setLabelDrawing(true)
					.calculateWeightAndHeight();
			Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

			Path source = FileSystems.getDefault().getPath("c:\\users\\test\\desktop\\yo.png");
			Path out = FileSystems.getDefault().getPath("yo.png");
			try {
			    Files.copy(source, out, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
			    e.printStackTrace();
			}

			//draw on screen
			DisplayMode.setMode("screen");
			
			NetworkVisualizer vis = new NetworkVisualizer()
					.setData(graph)
					.setDefaultWidth(800)
					.calculateWeightAndHeight();
			SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
						
	}
	
	public static Pair<Edge, Vertex> findEdgeAndPointToDelete(DefaultDirectedWeightedGraph<Vertex,Edge> graph, double edgeDeletingThreshold) {
		Iterator<Edge> edgeIterator = graph.edgeSet().iterator();
		
		while (edgeIterator.hasNext()) {
			Edge e = edgeIterator.next();
			if (e.length < edgeDeletingThreshold) {
				boolean aCannotBeDeleted = e.a.fixed || graph.edgesOf(e.a).size() == 1 || e.a.pumpStationEntry || e.a.pumpStationExit || e.a.canBeLocked;
				boolean bCannotBeDeleted = e.b.fixed || graph.edgesOf(e.b).size() == 1 || e.b.pumpStationEntry || e.b.pumpStationExit || e.b.canBeLocked;				
				
				Vertex pointToDelete;
				if (aCannotBeDeleted && bCannotBeDeleted) {
					continue;
				} else if (aCannotBeDeleted) {
					pointToDelete = e.b;
				} else if (bCannotBeDeleted) {
					pointToDelete = e.a;
				} else {
					pointToDelete = (graph.edgesOf(e.a).size() < graph.edgesOf(e.b).size()) ? e.a : e.b;
				}
				return new Pair<>(e, pointToDelete);
			}
		}
		
		return null;
	}
	
	public static ArrayList<Vertex> getPoints() {
		//layouted graph
		ArrayList<Vertex> points = JsonGraphReader.readNodes("C:\\Users\\test\\Desktop\\������\\FINAL_ALL_POINTS.json");
		
		JsonGraphReader.populatePointsWithParameters(points, "C:\\Users\\test\\Desktop\\������\\d3full.json");	
		
		// now we want to resolve parameters of points from their subtypes
		for (Vertex p : points) {
			String type = p.type.toUpperCase();
			p.canBeLocked = type.contains("� ���������") ||
					    	type.contains("��������") ||
					    	type.contains("��������") ||
					    	type.contains("� ���������");
					    	
			p.pumpStationEntry = type.contains("����� ����� �� ���");
			p.pumpStationExit = type.contains("����� ������ � ���");
		}
		
		return points;
	}
	
	public static ArrayList<Edge> filterUnneededEdges(ArrayList<Edge> edges) {
		edges = new ArrayList<Edge>(edges);
		
		filterEdgesWithInvalidPoints(edges);
		
		//here we get rid of unneeded edges and graph components
		//map is used to be compatible with old API
		HashMap<Integer,Edge> idToEdge = new HashMap<>(edges.size());
		for(Edge e : edges)
			idToEdge.put(e.id, e);
						
		ExcelGraphReader.filterUnusedEdges(idToEdge, "C:\\Users\\test\\Desktop\\������\\����.xlsx");
		ConvergenceImprover.removeSmallConnectedComponents(idToEdge, true, 16);
		
		return new ArrayList<Edge>(idToEdge.values());
	}
	
	public static void filterEdgesWithInvalidPoints(ArrayList<Edge> edges) {
		//������� ���������������� �����
		Iterator<Edge> iter = edges.iterator();
		while(iter.hasNext()){
			Edge e = iter.next();
			if("��� �����".equals(e.a.type) || "��� �����".equals(e.b.type))	
				iter.remove();	
			/*
			else if ( 
					(e.a.fixed && (e.a.x < 10000 || e.a.y < 10000))
					|| 
					(e.b.fixed && (e.b.x < 10000 || e.b.y < 10000))
					)
				iter.remove();	
			else {
				System.out.print("===.>");
				System.out.println(e.a);
				System.out.print("===..>");
				System.out.println(e.b);
			}
			*/	
		}
	}
}









/* 100% dead code:
  
  
 
	public static void denormalizeCoordsAndWriteIntoFiles(){

		ArrayList<Vertex> points = getPoints();
		
		//from here we get all needed data about edges
		ArrayList<Edge> edges = JsonGraphReader.readEdges("C:\\Users\\test\\Desktop\\������\\d3full.json");
				
		filterEdgesWithInvalidPoints(edges);
		
		//here we get rid of unneeded edges and graph components
		HashMap<Integer,Edge> idToEdge = new HashMap<>(edges.size());
		for(Edge e : edges)
			idToEdge.put(e.id, e);
		
		ExcelGraphReader.filterUnusedEdges(idToEdge, "C:\\Users\\test\\Desktop\\������\\����.xlsx");
		ConvergenceImprover.removeSmallConnectedComponents(idToEdge, true, 16);
		
		edges = new ArrayList<Edge>(idToEdge.values());
		Collections.sort(edges, (a,b)->Integer.compare(a.id, b.id));
		
		//there are some unneeded points after cleaning, lets remove them
		Set<Vertex> usedPoints = new HashSet<Vertex>(points.size());
		for (Edge e : edges){
			usedPoints.add(e.a);
			usedPoints.add(e.b);		
		}
		
		points.removeIf(v->!usedPoints.contains(v));
		
		// now we want to resolve parameters of points from their subtypes
	    for (Vertex p : points) {
	    	p.canBeLocked = p.type.toUpperCase().contains("� ���������") ||
			    			p.type.toUpperCase().contains("��������") ||
			    			p.type.toUpperCase().contains("��������") ||
	    					p.type.toUpperCase().contains("� ���������");
	    	
	    	p.pumpStationEntry = p.type.toUpperCase().contains("����� ����� �� ���");
	    	p.pumpStationExit = p.type.toUpperCase().contains("����� ������ � ���");
	    }
	   
		HashMap<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(points.size());
		for (Vertex p : points){
			idToVertex.put(p.id, p);
		}
		
		for (Edge e: edges) {
			e.a = idToVertex.get(e.a.id);
			e.b = idToVertex.get(e.b.id);
			System.out.println("filling graph with edge: " + e);
		}
		
		int idOfFixedNode = -1;
		int oldIdOfFixedNode = -1;
		for (Vertex p : points) {
			if (p.fixed) {
				idOfFixedNode = p.id;
				oldIdOfFixedNode = p.oldId;
				break;
			}				
		}
				
		Vertex denormalizedVertex = CSVGraphReader.getPointWithId("C:\\Users\\test\\Desktop\\������\\����� � ������������.csv", oldIdOfFixedNode);
		
		double shiftX = idToVertex.get(idOfFixedNode).x*1000 - denormalizedVertex.x;
		double shiftY = idToVertex.get(idOfFixedNode).y*1000 - denormalizedVertex.y;
		
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
		
		for (Vertex p : points) {
			p.x = p.x * 1000 - shiftX;
			p.y = p.y * 1000 - shiftY;
			
			if (p.fixed)
				System.out.println(p.oldId + " " + df.format(p.x) + " " + df.format(p.y));
		}
		
		CSVGraphWriter.writeEdgesForAnalyst(edges);
		CSVGraphWriter.writeNodesForAnalyst(points);
		
		
		//check everything is fine
		
		//now we want to normalize points coordinates (normalization can be ruined after layouting)
	    double minX = points.stream().mapToDouble(p->p.x).min().getAsDouble();
	    double minY = points.stream().mapToDouble(p->p.y).min().getAsDouble();
	   
	    points.stream()
		    .forEach(v->{
		    	v.x -= minX;
		    	v.y -= minY;
		    });
		
	    	    
	    //construct graph 
		DefaultDirectedWeightedGraph<Vertex,Edge> graph = new DefaultDirectedWeightedGraph<Vertex,Edge>(Edge.class);
		
		idToVertex = new HashMap<Integer, Vertex>(points.size());
		for (Vertex p : points){
			idToVertex.put(p.id, p);
			graph.addVertex(p);
		}
		
		for (Edge e: edges) {
			e.a = idToVertex.get(e.a.id);
			e.b = idToVertex.get(e.b.id);
			graph.addEdge(e.a, e.b, e);
			System.out.println("filling graph with edge: " + e);
		}
		//display 
		
		//save as png file
		DisplayMode.setMode("screenshot");
		
		NetworkVisualizer visScreenshot = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(10000)
				.calculateWeightAndHeight();
		Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

		//draw on screen
		DisplayMode.setMode("screen");
		
		NetworkVisualizer vis = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(800)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
	}
 
 
 
 
 */